package team017.AI;

import java.util.HashSet;
import java.util.Set;

import team017.message.BorderMessage;
import team017.message.MessageHandler;
import team017.construction.UnitType;

import battlecode.common.*;

public class ConstructorAI extends AI {
	
	Set<MapLocation> mineLocations = new HashSet<MapLocation>();
	Set<MapLocation> recyclerLocations = new HashSet<MapLocation>();
	
	public ConstructorAI(RobotController rc) {
		super(rc);
	}
	
	public void yield() throws GameActionException {
		controllers.myRC.yield();
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
				e.printStackTrace();
			}
//			init_return();
		}
		
		while (true) {

			try {
				if (controllers.motor != null) {
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
					controllers.motor.setDirection(controllers.myRC.getDirection().rotateRight().rotateRight());
					yield();
//					controllers.myRC.setIndicatorString(0, borders[0] + "," + borders[1] + "," + borders[2] + "," + borders[3]);
//					controllers.myRC.setIndicatorString(1,controllers.myRC.getLocation() + "");
				}
			} catch (GameActionException e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
	}

	private void sense_border(){
		try {
			
			Direction[] addDirs = new Direction[3];
			
			if (controllers.myRC.getDirection().isDiagonal()) {
				addDirs[0] = controllers.myRC.getDirection().rotateLeft();
				addDirs[1] = controllers.myRC.getDirection().rotateRight();
			} else {
				addDirs[0] = controllers.myRC.getDirection();
			}
			
			int j = -1;
			while (addDirs[++j] != null) {
				MapLocation currentLoc = controllers.myRC.getLocation();

				int i;
				for (i = 3; i > 0; i--) {
					if (controllers.myRC.senseTerrainTile(currentLoc.add(addDirs[j],i)) != TerrainTile.OFF_MAP)
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
//				controllers.myRC.setIndicatorString(2, controllers.myRC.getLocation().toString()+locationList[index].toString());
				
				Direction nextDir = navigator.getNextDir(0);
				if (nextDir == Direction.OMNI) {
					index++;
					if (index == 4)	return;
				
					continue;
				}
				
				if (!controllers.motor.isActive() && controllers.motor.canMove(nextDir) ) {
					if ( controllers.myRC.getDirection() == nextDir ) {
						controllers.motor.moveForward();
					} else {
						controllers.motor.setDirection(nextDir);
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
				
				if (!controllers.motor.isActive() && controllers.motor.canMove(nextDir)) {
					if ( controllers.myRC.getDirection() == nextDir ) 
						controllers.motor.moveForward();
					else 
						controllers.motor.setDirection(nextDir);
				}
				yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
		
		MessageHandler msgHandler = new BorderMessage(controllers.myRC, controllers.comm, borders);
		msgHandler.send();
		controllers.myRC.yield();
		
	}
	
	private void updateMineSet() throws GameActionException {
		Mine[] minelist = controllers.sensor.senseNearbyGameObjects(Mine.class);
		for (Mine mine : minelist){
			if(controllers.sensor.senseObjectAtLocation(mine.getLocation(), RobotLevel.ON_GROUND) != null){
				if (mineLocations.contains(mine.getLocation()))
					mineLocations.remove(mine.getLocation());	
			} else {
				mineLocations.add(mine.getLocation());
			}
			
		}
	}
	
	private void build() throws GameActionException{
		controllers.myRC.setIndicatorString(0, mineLocations.toString());
		
		for (MapLocation mineLoc : mineLocations){
			if(controllers.myRC.getLocation().isAdjacentTo(mineLoc)){
				if(controllers.sensor.canSenseSquare(mineLoc)){
					 if(controllers.sensor.senseObjectAtLocation(mineLoc, RobotLevel.ON_GROUND) != null)
						 continue;
				}
				if(controllers.myRC.getDirection()!=controllers.myRC.getLocation().directionTo(mineLoc)){
					while(controllers.motor.isActive())
						controllers.myRC.yield();
					controllers.motor.setDirection(controllers.myRC.getLocation().directionTo(mineLoc));
					controllers.myRC.yield();
				}
				if(controllers.sensor.senseObjectAtLocation(mineLoc, RobotLevel.ON_GROUND) == null){
					while(!buildingSystem.constructUnit(controllers.myRC.getLocation().add(controllers.myRC.getDirection()),UnitType.RECYCLER))
						controllers.myRC.yield();					
				}
			}	
		}
//		if(fluxRate > 0  && controllers.myRC.getTeamResources() > 120)
//			buildingSystem.constructUnit(controllers.myRC.getLocation().add(controllers.myRC.getDirection()),UnitType.FACTORY);
	}
	
	private void navigate() throws GameActionException{
		
		if (!controllers.motor.isActive()) {
			if (!mineLocations.isEmpty()) {
				MapLocation currentLoc = controllers.myRC.getLocation();
				MapLocation nearest = currentLoc.add(Direction.NORTH, 100);
				for (MapLocation loc : mineLocations) {
					if (currentLoc.distanceSquaredTo(loc) < currentLoc.distanceSquaredTo(nearest))
						nearest = loc;
				}
				
				controllers.myRC.setIndicatorString(0, currentLoc + "," + nearest);
				
				navigator.setDestination(nearest);
				Direction nextDir = navigator.getNextDir(0);
				
				if (nextDir != Direction.OMNI) {
					if (controllers.myRC.getDirection() == nextDir) {
						if (controllers.motor.canMove(nextDir)) {
							controllers.motor.moveForward();
						}
					} else {
						controllers.motor.setDirection(nextDir);
					}	
				}
				
			} else {
				roachNavigate();
			}
		}
			
	}
	
	private void roachNavigate() throws GameActionException {
		// navigate();
		if (controllers.motor.canMove(controllers.myRC.getDirection())) {
			// System.out.println("about to move");
			controllers.motor.moveForward();
		} else {
			Direction tempDir = controllers.myRC.getDirection();
			int rotationTimes = Clock.getRoundNum() % 7;
			for (int i = 0; i <= rotationTimes; ++i){
				tempDir = tempDir.rotateRight();
			}
				controllers.motor.setDirection(tempDir);
		}
	}
}
