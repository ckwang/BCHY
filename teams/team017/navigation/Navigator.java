package team017.navigation;

import java.util.LinkedList;
import java.util.List;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;

import team017.util.*;

/**
 * Navigator object that navigates the robot using tangent bug algorithm
 */

public class Navigator {
	private Controllers controllers;
	
	private MapLocation destination;
	private MapLocation backTrackDes;
	
	private MapLocation startTracingLoc; // used for check if the robot move around the obstacle
	private MapLocation previousTracingLoc; // use for eliminating loop motion
	
	private MapLocation previousRobLoc;
	private Direction previousDir;
	
	private static enum State{
		RECKONING,
		START,
		TRACING,
		ROUNDED
	}
	
	private State navigationState = State.RECKONING;
	
	private boolean isCCW;

	public Navigator(Controllers cs) {
		controllers = cs;
		navigationState = State.RECKONING;
		isCCW = false;
	}
	
	public void reset() {
		previousRobLoc = null;
		previousTracingLoc = null;
		startTracingLoc = null;
		destination = null;
		backTrackDes = null;
		
		navigationState = State.RECKONING;
		isCCW = false;
	}

	public void setDestination(MapLocation loc) {
		if (loc.equals(destination))
			return;
		else{
			destination = new MapLocation(loc.x, loc.y);
			backTrackDes = new MapLocation(loc.x, loc.y);
		}
	}

	public MapLocation getDestination() {
		return destination;
	}

	public Direction getNextDir(int tolerance) throws GameActionException {
		
		controllers.myRC.setIndicatorString(0,"");
		controllers.myRC.setIndicatorString(1,"");
//		controllers.myRC.setIndicatorString(2,"");
		
		if (controllers.myRC.getLocation().equals(previousRobLoc)){
			controllers.myRC.setIndicatorString(0,"STAY" );
			return previousDir;
		}
		else{
			previousRobLoc = controllers.myRC.getLocation();
			if (destination == null){
				controllers.myRC.setIndicatorString(0,"NULL" );
				reset();
				previousDir = Direction.OMNI;
				return Direction.OMNI;
			}
			else {
				controllers.myRC.setIndicatorString(0,controllers.myRC.getLocation()+" "+destination.toString() );
				
				previousDir = Bug(controllers.myRC.getLocation(), backTrackDes, tolerance);
				return previousDir;
			}
		}
	}

	public Direction Bug(MapLocation s, MapLocation t, int tolerance)
			throws GameActionException {
		
		if (s.distanceSquaredTo(t) <= tolerance) {
			reset();
			return Direction.OMNI;
		}

		
		Direction nextDir;

		// if target is not traversable
		while (!isTraversable(t)) {
			nextDir = s.directionTo(t);

			if (nextDir == Direction.OMNI) {
				reset();
				return Direction.OMNI;
			}
			t = t.subtract(nextDir);
		}
		
		backTrackDes = t;
		
		Direction initDir = s.directionTo(t);
		MapLocation nextLoc;
		
		switch(navigationState){
			case RECKONING:
				if ( controllers.motor.canMove(initDir) ) {
					return initDir;
				}
				else {
					navigationState = State.START;
					nextLoc = traceNext(s, controllers.myRC.getDirection(), !isCCW);
					return s.directionTo(nextLoc);
				}
			case START:
				navigationState = State.TRACING;
				startTracingLoc = controllers.myRC.getLocation();
				
				if (startTracingLoc.equals(previousTracingLoc)){
					isCCW = !isCCW;
					previousTracingLoc = null;
				}
				nextLoc = traceNext(s, controllers.myRC.getDirection(), !isCCW);
				return s.directionTo(nextLoc);
			case TRACING:
				// Have gone around the obstacle
				if (controllers.myRC.getLocation().equals(startTracingLoc) ){
					navigationState = State.ROUNDED;
				}
				
				// The way is open
				if (  isOpen(backTrackDes) && controllers.motor.canMove(initDir) ){
					navigationState = State.RECKONING;
					previousTracingLoc = startTracingLoc;
					startTracingLoc = null;
					return initDir;
				}
				
				nextLoc = traceNext(s, controllers.myRC.getDirection(), !isCCW);
				controllers.myRC.setIndicatorString(1, startTracingLoc.toString()+" "+nextLoc.toString() + " KEEP" );
				return s.directionTo(nextLoc);
			case ROUNDED:
				if (  controllers.motor.canMove(initDir) ){
					navigationState = State.RECKONING;
					previousTracingLoc = startTracingLoc;
					startTracingLoc = null;
					return initDir;
				}
				
				nextLoc = traceNext(s, controllers.myRC.getDirection(), !isCCW);
				controllers.myRC.setIndicatorString(1, startTracingLoc.toString()+" "+nextLoc.toString() + " KEEP" );
				return s.directionTo(nextLoc);
			default:
				return initDir;
		}
		
	}

