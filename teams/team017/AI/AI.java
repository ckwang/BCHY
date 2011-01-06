package team017.AI;

import java.util.ArrayList;
import java.util.List;

import team017.construction.UnitType;
import team017.navigation.Navigator;
import battlecode.common.BroadcastController;
import battlecode.common.BuilderController;
import battlecode.common.Chassis;
import battlecode.common.ComponentController;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.MovementController;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;
import battlecode.common.TerrainTile;
import battlecode.common.WeaponController;

public abstract class AI {

	protected RobotController myRC;
	protected MovementController motor;
	protected BuilderController builder;
	protected SensorController sensor;
	protected Navigator navigator;
	protected BroadcastController comm;
	protected List<WeaponController> weapons;

	// {NORTH, EAST, SOUTH, WEST}
	protected int[] borders = { -1, -1, -1, -1 };
	protected MapLocation homeLocation;
	protected double fluxRate;
	protected double[] fluxRecord = new double[10];

	public AI(RobotController rc) {
		myRC = rc;
		navigator = new Navigator(myRC);
		weapons = new ArrayList<WeaponController>();
		homeLocation = rc.getLocation();
		updateComponents();
	}

	abstract public void proceed();
	
	abstract public void yield() throws GameActionException;

	protected void updateComponents() {
		ComponentController[] components = myRC.newComponents();

		for (ComponentController com : components) {
			switch (com.componentClass()) {
			case MOTOR:
				motor = (MovementController) com;
				navigator.setMotor(motor);
				break;
			case BUILDER:
				builder = (BuilderController) com;
				break;
			case SENSOR:
				sensor = (SensorController) com;
				navigator.setSensor(sensor);
				break;
			case COMM:
				comm = (BroadcastController) com;
				break;
			case WEAPON:
				weapons.add((WeaponController) com);
				break;
			}
		}
	}

	protected void updateFluxRate() {
		for (int i = 9; i > 0; --i) {
			fluxRecord[i] = fluxRecord[i - 1];
		}
		fluxRecord[0] = myRC.getTeamResources();
		fluxRate = fluxRecord[0] - fluxRecord[1];
	}

	protected double calculateTotalCost(Chassis chassis,
			ComponentType[] components) {
		double totalCost = chassis.cost;
		for (ComponentType com : components) {
			totalCost += com.cost;
		}
		return totalCost;
	}

	protected boolean constructUnit(MapLocation buildLoc, UnitType type){
		try{
			updateFluxRate();
			if (myRC.getTeamResources() > type.chassis.cost * 1.1
					&& fluxRate > type.chassis.upkeep) {
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
	
	protected boolean constructComponent(MapLocation buildLoc, Chassis chassis, ComponentType[] coms){
		try{
			updateFluxRate();
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
	
	

	protected boolean canConstruct(RobotLevel level) throws GameActionException {
		if (sensor.senseObjectAtLocation(
				myRC.getLocation().add(myRC.getDirection()), level) == null
				&& myRC.senseTerrainTile(myRC.getLocation().add(
						myRC.getDirection())) == TerrainTile.LAND)
			return true;
		return false;
	}

	protected MapLocation turnToAvailableSquare(Chassis chassis)
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
