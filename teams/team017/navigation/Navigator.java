package team017.navigation;

import java.util.LinkedList;
import java.util.List;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.MovementController;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;
import battlecode.common.TerrainTile;

import team017.util.*;

/**
 * Navigator object that navigates the robot using tangent bug algorithm
 */

public class Navigator {
	private RobotController myRC;
	private List<MapLocation> subDestinations;
	private MapLocation destination;
	
	private SensorController sensor;
	private MovementController motor;
	
	private boolean isTracing;
	private MapLocation previousLoc;
	private Direction previousDir;
	
	public Navigator(RobotController rc) {
		myRC = rc;
		subDestinations = new LinkedList<MapLocation>();
		isTracing = false;
	}
	
	public void setSensor(SensorController s) {
		sensor = s;
	}
	
	public void setMotor(MovementController m) {
		motor = m;
	}
	
	public void setDestination(MapLocation loc) {
		setDestination(loc.x, loc.y);
	}
	
	public void setDestination (int x, int y){
		destination = new MapLocation( x, y );
	}
	
	public MapLocation getDestination(){
		return destination;
	}
	
	public void setPreviousDir(Direction dir){
		previousDir = dir;
	}
	
	public Direction getNextDir(int tolerance) throws GameActionException {
		myRC.setIndicatorString(0, myRC.getLocation().toString()+ " " + destination.toString() );
		myRC.setIndicatorString(1, "" );
		
		if (destination == null)
			return Direction.OMNI;
		else if (myRC.getLocation().equals(previousLoc) && previousDir != null ){
			myRC.setIndicatorString(1, previousLoc.toString()+ " " + previousDir.toString() );
			return previousDir;
		}
		else{
			previousLoc = myRC.getLocation();
			Direction nextDir = Bug(myRC.getLocation(), destination, tolerance);
			
			if (nextDir == Direction.OMNI){
				previousLoc = null;
				previousDir = null;
				myRC.setIndicatorString(1, "GOT THERE!" );
			}
			else{
				if (motor.canMove(nextDir)){
					previousDir = nextDir;
				}
			}
			return nextDir;
		}
		
//		else if (subDestinations.size() == 0){
//			return tangentBug(myRC.getLocation(), destination, tolerance);
//		}
//		else {
//			MapLocation currentDes;
//			currentDes = subDestinations.get(0);
//			if ( currentDes.equals(myRC.getLocation()) ){
//				subDestinations.remove(0);
//				if (subDestinations.size() == 0){
//					return tangentBug(myRC.getLocation(), destination, tolerance);
//				}
//				else {
//					currentDes = subDestinations.get(0);
//					return myRC.getLocation().directionTo(currentDes);
//				}
//			}
//			else {
//				return myRC.getLocation().directionTo(currentDes);
//			}
//		}
		
	}
	
	public Direction Bug(MapLocation s, MapLocation t, int tolerance) throws GameActionException {
		if ( s.distanceSquaredTo(t) <= tolerance ) {
			destination = null;
			return Direction.OMNI;
		}
		
		
		Direction initDir = s.directionTo(t);
		Direction nextDir;
		
		// if target is not traversable
		while ( !isTraversable(t) ) {
			nextDir = s.directionTo(t);
			
			if ( nextDir == Direction.OMNI ) {
				return Direction.OMNI;
			}
			
			t = t.subtract(nextDir);
		}

		if (!isTracing){
			if ( isTraversable(s.add(initDir)) /*&& !isBlockedDiagonally(s, initDir)*/  ){
				isTracing = false;
//				myRC.setIndicatorString(1, initDir.toString()  );
				return initDir;
			}
			else{
				isTracing = true;
				MapLocation nextLoc = traceNext( s, myRC.getDirection(), true );
//				myRC.setIndicatorString(1, nextLoc.toString()  );
				return s.directionTo(nextLoc);
			}
		}
		else{
			if ( isTraversable(s.add(initDir)) 
					&& Util.isNotTurningBackward( initDir, myRC.getDirection() )
					/*&& !isBlockedDiagonally(s, initDir)*/
					){
				isTracing = false;
//				myRC.setIndicatorString(1, initDir.toString()+" Tracing"  );
				return initDir;
			}
			else{
				isTracing = true;
				MapLocation nextLoc = traceNext( s, myRC.getDirection(), true );
//				myRC.setIndicatorString(1, nextLoc.toString()+" Tracing"  );
				return s.directionTo(nextLoc);
			}
		}
	}
	
