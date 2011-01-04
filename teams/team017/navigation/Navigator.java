package team017.navigation;

import java.util.LinkedList;
import java.util.List;

import battlecode.common.ComponentClass;
import battlecode.common.ComponentController;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;
import battlecode.common.TerrainTile;

/**
 * Navigator object that navigates the robot using tangent bug algorithm
 */

public class Navigator {
	private RobotController myRC;
	private SensorController sensor;
	private List<MapLocation> subDestinations;
	private String indicator;
	
	public Navigator(RobotController rc) {
		myRC = rc;
		ComponentController[] components = myRC.components();
		for (ComponentController cc: components) {
			if (cc.componentClass() == ComponentClass.SENSOR)
				sensor = (SensorController)cc;
		}
		subDestinations = new LinkedList<MapLocation>();
	}
	
	public Direction tangentBug(MapLocation s, MapLocation t) throws GameActionException {
		if ( s.equals(t) ) {
			return Direction.OMNI;
		}
		
		Direction initDir;
		Direction nextDir;
		
		// if target is not walkable
		while ( !isWalkable(t) ) {
			nextDir = s.directionTo(t);
			t = t.subtract(nextDir);
		}

		initDir = s.directionTo(t);
		MapLocation currentLoc = s;
		
		int step = 0;
		int THRESHOLD = 10;
		
		// dead reckoning
		do {
			nextDir = currentLoc.directionTo(t);
			if ( step++ > THRESHOLD || nextDir == Direction.OMNI )	{
				myRC.setIndicatorString(0, "DEAD RECKONING!");
				indicator += currentLoc.toString();
				myRC.setIndicatorString(2, indicator);
				return initDir;
			}
			currentLoc = currentLoc.add(nextDir);
		} while ( isWalkable(currentLoc) );
		currentLoc = currentLoc.subtract(nextDir);
		indicator += currentLoc.toString();
		myRC.setIndicatorString(2, indicator);
		
		MapLocation traceLoc[] = {currentLoc, currentLoc};
		Direction initTraceDir[] = {Direction.NONE, Direction.NONE};
		
		// tracing
		boolean isCW = true; 
		do {
			//nextDir = currentLoc.directionTo(t);
			if ( isCW ) {
				traceLoc[1] = traceNext( traceLoc[1], nextDir, isCW );
				if (initTraceDir[1].equals(Direction.NONE))	initTraceDir[1] = currentLoc.directionTo(traceLoc[1]);
				if (traceLoc[1].equals(t))	return initTraceDir[1];
			} else {
				traceLoc[0] = traceNext( traceLoc[0], nextDir, isCW );
				if (initTraceDir[0].equals(Direction.NONE))	initTraceDir[0] = currentLoc.directionTo(traceLoc[0]);
				if (traceLoc[0].equals(t))	return initTraceDir[0];
			} 
			
			isCW = !isCW;
		} while ( !isOpen(s, t, traceLoc[isCW ? 0 : 1]) );
		
		myRC.setIndicatorString(0, !isCW ? "CW!" : "CCW!");
		myRC.setIndicatorString(1, s.directionTo(traceLoc[isCW ? 0 : 1]).toString());
		indicator += traceLoc[isCW ? 0 : 1].toString();
		myRC.setIndicatorString(2, indicator);

		return tangentBug(s, traceLoc[isCW ? 0 : 1]);
	}
	
	public Direction newTangentBug(MapLocation s, MapLocation t) throws GameActionException{
		indicator = s.toString()+",";
		return tangentBug(s, t);
	}
	
	private boolean isOpen(MapLocation sourceLoc, MapLocation destLoc, MapLocation bugLoc) throws GameActionException {
		return isWalkable( bugLoc.add(sourceLoc.directionTo(bugLoc)) ) && isWalkable( bugLoc.add(bugLoc.directionTo(destLoc)));
	}
	
	private MapLocation traceNext(MapLocation currentLoc, Direction faceDir, boolean cw) throws GameActionException {
		
		// Put Walkable first so that the second while loop will rotate the direction to walkable position
		// Try code reuse in the future
		if (cw){
			while ( isWalkable(currentLoc.add(faceDir)) ) {
				faceDir = faceDir.rotateRight();
			}
			while ( !isWalkable(currentLoc.add(faceDir)) ) {
				faceDir = faceDir.rotateLeft();
			}
		}
		else {
			while ( isWalkable(currentLoc.add(faceDir)) ) {
				faceDir = faceDir.rotateLeft();
			}
			while ( !isWalkable(currentLoc.add(faceDir)) ) {
				faceDir = faceDir.rotateRight();
			}
		}
		
		return currentLoc.add(faceDir);
	}

	private boolean isWalkable( MapLocation loc ) throws GameActionException {
		TerrainTile tile = myRC.senseTerrainTile( loc );
		return ((tile == null) || (tile.isTraversableAtHeight(RobotLevel.ON_GROUND))) &&
		(sensor.senseObjectAtLocation(loc, RobotLevel.ON_GROUND) != null);
//		(!myRC.canSenseSquare(loc) || myRC.senseAirRobotAtLocation(loc) == null);
	}	
}
