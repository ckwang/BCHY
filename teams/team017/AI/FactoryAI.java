package team017.AI;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import team017.construction.UnitType;
import team017.message.BuildingRequestMessage;
import team017.message.ConstructUnitMessage;
import team017.message.ConstructionCompleteMessage;
import team017.message.GridMapMessage;
import team017.message.MineInquiryMessage;
import team017.message.MineLocationsMessage;
import team017.message.MineResponseMessage;
import team017.message.PatrolDirectionMessage;
import team017.message.ScoutingInquiryMessage;
import team017.message.ScoutingResponseMessage;
import team017.message.UnitReadyMessage;
import team017.util.Util;
import battlecode.common.Clock;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class FactoryAI extends BuildingAI {

	private Set<MapLocation> emptyMineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> alliedMineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> enemyMineLocations = new HashSet<MapLocation>();
	
	private Set<MapLocation> tempEmpty = new HashSet<MapLocation>();
	private Set<MapLocation> tempAllied = new HashSet<MapLocation>();
	private Set<MapLocation> tempEnemy = new HashSet<MapLocation>();
	
	private Deque<UnitType> constructingQueue = new ArrayDeque<UnitType>(50);

	private Direction enemyBase; //direction to enemy base
	private Direction[] toExplore = new Direction[3];
	private int toExploreIndex;
	
	private Direction birthDir;
	private Direction previousWatchingDir;
	
	public FactoryAI(RobotController rc) {
		super(rc);
		toExploreIndex = Clock.getRoundNum() < 400 ? 0 : 1;
	}
	
	public void yield() {
		super.yield();
		controllers.senseMine();
		controllers.senseRobot();
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
			
			while (controllers.myRC.getTeamResources() < ComponentType.TELESCOPE.cost * 1.2)
				yield();
			controllers.builder.build(ComponentType.TELESCOPE, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		birthDir = controllers.myRC.getDirection();
		
		// watch 8 directions
		for (int i = 0; i < 8; i++) {
			watch();
			yield();
			senseBorder();
		}
		msgHandler.queueMessage(new GridMapMessage(borders, homeLocation, gridMap));

		//calculate exploring directions
		if (enemyBaseLoc[0] != null){
			enemyBase = homeLocation.directionTo(enemyBaseLoc[0]);
			toExplore[0] = enemyBase;
			if (enemyBase.isDiagonal()) {
				toExplore[1] = enemyBase.rotateLeft();
				toExplore[2] = enemyBase.rotateRight();
			} else {
				toExplore[1] = enemyBase.rotateLeft().rotateLeft();
				toExplore[2] = enemyBase.rotateRight().rotateRight();
			}

		}
		
		// Main Loop
		while (true) {
			try {
							
				processMessages();
				constructing();
				
				if ( controllers.myRC.getDirection() == previousWatchingDir )
					watch();
				else if ( !controllers.motor.isActive() )
					controllers.motor.setDirection(previousWatchingDir);
				
				if (controllers.myRC.getDirection() == birthDir){			
					updateMineSets();
				}
				controllers.myRC.setIndicatorString(0, "EmptyMines: " + emptyMineLocations.size() + 
													", AlliedMines: " + alliedMineLocations.size() +  
													", EnemyMines: " + enemyMineLocations.size());
				controllers.myRC.setIndicatorString(1, enemyBase + "");


				yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}

	private void constructing() {
		if (constructingQueue.size() != 0) {
			try {
				UnitType type = constructingQueue.getFirst();
				MapLocation buildLoc = buildingLocs.constructableLocation(Util.FACTORY_CODE, type.requiredBuilders);
				if (buildLoc != null) {
					// face the location
					Direction dir = controllers.myRC.getLocation().directionTo(buildLoc);
					Direction myDir = controllers.myRC.getDirection();
					if (myDir != dir) {
						while (controllers.motor.isActive())
							yield();
						controllers.motor.setDirection(dir);
					}
					if (buildingSystem.constructUnit(buildLoc,type, buildingLocs))
							constructingQueue.pop();	
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	private void watch() {
		try {
			tempEmpty.addAll(controllers.emptyMines);
			tempAllied.addAll(controllers.allyMines);
			tempEnemy.addAll(controllers.enemyMines);
			
			
			
			if (controllers.enemyMobile.size() > 0) {
				List<UnitType> types = new ArrayList<UnitType>();
				for (RobotInfo info: controllers.enemyMobile) {
					switch(info.chassis) {
					case HEAVY:
					case MEDIUM:
						types.add(UnitType.APOCALYPSE);
						break;
					case LIGHT:
						types.add(UnitType.RHINO_TANK);
						break;
					case FLYING:
						types.add(UnitType.GRIZZLY);
						break;
					}
				}

				msgHandler.queueMessage(new ConstructUnitMessage(buildingLocs.recyclerLocation, types, true));

			}
			
			if ( !controllers.motor.isActive() ) {
				previousWatchingDir = controllers.myRC.getDirection().rotateRight();
				controllers.motor.setDirection(previousWatchingDir);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void updateMineSets(){
		Set<MapLocation> temp;
		
		temp = emptyMineLocations;
		emptyMineLocations = tempEmpty;
		temp.clear();
		tempEmpty = temp;
		
		temp = alliedMineLocations;
		alliedMineLocations = tempAllied;
		temp.clear();
		tempAllied = temp;
		
		temp = enemyMineLocations;
		enemyMineLocations = tempEnemy;
		temp.clear();
		tempEnemy = temp;
		
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
//				controllers.myRC.setIndicatorString (2, "ConstructMessageGot" + Clock.getRoundNum());
				ConstructUnitMessage handler = new ConstructUnitMessage(msg);
				if (controllers.myRC.getLocation().equals(handler.getBuilderLocation())) {
					if (handler.isList()) {
						if (handler.isUrgent()) {
							for (int i = handler.getTypes().size() - 1; i >= 0; i--)
								constructingQueue.addFirst(handler.getTypes().get(i));
						} else {
							constructingQueue.addAll(handler.getTypes());
						}	
					} else {
						if (handler.isUrgent())
							constructingQueue.addFirst(handler.getType());
						else
							constructingQueue.addLast(handler.getType());
					}
					
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
				
				boolean homeChanged = !homeLocation.equals(handler.getHomeLocation());
				homeLocation = handler.getHomeLocation();
				computeEnemyBaseLocation();
				gridMap.merge(homeLocation, handler.getBorders(), handler.getInternalRecords());
				gridMap.updateScoutLocation(homeLocation);
				
				if (homeChanged) {
					// calculate exploring directions
					if (enemyBaseLoc[0] != null){
						enemyBase = homeLocation.directionTo(enemyBaseLoc[0]);
						toExplore[0] = enemyBase;
						if (enemyBase.isDiagonal()) {
							toExplore[1] = enemyBase.rotateLeft();
							toExplore[2] = enemyBase.rotateRight();
						} else {
							toExplore[1] = enemyBase.rotateLeft().rotateLeft();
							toExplore[2] = enemyBase.rotateRight().rotateRight();
						}
	
					}
				}
				
//				gridMap.printGridMap();
//				controllers.myRC.setIndicatorString(1, homeLocation + "," + gridMap.getScoutLocation() + gridMap.getOriginGrid() + gridMap.getScoutGrid());
//				controllers.myRC.setIndicatorString(2, gridMap.gridBorders[0] + "," + gridMap.gridBorders[1] + "," + gridMap.gridBorders[2] + "," + gridMap.gridBorders[3]);
				
				break;
			}
			
			case MINE_INQUIRY_MESSAGE: {
				MineInquiryMessage handler = new MineInquiryMessage(msg);
				
				msgHandler.queueMessage(new MineResponseMessage(handler.getSourceID(), emptyMineLocations, null));
				break;
			}
			
			case SCOUTING_INQUIRY_MESSAGE: {
				ScoutingInquiryMessage handler = new ScoutingInquiryMessage(msg);
				boolean isConstructor = handler.isConstructor();
				
				Direction scoutingDir = toExplore[toExploreIndex];
				
				msgHandler.queueMessage(new GridMapMessage(borders, homeLocation, gridMap));
				yield();

				msgHandler.queueMessage(new ScoutingResponseMessage(handler.getSourceID(), scoutingDir, toExploreIndex == 0, toExploreIndex == 2 ));
				
				if (isConstructor)
					toExploreIndex = (toExploreIndex+1)%3;
				
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
			
			case UNIT_READY: {
				UnitReadyMessage handler = new UnitReadyMessage(msg);
				
				msgHandler.queueMessage(new GridMapMessage(borders, homeLocation, gridMap));
				
				if (handler.getUnitType() == UnitType.CHRONO_APOCALYPSE) {
					msgHandler.queueMessage(new PatrolDirectionMessage(toExplore[toExploreIndex], toExploreIndex == 2));
					toExploreIndex = (toExploreIndex+1)%3;
				}
				
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
