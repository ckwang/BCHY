package team017.construction;

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

	
	public Builder(RobotController rc, BuilderController builder, MovementController motor, SensorController sensor) {
		myRC = rc;
		this.builder = builder;
		this.motor = motor;
		this.sensor = sensor;
	}

	public boolean constructUnit(MapLocation buildLoc, UnitType type){
		try{
			if (myRC.getTeamResources() > type.chassis.cost * 1.1) {
				if (canConstruct(type.chassis.level)) {
					builder.build(type.chassis, buildLoc);
					myRC.yield();
					for (ComponentType com : type.coms) {
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

	public boolean randomConstructUnit(UnitType type){
		try{
			MapLocation buildLoc = turnToAvailableSquare(type.chassis);
			return constructUnit(buildLoc, type);
		} catch (Exception e){
			return false;
		}
	}

	public boolean constructComponent(MapLocation buildLoc, Chassis chassis, ComponentType[] coms){
		try{
			for (ComponentType com : coms) {
				while(myRC.getTeamResources() < com.cost * 1.1)
					myRC.yield();
				while(builder.isActive())
					myRC.yield();
				builder.build(com, buildLoc, chassis.level);
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
	
	public double calculateTotalCost(UnitType type) {
		double totalCost = type.chassis.cost;
		for (ComponentType com : type.coms) {
			totalCost += com.cost;
		}
		return totalCost;
	}
}
