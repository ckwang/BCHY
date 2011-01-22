package team017.navigation;

import java.util.Comparator;
import java.util.PriorityQueue;

import team017.util.*;
import battlecode.common.*;

/**
 * Navigator object that navigates the robot using tangent bug algorithm
 */

public class Navigator {
	private Controllers controllers;

	private InfoMap myMap;
	private int mapResetCounter = 100;
	
	private MapLocation destination;
	private MapLocation modifiedDes;
	
	private MapLocation previousRobLoc;
	private Direction previousDir;
	
	private int moveOrthogonally = 3;
	private int moveDiagonally = 4;
	
	private boolean isTracing;
	private boolean isCW;
	
	private class EnhancedMapLocation{
		public MapLocation loc;
		public EnhancedMapLocation lastWayPoint;
		public Direction faceDir;
		public int cost;
		public boolean isTracing;
		public boolean isCW;
		
		public EnhancedMapLocation(MapLocation l, EnhancedMapLocation el, Direction dir, int c, boolean t, boolean cw){
			loc = l;
			lastWayPoint = el;
			faceDir = dir;
			cost = c;
			isTracing = t;
			isCW = cw;
		}
		
		public boolean equals(EnhancedMapLocation m1){
			return loc.equals(m1.loc);
		}
	}
	
	private class costComparator implements Comparator<EnhancedMapLocation>{
		public int compare(EnhancedMapLocation loc1, EnhancedMapLocation loc2){
			if ( loc1.cost > loc2.cost)
				return 1;
			else if ( loc1.cost < loc2.cost)
				return -1;
			else
				return 0;
		}
	}
	
	costComparator comparator;
	PriorityQueue<EnhancedMapLocation> queue;

	public Navigator(Controllers cs) {
		controllers = cs;
		isCW = true;
		isTracing = false;
		myMap = new InfoMap( cs.myRC.getLocation() );
		comparator = new costComparator();
		queue = new PriorityQueue<EnhancedMapLocation>(50, comparator);
	}
	
	public void reset() {
		destination = null;
		modifiedDes = null;
		previousRobLoc = null;
		previousDir = null;
		isTracing = false;
		queue.clear();
	}

	public void setDestination(MapLocation loc) {
		if (loc.equals(destination))
			return;
		else{
			reset();
			destination = new MapLocation(loc.x, loc.y);
			modifiedDes = new MapLocation(loc.x, loc.y);
		}
	}

	public MapLocation getDestination() {
		return destination;
	}

	public Direction getNextDir(int tolerance) {

		if( controllers.myRC.getLocation().equals(previousRobLoc) ){
			if ( controllers.motor.canMove(previousDir) ){
				controllers.myRC.setIndicatorString(1, "precomputed");
				return previousDir;
			}
//			else if (!controllers.motor.isActive() && controllers.motor.canMove(previousDir.rotateRight())) {
//				isTracing = false;
//				previousDir = previousDir.rotateRight();
//				controllers.myRC.setIndicatorString(1, "yield");
//				return previousDir;
//			}
			else {
				controllers.myRC.setIndicatorString(1, "detour " + isCW);
				if (!isTracing){
					previousDir = detour(controllers.myRC.getDirection(), isCW);
				}
				else {
					Direction faceDir = controllers.myRC.getDirection();
					Direction startTracingDir = isCW? faceDir.rotateRight().rotateRight(): faceDir.rotateLeft().rotateLeft();
					previousDir = detour(startTracingDir, isCW);
				}
				
			}
			return previousDir;
			
		}
		else {
			controllers.myRC.setIndicatorString(1, "bugging");
			previousRobLoc = controllers.myRC.getLocation();
			
			if (destination == null){
				reset();
				previousDir = Direction.OMNI;
				return Direction.OMNI;
			}
			else {
				previousDir = Bug(controllers.myRC.getLocation(), modifiedDes, tolerance);
				return previousDir;
			}
		}
	}

