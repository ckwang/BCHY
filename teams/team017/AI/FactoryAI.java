package team017.AI;

import java.util.HashSet;
import java.util.Set;

import team017.construction.UnitType;
import team017.message.BuildingRequestMessage;
import team017.message.ConstructUnitMessage;
import team017.message.ConstructionCompleteMessage;
import team017.message.GridMapMessage;
import team017.message.MineInquiryMessage;
import team017.message.MineLocationsMessage;
import team017.message.MineResponseMessage;
import team017.message.ScoutingInquiryMessage;
import team017.message.ScoutingResponseMessage;
import team017.util.Util;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;

public class FactoryAI extends BuildingAI {

	private Set<MapLocation> emptyMineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> alliedMineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> enemyMineLocations = new HashSet<MapLocation>();
	
	private Direction enemyBase; //direction to enemy base
	private Direction[] toExplore = new Direction[3];
	private int toExploreIndex = 0;
	private boolean diagonallyBranching;
	private int order = 0;
	
	public FactoryAI(RobotController rc) {
		super(rc);
	}
	
	public void yield() {
		super.yield();
		controllers.senseMine();
	}

	@Override
	public void proceed() {
		while (controllers.motor.isActive() || controllers.comm == null)
			yield();
		msgHandler.queueMessage(new ConstructionCompleteMessage(controllers.myRC.getLocation(), UnitType.FACTORY));

		// build a telescope on itself
		while (controllers.builder.isActive())
			yield();
		try {
			controllers.builder.build(ComponentType.TELESCOPE, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < 8; i++) {
			watch();
			yield();
			senseBorder();
		}

		enemyBase = controllers.myRC.getLocation().directionTo(enemyBaseLoc[0]);
		toExplore[0] = enemyBase;
		if (enemyBase.isDiagonal()) {
			diagonallyBranching = true;
			toExplore[1] = enemyBase.rotateLeft();
			toExplore[2] = enemyBase.rotateRight();
		} else {
			diagonallyBranching = false;
			toExplore[1] = enemyBase.rotateLeft().rotateLeft();
			toExplore[2] = enemyBase.rotateRight().rotateRight();
		}
		
		while (true) {
			try {

				processMessages();
				yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}

	}

	private void watch() {
		try {
			emptyMineLocations.addAll(controllers.emptyMines);
			alliedMineLocations.addAll(controllers.allyMines);
			enemyMineLocations.addAll(controllers.enemyMines);
			
			controllers.motor.setDirection(controllers.myRC.getDirection().rotateRight());
			controllers.myRC.setIndicatorString(0, emptyMineLocations.size() + "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void processMessages() throws GameActionException {
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			outer:
			switch (msgHandler.getMessageType(msg)) {
			case BUILDING_REQUEST:{
				BuildingRequestMessage handler = new BuildingRequestMessage(msg);
				if (handler.getBuilderLocation().equals(controllers.myRC.getLocation())) {
					Direction buildDir = controllers.myRC.getLocation().directionTo(handler.getBuildingLocation());
					if (controllers.myRC.getDirection() != buildDir) {
						controllers.motor.setDirection(buildDir);
						yield();
					}
					
					while(!buildingSystem.constructComponent(handler.getBuildingLocation(),handler.getUnitType())){
						if(controllers.sensor.senseObjectAtLocation(handler.getBuilderLocation(),handler.getUnitType().chassis.level).getTeam() != controllers.myRC.getTeam())
							break;
						yield();
					}
					yield();
				}
				break;
			}
			
			case CONSTRUCTION_COMPLETE: {
				ConstructionCompleteMessage handler = new ConstructionCompleteMessage(msg);
				MapLocation currentLoc = controllers.myRC.getLocation();
				MapLocation buildingLocation = handler.getBuildingLocation();
//				Direction builderDir = currentLoc.directionTo(buildingLocation);
				
				if (handler.getBuildingLocation().distanceSquaredTo(currentLoc) < 5) {

					buildingLocs.setLocations(handler.getBuildingType(), handler.getBuildingLocation());
					if (handler.getBuildingType() == UnitType.RAILGUN_TOWER) {
						buildingLocs.setLocations(handler.getBuildingType(), buildingLocation);
						while(!buildingSystem.constructComponent(buildingLocation, UnitType.RAILGUN_TOWER)) {
							GameObject obj = controllers.sensor.senseObjectAtLocation(buildingLocation,RobotLevel.ON_GROUND);
							if (obj == null || obj.getTeam() != controllers.myRC.getTeam()) {
								buildingLocs.setLocations(handler.getBuildingType(), null);
								break outer;
							}
							yield();
						}
						break;
					}
				}
				break;
			}
			
			case CONSTRUCT_UNIT_MESSAGE: {
				ConstructUnitMessage handler = new ConstructUnitMessage(msg);
				if (controllers.myRC.getLocation() == handler.getBuilderLocation()) {
					UnitType type = handler.getType();
					MapLocation buildLoc = buildingLocs.constructableLocation(Util.FACTORY_CODE, type.requiredBuilders);
					if (buildLoc != null)
						buildingSystem.constructUnit(buildLoc,type, buildingLocs);
				}
				break;
			}
			
			case GRID_MAP_MESSAGE: {
				GridMapMessage handler = new GridMapMessage(msg);
				// update the borders
				int[] newBorders = handler.getBorders();

				for (int i = 0; i < 4; ++i) {
					if (newBorders[i] != -1){
						if (borders[i] != newBorders[i]){
							borders[i] = newBorders[i];
						}
					}
				}
				
				homeLocation = handler.getHomeLocation();
				computeEnemyBaseLocation();
				gridMap.merge(homeLocation, handler.getBorders(), handler.getInternalRecords());
				gridMap.updateScoutLocation(homeLocation);
//				gridMap.printGridMap();
//				controllers.myRC.setIndicatorString(1, homeLocation + "," + gridMap.getScoutLocation() + gridMap.getOriginGrid() + gridMap.getScoutGrid());
//				controllers.myRC.setIndicatorString(2, gridMap.gridBorders[0] + "," + gridMap.gridBorders[1] + "," + gridMap.gridBorders[2] + "," + gridMap.gridBorders[3]);
				
				break;
			}
			
			case MINE_INQUIRY_MESSAGE: {
				MineInquiryMessage handler = new MineInquiryMessage(msg);
				
				msgHandler.queueMessage(new MineResponseMessage(handler.getSourceID(), emptyMineLocations));
				break;
			}
			
			case SCOUTING_INQUIRY_MESSAGE: {
				ScoutingInquiryMessage handler = new ScoutingInquiryMessage(msg);
				Direction scoutingDir = toExplore[toExploreIndex];
				
				boolean branching = scoutingDir.isDiagonal()? diagonallyBranching: !diagonallyBranching;
				
				toExploreIndex = (toExploreIndex+1)%3;
				order = 1 - order;
				msgHandler.queueMessage(new ScoutingResponseMessage(handler.getSourceID(), scoutingDir, branching, order ));
				gridMap.setScouted(gridMap.getScoutLocation());
				break;
			}
			
			case MINE_LOCATIONS_MESSAGE: {
				MineLocationsMessage handler = new MineLocationsMessage(msg);
				
				emptyMineLocations.addAll(handler.getEmptyMineLocations());
				alliedMineLocations.addAll(handler.getAlliedMineLocations());
				enemyMineLocations.addAll(handler.getEnemyMineLocations());

//				controllers.myRC.setIndicatorString(1, mineLocations.toString());
				
				break;
			}
			
//			case TURN_OFF_MESSAGE: {
//				TurnOffMessage handler = new TurnOffMessage(msg);
//				if (controllers.myRC.getLocation() == handler.getBuildingLoc())
//					controllers.myRC.turnOff();
//				break;
//			}
			
		}
	}
	}
}
