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
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;

public class Builder {
	
	private Controllers controllers;
	private MessageHandler msgHandler;
	
	public Builder(Controllers controllers, MessageHandler msgHandler) {
		this.controllers = controllers;
		this.msgHandler = msgHandler;
	}

	public boolean constructUnit(MapLocation buildLoc, UnitType type, BuilderDirections builderDirs){
		try {
			
			// check if buildLoc is adjacent to current location
			if (!buildLoc.isAdjacentTo(controllers.myRC.getLocation()))	return false;
			Direction buildDir = controllers.myRC.getLocation().directionTo(buildLoc);
			
			// if there's enough resource
			if (controllers.myRC.getTeamResources() > type.chassis.cost * 1.1) {
				
				
				// see if there are all the required builders 
				builderDirs.updateBuilderDirs();
				if (!builderDirs.isComplete(controllers.builder.type(), type.requiredBuilders))	{
					return false;
				}
				
				// check if the unit can be built at the desired location
				if (canConstruct(buildDir, type.chassis.level)) {
					controllers.builder.build(type.chassis, buildLoc);
					controllers.myRC.yield();
					
					// send messages to other builders
					for(ComponentType builder: type.requiredBuilders){
						if (builder != controllers.builder.type())
							msgHandler.queueMessage(new BuildingRequestMessage(controllers.myRC.getLocation().add(builderDirs.getDirections(builder)), buildLoc, type));
					}
					
					// build my responsible parts
					for (ComponentType com : type.getComponentList(controllers.builder.type())) {
						while(controllers.myRC.getTeamResources() < com.cost * 1.1 || controllers.builder.isActive()) {
							msgHandler.process();
							controllers.myRC.yield();
						}
						controllers.builder.build(com, buildLoc, type.chassis.level);
					}
					
					// turn on if the unit has all the parts
					if (Util.containsComponent(controllers, buildLoc, type.chassis.level, type))
						controllers.myRC.turnOn(buildLoc, type.chassis.level);
					
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
			if(type.selfBuild == false)
				return false;
			
			if (controllers.myRC.getTeamResources() > type.chassis.cost * 1.1) {
				if (canConstruct(type.chassis.level)) {
					while (controllers.builder.isActive())
						controllers.myRC.yield();
					controllers.builder.build(type.chassis, buildLoc);
					controllers.myRC.yield();
					for (ComponentType com : type.getComponentList(controllers.builder.type())) {
						while(controllers.builder.isActive() || controllers.myRC.getTeamResources() < com.cost * 1.1)
							controllers.myRC.yield();
						controllers.builder.build(com, buildLoc, type.chassis.level);
					}
					controllers.myRC.turnOn(buildLoc, type.chassis.level);
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
				while(controllers.myRC.getTeamResources() < com.cost * 1.1)
					controllers.myRC.yield();
				while(controllers.builder.isActive())
					controllers.myRC.yield();
				controllers.builder.build(com, buildLoc, type.chassis.level);
			}
			
			// turn on if the unit has all the parts
			if (Util.containsComponent(controllers, buildLoc, type.chassis.level, type))
				controllers.myRC.turnOn(buildLoc, type.chassis.level);
			
			return true;
		}catch (Exception e){
			return false;
		}
	}
	
	public boolean canConstruct(Direction dir, RobotLevel level) throws GameActionException {
		if (controllers.sensor.senseObjectAtLocation(controllers.myRC.getLocation().add(dir), level) == null
				&& controllers.myRC.senseTerrainTile(controllers.myRC.getLocation().add(dir)) == TerrainTile.LAND)
			return true;
		return false;
	}
	
	public boolean canConstruct(RobotLevel level) throws GameActionException {
		return canConstruct(controllers.myRC.getDirection(), level);
	}

	private MapLocation turnToAvailableSquare(Chassis chassis)
			throws GameActionException {
		Direction buildDir = controllers.myRC.getDirection();
		for (int i = 1; i < 8; ++i) {
			if (controllers.sensor.senseObjectAtLocation(controllers.myRC.getLocation().add(buildDir), chassis.level) == null
					&& controllers.myRC.senseTerrainTile(controllers.myRC.getLocation().add(buildDir)) == TerrainTile.LAND
					&& controllers.sensor.senseObjectAtLocation(controllers.myRC.getLocation().add(buildDir), RobotLevel.MINE) == null) {
				if (controllers.myRC.getDirection() != buildDir){
					while(controllers.motor.isActive())
						controllers.myRC.yield();
					controllers.motor.setDirection(buildDir);
				}
					break;
			}
			buildDir = buildDir.rotateLeft();
		}
		return controllers.myRC.getLocation().add(buildDir);
	}
	
}
