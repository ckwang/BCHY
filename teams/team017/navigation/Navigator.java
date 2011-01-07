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
	
	private MapLocation startTracingLoc;
	private MapLocation previousTracingLoc;
	
	private MapLocation previousRobLoc;
	private Direction previousDir;
	
	private boolean isTracing;
	private boolean isRounded;
	private boolean isCCW;

	public Navigator(Controllers cs) {
		controllers = cs;
		isTracing = false;
		isRounded = false;
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
		
		if (controllers.myRC.getLocation().equals(previousRobLoc)){
			return previousDir;
		}
		else{
			previousRobLoc = controllers.myRC.getLocation();
			if (destination == null){
				previousDir = Direction.OMNI;
				return Direction.OMNI;
			}
			else {
				controllers.myRC.setIndicatorString(0,controllers.myRC.getLocation()+" "+destination.toString() );
				controllers.myRC.setIndicatorString(1,"");
				controllers.myRC.setIndicatorString(2,"");
				
				previousDir = Bug(controllers.myRC.getLocation(), backTrackDes, tolerance);
				return previousDir;
			}
		}
	}

	public Direction Bug(MapLocation s, MapLocation t, int tolerance)
			throws GameActionException {
		
		if (s.distanceSquaredTo(t) <= tolerance) {
			previousRobLoc = null;
			previousTracingLoc = null;
			startTracingLoc = null;
			destination = null;
			return Direction.OMNI;
		}

		
		Direction nextDir;

		// if target is not traversable
		while (!isTraversable(t)) {
			nextDir = s.directionTo(t);

			if (nextDir == Direction.OMNI) {
				previousRobLoc = null;
				previousTracingLoc = null;
				startTracingLoc = null;
				destination = null;
				return Direction.OMNI;
			}
			t = t.subtract(nextDir);
		}
		
		backTrackDes = t;
		
		Direction initDir = s.directionTo(t);
		
		if (isTracing) {
			
				// Have gone around the obstacle
				if (controllers.myRC.getLocation().equals(startTracingLoc) ){
					isRounded = true;
				}
				// Find a way to leave the obstacle
				if ( (isRounded || isOpen(backTrackDes)) && controllers.motor.canMove(initDir)){
					isRounded = false;
					isTracing = false;
					previousTracingLoc = startTracingLoc;
					startTracingLoc = null;
					return initDir;
				}
				
				MapLocation nextLoc = traceNext(s, controllers.myRC.getDirection(), !isCCW);
				controllers.myRC.setIndicatorString(2,nextLoc.toString() + " KEEP" );
				return s.directionTo(nextLoc);
		}
		else{
			if ( controllers.motor.canMove(initDir) ) {
				return initDir;
			}
			else{
				startTracingLoc = controllers.myRC.getLocation();
				if (startTracingLoc.equals(previousTracingLoc)){
					isCCW = !isCCW;
					controllers.myRC.setIndicatorString(1, isCCW+"");
					previousTracingLoc = null;
				}
				isTracing = true;
				MapLocation nextLoc = traceNext(s, controllers.myRC.getDirection(), !isCCW);
				controllers.myRC.setIndicatorString(2,nextLoc.toString() + " START" );
				return s.directionTo(nextLoc);
			}
		}
		

//		if (!isTracing) {
//			if ( controllers.motor.canMove(initDir)/*isTraversable(s.add(initDir))*/ ) {
//				isTracing = false;
//				//controllers.myRC.setIndicatorString(1, initDir.toString());
//				return initDir;
//			} else {
//				isTracing = true;
//				MapLocation nextLoc = traceNext(s, controllers.myRC.getDirection(), true);
//				//controllers.myRC.setIndicatorString(1, nextLoc.toString());
//				return s.directionTo(nextLoc);
//			}
//		} else {
//			if ( controllers.motor.canMove(initDir)/*isTraversable(s.add(initDir) )*/
//					&& Util.isNotTurningBackward(initDir, controllers.myRC.getDirection())
//			) {
//				isTracing = false;
//				//controllers.myRC.setIndicatorString(1, initDir.toString() + " Tracing" );
//				return initDir;
//			} else {
//				isTracing = true;
//				MapLocation nextLoc = traceNext(s, controllers.myRC.getDirection(), true);
//				//controllers.myRC.setIndicatorString(1, nextLoc.toString() + " Tracing" );
//				return s.directionTo(nextLoc);
//			}
//		}
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