	public Direction Bug(MapLocation s, MapLocation t, int tolerance) {
		
		// arrive the destination
		if (s.distanceSquaredTo(t) <= tolerance) {
			reset();
			return Direction.OMNI;
		}

		
		Direction nextDir;
		Direction faceDir = controllers.myRC.getDirection();

		// if target is not traversable, back-tracing the destination
		while (!isTraversable(t)) {
			nextDir = s.directionTo(t);
			t = t.subtract(nextDir);
			
			// beside the source
			if (s.distanceSquaredTo(t) <= tolerance) {
				reset();
				return Direction.OMNI;
			}
		}
		
		modifiedDes = t;
		
		
		Direction desDir = s.directionTo(t);
		MapLocation nextLoc;
		
		if (isTracing){
			
			Direction startTracingDir = isCW? faceDir.rotateRight().rotateRight(): faceDir.rotateLeft().rotateLeft();
			nextLoc = traceNext(s, startTracingDir, isCW);
			nextDir = s.directionTo(nextLoc);
			
			controllers.myRC.setIndicatorString(1, "TRACING to des:"+modifiedDes.toString()+" next: "+nextLoc.toString() );
			
			// The way is open
			if ( isOpen(s, faceDir, nextDir, desDir, isCW) && isTraversable(s.add(desDir) ) ){
				isTracing = false;
				return desDir;
			}
			
			return nextDir;
		
		} else {
//			controllers.myRC.setIndicatorString(1, "NT "+modifiedDes.toString());
			if ( isTraversable(s.add(desDir)) ) {
				controllers.myRC.setIndicatorString(1, "RECKONING to "+modifiedDes.toString());
				return desDir;
			}
			else {
				controllers.myRC.setIndicatorString(1, "TRACE to "+modifiedDes.toString() + isCW);
				
				isTracing = true;
				
				queue.clear();

				MapLocation nextCW = traceNext(s, desDir, true);
				MapLocation nextCCW = traceNext(s, desDir, false);
				
				queue.add(new EnhancedMapLocation(nextCW, null, s.directionTo(nextCW), 0, true, true) );
				queue.add(new EnhancedMapLocation(nextCCW, null, s.directionTo(nextCCW), 0, true, false) );
				
				EnhancedMapLocation ibugLoc;
				do{
					ibugLoc = queue.poll();
					
					Direction startTracingDir = ibugLoc.isCW? ibugLoc.faceDir.rotateRight().rotateRight()
															: ibugLoc.faceDir.rotateLeft().rotateLeft();
					nextLoc = traceNext(ibugLoc.loc, startTracingDir, ibugLoc.isCW);
					nextDir = ibugLoc.loc.directionTo(nextLoc);
					desDir = ibugLoc.loc.directionTo(t);;
					
					if ( isOpen(ibugLoc.loc, ibugLoc.faceDir, nextDir, desDir, ibugLoc.isCW) && isTraversable(ibugLoc.loc.add(desDir) )  ){
						isCW = ibugLoc.isCW;
//						controllers.myRC.setIndicatorString(1,"TRACING DIR: " + (isCW? "CW" : "CCW") + modifiedDes.toString());
						return s.directionTo( isCW? nextCW:nextCCW );
					} else {
						queue.add(new EnhancedMapLocation(nextLoc, null, nextDir, computeCost(ibugLoc, nextDir), true, ibugLoc.isCW) );
					}
					
					
				}while ( !queue.isEmpty() );
				
				return s.directionTo( nextCW );
			}
		}
		
	}
	
	public void debug_tangentBug(MapLocation s, MapLocation t, int tolerance)
			throws GameActionException {

		
		// arrive the destination
		if (s.distanceSquaredTo(t) <= tolerance) {
			reset();
			previousDir = Direction.OMNI;
			return;
		}

		Direction nextDir;
		Direction desDir;
		
		// if target is not traversable, back-tracing the destination
		while ( !isTraversable(t) ) {
			nextDir = s.directionTo(t);
			t = t.subtract(nextDir);
			
			// beside the source
			if (s.distanceSquaredTo(t) <= tolerance) {
				reset();
				previousDir = Direction.OMNI;
				return;
				
			}
		}
		
		
		modifiedDes = t;
		
//		System.out.println("DES: " + modifiedDes);
		MapLocation nextLoc;
		EnhancedMapLocation start;

		start = new EnhancedMapLocation(s, null, s.directionTo(t), 0, false, false);
		
		queue.clear();
		queue.add(start);
		
		while ( !queue.isEmpty() ) {
//			System.out.println(Clock.getBytecodeNum());
			EnhancedMapLocation bugLoc = queue.poll();

			if (bugLoc.loc.distanceSquaredTo(t) <= tolerance) {
				EnhancedMapLocation nearestWayPoint = bugLoc;

				while (!nearestWayPoint.lastWayPoint.equals(start)) {
					
					nearestWayPoint = nearestWayPoint.lastWayPoint;
					
				}
				previousDir = s.directionTo(nearestWayPoint.loc);
				return;
			}
			else if (controllers.myRC.senseTerrainTile(bugLoc.loc) == null){
				EnhancedMapLocation nearestWayPoint = bugLoc;
				queue.add(nearestWayPoint.lastWayPoint);
				
				while (!nearestWayPoint.lastWayPoint.equals(start)) {
					
					nearestWayPoint = nearestWayPoint.lastWayPoint;
					
				}
				
				previousDir = s.directionTo(nearestWayPoint.loc);
				return;
			}
			else {
				if (bugLoc.isTracing) {

					Direction startTracingDir = bugLoc.isCW ? 
							bugLoc.faceDir.rotateRight().rotateRight() : 
							bugLoc.faceDir.rotateLeft().rotateLeft();
					
				    desDir = bugLoc.loc.directionTo(t);

					nextLoc = traceNext(bugLoc.loc, startTracingDir, bugLoc.isCW);

					nextDir = bugLoc.loc.directionTo(nextLoc);

					if (isOpen(bugLoc.loc, bugLoc.faceDir, nextDir, desDir,bugLoc.isCW)
							&& isTraversable(bugLoc.loc.add(desDir))) 
					{
						queue.add(new EnhancedMapLocation(bugLoc.loc.add(desDir), bugLoc, desDir, 
								computeCost(bugLoc, desDir), false, false));
					}
					else{
						queue.add(new EnhancedMapLocation(nextLoc, bugLoc, nextDir,
							computeCost(bugLoc, nextDir), true, bugLoc.isCW));
					}
				}

				else {

					desDir = bugLoc.loc.directionTo(t);
					if (isTraversable(bugLoc.loc.add(desDir))) {
						queue.add(new EnhancedMapLocation(bugLoc.loc.add(desDir), bugLoc, desDir,
								computeCost(bugLoc, desDir) , false, false));
					} 
					else {
						MapLocation nextCW = traceNext(bugLoc.loc,bugLoc.faceDir,true);
						
						nextDir = bugLoc.loc.directionTo(nextCW);
						queue.add(new EnhancedMapLocation(nextCW, bugLoc, nextDir, 
								computeCost(bugLoc, nextDir), true, true));

						MapLocation nextCCW = traceNext(bugLoc.loc,bugLoc.faceDir, false);
						nextDir = bugLoc.loc.directionTo(nextCCW);
						queue.add(new EnhancedMapLocation(nextCCW, bugLoc, nextDir, 
								computeCost(bugLoc, nextDir), true, false));

					}
				}

			}
		}

	}
	
	
	
