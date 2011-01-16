package team017.AI;

import team017.construction.UnitType;
import team017.message.BorderMessage;
import team017.message.BuildingLocationInquiryMessage;
import team017.message.BuildingLocationResponseMessage;
import team017.message.BuildingRequestMessage;
import team017.message.ConstructionCompleteMessage;
import team017.message.GridMapMessage;
import team017.util.Util;
import battlecode.common.*;

public class RecyclerAI extends BuildingAI {

	private Mine myMine;
	private int unitConstructed = 0;
	private int birthRoundNum;
	private int inquiryIdleRound = 0;
	private MapLocation currentLoc = controllers.myRC.getLocation();

	int [] unitRatios = {5, 1, 1};
	int [] cumulatedRatios = {5, 6, 7};
	int total = 7;
	private UnitType [] types = { UnitType.CONSTRUCTOR, UnitType.GRIZZLY, UnitType.RADARGUN} ;
	double thresholds = 0.3;
	
	private enum spawningState { EARLY, MIDDLE, LATE };
	
	spawningState mySpawningState = spawningState.EARLY;
	
	public RecyclerAI(RobotController rc) {
		super(rc);		
		birthRoundNum = Clock.getRoundNum();
		try {
			myMine = (Mine) controllers.sensor.senseObjectAtLocation(controllers.myRC.getLocation(), RobotLevel.MINE);
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void yield() {
		super.yield();
		if (inquiryIdleRound != 0)
			inquiryIdleRound--;
	}
	

	@Override
	public void proceed() {
		
//		controllers.myRC.setIndicatorString(0, "START");
		if (Clock.getRoundNum() <= 5)
			init();
		else{
			
			// turn off if there is already a recycler nearby
			if (buildingLocs.recyclerLocation != null) {
//				try {
//					while (controllers.sensor.isActive())
//						yield();
//					
//					// Build 1 PLATING & 2 SHIELDs on itself
//					while (controllers.myRC.getTeamResources() < 9)
//						yield();
//					controllers.builder.build(ComponentType.PLATING, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
//					yield();
//					while (controllers.myRC.getTeamResources() < 11)
//						yield();
//					controllers.builder.build(ComponentType.SHIELD, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
//					yield();
//					while (controllers.myRC.getTeamResources() < 11)
//						yield();
//					controllers.builder.build(ComponentType.SHIELD, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
//					yield();						
				
					controllers.myRC.turnOff();
//				} catch (GameActionException e) {
//					e.printStackTrace();
//				}
			} else {
				
				while(controllers.myRC.getTeamResources() < 10)
					controllers.myRC.yield();
				try {
					// build an antenna on itself
					while(controllers.builder.isActive())
						controllers.myRC.yield();
					controllers.builder.build(ComponentType.ANTENNA, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
					yield();
//					while (controllers.myRC.getTeamResources() < 15)
//						yield();
//					controllers.builder.build(ComponentType.PLATING, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
//					yield();
//					while (controllers.myRC.getTeamResources() < 20)
//						yield();
//					controllers.builder.build(ComponentType.SHIELD, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
//					yield();				
				} catch (GameActionException e1) {
					System.out.println("caught exception:");
					e1.printStackTrace();
				}
			}
			
		}
		
		while (true) {
			try {
			
				processMessages();
				
				double fluxRate = getEffectiveFluxRate();
				
				if (controllers.myRC.getTeamResources() > 170 ) {
					if (fluxRate > 0)
						constructUnitAtRatio();
				}
				
				// Send message to build CHRONO_APOCALYPSE
				MapLocation armoryLoc = buildingLocs.armoryLocation;
				MapLocation factoryLoc = buildingLocs.factoryLocation;
//				Direction armoryDir = buildingLocs.armoryDirection;
//				Direction factoryDir = buildingLocs.factoryDirection;
				if (armoryLoc != null && factoryLoc != null) {
					/*
					 * Case 1:
					 * - - A
					 * - R x
					 * - - F
					 * 
					 * OR
					 * 
					 * - A x
					 * - R F
					 * - - -
					 *
					 * Case 2:
					 * - - F
					 * - R x
					 * - - A
					 * 
					 * OR
					 *  
					 * - F x
					 * - R A
					 * - - -
					 */
					
					if (buildingLocs.rotateLeft(armoryLoc, 2) == factoryLoc) {
						
					}
					
				}
				
				// turn off when the mine is depleted
				if (controllers.sensor.senseMineInfo(myMine).roundsLeft == 0 && buildingLocs.clusterSize == 1)
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
			if (info != null
					&& controllers.myRC.getTeamResources() >= 2 * ComponentType.ANTENNA.cost
					&& !Util.containsComponent(info.components,ComponentType.ANTENNA)) {
				controllers.builder.build(ComponentType.ANTENNA, info.location, RobotLevel.ON_GROUND);
			}
			controllers.myRC.turnOff();
			while(controllers.builder.isActive())
				yield();

			// Turn off 1 of the initial recyclers
			RobotInfo otherRecycler = senseAdjacentChassis(Chassis.BUILDING);
			if(otherRecycler != null && Util.containsComponent(otherRecycler.components, ComponentType.ANTENNA)){
				
//				// Build 1 PLATING & 2 SHIELDs on itself
//				while (controllers.myRC.getTeamResources() < 9)
//					yield();
//				controllers.builder.build(ComponentType.PLATING, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
//				yield();
//				while (controllers.myRC.getTeamResources() < 11)
//					yield();
//				controllers.builder.build(ComponentType.SHIELD, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
//				yield();
//				while (controllers.myRC.getTeamResources() < 11)
//					yield();
//				controllers.builder.build(ComponentType.SHIELD, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
//				yield();				
				controllers.myRC.turnOff();
			}
			else{
				controllers.builder.build(ComponentType.ANTENNA, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
				yield();
//				// Build 1 PLATING & 1 SHIELD on itself
//				while (controllers.myRC.getTeamResources() < 9)
//					yield();
//				controllers.builder.build(ComponentType.PLATING, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
//				yield();
//				while (controllers.myRC.getTeamResources() < 11)
//					yield();
//				controllers.builder.build(ComponentType.SHIELD, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
//				yield();				
			}
		} catch (Exception e) {
			System.out.println("caught exception:");
			e.printStackTrace();
		}
	}
	@Override
	protected void processMessages() throws GameActionException {
//		controllers.myRC.setIndicatorString (1, Clock.getRoundNum() + "");
		// receive messages and handle them
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			outer:
			switch (msgHandler.getMessageType(msg)) {
			case BORDER: {						
				BorderMessage handler = new BorderMessage(msg);
				// update the borders
				int[] newBorders = handler.getBorderDirection();

				for (int i = 0; i < 4; ++i) {
					if (newBorders[i] != -1){
						if (borders[i] != newBorders[i]){
							borders[i] = newBorders[i];
						}
					}
				}
				
				homeLocation = handler.getHomeLocation();
				computeEnemyBaseLocation();
				gridMap.setBorders(borders);
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
				gridMap.merge(handler.getGridMap(controllers));
//				gridMap.printGridMap();
				
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
				}
				break;
				
//				BuildingRequestMessage bhandler = new BuildingRequestMessage(msg);
//				if (bhandler.getBuilderLocation().equals(controllers.myRC.getLocation())) {
//					buildingSystem.constructComponent(bhandler.getBuildingLocation(),bhandler.getUnitType());
//					yield();
//				}
//				break;
			}
			
			case BUILDING_LOCATION_INQUIRY_MESSAGE: {
/*
 *	Case 5: T -> F -> rT -> A
 *	T  rT F
 * 	/  R  -
 *  /  /  A
 * 
 *  /  T rT
 *  /  R  F
 *  /  A  -
 *  
 *  Case 4: T -> F -> rT
 *  T rT  F
 *  /  R  -
 *  /  /  /
 *  
 *  /  T rT
 *  /  R  F
 *  /  /  -
 *  
 *  Case 3: F -> rT
 *  / rT  F
 *  /  R  -
 *  /  /  /
 *  
 *  Case 2: T
 *  T - /
 *  / R /
 *  / / /
 *  
 *  Conclusion : 
 *  1. Always build a railgunTower at the left position of a Factory
 *  2. Build a Tower at the empty location if empty != 3
 *  3. Build at the second empty space if empty == 3 or empty == 4 and tower != null
 *  
 */
				BuildingLocationInquiryMessage handler = new BuildingLocationInquiryMessage(msg);
				
				// if the constructor is inquiring it 
				if (handler.getBuilderLocation().equals(controllers.myRC.getLocation())) {
					
					int constructorID = handler.getSourceID();
					MapLocation loc;
					
					if (inquiryIdleRound > 0)
						break;
					
					
					if (buildingLocs.towerLocations.size() == 0) {
						for (int i = 5; i >= 2; i--) {
							loc = buildingLocs.consecutiveEmpties(i);
							if (loc != null) {
								if (i == 3 )
									break;
								msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, loc, UnitType.TOWER));
								inquiryIdleRound = 5;
								break;
							}
						}
					} else if (buildingLocs.factoryLocation == null) {
						for (int i = 4; i >= 3; i--) {
							loc = buildingLocs.consecutiveEmpties(i);
							if (loc != null)
								msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, buildingLocs.rotateRight(loc), UnitType.FACTORY));
						}
						inquiryIdleRound = 5;
					} else if (buildingLocs.factoryLocation != null && buildingLocs.railgunTowerLocations.size() == 0) {
						msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, buildingLocs.rotateLeft(buildingLocs.factoryLocation), UnitType.RAILGUN_TOWER));
						inquiryIdleRound = 5;
					} else if (buildingLocs.armoryLocation == null) {
						loc = buildingLocs.consecutiveEmpties(2);
						if (loc != null) {
							msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, buildingLocs.rotateRight(loc), UnitType.ARMORY));
						}
						inquiryIdleRound = 5;
					} else {
						msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, null, null));
					}


					
					

					yield();
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
				
				// see if the target is adjacent
				if (buildingLocation.isAdjacentTo(currentLoc)) {
					// UnitType.TOWER
					if (handler.getBuildingType() == UnitType.TOWER) {
						buildingLocs.setLocations(handler.getBuildingType(), buildingLocation);
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
//						
						// build an antenna if it doesn't have one
						if (!Util.containsComponent(controllers, buildingLocation, RobotLevel.ON_GROUND, ComponentType.ANTENNA)) {
							controllers.builder.build(ComponentType.ANTENNA, handler.getBuildingLocation(), RobotLevel.ON_GROUND);
						}
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
	
	private void updateRatios(){
		total = 0;
		for (int index = 0; index < unitRatios.length; index++){
			total += unitRatios[index];
			cumulatedRatios[index] = total;
		}
	}
	
	private void constructUnitAtRatio() {

		int index;
		int seed = ((int) (getEffectiveFluxRate()*100) + Clock.getRoundNum()) % total; 

		controllers.myRC.setIndicatorString(0, seed+"");
		
		// Find the production index
		for (index = 0; seed >= cumulatedRatios[index]; ++index);

		if (buildingSystem.constructUnit(types[index])) {
			++unitConstructed;
			if (types[index] == UnitType.CONSTRUCTOR)
				msgHandler.queueMessage(new GridMapMessage(borders, homeLocation, gridMap));
			else
				msgHandler.queueMessage(new BorderMessage(borders, homeLocation));
		}
		
		if (mySpawningState == spawningState.EARLY && Clock.getRoundNum() > 300 && Clock.getRoundNum() < 1500 ){
			mySpawningState = spawningState.MIDDLE;
			unitRatios[0] = 1;
			unitRatios[1] = 2;
			updateRatios();
		} else if (mySpawningState == spawningState.MIDDLE && Clock.getRoundNum() > 1500) {
			mySpawningState = spawningState.LATE;
			unitRatios[0] = 1;
			unitRatios[1] = 3;
			updateRatios();
		}
	}
}
