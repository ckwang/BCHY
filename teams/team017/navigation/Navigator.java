package team017.navigation;

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
	private boolean jumpingTracing;
	private boolean isCW;

	public Navigator(Controllers cs) {
		controllers = cs;
		isCW = true;
		isTracing = false;
		myMap = new InfoMap( cs.myRC.getLocation() );
	}
	
	public void reset() {
		destination = null;
		modifiedDes = null;
		previousRobLoc = null;
		previousDir = null;
		isTracing = false;
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
//				controllers.myRC.setIndicatorString(1, "precomputed");
				return previousDir;
			}
//			else if (!controllers.motor.isActive() && controllers.motor.canMove(previousDir.rotateRight())) {
//				isTracing = false;
//				previousDir = previousDir.rotateRight();
//				controllers.myRC.setIndicatorString(1, "yield");
//				return previousDir;
//			}
			else {
//				controllers.myRC.setIndicatorString(1, "detour " + isCW);
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
//			controllers.myRC.setIndicatorString(1, "bugging");
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
	
	public MapLocation getNextJumpingLocTowardDes (int tolerance){
		MapLocation currentLoc = controllers.myRC.getLocation();
		MapLocation jumpLoc = controllers.myRC.getLocation();
		MapLocation nextLoc = jumpLoc;
		Direction nextDir;
		
		do{
			jumpLoc = nextLoc;
			if ( jumpLoc.distanceSquaredTo(destination) < tolerance && isTraversable(jumpLoc) ){
				return jumpLoc;
			}
			nextDir = jumpLoc.directionTo(destination);
			nextLoc = nextLoc.add( nextDir );
			
		}while( currentLoc.distanceSquaredTo(nextLoc) <= 16 );
		
		
//		controllers.myRC.setIndicatorString(2, "optimal jump to: " + jumpLoc);
		
		// Find alternative jumping location
		if ( !isTraversable(jumpLoc) ){
			while( !jumpLoc.isAdjacentTo(currentLoc) && !jumpLoc.equals(currentLoc) ){
				MapLocation temp = jumpLoc;
				MapLocation best = null;
				int distance = currentLoc.distanceSquaredTo(destination);
				for( int i = 0; i < 8; i++ ){
					temp = jumpLoc.add(Util.dirs[i]);
					if ( currentLoc.distanceSquaredTo(temp) <= 16 
							&& isTraversable(temp) 
							&& temp.distanceSquaredTo(destination) < distance ){
						best = temp;
						distance = temp.distanceSquaredTo(destination);
					}
				}
				
				if (best != null){
					return best;
				}
				else {
					jumpLoc = jumpLoc.subtract(currentLoc.directionTo( jumpLoc ) );
				}
			}
		}
		if (jumpLoc.isAdjacentTo(currentLoc) || jumpLoc.equals(currentLoc))
			return null;
		
		return jumpLoc;
	}
	
	public MapLocation getNextJumpingLoc (int tolerance){
		MapLocation jumpLoc = controllers.myRC.getLocation();
		MapLocation currentLoc = controllers.myRC.getLocation();
		Direction desDir = currentLoc.directionTo(destination);
		MapLocation nextLoc = controllers.myRC.getLocation();
		Direction nextDir = controllers.myRC.getDirection();
		Direction startTracingDir;
		
		if (jumpingTracing){
			controllers.myRC.setIndicatorString(2, "tracing");
			
			startTracingDir = isCW? nextDir.rotateRight().rotateRight(): nextDir.rotateLeft().rotateLeft();
			
			nextLoc = currentLoc.add(startTracingDir,2);
			do {
				if (isTraversable(nextLoc))
					jumpLoc = nextLoc;
				
				nextLoc = nextLoc.add(startTracingDir);
			} while (currentLoc.distanceSquaredTo(nextLoc) < 16);
			
			if ( !jumpLoc.equals(currentLoc) ){
				jumpingTracing = false;
				controllers.myRC.setIndicatorString(2, "Find good jumpLoc: " + jumpLoc);
				return jumpLoc;
			}
			
			nextLoc = currentLoc;
			nextDir = controllers.myRC.getDirection();
			
//			if( controllers.myRC.senseTerrainTile(currentLoc.add(startTracingDir)) == TerrainTile.OFF_MAP ){
//				isCW = !isCW;
//				nextDir = nextDir.opposite();
//			}
			
//			controllers.myRC.setIndicatorString(2, "Do the bugging");
			
			Direction faceDir;
			
			do{
				jumpLoc = nextLoc;
				faceDir = nextDir;
				startTracingDir = isCW? nextDir.rotateRight().rotateRight(): nextDir.rotateLeft().rotateLeft();
				nextLoc = traceNext(jumpLoc, startTracingDir, isCW);
				nextDir = jumpLoc.directionTo(nextLoc);
				desDir = jumpLoc.directionTo(destination);
				
				if (isOpen(jumpLoc, faceDir, nextDir, desDir, isCW) 
						&& isTraversable(jumpLoc.add(desDir)) ){
					controllers.myRC.setIndicatorString(2, "Open");
					jumpingTracing = false;
					return jumpLoc;
				}
				
			}	while (currentLoc.distanceSquaredTo(nextLoc) <= 16);
			
			return jumpLoc;
			
			
		}
		else{
			controllers.myRC.setIndicatorString(2, "go");
			jumpLoc = getNextJumpingLocTowardDes(tolerance);
			if (jumpLoc == null) {
				controllers.myRC.setIndicatorString(2, "No way to go");
				if ( isTraversable(currentLoc.add(desDir)) )
					isCW = betterTracingWay(currentLoc.add(desDir), destination);
				else
					isCW = betterTracingWay(currentLoc, destination);
				jumpingTracing = true;
			}
		}
		
		return jumpLoc;
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
			
			// The way is open
			if ( isOpen(s, faceDir, nextDir, desDir, isCW) && isTraversable(s.add(desDir) ) ){
				isTracing = false;
				return desDir;
			}
			
			return nextDir;
		
		} else {
			if ( isTraversable(s.add(desDir)) ) {
				return desDir;
			}
			else {
				isTracing = true;
				isCW = betterTracingWay(s, t);
				nextLoc = traceNext(s, desDir, isCW);
				
				return s.directionTo(nextLoc);
			}
		}
		
	}
	
	private boolean betterTracingWay(MapLocation s, MapLocation t){
		
		MapLocation [] tracingLoc = new MapLocation [2];
		Direction [] tracingDir = new Direction [2];
		int [] tracingcost = new int [2];
		
		int tracingIndex;
		Direction nextDir, startTracingDir;
		MapLocation nextLoc;
		
		Direction faceDir = controllers.myRC.getDirection();
		Direction desDir = s.directionTo(t);
		
		tracingLoc[0] = traceNext(s, desDir, true);
		tracingLoc[1] = traceNext(s, desDir, false);
		
		tracingDir[0] = s.directionTo(tracingLoc[0]);
		tracingDir[1] = s.directionTo(tracingLoc[0]);
		
		tracingcost[0] = computeCost(0, faceDir, tracingDir[0]);
		tracingcost[1] = computeCost(0, faceDir,  tracingDir[1]);
		
		
		
		do{
			if ( tracingcost[0] <= tracingcost[1] ){
				tracingIndex = 0;
				startTracingDir = tracingDir[0].rotateRight().rotateRight();
				
				nextLoc = traceNext(tracingLoc[0], startTracingDir, true);
			}
			else {
				tracingIndex = 1;
				startTracingDir = tracingDir[1].rotateLeft().rotateLeft();
				
				nextLoc = traceNext(tracingLoc[1], startTracingDir, false);
			}
			
			
			nextDir = tracingLoc[tracingIndex].directionTo(nextLoc);
			desDir = tracingLoc[tracingIndex].directionTo(t);;
			
			if ( isOpen(tracingLoc[tracingIndex], tracingDir[tracingIndex], nextDir, desDir, tracingIndex == 0) 
					&& isTraversable(tracingLoc[tracingIndex].add(desDir) )  ){
				return tracingIndex == 0;
			} else {
				tracingcost[tracingIndex] = computeCost(tracingcost[tracingIndex], tracingDir[tracingIndex], nextDir);
				tracingDir[tracingIndex] = nextDir;
				tracingLoc[tracingIndex] = nextLoc;
			}
			
		}while ( true );
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
		return (nextDir == desDir);
	}
	
	private int computeCost(int cost, Direction faceDir, Direction nextDir){
		if (nextDir.isDiagonal()){
			return cost + moveDiagonally + ((faceDir==nextDir)? 0: 1);
		}
		else {
			return cost + moveOrthogonally + ((faceDir==nextDir)? 0: 1);
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
	
	/*
	
	public void debug_tangentBug(MapLocation s, MapLocation t, int tolerance) throws GameActionException {

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
		
		//System.out.println("DES: " + modifiedDes);
		MapLocation nextLoc;
		EnhancedMapLocation start;
		
		start = new EnhancedMapLocation(s, null, s.directionTo(t), 0, false, false);
		
		queue.clear();
		queue.add(start);
		
		while ( !queue.isEmpty() ) {
		//	System.out.println(Clock.getBytecodeNum());
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
		
	} */
	/*
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
	}*/
	
}
