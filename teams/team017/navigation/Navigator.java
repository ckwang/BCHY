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
	private List<MapLocation> subDestinations;
	private MapLocation destination;
	
	public Navigator(RobotController rc) {
		myRC = rc;
		subDestinations = new LinkedList<MapLocation>();
		destination = null;
	}
	
	public void setDestination (int x, int y){
		destination = new MapLocation( x, y );
		//myRC.setIndicatorString(1, destination.toString());
	}
	
	public MapLocation getDestination(){
		return destination;
	}
	
	public Direction getNextDir() throws GameActionException {
		myRC.setIndicatorString(1, subDestinations.toString());
		if (destination == null)
			return Direction.NONE;
		else if (subDestinations.size() == 0){
			return tangentBug(myRC.getLocation(), destination);
		}
		else {
			MapLocation currentDes;
			currentDes = subDestinations.get(0);
			if ( currentDes.equals(myRC.getLocation()) ){
				subDestinations.remove(0);
				if (subDestinations.size() == 0){
					return tangentBug(myRC.getLocation(), destination);
				}
				else {
					currentDes = subDestinations.get(0);
					return myRC.getLocation().directionTo(currentDes);
				}
			}
			else {
				return myRC.getLocation().directionTo(currentDes);
			}
		}
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
		int THRESHOLD = 5;
		
		// dead reckoning
		do {
			nextDir = currentLoc.directionTo(t);
			if ( step++ > THRESHOLD || nextDir == Direction.OMNI ){
				myRC.setIndicatorString(1, subDestinations.toString());
				if(myRC.senseTerrainTile( currentLoc ) != null)
					subDestinations.add(0,currentLoc);
				return initDir;
			}
			currentLoc = currentLoc.add(nextDir);
		} while ( isWalkable(currentLoc) );
		currentLoc = currentLoc.subtract(nextDir);
		
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
			return tangentBug(s, traceLoc[isCW ? 0 : 1]);
		}
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
		return ( (tile == null) || (tile == TerrainTile.LAND) );
		//&& (!sensor.canSenseSquare(loc) || sensor.senseObjectAtLocation(loc, RobotLevel.ON_GROUND) != null);
	}	
}
