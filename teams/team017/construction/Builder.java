package team017.construction;

import java.util.ArrayList;
import java.util.List;

import team017.message.BuildingRequestMessage;
import team017.message.MessageHandler;
import team017.util.Controllers;
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
		try{
			ComponentType[] builderTypeList = {ComponentType.RECYCLER,ComponentType.ARMORY,ComponentType.FACTORY,ComponentType.CONSTRUCTOR}; 
			List<ComponentType> otherBuilders = new ArrayList<ComponentType>();
			
			for(ComponentType com: builderTypeList){
				if(!com.equals(controllers.builder.type())){
					if(type.getComponentList(com).length != 0){
						if(builderDirs.getDirections(com) == null){
							return false;
						}
						else{
							otherBuilders.add(com);
						}
					}
				}
			}
			
			if (controllers.myRC.getTeamResources() > type.chassis.cost * 1.1) {
				if (canConstruct(type.chassis.level)) {
					controllers.builder.build(type.chassis, buildLoc);
					controllers.myRC.yield();
					for(ComponentType otherBuilder: otherBuilders){
						msgHandler.queueMessage(new BuildingRequestMessage(controllers.myRC.getLocation().add(builderDirs.getDirections(otherBuilder)) ,buildLoc,type));
					}
					for (ComponentType com : type.getComponentList(controllers.builder.type())) {
						while(controllers.myRC.getTeamResources() < com.cost * 1.1)
							controllers.myRC.yield();
						while(controllers.builder.isActive())
							controllers.myRC.yield();
						controllers.builder.build(com, buildLoc, type.chassis.level);
					}
					controllers.myRC.turnOn(buildLoc, type.chassis.level);
					return true;
				}
			}
			return false;
		}catch (Exception e){
			return false;
		}
	}

	public boolean constructUnit(MapLocation buildLoc, UnitType type){
		try{
			if(type.selfBuild == false)
				return false;
			
			if (controllers.myRC.getTeamResources() > type.chassis.cost * 1.1) {
				if (canConstruct(type.chassis.level)) {
					controllers.builder.build(type.chassis, buildLoc);
					controllers.myRC.yield();
					for (ComponentType com : type.getComponentList(controllers.builder.type())) {
						while(controllers.myRC.getTeamResources() < com.cost * 1.1)
							controllers.myRC.yield();
						while(controllers.builder.isActive())
							controllers.myRC.yield();
						controllers.builder.build(com, buildLoc, type.chassis.level);
					}
					controllers.myRC.turnOn(buildLoc, type.chassis.level);
					return true;
				}
			}
			return false;
		}catch (Exception e){
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
			controllers.myRC.turnOn(buildLoc, type.chassis.level);
			return true;
		}catch (Exception e){
			return false;
		}
	}
	
	private boolean canConstruct(RobotLevel level) throws GameActionException {
		
		if (controllers.sensor.senseObjectAtLocation(
				controllers.myRC.getLocation().add(controllers.myRC.getDirection()), level) == null
				&& controllers.myRC.senseTerrainTile(controllers.myRC.getLocation().add(
						controllers.myRC.getDirection())) == TerrainTile.LAND)
			return true;
		return false;
	}

	private MapLocation turnToAvailableSquare(Chassis chassis)
			throws GameActionException {
		Direction buildDir = controllers.myRC.getDirection();
		for (int i = 1; i < 8; ++i) {
			if (controllers.sensor.senseObjectAtLocation(controllers.myRC.getLocation().add(buildDir),
					chassis.level) == null
					&& controllers.myRC.senseTerrainTile(controllers.myRC.getLocation().add(buildDir)) == TerrainTile.LAND) {
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
