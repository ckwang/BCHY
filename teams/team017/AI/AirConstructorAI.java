package team017.AI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import team017.construction.UnitType;
import team017.message.BorderMessage;
import team017.message.BuildingLocationInquiryMessage;
import team017.message.BuildingLocationResponseMessage;
import team017.message.ConstructionCompleteMessage;
import team017.message.GridMapMessage;
import team017.message.MineInquiryMessage;
import team017.message.MineResponseMessage;
import team017.message.ScoutingInquiryMessage;
import team017.message.ScoutingResponseMessage;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;


public class AirConstructorAI extends AI {


	private int id;
	
	private Set<MapLocation> mineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> recyclerLocations = new HashSet<MapLocation>();
	private Set<MapLocation> builtLocations = new HashSet<MapLocation>();
	private MapLocation currentLoc = controllers.myRC.getLocation();
	private int builtIdleRound = 0;
	
	MapLocation nearestMine = null;
	
	
	public AirConstructorAI(RobotController rc) {
		super(rc);
		id = rc.getRobot().getID();
	}
	
	@Override
	public void yield() {
		super.yield();
		currentLoc = controllers.myRC.getLocation();
		if (builtIdleRound > 0)
			builtIdleRound--;
	}

	@Override
	public void proceed() {

		// ask for mine locations
		while (controllers.comm.isActive())
			yield();
		msgHandler.queueMessage(new MineInquiryMessage());
		
		while (true) {
			
			controllers.myRC.setIndicatorString(0, builtIdleRound + "");
			try {processMessages();} catch (Exception e) {e.printStackTrace();}
			
			try {
				if (buildRecyclers()) {
					boolean hasAdjacentMine = false;
					for (MapLocation mineLoc: mineLocations) {
						if (mineLoc.isAdjacentTo(nearestMine) && !mineLoc.equals(nearestMine)) {
							if (!recyclerLocations.contains(mineLoc)) {
								hasAdjacentMine = true;
								nearestMine = mineLoc;
								break;
							}	
						}
					}
					if (!hasAdjacentMine) {
						msgHandler.queueMessage(new BuildingLocationInquiryMessage(nearestMine));
						builtIdleRound = 50;
						nearestMine = null;
					}
				} else {
					msgHandler.queueMessage(new MineInquiryMessage());
				}
				
			} catch (Exception e) {e.printStackTrace();}
			

			if (builtIdleRound == 0)
				navigate();
			else if (Clock.getRoundNum() % 5 == 0)
				msgHandler.queueMessage(new MineInquiryMessage());

			
//			String s = "";
//			for (MapLocation loc : mineLocations) {
//				s += loc.toString();
//			}
//			
//			controllers.myRC.setIndicatorString(0, s);
//			
//			s = "";
//			for (MapLocation loc : recyclerLocations) {
//				s += loc.toString();
//			}
//			controllers.myRC.setIndicatorString(1, s);
//			
			yield();
		}
	}
	
