package team017.construction;

import java.util.ArrayList;
import java.util.List;

import team017.message.BuildingRequestMessage;
import team017.message.MessageHandler;
import team017.util.Controllers;
import team017.util.Util;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;

public class Builder {
	
	private Controllers controllers;
	private MessageHandler msgHandler;
	private RobotController rc;
	public Builder(Controllers controllers, MessageHandler msgHandler) {
		this.controllers = controllers;
		this.msgHandler = msgHandler;
		rc = controllers.myRC; 
	}

	public boolean constructUnit(MapLocation buildLoc, UnitType type, BuildingLocations builderLocs){
		try {
			
			// check if buildLoc is adjacent to current location
			if (!buildLoc.isAdjacentTo(rc.getLocation()))	return false;
			Direction buildDir = rc.getLocation().directionTo(buildLoc);
			
			// if there's enough resource
			if (rc.getTeamResources() > type.chassis.cost + 10) {
				
				
				// see if there are all the required builders 
				builderLocs.updateBuildingLocs();
				if (!builderLocs.isComplete(Util.getBuilderCode(controllers.builder.type()), type.requiredBuilders))	{
					return false;
				}
				
				// check if the unit can be built at the desired location
				if (canConstruct(buildDir, type.chassis)) {
					controllers.builder.build(type.chassis, buildLoc);
					rc.yield();
					
					// send messages to other builders
					
					for (int c : Util.builderCodes) {
						if (c != Util.getBuilderCode(controllers.builder.type()) && (c & type.requiredBuilders) > 0)
							msgHandler.queueMessage(new BuildingRequestMessage(builderLocs.getLocations(c), buildLoc, type));
					}
					
//					for (int builder: type.requiredBuilders)
					
//					for(ComponentType builder: type.requiredBuilders){
//						if (builder != controllers.builder.type())
//							msgHandler.queueMessage(new BuildingRequestMessage(builderLocs.getLocations(builder), buildLoc, type));
//					}
					
					// build my responsible parts
					for (ComponentType com : type.getComponentList(controllers.builder.type())) {
						while(rc.getTeamResources() < com.cost + 10 || controllers.builder.isActive()) {
							msgHandler.process();
							rc.yield();
						}
						controllers.builder.build(com, buildLoc, type.chassis.level);
					}
					
					// turn on if the unit has all the parts
					if (Util.containsComponent(controllers, buildLoc, type.chassis.level, type))
						rc.turnOn(buildLoc, type.chassis.level);
					
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean constructUnit(MapLocation buildLoc, UnitType type){
		try{
			Chassis chassis = type.chassis;
			if (rc.getTeamResources() > chassis.cost + 20) {
				if (canConstruct(chassis)) {
					// build a chassis there
					controllers.builder.build(chassis, buildLoc);
					rc.yield();
					
					// build the components
					for (ComponentType com : type.getComponentList(controllers.builder.type())) {
						while(controllers.builder.isActive() || rc.getTeamResources() < com.cost + 20)
							rc.yield();
						
						// if the chassis is not there anymore
						if (controllers.builder.canBuild(Chassis.BUILDING, buildLoc))
							return false;
						
//						if (controllers.sensor.senseObjectAtLocation(buildLoc, RobotLevel.ON_GROUND) == null)
//							return false;
//						
						controllers.builder.build(com, buildLoc, chassis.level);
					}
					rc.turnOn(buildLoc, chassis.level);
					return true;
				}
			}
			return false;
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}

	
	
	public boolean constructUnit(UnitType type){
		try{
			MapLocation buildLoc = turnToAvailableSquare(type.chassis);
			return constructUnit(buildLoc, type);
		} catch (Exception e){
			return false;
		}
	}

	public boolean constructComponent(MapLocation buildLoc, UnitType type){
		try{
			for (ComponentType com : type.getComponentList(controllers.builder.type())) {
				while (rc.getTeamResources() < com.cost + 20)
					rc.yield();
				while (controllers.builder.isActive())
					rc.yield();
				controllers.builder.build(com, buildLoc, type.chassis.level);
			}
			
			// turn on if the unit has all the parts
			if (Util.containsComponent(controllers, buildLoc, type.chassis.level, type))
				rc.turnOn(buildLoc, type.chassis.level);
			
			return true;
		}catch (Exception e){
			return false;
		}
	}
	
	public boolean canConstruct(Direction dir, Chassis chassis) throws GameActionException {
//		if (controllers.sensor.senseObjectAtLocation(rc.getLocation().add(dir), level) == null
//				&& rc.senseTerrainTile(rc.getLocation().add(dir)) == TerrainTile.LAND)
//			return true;
//		return false;
		return controllers.builder.canBuild(chassis, rc.getLocation().add(dir));
	}
	
	public boolean canConstruct(Chassis chassis) throws GameActionException {
		return canConstruct(rc.getDirection(), chassis);
	}

	private MapLocation turnToAvailableSquare(Chassis chassis)
			throws GameActionException {
		Direction buildDir = rc.getDirection();
		for (int i = 1; i < 8; ++i) {
			if (controllers.sensor.senseObjectAtLocation(rc.getLocation().add(buildDir), chassis.level) == null
					&& rc.senseTerrainTile(rc.getLocation().add(buildDir)) == TerrainTile.LAND
					&& controllers.sensor.senseObjectAtLocation(rc.getLocation().add(buildDir), RobotLevel.MINE) == null) {
				if (rc.getDirection() != buildDir){
					while(controllers.motor.isActive())
						rc.yield();
					controllers.motor.setDirection(buildDir);
				}
					break;
			}
			buildDir = buildDir.rotateLeft();
		}
		return rc.getLocation().add(buildDir);
	}
	
}
