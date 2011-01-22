package team017.AI;

import java.util.ArrayDeque;
import java.util.Deque;

import team017.construction.UnitType;
import team017.message.BuildingLocationResponseMessage;
import team017.message.BuildingRequestMessage;
import team017.message.ConstructBaseMessage;
import team017.message.ConstructUnitMessage;
import team017.message.ConstructionCompleteMessage;
import team017.message.GridMapMessage;
import team017.message.NotEnoughSpaceMessage;
import team017.message.TurnOffMessage;
import team017.message.UnitReadyMessage;
import team017.util.Util;
import battlecode.common.*;

public class RecyclerAI extends BuildingAI {

	private Mine myMine;
	private int unitConstructed = 0;
	private int birthRoundNum;
	private int inquiryIdleRound = 0;
	private int constructIdleRound = 0;
	private MapLocation currentLoc = controllers.myRC.getLocation();
	private boolean built = false;
	private boolean clusterIsDone = false;
	private BuilderController recycler;
	private BuilderController constructor;
	
	private boolean buildArmory = false;
	private boolean buildFactory = false;
	private boolean buildTower = false;
	private boolean buildRailgunTower = false;
	
	int [] unitRatios = {1, 0, 0, 0, 1};
	int [] cumulatedRatios = new int[5];
	int total;
	
	private UnitType [] types = { UnitType.CONSTRUCTOR, UnitType.FLYING_CONSTRUCTOR, UnitType.TELESCOPER, UnitType.APOCALYPSE, UnitType.CHRONO_APOCALYPSE};
	double fluxThresholds = 0.3;
//	double resourceThresholds = UnitType.TOWER.totalCost + UnitType.RECYCLER.totalCost;
	double resourceThresholds = 100;
	
	private Deque<UnitType> constructingQueue;
	private UnitType unitUnderConstruction;
	
	private enum spawningState { COLLECTING, ATTACKING, BALANCE,LATE };
	
	spawningState mySpawningState = spawningState.COLLECTING;
	
