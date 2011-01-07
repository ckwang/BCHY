package team017.construction;

import java.util.ArrayList;
import java.util.List;

import team017.message.BorderMessage;
import team017.message.BuildingRequestMessage;
import team017.message.MessageHandler;
import team017.util.Controllers;
import battlecode.common.BroadcastController;
import battlecode.common.BuilderController;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.MovementController;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;
import battlecode.common.TerrainTile;

public class Builder {
	
	private RobotController myRC;
	private BuilderController builder;
	private MovementController motor;
	private SensorController sensor;
	private BroadcastController comm;
	
	public Builder(Controllers controllers) {
		myRC = controllers.myRC;
		builder = controllers.builder;
		motor = controllers.motor;
		sensor = controllers.sensor;
		comm = controllers.comm;
	}

	public boolean constructUnit(MapLocation buildLoc, UnitType type, BuilderDirections builderDirs){
		try{
			ComponentType[] builderTypeList = {ComponentType.RECYCLER,ComponentType.ARMORY,ComponentType.FACTORY,ComponentType.CONSTRUCTOR}; 
			List<ComponentType> otherBuilders = new ArrayList<ComponentType>();
			
			for(ComponentType com: builderTypeList){
				if(!com.equals(builder.type())){
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
			
			if (myRC.getTeamResources() > type.chassis.cost * 1.1) {
				if (canConstruct(type.chassis.level)) {
					builder.build(type.chassis, buildLoc);
					myRC.yield();
					for(ComponentType otherBuilder: otherBuilders){
						MessageHandler msgHandler = new BuildingRequestMessage(myRC, comm, buildLoc,type);
						msgHandler.send();

					}
					for (ComponentType com : type.getComponentList(builder.type())) {
						while(myRC.getTeamResources() < com.cost * 1.1)
							myRC.yield();
						while(builder.isActive())
							myRC.yield();
						builder.build(com, buildLoc, type.chassis.level);
					}
					myRC.turnOn(buildLoc, type.chassis.level);
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
			
			if (myRC.getTeamResources() > type.chassis.cost * 1.1) {
				if (canConstruct(type.chassis.level)) {
					builder.build(type.chassis, buildLoc);
					myRC.yield();
					for (ComponentType com : type.getComponentList(builder.type())) {
						while(myRC.getTeamResources() < com.cost * 1.1)
							myRC.yield();
						while(builder.isActive())
							myRC.yield();
						builder.build(com, buildLoc, type.chassis.level);
					}
					myRC.turnOn(buildLoc, type.chassis.level);
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
			for (ComponentType com : type.getComponentList(builder.type())) {
				while(myRC.getTeamResources() < com.cost * 1.1)
					myRC.yield();
				while(builder.isActive())
					myRC.yield();
				builder.build(com, buildLoc, type.chassis.level);
			}
			return true;
		}catch (Exception e){
			return false;
		}
	}
	
	private boolean canConstruct(RobotLevel level) throws GameActionException {
		
		if (sensor.senseObjectAtLocation(
				myRC.getLocation().add(myRC.getDirection()), level) == null
				&& myRC.senseTerrainTile(myRC.getLocation().add(
						myRC.getDirection())) == TerrainTile.LAND)
			return true;
		return false;
	}

	private MapLocation turnToAvailableSquare(Chassis chassis)
			throws GameActionException {
		Direction buildDir = myRC.getDirection();
		for (int i = 1; i < 8; ++i) {
			if (sensor.senseObjectAtLocation(myRC.getLocation().add(buildDir),
					chassis.level) == null
					&& myRC.senseTerrainTile(myRC.getLocation().add(buildDir)) == TerrainTile.LAND) {
				if (myRC.getDirection() != buildDir){
					while(motor.isActive())
						myRC.yield();
					motor.setDirection(buildDir);
				}
					break;
			}
			buildDir = buildDir.rotateLeft();
		}
		return myRC.getLocation().add(buildDir);
	}
	
}
