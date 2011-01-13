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

	private Map myMap;
	
	private MapLocation destination;
	private MapLocation modifiedDes;
	
	private MapLocation previousRobLoc;
	private Direction previousDir;
	private int previousRoundNum;
	
	private int moveOrthogonally = 3;
	private int moveDiagonally = 4;
	
	private boolean istracing;
	private boolean iscw;
	
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
		iscw = true;
		istracing = false;
		myMap = new Map( cs.myRC.getLocation() );
		comparator = new costComparator();
		queue = new PriorityQueue<EnhancedMapLocation>(50, comparator);
	}
	
	public void reset() {
		destination = null;
		modifiedDes = null;
		previousRobLoc = null;
		previousDir = Direction.OMNI;
		istracing = false;
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

	public Direction getNextDir(int tolerance) throws GameActionException {
//		updateMap();
		// return same direction at same location (given same destination)
		if (controllers.myRC.getLocation().equals(previousRobLoc)){
			if ( !controllers.motor.isActive() && !controllers.motor.canMove(previousDir) ){
				controllers.myRC.setIndicatorString(1, "DETOUR");
				Direction faceDir = controllers.myRC.getDirection();
				faceDir = iscw? faceDir.rotateRight().rotateRight(): faceDir.rotateLeft().rotateLeft();
				previousDir = detour(faceDir, iscw);
				istracing = false;
			}
			return previousDir;
		}
		// navigation has been interrupted before
		else if (previousRobLoc != null && 
				   !previousRobLoc.isAdjacentTo(controllers.myRC.getLocation() )){
			controllers.myRC.setIndicatorString(1, "RESET");
			reset();
			return Direction.OMNI; 
		}
//		 using (BUG/ TangentBug) algorithm to find direction to destination
		else{
			previousRobLoc = controllers.myRC.getLocation();
			controllers.myRC.setIndicatorString(1, "BUGGING");
			if (destination == null){
				controllers.myRC.setIndicatorString(1, "NULL");
				reset();
				previousDir = Direction.OMNI;
				return Direction.OMNI;
			}
			else {
				controllers.myRC.setIndicatorString(1, "BUGGING TO: " + modifiedDes.toString());
				previousDir = Bug(controllers.myRC.getLocation(), modifiedDes, tolerance);
				return previousDir;
			}
		}
	}

	public Direction Bug(MapLocation s, MapLocation t, int tolerance)
			throws GameActionException {
		
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
		
		if (istracing){
			
			Direction startTracingDir = iscw? faceDir.rotateRight().rotateRight(): faceDir.rotateLeft().rotateLeft();
			nextLoc = traceNext(s, startTracingDir, iscw);
			nextDir = s.directionTo(nextLoc);
			
			controllers.myRC.setIndicatorString(1, "TRACING to "+nextLoc.toString() );
			
			// The way is open
			if ( isOpen(s, faceDir, nextDir, desDir, iscw) && isTraversable(s.add(desDir) ) ){
				istracing = false;
				return desDir;
			}
			
			return nextDir;
		
		} else {
			controllers.myRC.setIndicatorString(1, "RECKONING to "+modifiedDes.toString());
			if ( isTraversable(s.add(desDir)) ) {
				return desDir;
			}
			else {
				istracing = true;
				
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
						iscw = ibugLoc.isCW;
						controllers.myRC.setIndicatorString(1, modifiedDes.toString() + "TRACING DIR: " + (iscw? "CW" : "CCW"));
						return s.directionTo( iscw? nextCW:nextCCW );
					} else {
						queue.add(new EnhancedMapLocation(nextLoc, null, nextDir, computeCost(ibugLoc, nextDir), true, ibugLoc.isCW) );
					}
					
					
				}while ( !queue.isEmpty() );
				
				return s.directionTo( nextCW );
			}
		}
		
	}
	
	public Direction debug_tangentBug(MapLocation s, MapLocation t, int tolerance)
			throws GameActionException {

		// arrive the destination
		if (s.distanceSquaredTo(t) <= tolerance) {
			reset();
			return Direction.OMNI;
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
				return Direction.OMNI;
			}
		}
		
		modifiedDes = t;
		MapLocation nextLoc;
		
		
		queue.clear();
        
        queue.add(new EnhancedMapLocation(s, null, s.directionTo(t), 0, false, false) );
        
        while( !queue.isEmpty() ){
        	EnhancedMapLocation bugLoc = queue.poll();
        	
        	if (bugLoc.loc.distanceSquaredTo(t) <= tolerance) {
        		EnhancedMapLocation nearestWayPoint = bugLoc;
        		
        		while (nearestWayPoint.lastWayPoint != null){
        			nearestWayPoint = bugLoc.lastWayPoint;
        		}
    			return s.directionTo(nearestWayPoint.loc);
    		}
        	else{
        		if( bugLoc.isTracing ){
        			Direction startTracingDir = bugLoc.isCW? bugLoc.faceDir.rotateRight().rotateRight()
        												   : bugLoc.faceDir.rotateLeft().rotateLeft();
        			desDir = bugLoc.loc.directionTo(t);
        			
        			nextLoc = traceNext(bugLoc.loc, startTracingDir , bugLoc.isCW);
        			
        			nextDir = bugLoc.loc.directionTo(nextLoc);
        			
        			if ( isOpen(bugLoc.loc, bugLoc.faceDir, nextDir, desDir , bugLoc.isCW) 
        				 && isTraversable(bugLoc.loc.add(desDir)) ){
        				queue.add(new EnhancedMapLocation(bugLoc.loc.add(desDir), bugLoc, desDir, computeCost(bugLoc,desDir), false, false ));
        			}
        			queue.add(new EnhancedMapLocation(nextLoc, bugLoc.lastWayPoint, nextDir, computeCost(bugLoc,nextDir), true, bugLoc.isCW ));
        		}
        		
        		else{
        			desDir = bugLoc.loc.directionTo(t);
        			if (isTraversable(bugLoc.loc.add(desDir))){
        				queue.add(new EnhancedMapLocation(bugLoc.loc.add(desDir), bugLoc.lastWayPoint, desDir, bugLoc.cost, false, false ));
        			}
        			else {
        				MapLocation nextCW = traceNext(bugLoc.loc, bugLoc.faceDir.rotateRight().rotateRight() , true);
        				nextDir = bugLoc.loc.directionTo(nextCW);
        				queue.add(new EnhancedMapLocation(nextCW, bugLoc, nextDir, computeCost(bugLoc,nextDir), true, true ));
        				
        				MapLocation nextCCW = traceNext(bugLoc.loc, bugLoc.faceDir.rotateLeft().rotateLeft() , false);
        				nextDir = bugLoc.loc.directionTo(nextCCW);
        				queue.add(new EnhancedMapLocation(nextCCW, bugLoc, nextDir, computeCost(bugLoc,nextDir), true, false ));
        			}
        		}
        		
        	}
        }
        
        

		return Direction.NONE;

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
			while ( !isTraversable(currentLoc.add(faceDir)) && step < 8 ) {
				faceDir = faceDir.rotateLeft();
				step++;
			}
		} else {
			while ( isTraversable(currentLoc.add(faceDir)) && step < 8 ) {
				faceDir = faceDir.rotateLeft();
				step++;
			}
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
	
	private boolean needDetour( Direction faceDir ){
		try {
			if (!controllers.motor.isActive() && !controllers.motor.canMove(faceDir) ){
				Robot r = (Robot) controllers.sensor.senseObjectAtLocation(controllers.myRC.getLocation().add(faceDir), RobotLevel.ON_GROUND);
				RobotInfo info = controllers.sensor.senseRobotInfo(r);
				if (Util.isFacing(info.direction.opposite(), controllers.myRC.getDirection()))
					return true;
			}
			return false;
		} catch (GameActionException e) {
			
			e.printStackTrace();
			return false;
		}
	}

	private boolean isTraversable(MapLocation loc){
		
		TerrainTile tile = controllers.myRC.senseTerrainTile(loc);
		return ((tile == null) || (tile == TerrainTile.LAND)) && (myMap.getScore(loc) != 1);
		
	}
	
	private boolean isOpen(MapLocation currentLoc, Direction currentDir, Direction nextDir, Direction desDir, boolean cw){
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
		return (currentDir == desDir);
	}
	
	private int computeCost(EnhancedMapLocation from, Direction nextDir){
		if (nextDir.isDiagonal()){
			return from.cost + moveDiagonally + ((from.faceDir==nextDir)? 1: 0);
		}
		else {
			return from.cost + moveOrthogonally + ((from.faceDir==nextDir)? 1: 0);
		}
	}
	
	public void updateMap(){
		Robot[] robots = controllers.sensor.senseNearbyGameObjects(Robot.class);
		RobotInfo info;
		for (Robot r : robots){
			try {
				info = controllers.sensor.senseRobotInfo(r);
				
				if (info.chassis == Chassis.BUILDING || info.chassis == Chassis.DEBRIS  || info.chassis == Chassis.DUMMY){
					myMap.setScore(info.location, 1);
				}
				
			} catch (GameActionException e) {
//				e.printStackTrace();
			}
		}
	}
	
}