	@Override
	protected void processMessages() throws GameActionException {
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			switch (msgHandler.getMessageType(msg)) {
			
			case MINE_RESPONSE_MESSAGE: {
				MineResponseMessage handler = new MineResponseMessage(msg);
				
				if (handler.getConstructorID() == id) {
					mineLocations.addAll(handler.getMineLocations());
				}
				break;
			}
			
			case BUILDING_LOCATION_RESPONSE_MESSAGE: {

				BuildingLocationResponseMessage handler = new BuildingLocationResponseMessage(msg);
				
//				controllers.myRC.setIndicatorString(0, "Type" +handler.getUnitType() + " " +  Clock.getRoundNum());
//				controllers.myRC.setIndicatorString(1, "current location:" + controllers.myRC.getLocation());
//				controllers.myRC.setIndicatorString(2, "build loc:" + handler.getBuildableLocation());
				
//				// see if the message is intended for it
//				if (handler.getConstructorID() != controllers.myRC.getRobot().getID())
//					break;

				// if it is not built
				if (builtLocations.contains(handler.getSourceLocation()))
					break;

				UnitType type = handler.getUnitType();
				if (type == null) { // there is nothing to build
					builtLocations.add(handler.getSourceLocation());
					builtIdleRound = 0;
				} else if (handler.getBuildableLocation() != null) {
					MapLocation buildLoc = handler.getBuildableLocation();
					if (buildBuildingAtLoc(buildLoc, type)) {
						if (type == UnitType.FACTORY)
							msgHandler.queueMessage(new MineInquiryMessage());
						builtIdleRound = 50;
						msgHandler.queueMessage(new BuildingLocationInquiryMessage(handler.getSourceLocation()));
						yield();
					}
				}

				break;
			}
			
				
			}
		}
		
	}
	
	private void findNearestMine () {
		if (nearestMine == null)
			nearestMine = new MapLocation(0, 0);

		// find a eligible mine
		for (MapLocation mineLoc : mineLocations) {
			if (recyclerLocations.contains(mineLoc))
				continue;
			
			if (currentLoc.distanceSquaredTo(mineLoc) < currentLoc.distanceSquaredTo(nearestMine))
				nearestMine = mineLoc;
		}

	}
	
	private boolean buildRecyclers() throws GameActionException {
		
		findNearestMine();
		
		if (nearestMine.x == 0) {
			nearestMine = null;
			return false;
		}

		// if there is a eligible site
		if (currentLoc.distanceSquaredTo(nearestMine) <= 2) {
			if (controllers.builder.canBuild(Chassis.BUILDING, nearestMine)) {
				if (buildBuildingAtLoc(nearestMine, UnitType.RECYCLER))
//					controllers.myRC.setIndicatorString(2, "BuildRecycler");
					recyclerLocations.add(nearestMine);
					return true;
			} else {
				recyclerLocations.add(nearestMine);
			}
			nearestMine = null;
		}

		return false;
	}
	
	private boolean buildBuildingAtLoc(MapLocation buildLoc, UnitType type) throws GameActionException {
		// if already standing on the building site
		if (controllers.myRC.getLocation().equals(buildLoc)) {
			while (controllers.motor.isActive())
				yield();

			// move forward or backward if possible
			if (controllers.motor.canMove(controllers.myRC.getDirection())) {
				controllers.motor.moveForward();
			} else if (controllers.motor.canMove(controllers.myRC.getDirection().opposite())) {
				controllers.motor.moveBackward();
			} else {
				return false;
			}
			yield();
		}

		// move to the adjacent of the building site
		navigator.setDestination(buildLoc);
		while (!controllers.myRC.getLocation().isAdjacentTo(buildLoc)) {
//			controllers.myRC.setIndicatorString(2, controllers.myRC.getLocation() + "," + buildLoc);
			if (!controllers.motor.isActive()) {
				Direction nextDir = navigator.getNextDir(0);

//				controllers.myRC.setIndicatorString(2, nextDir.toString());
				
				if (nextDir == Direction.OMNI)
					break;
				if (controllers.myRC.getDirection() == nextDir) {
					if (controllers.motor.canMove(nextDir))
						controllers.motor.moveForward();
				} else {
					controllers.motor.setDirection(nextDir);
				}
			}

			yield();
		}

		// face the building site
		Direction buildDir = controllers.myRC.getLocation().directionTo(buildLoc);
		if (controllers.myRC.getDirection() != buildDir) {
			while (controllers.motor.isActive())
				yield();

			controllers.motor.setDirection(buildDir);
			yield();
		}

		// if everything looks okay, construct
		while (!buildingSystem.constructUnit(buildLoc, type)) {
			if (!controllers.builder.canBuild(Chassis.BUILDING, buildLoc))
				return false;
			yield();
		}
		msgHandler.clearOutQueue();
		msgHandler.queueMessage(new ConstructionCompleteMessage(buildLoc, type));
		if (type == UnitType.RECYCLER) {
			msgHandler.queueMessage(new GridMapMessage(borders, homeLocation,gridMap));
		} else {
			msgHandler.queueMessage(new BorderMessage(borders, homeLocation));
		}
		return true;
	}
	
	private void navigate() {
		if (nearestMine != null) {
			while (!controllers.myRC.getLocation().equals(nearestMine)) {
				MapLocation myloc = controllers.myRC.getLocation();
				Direction mydir = controllers.myRC.getDirection();
				Direction todest = myloc.directionTo(nearestMine);
				if (mydir != todest) {
					while (!setDirection(todest))
						yield();
				}
				while (controllers.motor.isActive())
					yield();
				if (!controllers.motor.canMove(todest)) {
					while (!setDirection(todest.rotateLeft()))
							yield();
					while (controllers.motor.isActive())
						yield();
					if (moveForward())
						continue;
					while (!setDirection(todest.rotateRight()))
						yield();
					while (controllers.motor.isActive())
						yield();
				}
				moveForward();
				yield();
			}
		}
		
	}
	
	public boolean moveForward() {
		if (!controllers.motor.isActive() && controllers.motor.canMove(controllers.myRC.getDirection())) {
			try {
				controllers.motor.moveForward();
				return true;
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean setDirection(Direction dir) {
		if (controllers.myRC.getDirection() == dir)
			return true;
		if (!controllers.motor.isActive()) {
			try {
				controllers.motor.setDirection(dir);
				return true;
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
}
