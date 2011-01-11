package team017.AI;

import team017.construction.UnitType;
import team017.message.BorderMessage;
import team017.message.BuildingLocationInquiryMessage;
import team017.message.BuildingLocationResponseMessage;
import team017.message.BuildingRequestMessage;
import team017.message.ConstructionCompleteMessage;
import team017.message.ScoutingMessage;
import team017.util.Util;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Mine;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class RecyclerAI extends BuildingAI {

	private Mine myMine;
	private int unitConstructed = 0;
	private int birthRoundNum;
	
	private Direction[] scoutingDir;
	private int numOfDir = 0;
	
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
	public void proceed() {
		
		if (Clock.getRoundNum() == 0)
			init();
		else{
			
			// turn off if there is already a recycler nearby
			if (builderDirs.recyclerDirection != null) {
				try {
					while (controllers.sensor.isActive())
						yield();
						// Build 1 PLATING & 2 SHIELDs on itself
							while (controllers.myRC.getTeamResources() < 9)
								yield();
							controllers.builder.build(ComponentType.PLATING, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
							yield();
							while (controllers.myRC.getTeamResources() < 11)
								yield();
							controllers.builder.build(ComponentType.SHIELD, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
							yield();
							while (controllers.myRC.getTeamResources() < 11)
								yield();
							controllers.builder.build(ComponentType.SHIELD, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
							yield();						
						
						controllers.myRC.turnOff();
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				while(controllers.myRC.getTeamResources() < 10)
					controllers.myRC.yield();
				try {
					// build an antenna on itself
					while(controllers.builder.isActive())
						controllers.myRC.yield();
					controllers.builder.build(ComponentType.ANTENNA, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
					yield();
					while (controllers.myRC.getTeamResources() < 15)
						yield();
					controllers.builder.build(ComponentType.PLATING, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
					yield();
					while (controllers.myRC.getTeamResources() < 20)
						yield();
					controllers.builder.build(ComponentType.SHIELD, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
					yield();				
				} catch (GameActionException e1) {
					System.out.println("caught exception:");
					e1.printStackTrace();
				}
			}
			
		}
		
		while (true) {
			try {
				if (enemyBaseLoc != null){
//					controllers.myRC.setIndicatorString(0, enemyBaseLoc.toString());
				}

				processMessages();
				
//				/*
//				 * Producing constructor only
//				 */
//				if (getEffectiveFluxRate() > 0.3 && controllers.myRC.getTeamResources() > 150 && unitConstructed < 5) {
//						if (buildingSystem.constructUnit(UnitType.CONSTRUCTOR))
//							++unitConstructed;
//						msgHandler.queueMessage(new BorderMessage(borders, homeLocation));
//						if (enemyBaseLoc != null)
//							msgHandler.queueMessage(new ScoutingMessage( controllers.myRC.getLocation().add(controllers.myRC.getLocation().directionTo(enemyBaseLoc), unitConstructed*5) ) );
//						yield();
//				}
				
				double fluxRate = getEffectiveFluxRate();
				double [] thresholds = {3, 2.4, 1.8, 1.2, 0.3};
				int [][] unitRatio = {{2, 1, 3}, {2, 1, 1}, {1, 1, 1}, {1, 2, 2}, {1, 1}};
				UnitType [][] types = {{UnitType.GRIZZLY, UnitType.CONSTRUCTOR, UnitType.GRIZZLY},
						{UnitType.GRIZZLY, UnitType.CONSTRUCTOR, UnitType.GRIZZLY},
						{UnitType.GRIZZLY, UnitType.CONSTRUCTOR, UnitType.GRIZZLY},
						{UnitType.GRIZZLY, UnitType.CONSTRUCTOR, UnitType.GRIZZLY},
						{UnitType.GRIZZLY, UnitType.CONSTRUCTOR}};

				double [] laterThresholds = {6, 2.4, 1.8, 1.2, 0.3};
				int [][] laterUnitRatio = {{5, 1, 5}, {2, 2, 2}, {1, 1, 2}, {1, 1}, {1, 1, 1}};
				UnitType [][] laterTypes = {{UnitType.GRIZZLY, UnitType.CONSTRUCTOR, UnitType.GRIZZLY},
						{UnitType.GRIZZLY, UnitType.CONSTRUCTOR, UnitType.GRIZZLY},
						{UnitType.GRIZZLY, UnitType.CONSTRUCTOR, UnitType.GRIZZLY},
						{UnitType.GRIZZLY, UnitType.CONSTRUCTOR},
						{UnitType.CONSTRUCTOR, UnitType.GRIZZLY, UnitType.CONSTRUCTOR}};

				if (controllers.myRC.getTeamResources() > 170
						&& controllers.myRC.getTeamResources() > ((Clock.getRoundNum() - birthRoundNum) / 500) * 100) {
					if (Clock.getRoundNum() > 1000) {
						constructUnitAtRatio (fluxRate, laterThresholds, laterUnitRatio, laterTypes);
					} else if (Clock.getRoundNum() > 200 
							&& controllers.myRC.getTeamResources() > 150

							){
						constructUnitAtRatio (fluxRate, thresholds, unitRatio, types);
					}
				}


				
//				if (Clock.getRoundNum() > 1000 && getEffectiveFluxRate() > 0.3 && controllers.myRC.getTeamResources() > 200) {
//					buildingSystem.constructUnit(UnitType.GRIZZLY);
//					if (Clock.getRoundNum() < 1000) {
//						if (Clock.getRoundNum() % 3 == 0){
//							buildingSystem.constructUnit(UnitType.CONSTRUCTOR);
//						}
//						else{
//							buildingSystem.constructUnit(UnitType.GRIZZLY);
//							if (enemyBaseLoc != null){
//								msgHandler.queueMessage(new EnemyLocationMessage(enemyBaseLoc));
//								yield();
//							}
//						}
//
//					} else {
//						if (Clock.getRoundNum() % 5 == 0)
//							buildingSystem.constructUnit(UnitType.CONSTRUCTOR);
//						else{
//							buildingSystem.constructUnit(UnitType.GRIZZLY);
//							if (enemyBaseLoc != null){
//								msgHandler.queueMessage(new EnemyLocationMessage(enemyBaseLoc));
//								yield();
//							}
//						}
//					}
//				}
				
				// turn off when the mine is depleted
				if (controllers.sensor.senseMineInfo(myMine).roundsLeft == 0)
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
				controllers.builder.build(ComponentType.ANTENNA, info.location,RobotLevel.ON_GROUND);
			}
			yield();

			// Turn off 1 of the initial recyclers
			RobotInfo otherRecycler = senseAdjacentChassis(Chassis.BUILDING);
			if(otherRecycler != null && Util.containsComponent(otherRecycler.components, ComponentType.ANTENNA)){
				
				// Build 1 PLATING & 2 SHIELDs on itself
				while (controllers.myRC.getTeamResources() < 9)
					yield();
				controllers.builder.build(ComponentType.PLATING, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
				yield();
				while (controllers.myRC.getTeamResources() < 11)
					yield();
				controllers.builder.build(ComponentType.SHIELD, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
				yield();
				while (controllers.myRC.getTeamResources() < 11)
					yield();
				controllers.builder.build(ComponentType.SHIELD, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
				yield();				
				controllers.myRC.turnOff();
			}
			else{
				controllers.builder.build(ComponentType.ANTENNA, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
				yield();
				// Build 1 PLATING & 1 SHIELD on itself
				while (controllers.myRC.getTeamResources() < 9)
					yield();
				controllers.builder.build(ComponentType.PLATING, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
				yield();
				while (controllers.myRC.getTeamResources() < 11)
					yield();
				controllers.builder.build(ComponentType.SHIELD, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
				yield();				
			}
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
				determinScoutDirs();
				break;
			}
//			case BUILDING_REQUEST:{
//				BuildingRequestMessage handler = new BuildingRequestMessage(msg);
//				if (handler.getBuilderLocation().equals(controllers.myRC.getLocation())) {
//					while(!buildingSystem.constructComponent(handler.getBuildingLocation(),handler.getUnitType())){
//						if(controllers.sensor.senseObjectAtLocation(handler.getBuilderLocation(),handler.getUnitType().chassis.level).getTeam() != controllers.myRC.getTeam())
//							break;
//						yield();
//					}	
//				}
//				break;
//				
//				BuildingRequestMessage handler = new BuildingRequestMessage(msg);
//				if (handler.getBuilderLocation().equals(controllers.myRC.getLocation())) {
//					buildingSystem.constructComponent(handler.getBuildingLocation(),handler.getUnitType());
//					yield();
//				}
//				break;
//			}
			
//			case BUILDING_LOCATION_INQUIRY_MESSAGE: {
//				BuildingLocationInquiryMessage handler = new BuildingLocationInquiryMessage(msg);
//				if(handler.getBuilderLocation().equals(controllers.myRC.getLocation())){
//					if(builderDirs.armoryDirection != null && builderDirs.factoryDirection != null){
//						msgHandler.queueMessage(new BuildingLocationResponseMessage(builderDirs.consecutiveEmpties(3), -1));
//						controllers.myRC.setIndicatorString(0, "Consecutive -1");
//					} else if (builderDirs.consecutiveEmpties(3) != Direction.NONE) {
//						msgHandler.queueMessage(new BuildingLocationResponseMessage(builderDirs.consecutiveEmpties(3), 3));
//						controllers.myRC.setIndicatorString(0, "Consecutive 3");
//					} else if (builderDirs.consecutiveEmpties(2) != Direction.NONE) {
//						msgHandler.queueMessage(new BuildingLocationResponseMessage(builderDirs.consecutiveEmpties(2), 2));
//						controllers.myRC.setIndicatorString(0, "Consecutive 2");
//					}
//					yield();
//				}
//				break;
//			}
			
			case CONSTRUCTION_COMPLETE: {
				ConstructionCompleteMessage handler = new ConstructionCompleteMessage(msg);
//				controllers.myRC.setIndicatorString(1, "complete!" + Clock.getRoundNum());
				
				/*
				 * When a new building is constructed, we would like to build an antenna on it.
				 */
				
				MapLocation currentLoc = controllers.myRC.getLocation();

				// see if the target is adjacent
				if (handler.getBuildingLocation().isAdjacentTo(currentLoc)) {
					
					// update the builderDirs
					Direction builderDir = currentLoc.directionTo(handler.getBuildingLocation());
					builderDirs.setDirections(handler.getBuilderType(), builderDir);
					
					if(handler.getBuilderType() != ComponentType.RECYCLER){
						// face the correct direction
						if (controllers.myRC.getDirection() != builderDir){
							controllers.motor.setDirection(builderDir);
							yield();
						}
						
						// build an antenna if it doesn't have one
						if (!Util.containsComponent(controllers, handler.getBuildingLocation(), RobotLevel.ON_GROUND, ComponentType.ANTENNA)) {
							controllers.builder.build(ComponentType.ANTENNA, handler.getBuildingLocation(), RobotLevel.ON_GROUND);
						}
					}
				}
				break;
			}

			}
		}
	}
	
	
	private void determinScoutDirs() {

		Direction enemyBaseDir = homeLocation.directionTo(enemyBaseLoc[0]);
		
		if ( enemyBaseDir.isDiagonal() ){
			scoutingDir = new Direction [5];
			scoutingDir[0] = enemyBaseDir;
			scoutingDir[1] = enemyBaseDir.rotateLeft();
			scoutingDir[2] = enemyBaseDir.rotateRight();
			scoutingDir[3] = scoutingDir[1].rotateLeft();
			scoutingDir[4] = scoutingDir[2].rotateRight();
			numOfDir = 5;
		} else {
			scoutingDir = new Direction [7];
			scoutingDir[0] = enemyBaseDir;
			scoutingDir[1] = enemyBaseDir.rotateLeft();
			scoutingDir[2] = enemyBaseDir.rotateRight();
			scoutingDir[3] = scoutingDir[1].rotateLeft();
			scoutingDir[4] = scoutingDir[2].rotateRight();
			scoutingDir[5] = scoutingDir[3].rotateLeft();
			scoutingDir[6] = scoutingDir[4].rotateRight();
			numOfDir = 7;
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
	
	private void constructUnitAtRatio (double fluxRate, double [] thresholds, int [][] ratios, UnitType [][] types){
		for (int i = 0; i < thresholds.length; ++i){
			if (fluxRate > thresholds[i]){
				int ratioTotalSum = 0;
				int [] ratioPartialSum = new int[ratios[i].length];
				int ratioPointer = 0;
				for (int j = 0; j < ratios[i].length; ++j){
					ratioTotalSum += ratios[i][j];
					ratioPartialSum[j] = ratioTotalSum;
				}

				for (ratioPointer = 0; (unitConstructed + controllers.myRC.getRobot().getID()) % ratioTotalSum >= ratioPartialSum[ratioPointer]; ++ ratioPointer);
				if (buildingSystem.constructUnit(types[i][ratioPointer])) {
					++unitConstructed;
					controllers.myRC.setIndicatorString(2, ratioPointer + "");
				}

				msgHandler.queueMessage(new ScoutingMessage( controllers.myRC.getLocation().add(controllers.myRC.getLocation().directionTo(enemyBaseLoc[0]), unitConstructed*5) ) );
				if (enemyBaseLoc != null && types[i][ratioPointer] == UnitType.CONSTRUCTOR);
					msgHandler.queueMessage(new BorderMessage(borders, homeLocation));
				yield();
				break;
			}
		}
	}
}