	public RecyclerAI(RobotController rc) {
		super(rc);		
		birthRoundNum = Clock.getRoundNum();
		
		constructingQueue = new ArrayDeque<UnitType>(50);

		updateRatios();
		try {
			myMine = (Mine) controllers.sensor.senseObjectAtLocation(controllers.myRC.getLocation(), RobotLevel.MINE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void yield() {
		super.yield();
		if (constructIdleRound != 0)
			constructIdleRound = 0;
		if (inquiryIdleRound != 0)
			inquiryIdleRound--;

	}
	

	@Override
	public void proceed() {
		recycler = controllers.builder;
		if (Clock.getRoundNum() <= 5)
			init();
		else{
			
			if (unitConstructed > 3 && unitRatios[0]!= 0){
				unitRatios[0] = 0;
				updateRatios();
			}
			
			// turn off if there is already a recycler nearby
			if (buildingLocs.recyclerLocation != null) {

				encodeEmptyNumInDirection();
				controllers.myRC.turnOff();
			} else {
				
				while(controllers.myRC.getTeamResources() < 10)
					controllers.myRC.yield();
				try {
					// build an antenna on itself
					while(recycler.isActive())
						controllers.myRC.yield();
					recycler.build(ComponentType.ANTENNA, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
					yield();
				} catch (Exception e1) {
					System.out.println("caught exception:");
					e1.printStackTrace();
				}
			}
		}
		
		
		if (birthRoundNum < 200 || myMine == null) {
			buildFactory = true;
			buildArmory = true;
			buildRailgunTower = true;
			constructingQueue.add(UnitType.TELESCOPER);
			constructingQueue.add(UnitType.FLYING_CONSTRUCTOR);
			constructingQueue.add(UnitType.TELESCOPER);
			constructingQueue.add(UnitType.FLYING_CONSTRUCTOR);
			constructingQueue.add(UnitType.TELESCOPER);
			constructingQueue.add(UnitType.FLYING_CONSTRUCTOR);
		}
		
		while (true) {
			try {
				if (!clusterIsDone) {
					clusterIsDone = true;
					checkAdjacentRecyclers();
					Mine[] mines = controllers.sensor.senseNearbyGameObjects(Mine.class);
					for (Mine mine : mines) {
						if (mine.getLocation() != currentLoc) {
							Robot r = (Robot) controllers.sensor.senseObjectAtLocation(mine.getLocation(), RobotLevel.ON_GROUND);
							if (r == null) {
								clusterIsDone = false;
								break;
							} else if (r.getTeam().equals(controllers.self.getTeam())){
								RobotInfo info = controllers.sensor.senseRobotInfo(r);
								if (info.chassis != Chassis.BUILDING) {
									clusterIsDone = false;
									break;
								} else {
									boolean containsRecycler = false;
									for (ComponentType c: info.components) {
										if (c == ComponentType.RECYCLER) {
											containsRecycler = true;
											break;
										}
									}
									if (!containsRecycler) {
										clusterIsDone = false;
										break;
									}
								}
							}
						}
					}	
				} else if (constructor == null) {
					if (!recycler.isActive() && 
							(birthRoundNum < 200 || myMine == null || controllers.myRC.getTeamResources() > 400) &&
							controllers.myRC.getTeamResources() > ComponentType.CONSTRUCTOR.cost * 1.2) {
//						while (recycler.isActive())
//							yield();
						recycler.build(ComponentType.CONSTRUCTOR, currentLoc, RobotLevel.ON_GROUND);
						yield();
						constructor = controllers.builder;
						controllers.builder = recycler;	
					}
				}
				

				if (constructor != null) {
					try {
						constructBase();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				processMessages();
				
				
				double fluxRate = getEffectiveFluxRate();

				
				// Turn off recyclers and factories of cluster size 1				
				if (Clock.getRoundNum() - birthRoundNum > 100 && buildingLocs.clusterSize == 1 && buildingLocs.factoryLocation != null && buildingLocs.railgunTowerLocations.size() > 0) {
					msgHandler.queueMessage(new TurnOffMessage(buildingLocs.factoryLocation));
					while (msgHandler.getOutQueueSize() > 0)
						yield();
					controllers.myRC.turnOff();
				}
					
				
				
				if (controllers.myRC.getTeamResources() > resourceThresholds && fluxRate > fluxThresholds ) {
					constructUnit();
				}
				
				// turn off when the mine is depleted
				if (myMine != null && controllers.sensor.senseMineInfo(myMine).roundsLeft == 0 && buildingLocs.clusterSize == 1)
					controllers.myRC.turnOff();

				yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}

		}

	}

	private void init() {
		try {
			// install an antenna to the adjacent constructor
			RobotInfo info = senseAdjacentChassis(Chassis.LIGHT);
			if (info != null && controllers.myRC.getTeamResources() >= 2 * ComponentType.ANTENNA.cost && !Util.containsComponent(info.components,ComponentType.ANTENNA)) {
				recycler.build(ComponentType.ANTENNA, info.location, RobotLevel.ON_GROUND);
			}
			Direction dir = Direction.NORTH;
			for (int i = 0; i < buildingLocs.emptySize; i++) {
				dir = dir.rotateRight();
			}
			while (controllers.motor.isActive())
				controllers.myRC.yield();
			
			try {
				controllers.motor.setDirection(dir);
				controllers.myRC.yield();
			} catch (Exception e) {
				e.printStackTrace();
			}

			controllers.myRC.turnOff();

		} catch (Exception e) {
			System.out.println("caught exception:");
			e.printStackTrace();
		}
	}
	@Override
	protected void processMessages() throws GameActionException {
		// receive messages and handle them
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			outer:
			switch (msgHandler.getMessageType(msg)) {

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
				
				break;
			}
			
			case CONSTRUCT_BASE_MESSAGE:{
				ConstructBaseMessage handler = new ConstructBaseMessage(msg);
				// Check if is intended for it
				if (handler.getBuilderLoc() == currentLoc) {
					if (constructor == null) {
						while (recycler.isActive())
							yield();
						recycler.build(ComponentType.CONSTRUCTOR, currentLoc, RobotLevel.ON_GROUND);
					} 
					switch (handler.getType()) {
					case FACTORY:
						buildFactory = true;
						break;
					case ARMORY:
						buildArmory = true;
						break;
					case TOWER:
						buildTower = true;
						break;
					case RAILGUN_TOWER:
						buildRailgunTower = true;
						break;
					}
				}
				break;
			}
			
			case BUILDING_REQUEST:{

				BuildingRequestMessage handler = new BuildingRequestMessage(msg);
				if (handler.getBuilderLocation().equals(controllers.myRC.getLocation())) {
					
					while(!buildingSystem.constructComponent(handler.getBuildingLocation(),handler.getUnitType())){
						if(controllers.sensor.senseObjectAtLocation(handler.getBuilderLocation(),handler.getUnitType().chassis.level).getTeam() != controllers.myRC.getTeam())
							break;
						yield();
					}
					
//<<<<<<< HEAD
					if (handler.getUnitType() == unitUnderConstruction){
						unitUnderConstruction = null;
					}
//=======
//				}
//				break;
//			}
//			
//			case BUILDING_LOCATION_INQUIRY_MESSAGE: {
//				BuildingLocationInquiryMessage handler = new BuildingLocationInquiryMessage(msg);
////				controllers.myRC.setIndicatorString(0, "Inquiry Got" + Clock.getRoundNum());
//				
//				
//				// if the constructor is inquiring it 
//				if (handler.getBuilderLocation().equals(controllers.myRC.getLocation())) {
//					
//					
//					int constructorID = handler.getSourceID();
//					MapLocation loc;
//					
//					if (inquiryIdleRound > 0)
//						break;
//
//					//	Build a tower at the initial base
//					if (birthRoundNum < 200 || myMine == null) {
//						
//						for (int i = 4; i > 0; i--) {
////							controllers.myRC.setIndicatorString(0, i + "");
//							loc = buildingLocs.consecutiveEmpties(i);
//							if (loc != null) {
//								switch(i) {
//								case 4:
////									if (buildingLocs.towerLocations.size() == 0) {
////										msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, loc, UnitType.TOWER));
////										inquiryIdleRound = 5;
////									} else 
//									if (buildingLocs.factoryLocation == null && buildingLocs.towerLocations.size() == 0) {
//										msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, buildingLocs.rotateRight(buildingLocs.towerLocations.get(0)), UnitType.FACTORY));
//										inquiryIdleRound = 3;
//									} else if (buildingLocs.factoryLocation != null && buildingLocs.armoryLocation == null) {
//										msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, buildingLocs.rotateRight(buildingLocs.factoryLocation, 2), UnitType.ARMORY));
//										inquiryIdleRound = 5;
//									} else {
//										msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, null, null));
//									}
//									break;
//								case 3:
//									if (buildingLocs.factoryLocation == null) {
//										msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, buildingLocs.rotateRight(buildingLocs.towerLocations.get(0)), UnitType.FACTORY));
//										inquiryIdleRound = 3;
//									} else if (buildingLocs.factoryLocation != null && buildingLocs.armoryLocation == null) {
//										msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, buildingLocs.rotateRight(buildingLocs.factoryLocation, 2), UnitType.ARMORY));
//										inquiryIdleRound = 5;
//									} else {
//										msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, null, null));
//									}
//									break;
//								case 2:
//									// Initially has 2 empties only
//									if (buildingLocs.towerLocations.size() == 0 && buildingLocs.factoryLocation == null) {
//										msgHandler.clearOutQueue();
//										msgHandler.queueMessage(new NotEnoughSpaceMessage());
//										msgHandler.process();
//										controllers.myRC.turnOff();
//										inquiryIdleRound = 5;
//									} else if (buildingLocs.armoryLocation == null) {
//										msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, buildingLocs.rotateRight(buildingLocs.factoryLocation, 2), UnitType.ARMORY));
//										inquiryIdleRound = 5;
//									} else {
//										msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, null, null));
//									}
//									break;
//								}
//							break;
//							}
//						}
//						
//					} 
////					else {
////							for (int i = 4; i > 0; i--) {
////								loc = buildingLocs.consecutiveEmpties(i);
////								if (loc != null) {
////									switch(i) {
////									case 4:
////										if (buildingLocs.factoryLocation == null) {
////											msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, buildingLocs.rotateRight(loc), UnitType.FACTORY));
////											inquiryIdleRound = 5;
////										} else if (buildingLocs.railgunTowerLocations.size() == 0) {
////											msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, buildingLocs.rotateLeft(buildingLocs.factoryLocation), UnitType.RAILGUN_TOWER));
////											inquiryIdleRound = 3;
////										} else if (buildingLocs.railgunTowerLocations.size() != 0 && buildingLocs.armoryLocation == null) {
////											msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, buildingLocs.rotateRight(buildingLocs.factoryLocation, 2), UnitType.ARMORY));
////											inquiryIdleRound = 5;
////										} else {
////											msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, null, null));
////										}
////										break;
////									case 3:
////										if (buildingLocs.factoryLocation == null) {
////											msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, buildingLocs.rotateRight(loc), UnitType.FACTORY));
////											inquiryIdleRound = 3;
////										} else if (buildingLocs.railgunTowerLocations.size() == 0) {
////											msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, buildingLocs.rotateLeft(buildingLocs.factoryLocation), UnitType.RAILGUN_TOWER));
////											inquiryIdleRound = 5;
////										} else if (buildingLocs.armoryLocation == null){
////											msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, buildingLocs.rotateRight(buildingLocs.factoryLocation, 2), UnitType.ARMORY));
////											inquiryIdleRound = 5;
////										} else {
////											msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, null, null));
////										}
////										break;
////									case 2:
////										// Initially has 2 empties only
////										if (buildingLocs.factoryLocation == null) {
////											msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, buildingLocs.rotateRight(loc), UnitType.FACTORY));
////											inquiryIdleRound = 5;
////										} else if (buildingLocs.railgunTowerLocations.size() == 0) {
////											msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, buildingLocs.rotateLeft(buildingLocs.factoryLocation), UnitType.RAILGUN_TOWER));
////											inquiryIdleRound = 5;
////										} else if (buildingLocs.rotateLeft(loc) == buildingLocs.factoryLocation && buildingLocs.armoryLocation == null) {
////											msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, buildingLocs.rotateRight(buildingLocs.factoryLocation, 2), UnitType.ARMORY));
////											inquiryIdleRound = 5;
////										} else {
////											msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, null, null));
////										}
////										break;
////									case 1:
////										if (buildingLocs.factoryLocation == null) {
////											if (buildingLocs.towerLocations.size() == 0) {
////												msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, loc, UnitType.TOWER));
////												inquiryIdleRound = 5;
////											}
////										} else if(buildingLocs.railgunTowerLocations.size() == 0) {
////											msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, buildingLocs.rotateLeft(buildingLocs.factoryLocation), UnitType.RAILGUN_TOWER));
////											inquiryIdleRound = 5;
////										} else {
////											msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, null, null));
////										}
////										break;
////									}
////								break;
////								}
////							}	
////					}
//>>>>>>> branch 'refs/heads/master' of git@github.com:ckwang/BCHY.git
					
				}
				break;
			}
			
			case CONSTRUCTION_COMPLETE: {
				ConstructionCompleteMessage handler = new ConstructionCompleteMessage(msg);
				MapLocation buildingLocation = handler.getBuildingLocation();
				Direction builderDir = currentLoc.directionTo(buildingLocation);

				/*
				 * When a new building is constructed, we would like to build an antenna on it.
				 */
				
				// see if the target is near enough
				if (buildingLocation.distanceSquaredTo(currentLoc) < 5) {
					// UnitType.ARMORY
					if (handler.getBuildingType() == UnitType.ARMORY) {
						buildingLocs.setLocations(handler.getBuildingType(), buildingLocation);
//						Tell armory where the factory is
						if (buildingLocs.factoryLocation != null)
							msgHandler.queueMessage(new ConstructionCompleteMessage(buildingLocs.factoryLocation, UnitType.FACTORY));

//						msgHandler.queueMessage (new BuildingLocationResponseMessage(handler.getSourceID(), buildingLocs.rotateRight(buildingLocs.armoryLocation, 2), UnitType.FACTORY));
						yield();
						// UnitType.FACTORY	
					} else if (handler.getBuildingType() == UnitType.FACTORY) {
						buildingLocs.setLocations(handler.getBuildingType(), buildingLocation);
						if (buildingLocs.armoryLocation != null)
							msgHandler.queueMessage(new ConstructionCompleteMessage(buildingLocs.armoryLocation, UnitType.ARMORY));

						if (birthRoundNum > 200)
							msgHandler.queueMessage (new BuildingLocationResponseMessage(handler.getSourceID(), buildingLocs.rotateLeft(buildingLocs.factoryLocation), UnitType.RAILGUN_TOWER));
						else
							msgHandler.queueMessage (new BuildingLocationResponseMessage(handler.getSourceID(), buildingLocs.rotateRight(buildingLocs.factoryLocation, 2), UnitType.ARMORY));
						yield();
						
					// UnitType.TOWER
					} else if (handler.getBuildingType() == UnitType.TOWER) {
						buildingLocs.setLocations(handler.getBuildingType(), buildingLocation);
						
						msgHandler.queueMessage(new BuildingLocationResponseMessage(handler.getSourceID(), buildingLocs.rotateRight(buildingLocs.towerLocations.get(0)), UnitType.FACTORY));
						
						while(!buildingSystem.constructComponent(buildingLocation, UnitType.TOWER)) {
							GameObject obj = controllers.sensor.senseObjectAtLocation(buildingLocation,RobotLevel.ON_GROUND);
							if (obj == null || obj.getTeam() != controllers.myRC.getTeam()) {
								buildingLocs.setLocations(handler.getBuildingType(), null);
								break outer;
							}
							yield();
						}
						break;
					// UnitType.RAILGUN_TOWER
					} else if (handler.getBuildingType() == UnitType.RAILGUN_TOWER) {
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
					
					buildingLocs.setLocations(handler.getBuildingType(), buildingLocation);
					// update the buildingDirs
					
					
					if(handler.getBuildingType() != UnitType.RECYCLER){
						// face the correct direction
						if (controllers.myRC.getDirection() != builderDir){
							controllers.motor.setDirection(builderDir);
							yield();
						}
						// build an antenna if it doesn't have one
						if (!Util.containsComponent(controllers, buildingLocation, RobotLevel.ON_GROUND, ComponentType.ANTENNA)) {
							recycler.build(ComponentType.ANTENNA, handler.getBuildingLocation(), RobotLevel.ON_GROUND);
						}
					}
				}
				break;
			}
			
			case UNIT_READY: {
				UnitReadyMessage handler = new UnitReadyMessage(msg);
				
				if (controllers.myRC.getLocation().distanceSquaredTo(handler.getSourceLocation()) <= 2) {
					controllers.myRC.setIndicatorString(1, Clock.getRoundNum() + "" + handler.getUnitType());
					if (handler.getUnitType() == unitUnderConstruction) {
						unitUnderConstruction = null;
					}
				}
				
				break;
			}

			}
		}
	}

	/***
	 * Sense nearby robots and return the location of one robot with specific
	 * chassis. Return none if there is no such robot.
	 * 
	 * @throws GameActionException
	 */
	private RobotInfo senseAdjacentChassis(Chassis chassis) throws GameActionException {
		Robot[] robots = controllers.sensor.senseNearbyGameObjects(Robot.class);
		for (Robot r : robots) {
			if (r.getTeam() == controllers.myRC.getTeam()) {
				RobotInfo info = controllers.sensor.senseRobotInfo(r);
				if (info.chassis == chassis)
					return info;
			}
		}
		return null;
	}
	
	private void encodeEmptyNumInDirection () {
		
		Direction dir = Direction.NORTH;
		for (int i = 0; i < buildingLocs.emptySize; i++) {
			dir = dir.rotateRight();
		}
		while (controllers.motor.isActive())
			controllers.myRC.yield();
		
		try {
			controllers.motor.setDirection(dir);
			controllers.myRC.yield();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void checkAdjacentRecyclers() throws GameActionException {
		Robot[] robots = controllers.sensor.senseNearbyGameObjects(Robot.class);
		MapLocation maxEmptyLocation = currentLoc;
		int maxEmptyNum = buildingLocs.emptySize;
		RobotInfo maxRobotInfo = null;
		
		for (Robot r : robots) {
			if (r.getTeam() == controllers.myRC.getTeam()) {
				RobotInfo info = controllers.sensor.senseRobotInfo(r);
				if (info.chassis == Chassis.BUILDING && !info.on) {
					for (ComponentType com : info.components) {
						if (com == ComponentType.RECYCLER) {
							Direction dir = info.direction;
							if (buildingLocs.indexMapping(dir) > maxEmptyNum) {
								maxEmptyNum = buildingLocs.indexMapping(dir);
								maxEmptyLocation = info.location;
								maxRobotInfo = info;
							}
						} 
					}
				}
			}
		}
		
//		controllers.myRC.setIndicatorString(0, "My Location:" + controllers.myRC.getLocation() + "Empty:" + buildingLocs.emptySize);
//		controllers.myRC.setIndicatorString(1, "Max Location:" + maxEmptyLocation + "Empty:" + maxEmptyNum);

		if (maxEmptyLocation != currentLoc) {
			// Check if the recycler has an antenna
			boolean hasAntenna = false;
			for (ComponentType com : maxRobotInfo.components) {
				if (com == ComponentType.ANTENNA)
					hasAntenna = true;
			}
			
			// If not, build one on it
			if (!hasAntenna) {
				if (controllers.myRC.getTeamResources() > 10)
					recycler.build(ComponentType.ANTENNA, maxEmptyLocation, RobotLevel.ON_GROUND);
			}
			encodeEmptyNumInDirection();
			controllers.myRC.turnOn(maxEmptyLocation, RobotLevel.ON_GROUND);
			controllers.myRC.turnOff();
		}
	}
	
	
	private void updateRatios(){
		total = 0;
		for (int index = 0; index < unitRatios.length; index++){
			total += unitRatios[index];
			cumulatedRatios[index] = total;
		}
	}
	
	private void constructUnit() {
		
		if ( constructingQueue.size() == 0 && unitUnderConstruction == null)
			return;
		else if ( unitUnderConstruction == null || constructIdleRound == 0) {
			UnitType unitUnderConstruction = constructingQueue.peek();
			
			ComponentType chassisBuilder = unitUnderConstruction.getChassisBuilder();
			
			if (chassisBuilder == ComponentType.RECYCLER) {
				//Cannot be built by recycler itself
				if ((unitUnderConstruction.requiredBuilders ^ Util.RECYCLER_CODE) == 0) {
					if (buildingSystem.constructUnit(unitUnderConstruction)) {
						++unitConstructed;
						msgHandler.queueMessage(new UnitReadyMessage(unitUnderConstruction));
					}
				} else {
					MapLocation buildLoc = buildingLocs.constructableLocation(Util.RECYCLER_CODE, unitUnderConstruction.requiredBuilders);
					if (buildLoc != null) {
						if (buildingSystem.constructUnit(buildLoc,unitUnderConstruction, buildingLocs)) {
							++unitConstructed;
							msgHandler.queueMessage(new UnitReadyMessage(unitUnderConstruction));
						}
					}
				}
			} else {
				if (buildingLocs.getLocations(chassisBuilder) != null) {
					msgHandler.queueMessage(new ConstructUnitMessage(buildingLocs.getLocations(chassisBuilder), unitUnderConstruction));	
				} else {
					return;
				}
			}
			constructIdleRound = 30;
			this.unitUnderConstruction = constructingQueue.poll();

		}
		
	}
	
	private void constructBase() throws GameActionException {
		MapLocation loc;
		if (buildFactory) {
			// Build 1 factory only

			if (buildingLocs.factoryLocation == null) {
				// Try to build somewhere near armory
				MapLocation loc1 = null;
				MapLocation loc2 = null;
				if (buildingLocs.armoryLocation != null) {
					loc1 = buildingLocs.rotateLeft(buildingLocs.armoryLocation, 2);
					loc2 = buildingLocs.rotateRight(buildingLocs.armoryLocation, 2);
				}				
				if (buildingLocs.armoryLocation != null && (constructor.canBuild(Chassis.BUILDING, loc1) || constructor.canBuild(Chassis.BUILDING, loc2))) {
					if (constructBuilding(loc1, UnitType.FACTORY)) 
						buildFactory = false;
				else if (constructBuilding(loc2, UnitType.FACTORY)) 
						buildFactory = false;
				} else {
					for (int i = 4; i > 0; i--) {
						loc = buildingLocs.consecutiveEmpties(i);
						if (loc != null) {
							switch (i) {
							case 4:
								if (constructBuilding(buildingLocs.rotateRight(loc),UnitType.FACTORY))
									buildFactory = false;
								break;
							case 3:
								if (constructBuilding(loc, UnitType.FACTORY))
									buildFactory = false;
								break;
							default:
								if (birthRoundNum < 200) {
									msgHandler.queueMessage(new NotEnoughSpaceMessage());
									buildFactory = false;
									buildArmory = false;
								} else {
									
									if (constructBuilding(loc, UnitType.FACTORY)) 
										buildFactory = false;
									break;
								}
							}
							break;
						}
					}
				}
			} else {
				buildFactory = false;
			}
		}
		
		if (buildArmory) {
//			Build 1 armory only
			if (buildingLocs.armoryLocation == null) {
				MapLocation loc1 = null;
				MapLocation loc2 = null;
				if (buildingLocs.factoryLocation != null) {
					loc1 = buildingLocs.rotateRight(buildingLocs.factoryLocation, 2);
					loc2 = buildingLocs.rotateLeft(buildingLocs.factoryLocation, 2);
				}
				if (buildingLocs.factoryLocation != null && (constructor.canBuild(Chassis.BUILDING, loc1) || constructor.canBuild(Chassis.BUILDING, loc2))) {
					if (constructBuilding(loc1, UnitType.ARMORY)) 
							buildArmory = false;
					else if (constructBuilding(loc2, UnitType.ARMORY)) 
							buildArmory = false;
				} else {
					for (int i = 3; i > 0; i--) {
						loc = buildingLocs.consecutiveEmpties(i);
						if (loc != null) {
							if (constructBuilding(loc, UnitType.ARMORY)) {
								buildArmory = false;
								break;
							}
						}
					}
				}
			} else {
				buildArmory = false;
			}
		}
		
		if (buildTower) {
			loc = buildingLocs.consecutiveEmpties(1);
			if (loc != null) {
				if (constructBuilding(loc, UnitType.TOWER)) 
					buildTower = false;
			} else {
				buildTower = false;
			}
		}
		
		if (buildRailgunTower) {
			if (buildingLocs.factoryLocation != null) {
				MapLocation loc1 = buildingLocs.rotateLeft(buildingLocs.factoryLocation);
				MapLocation loc2 = buildingLocs.rotateRight(buildingLocs.factoryLocation);
				if (constructor.canBuild(Chassis.BUILDING, loc1)) {
					if (constructBuilding(loc1, UnitType.RAILGUN_TOWER)) 
						buildRailgunTower = false;
				} else if (constructor.canBuild(Chassis.BUILDING, loc2)) {
					if (constructBuilding(loc1, UnitType.RAILGUN_TOWER)) 
						buildRailgunTower = false;
				} else {
					buildRailgunTower = false;
				}
			} else {
				buildRailgunTower = false;
			}
		}
	}
	
	private boolean constructBuilding(MapLocation buildLoc, UnitType type) {
		try {
			if (constructor == null)
				return false;

			if (type == UnitType.RAILGUN_TOWER && buildingLocs.factoryLocation == null)
				return false;
			
			while (constructor.isActive() || controllers.myRC.getTeamResources() < 90)
				yield();
			if (constructor.canBuild(Chassis.BUILDING, buildLoc))
				constructor.build(Chassis.BUILDING, buildLoc);
			else
				return false;

			switch (type) {
			case FACTORY:
				while (constructor.isActive() || controllers.myRC.getTeamResources() < 80)
					yield();
				constructor.build(ComponentType.FACTORY, buildLoc, RobotLevel.ON_GROUND);
				buildingSystem.constructComponent(buildLoc, UnitType.FACTORY);
				break;
				
			case ARMORY:
				while (constructor.isActive() || controllers.myRC.getTeamResources() < 80)
					yield();
				constructor.build(ComponentType.ARMORY, buildLoc, RobotLevel.ON_GROUND);
				buildingSystem.constructComponent(buildLoc, UnitType.ARMORY);
				break;
				
			case TOWER:
				buildingSystem.constructComponent(buildLoc, UnitType.TOWER);
				break;
				
			case RAILGUN_TOWER:
				msgHandler.queueMessage(new BuildingRequestMessage(buildingLocs.factoryLocation, buildLoc, UnitType.RAILGUN_TOWER));
				buildingSystem.constructComponent(buildLoc, UnitType.RAILGUN_TOWER);
				break;
			}
			
			buildingLocs.setLocations(type, buildLoc);

			controllers.myRC.turnOn(buildLoc, RobotLevel.ON_GROUND);

			buildingLocs.updateBuildingLocs();

			return true;
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void constructUnitAtRatio() {

		int index;
		int seed = ((int) (getEffectiveFluxRate()*100) + Clock.getRoundNum()) % total; 

		// Find the production index
		for (index = 0; seed >= cumulatedRatios[index]; ++index);

//		
		UnitType type = types[index];
		
		ComponentType chassisBuilder = type.getChassisBuilder();
		

		
		if (chassisBuilder == ComponentType.RECYCLER) {
//			Cannot be built by recycler itself
			if ((type.requiredBuilders ^ Util.RECYCLER_CODE) == 0) {
				if (buildingSystem.constructUnit(type)) {
					built = true;
					++unitConstructed;
//					msgHandler.queueMessage(new GridMapMessage(borders, homeLocation, gridMap));	
				}
			} else {
				MapLocation buildLoc = buildingLocs.constructableLocation(Util.RECYCLER_CODE, type.requiredBuilders);
				if (buildLoc != null) {
					if (buildingSystem.constructUnit(buildLoc,type, buildingLocs)) {
						built = true;
						++unitConstructed;
//						msgHandler.queueMessage(new GridMapMessage(borders, homeLocation, gridMap));	
						
					}
				}
			}
		} else {
			if (buildingLocs.getLocations(chassisBuilder) != null) {
				built = true;
				msgHandler.queueMessage(new ConstructUnitMessage(buildingLocs.getLocations(chassisBuilder), type));
//				msgHandler.queueMessage(new GridMapMessage(borders, homeLocation, gridMap));	
			}
		}
		

		
//		Build more constructors if flux is insufficient
//		double fluxRate = getEffectiveFluxRate();
//		
//		if (Clock.getRoundNum() > 1000){
//			mySpawningState = spawningState.LATE;
//			unitRatios[0] = 0;
//			unitRatios[1] = 1;
//			unitRatios[2] = 1;
//			unitRatios[3] = 0;
//			unitRatios[4] = 0;
//
//		}
//
//
//		else if ( fluxRate > 2.0 && Clock.getRoundNum() > 300){
//			mySpawningState = spawningState.ATTACKING;
//			unitRatios[0] = 1;
//			unitRatios[1] = 1;
//			unitRatios[2] = 1;
//			unitRatios[3] = 0;
//			unitRatios[4] = 0;
//		}
//		else if ( fluxRate > 1.0 && Clock.getRoundNum() > 200 ){
//			mySpawningState = spawningState.BALANCE;
//			unitRatios[0] = 1;
//			unitRatios[1] = 1;
//			unitRatios[2] = 1;
//			unitRatios[3] = 0;
//			unitRatios[4] = 0;
//		}
//		else {
//			mySpawningState = spawningState.COLLECTING;
//			unitRatios[0] = 1;
//			unitRatios[1] = 1;
//			unitRatios[2] = 1;
//			unitRatios[3] = 0;
//			unitRatios[4] = 0;
//		}
//		updateRatios();
	}
}