	public Direction tangentBug(MapLocation s, MapLocation t, int tolerance) throws GameActionException {
		if ( s.distanceSquaredTo(t) <= tolerance ) {
			destination = null;
			return Direction.OMNI;
		}
//		myRC.setIndicatorString(1, t.toString()+destination.toString()  );
		
		Direction initDir;
		Direction nextDir;
		
		// if target is not traversable
		while ( !isTraversable(t) ) {
			nextDir = s.directionTo(t);
			
			if ( nextDir == Direction.OMNI ) {
				return Direction.OMNI;
			}
			
			t = t.subtract(nextDir);
		}

		initDir = s.directionTo(t);
		MapLocation currentLoc = new MapLocation(s.x, s.y);
		
		int step = 0;
		int THRESHOLD = 10;
		
		// dead reckoning
		do {
			nextDir = currentLoc.directionTo(t);
			if ( step++ > THRESHOLD || currentLoc.equals(t) ){
//				myRC.setIndicatorString(2, "dead reckoning");
//				if(myRC.senseTerrainTile( currentLoc ) != null)
//					subDestinations.add(0,currentLoc);
				return initDir;
			}
			currentLoc = currentLoc.add(nextDir);
		} while ( isTraversable(currentLoc) );
		currentLoc = currentLoc.subtract(nextDir);
		
//		myRC.setIndicatorString(2, "tracing");
		MapLocation traceLoc[] = {currentLoc, currentLoc};
		Direction initTraceDir[] = {Direction.NONE, Direction.NONE};
		
		// tracing
		boolean isCW = true; 

		do {
			if ( isCW ) {
				traceLoc[1] = traceNext( traceLoc[1], initDir, isCW );
				if (initTraceDir[1].equals(Direction.NONE))	initTraceDir[1] = currentLoc.directionTo(traceLoc[1]);
				if (traceLoc[1].equals(t))	return initTraceDir[1];
			} else {
				traceLoc[0] = traceNext( traceLoc[0], initDir, isCW );
				if (initTraceDir[0].equals(Direction.NONE))	initTraceDir[0] = currentLoc.directionTo(traceLoc[0]);
				if (traceLoc[0].equals(t))	return initTraceDir[0];
			} 
			
			isCW = !isCW;
		} while ( !isOpen(s, t, traceLoc[isCW ? 0 : 1]) );

		if (s.equals(traceLoc[isCW ? 0 : 1]))
			return myRC.getDirection();
		else {
			return tangentBug(s, traceLoc[isCW ? 0 : 1], tolerance);
		}
//		do {
//			currentLoc = traceNext( currentLoc, nextDir, true );
//		} while ( !isOpen(s, t, currentLoc) );
//		
//		if (s.equals(currentLoc))
//			return myRC.getDirection();
//		else {
//			return tangentBug(s, currentLoc, tolerance);
//		}
	}
	
	private boolean isOpen(MapLocation sourceLoc, MapLocation destLoc, MapLocation bugLoc) throws GameActionException {
		return isTraversable( bugLoc.add(sourceLoc.directionTo(bugLoc)) ) && isTraversable( bugLoc.add(bugLoc.directionTo(destLoc)));
	}
	
	private MapLocation traceNext(MapLocation currentLoc, Direction faceDir, boolean cw) throws GameActionException {
		
		// Put isTraversable first so that the second while loop will rotate the direction to walkable position
		// Try code reuse in the future
		
//		boolean isDiagonallyBlocked = isBlockedDiagonally(currentLoc, faceDir);
//		myRC.setIndicatorString(2, isDiagonallyBlocked? "TRUE": "FALSE");
		if (cw){
			while ( isTraversable(currentLoc.add(faceDir)) /*&& !isBlockedDiagonally(currentLoc, faceDir)*/ ) {
				faceDir = faceDir.rotateRight();
			}
			while ( !isTraversable(currentLoc.add(faceDir)) /*|| isBlockedDiagonally(currentLoc, faceDir)*/ ) {
				faceDir = faceDir.rotateLeft();
			}
		}
		else {
			while ( isTraversable(currentLoc.add(faceDir)) /*&& !isBlockedDiagonally(currentLoc, faceDir)*/ ) {
				faceDir = faceDir.rotateLeft();
			}
			while ( !isTraversable(currentLoc.add(faceDir)) /*|| isBlockedDiagonally(currentLoc, faceDir)*/ ) {
				faceDir = faceDir.rotateRight();
			}
		}
		
		return currentLoc.add(faceDir);
	}

	private boolean isTraversable( MapLocation loc ) throws GameActionException {
		TerrainTile tile = myRC.senseTerrainTile( loc );
		
		if (sensor != null && sensor.canSenseSquare(loc)){
			return ( (tile == null) || (tile == TerrainTile.LAND) ) && (sensor.senseObjectAtLocation(loc, RobotLevel.ON_GROUND) == null);
		}
		else
			return (tile == null) || (tile == TerrainTile.LAND) ;
	}
	
	private boolean isBlockedDiagonally ( MapLocation loc, Direction faceDir) throws GameActionException{
		return faceDir.isDiagonal() && !isTraversable(loc.add(faceDir.rotateLeft())) && !isTraversable(loc.add(faceDir.rotateRight()));
	}
}
