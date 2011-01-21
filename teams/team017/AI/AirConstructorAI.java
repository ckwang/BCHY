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
	
	private boolean needStay = false;
	private boolean arrivedGatheringLoc = true;
	private MapLocation gatheringLoc;
	private Direction scoutingDir;
	private int group;
	
	private Set<MapLocation> mineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> recyclerLocations = new HashSet<MapLocation>();
	private Set<MapLocation> builtLocations = new HashSet<MapLocation>();
	private MapLocation currentLoc = controllers.myRC.getLocation();
	private int roundSinceLastBuilt = 0;
	
	MapLocation nearestMine = null;
	
	
	public AirConstructorAI(RobotController rc) {
		super(rc);
		id = rc.getRobot().getID();
		gatheringLoc = new MapLocation(homeLocation.x, homeLocation.y);
	}
	
	@Override
	public void yield() {
		super.yield();
		currentLoc = controllers.myRC.getLocation();
		roundSinceLastBuilt++;
	}

	@Override
	public void proceed() {

		// ask for mine locations
		while (controllers.comm.isActive())
			yield();
		msgHandler.queueMessage(new ScoutingInquiryMessage());
		yield();
		msgHandler.queueMessage(new MineInquiryMessage());
		
		while (true) {
			
			try {processMessages();} catch (Exception e) {e.printStackTrace();}
			
			try {
				if (buildRecyclers()) {
					msgHandler.queueMessage(new BuildingLocationInquiryMessage(nearestMine));
					roundSinceLastBuilt = 0;
					nearestMine = null;
				} else {
					msgHandler.queueMessage(new MineInquiryMessage());
				}
				
			} catch (Exception e) {e.printStackTrace();}
			

			if (roundSinceLastBuilt > 30)
				navigate();

			String s = "";
			for (MapLocation loc : mineLocations) {
				s += loc.toString();
			}
			
			controllers.myRC.setIndicatorString(0, s);
			
			s = "";
			for (MapLocation loc : recyclerLocations) {
				s += loc.toString();
			}
			controllers.myRC.setIndicatorString(1, s);
			
			if ( controllers.myRC.getLocation().distanceSquaredTo(gatheringLoc) < controllers.comm.type().range )
				msgHandler.queueMessage(new MineInquiryMessage());
			
			if (arrivedGatheringLoc && mineLocations.size() == 0){
				arrivedGatheringLoc = false;
				// TODO update scoutingLoc;
				if (scoutingDir != null){
					if( gridMap.updateScoutLocation(scoutingDir) )
						gatheringLoc = gridMap.getScoutLocation();
				}
			}
				
			
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
				if ( handler.getSourceLocation().equals(gatheringLoc) ){
					arrivedGatheringLoc = true;
				}
				break;
			}
			
			case BUILDING_LOCATION_RESPONSE_MESSAGE: {

				BuildingLocationResponseMessage handler = new BuildingLocationResponseMessage(msg);
				
//				controllers.myRC.setIndicatorString(0, "Type" +handler.getUnitType() + " " +  Clock.getRoundNum());
//				controllers.myRC.setIndicatorString(1, "current location:" + controllers.myRC.getLocation());
//				controllers.myRC.setIndicatorString(2, "build loc:" + handler.getBuildableLocation());
				
				// see if the message is intended for it
				if (handler.getConstructorID() != controllers.myRC.getRobot().getID())
					break;

				// if it is not built
				if (builtLocations.contains(handler.getSourceLocation()))
					break;

				UnitType type = handler.getUnitType();
				if (type == null) { // there is nothing to build
					builtLocations.add(handler.getSourceLocation());
				} else if (handler.getBuildableLocation() != null) {
					MapLocation buildLoc = handler.getBuildableLocation();
					if (buildBuildingAtLoc(buildLoc, type)) {
						if (type == UnitType.FACTORY)
							msgHandler.queueMessage(new MineInquiryMessage());
						roundSinceLastBuilt = 0;
						msgHandler.queueMessage(new BuildingLocationInquiryMessage(handler.getSourceLocation()));
						yield();
					}
				}

				break;
			}
			
			case SCOUTING_RESPONSE_MESSAGE: {
				ScoutingResponseMessage handler = new ScoutingResponseMessage(msg);
				controllers.myRC.setIndicatorString(2, "received");
				if (handler.getTelescoperID() == id) {
					scoutingDir = handler.getScoutingDirection();
				}
				
				if( gridMap.updateScoutLocation(scoutingDir) ) {
					gatheringLoc = gridMap.getScoutLocation();
					arrivedGatheringLoc = true;
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
					controllers.myRC.setIndicatorString(2, "BuildRecycler");
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
		
		MapLocation currentLoc = controllers.myRC.getLocation();
		Direction currentDir = controllers.myRC.getDirection();
		
		if (controllers.motor.isActive())
			return;
		
		Direction desDir;
		
		if (nearestMine != null) {
			desDir = currentLoc.directionTo(nearestMine);
			if (desDir == Direction.OMNI)
				return;
		} else if ( !needStay ) {
			desDir = currentLoc.directionTo(gatheringLoc);
			if (desDir == Direction.OMNI)
				return;
		}
		else {
			return;
		}
		
		try{
			// Can go toward destination
			if ( controllers.motor.canMove(desDir) ){
				if (currentDir == desDir)
					controllers.motor.moveForward();
				else
					controllers.motor.setDirection(desDir);
			} 
			// if can go to the 
			else if ( controllers.motor.canMove(desDir.rotateLeft()) ){
				if (currentDir == desDir.rotateLeft())
					controllers.motor.moveForward();
				else
					controllers.motor.setDirection(desDir.rotateLeft());
			}
			else if ( controllers.motor.canMove(desDir.rotateRight()) ){
				if (currentDir == desDir.rotateRight())
					controllers.motor.moveForward();
				else
					controllers.motor.setDirection(desDir.rotateRight());
			}
		} catch (GameActionException e){
			
		}
		
	}
	
}