	private MapLocation traceNext(MapLocation currentLoc, Direction faceDir, boolean cw){

		// Put isTraversable first so that the second while loop will rotate the
		// direction to walkable position
		// Try code reuse in the future

		int step = 0;
		
		if (cw) {
			while ( isTraversable(currentLoc.add(faceDir)) && step < 8 ) {
				faceDir = faceDir.rotateRight();
				step++;
			}
			step = 0;
			while ( !isTraversable(currentLoc.add(faceDir)) && step < 8 ) {
				faceDir = faceDir.rotateLeft();
				step++;
			}
		} else {
			while ( isTraversable(currentLoc.add(faceDir)) && step < 8 ) {
				faceDir = faceDir.rotateLeft();
				step++;
			}
			step = 0;
			while ( !isTraversable(currentLoc.add(faceDir)) && step < 8 ) {
				faceDir = faceDir.rotateRight();
				step++;
			}
		}

		return currentLoc.add(faceDir);
	}
	
	
	private Direction detour(Direction faceDir, boolean cw){

		// Put isTraversable first so that the second while loop will rotate the
		// direction to walkable position
		// Try code reuse in the future

		if (cw) {
			while ( controllers.motor.canMove(faceDir) ) {
				faceDir = faceDir.rotateRight();
			}
			while ( !controllers.motor.canMove(faceDir) ) {
				faceDir = faceDir.rotateLeft();
			}
		} else {
			while ( controllers.motor.canMove(faceDir) ) {
				faceDir = faceDir.rotateLeft();
			}
			while ( !controllers.motor.canMove(faceDir) ) {
				faceDir = faceDir.rotateRight();
			}
		}

		return faceDir;
	}

	public boolean isTraversable(MapLocation loc){
		
		TerrainTile tile = controllers.myRC.senseTerrainTile(loc);
		return ((tile == null) || (tile == TerrainTile.LAND)) && (!myMap.isBlocked(loc));
		
	}
	
	private boolean isOpen(MapLocation currentLoc, Direction currentDir, Direction nextDir, Direction desDir, boolean cw){
		if (desDir == Direction.OMNI){
			return true;
		}
		
		if ( !isTraversable(currentLoc.add(currentDir)) )
			return false;
		
		if(cw){
			while(currentDir != nextDir){
				if (currentDir == desDir)
					return true;
				currentDir = currentDir.rotateRight();
			}
		}
		else {
			while(currentDir != nextDir){
				if (currentDir == desDir)
					return true;
				currentDir = currentDir.rotateLeft();
			}
		}
		return (nextDir == desDir)/* && (currentDir != nextDir)*/;
	}
	
	private int computeCost(EnhancedMapLocation from, Direction nextDir){
		if (nextDir.isDiagonal()){
			return from.cost + moveDiagonally + ((from.faceDir==nextDir)? 0: 1);
		}
		else {
			return from.cost + moveOrthogonally + ((from.faceDir==nextDir)? 0: 1);
		}
	}
	
	public void updateMap(){
		
		if (mapResetCounter < 0){
			myMap = new InfoMap( controllers.myRC.getLocation() );
			mapResetCounter = 100;
		}
		mapResetCounter--;
			
		
		for (RobotInfo info: controllers.allyImmobile) {
			if(info.chassis == Chassis.BUILDING)
				myMap.setBlocked(info.location, true);
		}
		for (RobotInfo info: controllers.enemyImmobile) {
			if(info.chassis == Chassis.BUILDING)
				myMap.setBlocked(info.location, true);
		}
		for (RobotInfo info: controllers.debris) {
			myMap.setBlocked(info.location, true);
		}
	}
	
}
