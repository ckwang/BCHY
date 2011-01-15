package team017.AI;

import java.util.ArrayList;
import java.util.List;

import team017.combat.CombatSystem;
import team017.message.BorderMessage;
import team017.message.BuildingLocationInquiryMessage;
import team017.message.EnemyInformationMessage;
import team017.message.FollowMeMessage;
import team017.util.EnemyInfo;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.MovementController;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;
import battlecode.common.TerrainTile;
import battlecode.common.WeaponController;

public class SoldierAI extends AI {

	private CombatSystem combat;
	private RobotController rc = controllers.myRC;
	private MovementController motor = controllers.motor;
	private SensorController sensor = controllers.sensor;
	private List<WeaponController> weapons = controllers.weapons;
	private MapLocation leaderLoc = null;
	private double prevHp = 50;
	private boolean attacked = false;
	private int attackRoundCounter = 0;
	private int leaderMessageRoundCounter = 0;
	int enemyNum = 0;
	
	private boolean reachedFirstBase = false;
	private boolean hasLeader = false;
	
	private Direction followDir;
	private Direction previousDir = null;
	private MapLocation scoutLocation;
	public SoldierAI(RobotController rc) {
		super(rc);
		combat = new CombatSystem(controllers);
		scoutLocation = getNextScoutLoc();
		navigator.updateMap();
	}

	public void proceed() {				
		
		while (true) {
			
			controllers.myRC.setIndicatorString(0, controllers.myRC.getLocation().toString() );
			controllers.myRC.setIndicatorString(1, "");
			controllers.myRC.setIndicatorString(2, "");

			combat.senseNearby();
			try {processMessages();}
			catch (GameActionException e1) {}

			enemyNum = combat.enemyInfosSet.size();
			MapLocation nextLoc = combat.attack();
			
			if (nextLoc != null && !controllers.motor.isActive()) {
				navigator.setDestination(nextLoc);
				try {
					Direction nextDir = navigator.getNextDir(0);
					if (rc.getDirection() == nextDir) {
						if (controllers.motor.canMove(controllers.myRC.getDirection()))
							controllers.motor.moveForward();
					} else if (nextDir == Direction.OMNI) {
						if (controllers.motor.canMove(controllers.myRC.getDirection().opposite()))
							controllers.motor.moveBackward();
					} else { 
						controllers.motor.setDirection(nextDir);
					}
				} catch (GameActionException e) {
					e.printStackTrace();
				}
			}
			
				// Set a int so that it doesn't call navigate() after seeing an enemy
				
//				if (controllers.comm != null){
////					String s = "";
////					for (int i = 0; i < combat.enemyInfos.size(); ++i)
////						s += combat.enemyInfos.get(i).location + " ";
////					controllers.myRC.setIndicatorString(1,"Broadcast:" + s);
//					if (enemyNum > 0) {
//
//					msgHandler.clearOutQueue();
//					msgHandler.queueMessage(new EnemyInformationMessage(combat.enemyInfosSet));
//					msgHandler.process();
//					} else if (!hasLeader) {
//						
//						if (previousDir != controllers.myRC.getDirection() || Clock.getRoundNum() % 3 == 0) {
//						
//						msgHandler.queueMessage(new FollowMeMessage(controllers.myRC.getDirection()));
//						controllers.myRC.setIndicatorString(1, "Follow Me Message Sent");
//						}
//					}
//					previousDir = controllers.myRC.getDirection();
//				}
				
			
			
			if (enemyNum > 0) 
				attackRoundCounter = 0;
			else 
				attackRoundCounter++;
			try {
				if (leaderMessageRoundCounter < 4) {
					if (!motor.isActive()) {
						MapLocation estimatedLeaderLoc = leaderLoc.add(followDir, leaderMessageRoundCounter);

						// Move in the same direction as the leader if being near enough to the leader
						if (rc.getLocation().distanceSquaredTo(estimatedLeaderLoc) <= 9) {
							if (motor.canMove(followDir)) {
								if (rc.getDirection() == followDir) {
									motor.moveForward();
								} else {
									motor.setDirection(followDir);
								}
							// Move to the front of the leader if cant move in the same direction	
							} else {
								navigator.setDestination(leaderLoc.add(followDir, 3));
								Direction nextDir = navigator.getNextDir(2);
								if (rc.getDirection() == nextDir)
									motor.moveForward();
								else
									motor.setDirection(nextDir);
							}
						}
					}
					leaderMessageRoundCounter++;
				} else {
					hasLeader = false;
				}
			} catch (Exception e) { }


			
			if (attackRoundCounter > 2 && leaderMessageRoundCounter > 3) {
				try {navigate();}
//				controllers.myRC.setIndicatorString(2, "navigate");}
				catch (GameActionException e) {}
				
			}
			
			sense_border();
			yield();

		}
	}