	public Direction tangentBug(MapLocation s, MapLocation t, int tolerance)
			throws GameActionException {
		
		if (s.distanceSquaredTo(t) <= tolerance) {
			destination = null;
			return Direction.OMNI;
		}

		Direction initDir;
		Direction nextDir;

		// if target is not traversable
		while (!isTraversable(t)) {
			nextDir = s.directionTo(t);
			
			if (nextDir == Direction.OMNI) {
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
			if (step++ > THRESHOLD || currentLoc.equals(t)) {
				// myRC.setIndicatorString(2, "dead reckoning");
				// if(myRC.senseTerrainTile( currentLoc ) != null)
				// subDestinations.add(0,currentLoc);
				return initDir;
			}
			currentLoc = currentLoc.add(nextDir);
		} while (isTraversable(currentLoc));
		currentLoc = currentLoc.subtract(nextDir);

		// myRC.setIndicatorString(2, "tracing");
		MapLocation traceLoc[] = { currentLoc, currentLoc };
		Direction initTraceDir[] = { Direction.NONE, Direction.NONE };

		// tracing
		boolean isCW = true;

		do {
			if (isCW) {
				traceLoc[1] = traceNext(traceLoc[1], initDir, isCW);
				if (initTraceDir[1].equals(Direction.NONE))
					initTraceDir[1] = currentLoc.directionTo(traceLoc[1]);
				if (traceLoc[1].equals(t))
					return initTraceDir[1];
			} else {
				traceLoc[0] = traceNext(traceLoc[0], initDir, isCW);
				if (initTraceDir[0].equals(Direction.NONE))
					initTraceDir[0] = currentLoc.directionTo(traceLoc[0]);
				if (traceLoc[0].equals(t))
					return initTraceDir[0];
			}

			isCW = !isCW;
		} while (!isOpen(s, t, traceLoc[isCW ? 0 : 1]));

		if (s.equals(traceLoc[isCW ? 0 : 1]))
			return controllers.myRC.getDirection();
		else {
			return tangentBug(s, traceLoc[isCW ? 0 : 1], tolerance);
		}
		// do {
		// currentLoc = traceNext( currentLoc, nextDir, true );
		// } while ( !isOpen(s, t, currentLoc) );
		//
		// if (s.equals(currentLoc))
		// return myRC.getDirection();
		// else {
		// return tangentBug(s, currentLoc, tolerance);
		// }
	}

	private boolean isOpen(MapLocation destLoc) throws GameActionException {
		MapLocation testLoc = controllers.myRC.getLocation();
		
		while(!testLoc.equals(destLoc)){
			testLoc = testLoc.add(testLoc.directionTo(destLoc));
			if ( !isTraversable(testLoc) ){
				return false;
			}
		}
		return true;
	}
	
	private boolean isOpen(MapLocation sourceLoc, MapLocation destLoc,
			MapLocation bugLoc) throws GameActionException {
		return isTraversable(bugLoc.add(sourceLoc.directionTo(bugLoc)))
				&& isTraversable(bugLoc.add(bugLoc.directionTo(destLoc)));
	}

	private MapLocation traceNext(MapLocation currentLoc, Direction faceDir,
			boolean cw) throws GameActionException {

		// Put isTraversable first so that the second while loop will rotate the
		// direction to walkable position
		// Try code reuse in the future

		if (cw) {
			while (controllers.motor.canMove(faceDir)/*
										 * isTraversable(currentLoc.add(faceDir))
										 */) {
				faceDir = faceDir.rotateRight();
			}
			while (!controllers.motor.canMove(faceDir)/*
										 * !isTraversable(currentLoc.add(faceDir)
										 */) {
				faceDir = faceDir.rotateLeft();
			}
		} else {
			while (controllers.motor.canMove(faceDir)/*
										 * isTraversable(currentLoc.add(faceDir))
										 */) {
				faceDir = faceDir.rotateLeft();
			}
			while (!controllers.motor.canMove(faceDir)/*
										 * !isTraversable(currentLoc.add(faceDir)
										 */) {
				faceDir = faceDir.rotateRight();
			}
		}

		return currentLoc.add(faceDir);
	}

	private boolean isTraversable(MapLocation loc) throws GameActionException {
		TerrainTile tile = controllers.myRC.senseTerrainTile(loc);

		if (controllers.sensor != null && controllers.sensor.canSenseSquare(loc)) {
			return ((tile == null) || (tile == TerrainTile.LAND))
					&& (controllers.sensor.senseObjectAtLocation(loc, RobotLevel.ON_GROUND) == null);
		} else
			return (tile == null) || (tile == TerrainTile.LAND);
	}
	

}
