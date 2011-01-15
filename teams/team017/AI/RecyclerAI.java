package team017.AI;

import team017.construction.UnitType;
import team017.message.BorderMessage;
import team017.message.BuildingLocationInquiryMessage;
import team017.message.BuildingLocationResponseMessage;
import team017.message.BuildingRequestMessage;
import team017.message.ConstructionCompleteMessage;
import team017.util.Util;
import battlecode.common.*;

public class RecyclerAI extends BuildingAI {

	private Mine myMine;
	private int unitConstructed = 0;
	private int birthRoundNum;
	private int inquiryIdleRound = 0;
	
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
		
		
		if (Clock.getRoundNum() == 0)
			init();
		else{
			
			// turn off if there is already a recycler nearby
			if (buildingDirs.recyclerDirection != null) {
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
				
				if (controllers.myRC.getTeamResources() > 170) {
					if (fluxRate > 0)
						constructUnitAtRatio();
				}
				
				// turn off when the mine is depleted
				if (controllers.sensor.senseMineInfo(myMine).roundsLeft == 0 && buildingDirs.clusterSize == 1)
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
				BuildingLocationInquiryMessage handler = new BuildingLocationInquiryMessage(msg);
				
				// if the constructor is inquiring it 
				if (handler.getBuilderLocation().equals(controllers.myRC.getLocation())) {
					
					int constructorID = handler.getSourceID();
					Direction dir;
					
					if (inquiryIdleRound > 0)
						break;
					
					// if there are no towers around
					if (buildingDirs.towerDirection == null) {
						for (int i = 4; i >= 2; i--) {
							dir = buildingDirs.consecutiveEmpties(i);
							if (dir != null)
								msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, dir, UnitType.TOWER));
						}
						inquiryIdleRound = 5;
					} else if (buildingDirs.armoryDirection == null) {
						for (int i = 3; i >= 2; i--) {
							dir = buildingDirs.consecutiveEmpties(i);
							if (dir != null)
								msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, dir, UnitType.ARMORY));
						}
						inquiryIdleRound = 5;
					} 
//					else if (buildingDirs.factoryDirection == null) {
//						dir = buildingDirs.consecutiveEmpties(2);
//						if (dir != null)
//							msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, dir.rotateRight(), UnitType.FACTORY));
//					inquiryIdleRound = 5;
//					}
					else {
						msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, Direction.NONE, null));
					}
					
					
//					// if there is already a tower around
//					if (buildingDirs.towerDirection != null || inquiryIdleRound > 0) {
//						msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, Direction.NONE, null));
//						controllers.myRC.setIndicatorString(0, "Consecutive -1");
//					} else if ( (dir = buildingDirs.consecutiveEmpties(2)) != Direction.NONE ) {
////						controllers.myRC.setIndicatorString(1, "available dir:" + dir);
//						msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, dir, UnitType.TOWER));
//						inquiryIdleRound = 5;
//						controllers.myRC.setIndicatorString(0, "Consecutive 2");
//					}
						
//					else if ( (dir = buildingDirs.consecutiveEmpties(4)) != Direction.NONE ) {
//						msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, dir, 4));
//						controllers.myRC.setIndicatorString(0, "Consecutive 4");
//					} else if ( (dir = buildingDirs.consecutiveEmpties(3)) != Direction.NONE ) {
//						msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, dir, 3));
//						controllers.myRC.setIndicatorString(0, "Consecutive 3");
//					} else if ( (dir = buildingDirs.consecutiveEmpties(2)) != Direction.NONE ) {
//						msgHandler.queueMessage(new BuildingLocationResponseMessage(constructorID, dir, 2));
//						controllers.myRC.setIndicatorString(0, "Consecutive 2");
//					}
					
					
//					} else if (buildingDirs.consecutiveEmpties(3) != Direction.NONE) {
//						msgHandler.queueMessage(new BuildingLocationResponseMessage(buildingDirs.consecutiveEmpties(3), 3));
//						controllers.myRC.setIndicatorString(0, "Consecutive 3");
//					} else 
//					if (buildingDirs.consecutiveEmpties(2) != Direction.NONE) {
//						msgHandler.queueMessage(new BuildingLocationResponseMessage(buildingDirs.consecutiveEmpties(2), 2));
////						controllers.myRC.setIndicatorString(0, "Consecutive 2");
//					}
					yield();
				}
				break;
			}
			
			case CONSTRUCTION_COMPLETE: {
				ConstructionCompleteMessage handler = new ConstructionCompleteMessage(msg);
				MapLocation currentLoc = controllers.myRC.getLocation();
				MapLocation buildingLocation = handler.getBuildingLocation();
				Direction builderDir = currentLoc.directionTo(buildingLocation);

				/*
				 * When a new building is constructed, we would like to build an antenna on it.
				 */
				
				// see if the target is adjacent
				if (buildingLocation.isAdjacentTo(currentLoc)) {
					
					// UnitType.TOWER
					if (handler.getBuildingType() == UnitType.TOWER) {
						buildingDirs.setDirections(handler.getBuildingType(), builderDir);
						while(!buildingSystem.constructComponent(buildingLocation, UnitType.TOWER)) {
							GameObject obj = controllers.sensor.senseObjectAtLocation(buildingLocation,RobotLevel.ON_GROUND);
							if (obj == null || obj.getTeam() != controllers.myRC.getTeam()) {
								buildingDirs.setDirections(handler.getBuildingType(), null);
								break outer;
							}
							yield();
						}
						break;
					}
					
					buildingDirs.setDirections(handler.getBuildingType(), builderDir);
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
			msgHandler.queueMessage(new BorderMessage(borders, homeLocation));
		}
		
		if (mySpawningState == spawningState.EARLY && Clock.getRoundNum() > 500 && Clock.getRoundNum() < 1500 ){
			mySpawningState = spawningState.MIDDLE;
			unitRatios[0] = 1;
			unitRatios[1] = 1;
			unitRatios[1] = 1;
			updateRatios();
		} else if (mySpawningState == spawningState.MIDDLE && Clock.getRoundNum() > 1500) {
			mySpawningState = spawningState.LATE;
			unitRatios[0] = 1;
			unitRatios[1] = 2;
			unitRatios[1] = 2;
			updateRatios();
		}
	}
}
