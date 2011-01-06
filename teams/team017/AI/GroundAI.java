package team017.AI;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import team017.util.UnitType;

import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;
import battlecode.common.WeaponController;

public class GroundAI extends AI {

	Set<MapLocation> mineLocations = new HashSet<MapLocation>();
	private boolean isSoldier;
	private boolean isConstructor;
	
	public GroundAI(RobotController rc) {
		super(rc);
	}

	public void proceed() {

		//Initial movement
		if (Clock.getRoundNum() == 0) {
			init();
//			init_revolve();
		} else {
			myRC.turnOff();
		}
		
		while (true) {
			// MessageHandler encoder = new BorderMessage(myRC, comm,
			// Direction.NORTH);
			// encoder.send();
			//
			// Message incomingMsg;
			// MessageHandler decoder = new BorderMessage(incomingMsg);
			// decoder.getMessageType();
			// ((BorderMessage) decoder).getBorderDirection();

			try {
				updateComponents();
				
				/*** beginning of main loop ***/
				if (!motor.isActive()) {
					// navigate();
					if (motor.canMove(myRC.getDirection())) {
						// System.out.println("about to move");
						motor.moveForward();
					} else {
						motor.setDirection(myRC.getDirection().rotateRight());
					}
				}
				
				if (isSoldier){
					attack();
				}
				
				if (isConstructor){
					updateMineSet();
					build();
				}

				evaluateNextState();
				sense_border();
				myRC.setIndicatorString(0, borders[0] + "," + borders[1] + "," + borders[2] + "," + borders[3]);
				myRC.setIndicatorString(1,myRC.getLocation() + "");

				myRC.yield();

				/*** end of main loop ***/
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}

	private void init() {
			try {
				for (int i = 0; i < 4; ++i){
					sense_border();
					// Rotate twice Right for a 90 degrees turn
					motor.setDirection(myRC.getDirection().rotateRight().rotateRight());
					updateMineSet();
					myRC.yield();
					myRC.setIndicatorString(0, borders[0] + "," + borders[1] + "," + borders[2] + "," + borders[3]);
					myRC.setIndicatorString(1,myRC.getLocation() + "");
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
				myRC.setIndicatorString(2, myRC.getLocation().toString()+locationList[index].toString());
				
				Direction nextDir = navigator.getNextDir(0);
				if (nextDir == Direction.OMNI) {
					index++;
					if (index == 4)	return;
				
					continue;
				}
				
				if (!motor.isActive() && motor.canMove(nextDir) ) {
					if ( myRC.getDirection() == nextDir ) {
						// System.out.println("about to move");
						motor.moveForward();
					} else {
						motor.setDirection(myRC.getDirection().rotateRight());
					}
				}
				
				myRC.yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
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
	
	private void evaluateNextState(){
		if(weapons != null)
			isSoldier = true;
		if(builder != null)
			isConstructor = true;
	}
	
	private boolean attack(){
		Robot[] robots = sensor.senseNearbyGameObjects(Robot.class);
		LinkedList<Robot> robotlist = new LinkedList<Robot>();
		RobotInfo info;
		double remainhp = Chassis.HEAVY.maxHp;
		for (Robot r: robots) {
			if (r.getTeam() == myRC.getTeam())
				continue;
			try {
				info = sensor.senseRobotInfo(r);
				if (!info.on)
					continue;
				if (info.maxHp - info.hitpoints < remainhp) {
					remainhp = info.maxHp - info.hitpoints;
					robotlist.addFirst(r);
				} else {
					robotlist.addLast(r);
				}
			} catch (GameActionException e) {
				System.out.println("cannot sense nearby in attack");
				e.printStackTrace();
			}
		}
		if (robotlist.size() == 0)
			return false;
		MapLocation enemyloc;
		for (Robot r: robotlist) {
			try {
				enemyloc = sensor.senseLocationOf(r);
				for (WeaponController weapon : weapons){
					weapon.attackSquare(enemyloc, r.getRobotLevel());
				}
				return true;
			} catch (GameActionException e) {continue;}		
		}
		return false;
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
					while(!constructUnit(myRC.getLocation().add(myRC.getDirection()),UnitType.RECYCLER))
						myRC.yield();					
				}
			}	
		}
	}
//	private void buildRecycler() throws GameActionException{
//		MapLocation buildLoc = myRC.getLocation().add(myRC.getDirection());
//		if (sensor.senseObjectAtLocation(buildLoc,RobotLevel.ON_GROUND) == null && myRC.senseTerrainTile(buildLoc) == TerrainTile.LAND) {
//			builder.build(Chassis.BUILDING, buildLoc);
//			myRC.yield();
//			builder.build(ComponentType.RECYCLER, buildLoc, RobotLevel.ON_GROUND);
//		}
//	}
	
	private void navigate(){
		
	}
}
