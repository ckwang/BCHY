package team017.AI;

import java.util.HashSet;
import java.util.Set;

import team017.message.BorderMessage;
import team017.message.MessageHandler;
import team017.construction.Builder;
import team017.construction.UnitType;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;

public class ConstructorAI extends AI {
	
	Set<MapLocation> mineLocations = new HashSet<MapLocation>();
	
	public ConstructorAI(RobotController rc) {
		super(rc);
	}
	
	public void yield() throws GameActionException {
		myRC.yield();
		updateComponents();
		updateMineSet();
		updateFluxRate();
		sense_border();
	}

	public void proceed() {

		//Initial movement
		if (Clock.getRoundNum() == 0) {
			init();
			init_revolve();
			try {
				yield();
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			updateComponents();
//			init_return();
		}
		
		while (true) {

			try {
				if (motor != null) {
					navigate();
				}
				build();
				yield();

			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}

	private void init() {
			try {
				for (int i = 0; i < 4; ++i){
					// Rotate twice Right for a 90 degrees turn
					motor.setDirection(myRC.getDirection().rotateRight().rotateRight());
					yield();
//					myRC.setIndicatorString(0, borders[0] + "," + borders[1] + "," + borders[2] + "," + borders[3]);
//					myRC.setIndicatorString(1,myRC.getLocation() + "");
				}
			} catch (GameActionException e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
	}

	private void sense_border(){
		try {
			
			Direction[] addDirs = new Direction[3];
			
			if (myRC.getDirection().isDiagonal()) {
				addDirs[0] = myRC.getDirection().rotateLeft();
				addDirs[1] = myRC.getDirection().rotateRight();
			} else {
				addDirs[0] = myRC.getDirection();
			}
			
			int j = -1;
			while (addDirs[++j] != null) {
				MapLocation currentLoc = myRC.getLocation();

				int i;
				for (i = 3; i > 0; i--) {
					if (myRC.senseTerrainTile(currentLoc.add(addDirs[j],i)) != TerrainTile.OFF_MAP)
						break;
				}

				// i == 3 means no OFF_MAP sensed
				if (i != 3) {
					switch (addDirs[j]) {
					case NORTH:
						borders[0] = currentLoc.y - (i + 1);
						break;
					case EAST:
						borders[1] = currentLoc.x + (i + 1);
						break;
					case SOUTH:
						borders[2] = currentLoc.y + (i + 1);
						break;
					case WEST:
						borders[3] = currentLoc.x - (i + 1);
						break;
					}
				}
			}
		} catch (Exception e) {
			System.out.println("caught exception:");
			e.printStackTrace();
		}
		
	}
	
	private void init_revolve() {
		
		MapLocation[] locationList = {
				homeLocation.add(Direction.NORTH_EAST, 2),
				homeLocation.add(Direction.SOUTH_EAST, 2),
				homeLocation.add(Direction.SOUTH_WEST, 2),
				homeLocation.add(Direction.NORTH_WEST, 2)};
		int index = 0;
		
		while (true) {
			try {
				
				navigator.setDestination(locationList[index]);
//				myRC.setIndicatorString(2, myRC.getLocation().toString()+locationList[index].toString());
				
				Direction nextDir = navigator.getNextDir(0);
				if (nextDir == Direction.OMNI) {
					index++;
					if (index == 4)	return;
				
					continue;
				}
				
				if (!motor.isActive() && motor.canMove(nextDir) ) {
					if ( myRC.getDirection() == nextDir ) {
						motor.moveForward();
					} else {
						motor.setDirection(nextDir);
					}
				}
				
				yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}
	
	private void init_return() {
		navigator.setDestination(homeLocation);
		
		while (true) {
			try {
				Direction nextDir = navigator.getNextDir(0);
				
				if (nextDir == Direction.OMNI)	break;
				
				if (!motor.isActive() && motor.canMove(nextDir)) {
					if ( myRC.getDirection() == nextDir ) 
						motor.moveForward();
					else 
						motor.setDirection(nextDir);
				}
				yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
		
		MessageHandler msgHandler = new BorderMessage(myRC, comm, borders);
		msgHandler.send();
		myRC.yield();
		
	}
	
	private void updateMineSet() throws GameActionException {
		Mine[] minelist = sensor.senseNearbyGameObjects(Mine.class);
		for (Mine mine : minelist){
			if(sensor.senseObjectAtLocation(mine.getLocation(), RobotLevel.ON_GROUND) != null){
				if (mineLocations.contains(mine.getLocation()))
					mineLocations.remove(mine.getLocation());	
			} else {
				mineLocations.add(mine.getLocation());
			}
			
		}
	}
	
	private void build() throws GameActionException{
		myRC.setIndicatorString(0, mineLocations.toString());
		
		for (MapLocation mineLoc : mineLocations){
			if(myRC.getLocation().isAdjacentTo(mineLoc)){
				if(sensor.canSenseSquare(mineLoc)){
					 if(sensor.senseObjectAtLocation(mineLoc, RobotLevel.ON_GROUND) != null)
						 continue;
				}
				if(myRC.getDirection()!=myRC.getLocation().directionTo(mineLoc)){
					while(motor.isActive())
						myRC.yield();
					motor.setDirection(myRC.getLocation().directionTo(mineLoc));
					myRC.yield();
				}
				if(sensor.senseObjectAtLocation(mineLoc, RobotLevel.ON_GROUND) == null){
					while(!buildingSystem.constructUnit(myRC.getLocation().add(myRC.getDirection()),UnitType.RECYCLER))
						myRC.yield();					
				}
			}	
		}
//		if(fluxRate > 0  && myRC.getTeamResources() > 120)
//			buildingSystem.constructUnit(myRC.getLocation().add(myRC.getDirection()),UnitType.FACTORY);
	}
	
	private void navigate() throws GameActionException{
		
		if (!motor.isActive()) {
			if (!mineLocations.isEmpty()) {
				MapLocation currentLoc = myRC.getLocation();
				MapLocation nearest = currentLoc.add(Direction.NORTH, 100);
				for (MapLocation loc : mineLocations) {
					if (currentLoc.distanceSquaredTo(loc) < currentLoc.distanceSquaredTo(nearest))
						nearest = loc;
				}
				
				myRC.setIndicatorString(0, currentLoc + "," + nearest);
				
				navigator.setDestination(nearest);
				Direction nextDir = navigator.getNextDir(0);
				
				if (nextDir != Direction.OMNI) {
					if (myRC.getDirection() == nextDir) {
						if (motor.canMove(nextDir)) {
							motor.moveForward();
						}
					} else {
						motor.setDirection(nextDir);
					}	
				}
				
			} else {
				roachNavigate();
			}
		}
			
	}
	
	private void roachNavigate() throws GameActionException {
		// navigate();
		if (motor.canMove(myRC.getDirection())) {
			// System.out.println("about to move");
			motor.moveForward();
		} else {
			if((Clock.getRoundNum()/10) % 2 == 0)
				motor.setDirection(myRC.getDirection().rotateRight());
			else
				motor.setDirection(myRC.getDirection().rotateLeft());
		}
	}
}
