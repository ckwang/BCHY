package team017.navigation;

import java.util.Comparator;
import java.util.PriorityQueue;

import team017.util.*;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;

/**
 * Navigator object that navigates the robot using tangent bug algorithm
 */

public class Navigator {
	private Controllers controllers;
	
	private MapLocation destination;
	private MapLocation modifiedDes;
	
	private MapLocation previousRobLoc;
	private Direction previousDir;
	
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
		
		// return same direction at same location (given same destination)
		if (controllers.myRC.getLocation().equals(previousRobLoc)){
			return previousDir;
		}
		// navigation has been interrupted before
		else if (previousRobLoc != null && 
				   !previousRobLoc.isAdjacentTo(controllers.myRC.getLocation() )){
			reset();
			return Direction.OMNI; 
		}
//		 using (BUG/ TangentBug) algorithm to find direction to destination
		else{
			previousRobLoc = controllers.myRC.getLocation();
			if (destination == null){
				reset();
				previousDir = Direction.OMNI;
				return Direction.OMNI;
			}
			else {
				controllers.myRC.setIndicatorString(0, "current: "+ controllers.myRC.getLocation().toString() + " des: " +  modifiedDes.toString());
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
			nextLoc = deTour(s, startTracingDir, iscw);
			nextDir = s.directionTo(nextLoc);
			
			controllers.myRC.setIndicatorString(2, "TRACING to "+nextLoc.toString() );
			
			// The way is open
			if ( isOpen(faceDir, nextDir, desDir, iscw) && controllers.motor.canMove(desDir)){
				istracing = false;
				return desDir;
			}
			
			return nextDir;
		
		} else {
			controllers.myRC.setIndicatorString(2, "RECKONING to "+modifiedDes.toString());
			if ( controllers.motor.canMove(desDir) ) {
				return desDir;
			}
			else {
				istracing = true;
				nextLoc = deTour(s, faceDir, iscw);
				return s.directionTo(nextLoc);
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
        			
        			if ( isOpen(bugLoc.faceDir, nextDir, desDir , bugLoc.isCW) 
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

		if (cw) {
			while ( isTraversable(currentLoc.add(faceDir)) ) {
				faceDir = faceDir.rotateRight();
			}
			while ( !isTraversable(currentLoc.add(faceDir)) ) {
				faceDir = faceDir.rotateLeft();
			}
		} else {
			while ( isTraversable(currentLoc.add(faceDir)) ) {
				faceDir = faceDir.rotateLeft();
			}
			while ( !isTraversable(currentLoc.add(faceDir)) ) {
				faceDir = faceDir.rotateRight();
			}
		}

		return currentLoc.add(faceDir);
	}
	
	private MapLocation deTour(MapLocation currentLoc, Direction faceDir, boolean cw){

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

		return currentLoc.add(faceDir);
	}

	private boolean isTraversable(MapLocation loc){
		try {
			TerrainTile tile = controllers.myRC.senseTerrainTile(loc);
			if (controllers.sensor != null && controllers.sensor.canSenseSquare(loc)) {
				
					return ((tile == null) || (tile == TerrainTile.LAND))
							&& (controllers.sensor.senseObjectAtLocation(loc, RobotLevel.ON_GROUND) == null);
				
			} else
				return (tile == null) || (tile == TerrainTile.LAND);
		
		} catch (GameActionException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean isOpen(Direction currentDir, Direction nextDir, Direction desDir, boolean cw){
		if ( !controllers.motor.canMove(currentDir) )
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
	
}
