package team017.AI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import team017.combat.CombatSystem;
import team017.construction.UnitType;
import team017.message.BuildingLocationInquiryMessage;
import team017.message.BuildingLocationResponseMessage;
import team017.message.ConstructionCompleteMessage;
import team017.message.GridMapMessage;
import team017.message.MineInquiryMessage;
import team017.message.MineResponseMessage;
import team017.util.Util;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;
import battlecode.common.TerrainTile;

public class ConstructorAI extends GroundAI {

	private Set<MapLocation> mineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> recyclerLocations = new HashSet<MapLocation>();
	private Set<MapLocation> builtLocations = new HashSet<MapLocation>();
	
	private int attackedRound = 0;;
	
	private double prevHp = 0;
	private CombatSystem combat;
	private int builtIdleRound;

//	Mine[] minelist;
	MapLocation nearestMine = null;

	public ConstructorAI(RobotController rc) {
		super(rc);
		navigator.updateMap();
		combat = new CombatSystem(controllers);
	}
	
	public void proceed() {
		// Initial movement
		if (Clock.getRoundNum() == 0) {
			init();
		}
		
		while (true) {
			try {
				while (controllers.builder.isActive())
					yield();
				
				processMessages();

				buildRecyclers();
				
				
//				while (evaluateDanger());
//
//				
//				if (attacked && controllers.enemyNum() == 0) {
//					
//					Direction mydir = controllers.myRC.getDirection();
//					if (!controllers.motor.isActive()
//							&& controllers.motor.canMove(mydir.opposite())) {
//						try {
//							controllers.motor.moveBackward();
//							yield();
//						} catch (GameActionException e) {
//						}
//					}
////					try {
////						if (Clock.getRoundNum() % 2 == 0)
////							controllers.motor.setDirection(mydir.rotateLeft().rotateLeft());
////						else
////							controllers.motor.setDirection(mydir.rotateRight().rotateRight());
////					}
////					catch (Exception e) {}
//					continue;
//				}
				
				
//<<<<<<< HEAD

				if (builtIdleRound == 0)
//=======
//				if (Clock.getRoundNum() >= 400)	controllers.myRC.suicide();
//				
//				if (roundSinceLastBuilt > 50)
//>>>>>>> branch 'refs/heads/master' of git@github.com:ckwang/BCHY.git
					navigate();
				if (controllers.myRC.getTeamResources() > 100 && Clock.getRoundNum() > 200 && Clock.getRoundNum() % 2 == 1)
					checkEmptyRecyclers();
//				if (Clock.getRoundNum() % 15 == 0) {
////					msgHandler.queueMessage(new BorderMessage(borders, homeLocation));
//					msgHandler.queueMessage(new GridMapMessage(borders, homeLocation, gridMap));
//				}

				
				yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}
	
	public boolean evaluateDanger() {
		controllers.senseAll();
		MapLocation myloc = controllers.myRC.getLocation();
		if (controllers.mobileEnemyNum() > controllers.allyMobile.size() + 2) {
			MapLocation enemyCenter = Util.aveLocation(controllers.enemyMobile);
			Direction edir = myloc.directionTo(enemyCenter);
			int dist = myloc.distanceSquaredTo(enemyCenter);
			escape(38 - dist, edir);
			return true;
		}
//		else if (attacked) {
//			if (!controllers.motor.isActive() && controllers.motor.canMove(mydir.opposite())) {
//				try {controllers.motor.moveBackward();} 
//				catch (GameActionException e) {}
//			}
//			return true;
//		}
		return false;
			
	}
	
	public void escape(int steps, Direction dir) {
		int s = 0;
		boolean flee = false;
		while (s*s < steps) {
			try {flee = combat.flee(dir);} 
			catch (GameActionException e) {}
			if (flee) {
				++s;
				yield();
			}
		}
	}
	
	public void yield() {
		super.yield();
		if (builtIdleRound > 0)
			builtIdleRound--;
		controllers.senseAll();
		updateLocationSets();
		navigator.updateMap();
		senseBorder();
		
		if (controllers.myRC.getHitpoints() < prevHp) {
			attackedRound = Clock.getRoundNum();
		}
		prevHp = controllers.myRC.getHitpoints();
//		controllers.myRC.setIndicatorString(2, "" + attacked + ": "+ prevHp);
	}

	private void init() {
		try {
			// look at the other three angles
			for (int i = 0; i < 4; ++i) {
				// Rotate twice Right for a 90 degrees turn
				controllers.motor.setDirection(controllers.myRC.getDirection()
						.rotateRight().rotateRight());
				yield();
				controllers.updateComponents();
			}

			// go build recyclers on the other two initial mines
			if (!mineLocations.isEmpty()) {
				buildBuildingAtLoc((MapLocation) mineLocations.toArray()[0],UnitType.RECYCLER);
			}
			yield();
			if (!mineLocations.isEmpty())
				buildBuildingAtLoc((MapLocation) mineLocations.toArray()[0],UnitType.RECYCLER);
			yield();

			controllers.updateComponents();
			computeEnemyBaseLocation();
			msgHandler.queueMessage(new GridMapMessage(borders, homeLocation, gridMap));

		} catch (Exception e) {
			System.out.println("caught exception:");
			e.printStackTrace();
		}
	}

	private void updateLocationSets() {
		controllers.senseAll();
		mineLocations.addAll(controllers.emptyMines);
		
		for (MapLocation mineloc : controllers.allyMines) {
			mineLocations.remove(mineloc);
			recyclerLocations.add(mineloc);
			
			try {
				GameObject object = controllers.sensor.senseObjectAtLocation(mineloc, RobotLevel.ON_GROUND);
				if (!controllers.sensor.senseRobotInfo((Robot) object).on) {
					recyclerLocations.remove(mineloc);
				}
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}
		
		mineLocations.removeAll(controllers.enemyMines);

	}

	private void checkEmptyRecyclers() throws GameActionException {
		for (MapLocation recyclerLoc : recyclerLocations) {
			if (controllers.myRC.getLocation().distanceSquaredTo(recyclerLoc) <= 9 && !builtLocations.contains(recyclerLoc)) {
				msgHandler.queueMessage(new BuildingLocationInquiryMessage(recyclerLoc));
				break;
			}
		}
	}

	private boolean buildRecyclers() throws GameActionException {
		if (nearestMine == null)
			nearestMine = new MapLocation(0, 0);

		// find a eligible mine
		MapLocation currentLoc = controllers.myRC.getLocation();
		List<MapLocation> toBeRemoved = new ArrayList<MapLocation>();
		for (MapLocation mineLoc : mineLocations) {
			// it needs to be empty
			if (controllers.sensor.canSenseSquare(mineLoc)) {
				GameObject object = controllers.sensor.senseObjectAtLocation(mineLoc, RobotLevel.ON_GROUND);
				if (object != null) {
					toBeRemoved.add(mineLoc);
					continue;
				}
			}
			if (currentLoc.distanceSquaredTo(mineLoc) < currentLoc.distanceSquaredTo(nearestMine))
				nearestMine = mineLoc;
		}

		// remove mines with buildings on them
		mineLocations.removeAll(toBeRemoved);

		if (nearestMine.x == 0) {
			nearestMine = null;
			return false;
		}

		// if there is a eligible site
		if (currentLoc.distanceSquaredTo(nearestMine) <= 2) {
			buildBuildingAtLoc(nearestMine, UnitType.RECYCLER);
			nearestMine = null;
		}
		return false;
	}

	private boolean buildBuildingAtLoc(MapLocation buildLoc, UnitType type) throws GameActionException {
		// if already standing on the building site
		if (controllers.myRC.getLocation().equals(buildLoc)) {
			while (controllers.motor.isActive())
				yield();

			// move forward or backward if possible
			if (controllers.motor.canMove(controllers.myRC.getDirection())) {
				controllers.motor.moveForward();
			} else if (controllers.motor.canMove(controllers.myRC.getDirection().opposite())) {
				controllers.motor.moveBackward();
			} else {
				return false;
			}
			yield();
		}

		// move to the adjacent of the building site
		navigator.setDestination(buildLoc);
		while (!controllers.myRC.getLocation().isAdjacentTo(buildLoc)) {
//			controllers.myRC.setIndicatorString(2, controllers.myRC.getLocation() + "," + buildLoc);
			if (!controllers.motor.isActive()) {
				Direction nextDir = navigator.getNextDir(0);

//				controllers.myRC.setIndicatorString(2, nextDir.toString());
				
				if (nextDir == Direction.OMNI)
					break;
				if (controllers.myRC.getDirection() == nextDir) {
					if (controllers.motor.canMove(nextDir))
						controllers.motor.moveForward();
				} else {
					controllers.motor.setDirection(nextDir);
				}
			}

			yield();
		}

		// face the building site
		Direction buildDir = controllers.myRC.getLocation().directionTo(buildLoc);
		if (controllers.myRC.getDirection() != buildDir) {
			while (controllers.motor.isActive())
				yield();

			controllers.motor.setDirection(buildDir);
			yield();
		}

		// if everything looks okay, construct
		while (!buildingSystem.constructUnit(buildLoc, type)) {
			if (controllers.sensor.senseObjectAtLocation(buildLoc,type.chassis.level) != null)
				return false;
			yield();
		}
		
		builtIdleRound = 30;
		msgHandler.clearOutQueue();
		msgHandler.queueMessage(new ConstructionCompleteMessage(buildLoc, type));
		msgHandler.queueMessage(new GridMapMessage(borders, homeLocation, gridMap));
			
		return true;
	}

	private void navigate() throws GameActionException {
		
		
		if (Clock.getRoundNum() - attackedRound <= 50) {
			navigateToDestination(homeLocation, 9);
		} else if (nearestMine != null) {
			navigateToDestination(nearestMine, 2);
		} else if (enemyBaseLoc[0] != null) {
			if(navigateToDestination(enemyBaseLoc[0], 9)){
				enemyBaseLoc[0] = null;
			}
		} else if (enemyBaseLoc[1] != null) {
			if(navigateToDestination(enemyBaseLoc[1], 9)){
				enemyBaseLoc[1] = null;
			}
		} else if (enemyBaseLoc[2] != null){
			if(navigateToDestination(enemyBaseLoc[2], 9)){
				enemyBaseLoc[2] = null;
			}
		} else {
			roachNavigate();
		}
	}
	
	
	private boolean checkFourConsecutiveEmpties () throws GameActionException {
		SensorController sensor = controllers.sensor;
		RobotController rc = controllers.myRC;
		MapLocation currentLoc = rc.getLocation();
		Direction dir = rc.getDirection();
		if (dir.isDiagonal()) {
			if (rc.senseTerrainTile(currentLoc.add(dir)) != TerrainTile.LAND || sensor.senseObjectAtLocation(currentLoc.add(dir), RobotLevel.ON_GROUND) != null)
				return false;
			/*
			 * 2 3 4
			 * 1 E 5
			 * c 7 6
			 * check if there's 4 consecutiveEmpties
			 */
			int consecutiveCounter = 0;
			Direction oppDir = dir.opposite();
			MapLocation emptyLoc = currentLoc.add(dir);
			for (int i = 0; i < 7; i++) {
				oppDir = oppDir.rotateRight();
				if (rc.senseTerrainTile(emptyLoc.add(oppDir)) != TerrainTile.LAND || sensor.senseObjectAtLocation(emptyLoc.add(oppDir), RobotLevel.ON_GROUND) != null)
					consecutiveCounter = 0;
				else
					consecutiveCounter++;
				if (consecutiveCounter == 4)
					return true;
			}
			return false;
			
		} else {
			/*
			 * check
			 * 
			 * E E E
			 * - E -
			 * - c -
			 * 
			 * then
			 * 
			 * E - -
			 * - c -
			 * 
			 * or
			 * 
			 * - - E
			 * - c -
			 */
			MapLocation [] locCheckList1 = {currentLoc.add(dir), currentLoc.add(dir,2), currentLoc.add(dir).add(dir.rotateLeft()),currentLoc.add(dir).add(dir.rotateRight())};
			for (MapLocation loc: locCheckList1) {
				if (rc.senseTerrainTile(loc) != TerrainTile.LAND || sensor.senseObjectAtLocation(loc, RobotLevel.ON_GROUND) != null)
					return false;
			}
			MapLocation [] locCheckList2 = {currentLoc.add(dir.rotateLeft()),currentLoc.add(dir.rotateRight())};
			for (MapLocation loc: locCheckList2) {
				if (rc.senseTerrainTile(loc) == TerrainTile.LAND && sensor.senseObjectAtLocation(loc, RobotLevel.ON_GROUND) == null)
					return true;
			}
			return false;
		}
	}
	
	
	@Override
	protected void processMessages() throws GameActionException {
		// Check messages
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			switch (msgHandler.getMessageType(msg)) {
			case NOT_ENOUGH_SPACE_MESSAGE: {
				while (!checkFourConsecutiveEmpties()) {
					navigate();
					yield();
				}
				MapLocation buildLoc = controllers.myRC.getLocation().add(controllers.myRC.getDirection());
//	Move forward twice
				while(controllers.motor.isActive())
					yield();
				controllers.motor.moveForward();
				while(controllers.motor.isActive())
					yield();
				controllers.motor.moveForward();

				
				while(controllers.builder.isActive())
					yield();
				while (!buildBuildingAtLoc(buildLoc, UnitType.RECYCLER))
					yield();
				for (int i = 0; i < 20; i++)
					yield();
				msgHandler.queueMessage(new BuildingLocationInquiryMessage(buildLoc));
				builtIdleRound = 50;
				break;
			}
			
			case BUILDING_LOCATION_RESPONSE_MESSAGE: {
				BuildingLocationResponseMessage handler = new BuildingLocationResponseMessage(msg);
				
//				controllers.myRC.setIndicatorString(0, "Type" +handler.getUnitType() + " " +  Clock.getRoundNum());
//				controllers.myRC.setIndicatorString(1, "current location:" + controllers.myRC.getLocation());
//				controllers.myRC.setIndicatorString(2, "build loc:" + handler.getBuildableLocation());
				
				// see if the message is intended for it
				if (handler.getConstructorID() != controllers.myRC.getRobot().getID())
					break;

				// if it is not built
				if (builtLocations.contains(handler.getSourceLocation()))
					break;

				UnitType type = handler.getUnitType();
				if (type == null) { // there is nothing to build
					builtLocations.add(handler.getSourceLocation());
					builtIdleRound = 0;
				} else if (handler.getBuildableLocation() != null) {
					MapLocation buildLoc = handler.getBuildableLocation();
					if (buildBuildingAtLoc(buildLoc, type)) {
						builtIdleRound = 50;
						msgHandler.queueMessage(new BuildingLocationInquiryMessage(handler.getSourceLocation()));
						yield();
					}
				}

				break;
			}

			case GRID_MAP_MESSAGE: {
				GridMapMessage handler = new GridMapMessage(msg);
				// update the borders
				int[] newBorders = handler.getBorders();

				for (int i = 0; i < 4; ++i) {
					if (newBorders[i] != -1) {
						if (borders[i] != newBorders[i]) {
							borders[i] = newBorders[i];
						}
					}
				}

				homeLocation = handler.getHomeLocation();
				computeEnemyBaseLocation();
				gridMap.merge(homeLocation, handler.getBorders(), handler.getInternalRecords());

				break;
			}
			
			case MINE_RESPONSE_MESSAGE: {
				MineResponseMessage handler = new MineResponseMessage(msg);
				
				if (handler.getConstructorID() == controllers.myRC.getRobot().getID()) {
					mineLocations.addAll(handler.getMineLocations());
				}
				break;
			}
			
			}
		}
	}
}
