package team017.navigation;

import java.util.LinkedList;
import java.util.List;

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
	
	public Navigator(RobotController rc, SensorController sensor) {
		myRC = rc;
		this.sensor = sensor;
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
			if ( step++ > THRESHOLD || nextDir == Direction.OMNI )
				return initDir;
			currentLoc = currentLoc.add(nextDir);
		} while ( isWalkable(currentLoc) );
		currentLoc = currentLoc.subtract(nextDir);
		
		MapLocation traceLoc[] = {currentLoc, currentLoc};
		Direction initTraceDir[] = {Direction.NONE, Direction.NONE};
		
		// tracing
		boolean isCW = true; 
		do {
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

		return tangentBug(s, traceLoc[isCW ? 0 : 1]);
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
		(!sensor.withinRange(loc) || sensor.senseObjectAtLocation(loc, RobotLevel.ON_GROUND) != null);
	}	
}