	public void yield() {
		super.yield();
		combat.reset();
		navigator.updateMap();
//		controllers.myRC.setIndicatorString(0, controllers.myRC.getLocation() + "");

		if (controllers.myRC.getHitpoints() < prevHp) {
			prevHp = controllers.myRC.getHitpoints();
			attacked = true;
		} else {
			attacked = false;
		}
	}
	
	@Override
	protected void processMessages() throws GameActionException{
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			switch (msgHandler.getMessageType(msg)) {
			case BORDER:
				BorderMessage handler = new BorderMessage(msg);
				// update the borders
				int[] newBorders = handler.getBorderDirection();

				for (int i = 0; i < 4; ++i) {
					if (newBorders[i] != -1) {
						if (borders[i] != newBorders[i]) {
							borders[i] = newBorders[i];
						}
					}
				}
				homeLocation = handler.getHomeLocation();
				computeEnemyBaseLocation();
//				controllers.myRC.setIndicatorString(1, "Border Message got" + handler.getRoundNum());
				break;
				
//			case FOLLOW_ME_MESSAGE:
//					// System.out.println("follow me message");
//					FollowMeMessage fhandler = new FollowMeMessage(msg);
//					
//					// If 2 commanders meet, follow the one with a smaller ID
//					if (controllers.comm != null && fhandler.getSourceID() < rc.getRobot().getID())
//						break;
//
//					leaderMessageRoundCounter = 0;
//					hasLeader = true;
//					MapLocation loc = fhandler.getSourceLocation();
//					followDir = fhandler.getFollowDirection();
//					if (leaderLoc != null) {
//						int curdist = rc.getLocation().distanceSquaredTo(leaderLoc);
//						int newdist = rc.getLocation().distanceSquaredTo(loc);
//						if (newdist < curdist)
//							leaderLoc = loc;
//					} else
//						leaderLoc = loc;
////					navigator.setDestination(leaderLoc);
//					controllers.myRC.setIndicatorString(1, "Follow me message got" + fhandler.getRoundNum());					
//				
//
//				break;
				
			case ENEMY_INFORMATION_MESSAGE:
				if (enemyNum == 0) {
					EnemyInformationMessage ehandler = new EnemyInformationMessage(msg);
					if (Clock.getRoundNum() - ehandler.getRoundNum() <= 1) {
						for (EnemyInfo e: ehandler.getInfos()) {
							combat.enemyInfosSet.remove(e);
							combat.enemyInfosSet.add(e);
						}	
//						String s = "";
//						for (int i = 0; i < combat.enemyInfosInbox.size(); ++i)
//							s += combat.enemyInfosInbox.get(i).location + " ";
//						controllers.myRC.setIndicatorString(1, "Mess:" + s + ehandler.getRoundNum());
					}
				}
				break;				
			}
		}
	}
	
	public void roachNavigate() throws GameActionException {
		int round = Clock.getRoundNum();
		switch (round % 3) {
		case 0:
			motor.setDirection(controllers.myRC.getDirection().rotateLeft().rotateLeft());
			break;
		case 1:
			super.roachNavigate();
			break;
		case 2:
			super.roachNavigate();
			break;
		case 3:
			motor.setDirection(controllers.myRC.getDirection().rotateRight().rotateRight());
			break;
		}
		return;
	}
	
	private MapLocation getNextScoutLoc() {
		TerrainTile tile, checkTile;
		Direction faceDir = controllers.myRC.getDirection();
		final int EXPLORATION_SIZE = 10;
		
		// add some randomness to the initial direction
		int n = Clock.getRoundNum() % 8;
		for (int i = 0; i < n; i++) {
			faceDir = faceDir.rotateRight();
		}
		
		MapLocation currentLoc = controllers.myRC.getLocation();
		int multiple = 1;
		while( multiple < 5 ){
		
			for (int i = 0; i < 8; i++){
				MapLocation projectedLoc = currentLoc.add(faceDir, EXPLORATION_SIZE*multiple);
				if ( (borders[1] == -1 || projectedLoc.x < borders[1]) &&
					 (borders[3] == -1 || projectedLoc.x > borders[3]) &&
					 (borders[2] == -1 || projectedLoc.y < borders[2]) &&
					 (borders[0] == -1 || projectedLoc.y > borders[0])) {
					
				
					tile = controllers.myRC.senseTerrainTile(projectedLoc);
					checkTile = controllers.myRC.senseTerrainTile(currentLoc.add(faceDir, 3));
					if (tile == null && checkTile != TerrainTile.OFF_MAP)
						return projectedLoc;
				}
				faceDir = faceDir.rotateRight();
			}
			
			multiple++;
		}
		
		return currentLoc.add(faceDir.opposite(), EXPLORATION_SIZE*multiple);
	}

	private void navigate() throws GameActionException {
		
		Direction nextDir = Direction.OMNI;
		if (enemyBaseLoc[0] != null) {
			navigator.setDestination(enemyBaseLoc[0]);
			nextDir = navigator.getNextDir(2);
			if (nextDir == Direction.OMNI){
				enemyBaseLoc[0] = null;
				navigator.setDestination(enemyBaseLoc[1]);
				nextDir = navigator.getNextDir(2);
			}
			controllers.myRC.setIndicatorString(0, controllers.myRC.getLocation() + ", e0: " + enemyBaseLoc[0]);
		}
		else if (enemyBaseLoc[1] != null){
			navigator.setDestination(enemyBaseLoc[1]);
			nextDir = navigator.getNextDir(2);
			if (nextDir == Direction.OMNI){
				enemyBaseLoc[1] = null;
				navigator.setDestination(enemyBaseLoc[2]);
				nextDir = navigator.getNextDir(2);
			}
			controllers.myRC.setIndicatorString(0, controllers.myRC.getLocation() + ", e1: " + enemyBaseLoc[1]);
		}
		else if (enemyBaseLoc[2] != null){
			navigator.setDestination(enemyBaseLoc[2]);
			nextDir = navigator.getNextDir(2);
			if (nextDir == Direction.OMNI){
				enemyBaseLoc[2] = null;
			}
			controllers.myRC.setIndicatorString(0, controllers.myRC.getLocation() + ", e2: " + enemyBaseLoc[2]);
		}
		else {
			
			TerrainTile checkTile = 
				controllers.myRC.senseTerrainTile(
						controllers.myRC.getLocation().add(controllers.myRC.getDirection(), 3));
			
			if (checkTile == TerrainTile.OFF_MAP)
				scoutLocation = getNextScoutLoc();
			
			navigator.setDestination(scoutLocation);
			nextDir = navigator.getNextDir(9);
			
			if (nextDir == Direction.OMNI){
				scoutLocation = getNextScoutLoc();
				navigator.setDestination(scoutLocation);
				nextDir = navigator.getNextDir(9);
			}
			controllers.myRC.setIndicatorString(0, controllers.myRC.getLocation() + ", scout: " + scoutLocation);
		}
		
		
		
		
		if (nextDir != Direction.OMNI) {
			if (!controllers.motor.isActive() ) {
				if (controllers.myRC.getDirection() == nextDir) {
					if (controllers.motor.canMove(nextDir)) {
						controllers.motor.moveForward();
					}
				} else {
					controllers.motor.setDirection(nextDir);
				}
			}
		}
		else {
//			controllers.myRC.setIndicatorString(1,"roachNavigate");
			// do nothing;
			if (!controllers.motor.isActive() )
				roachNavigate();
		}
	}
}
