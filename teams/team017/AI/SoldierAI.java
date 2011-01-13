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
import battlecode.common.WeaponController;

public class SoldierAI extends AI {

	private CombatSystem combat;
	private RobotController rc = controllers.myRC;
	private MovementController motor = controllers.motor;
	private SensorController sensor = controllers.sensor;
	private List<WeaponController> weapons = controllers.weapons;
	private MapLocation leaderLoc;
	private double prevHp = 50;
	private boolean attacked = false;
	private int attackRoundCounter = 0;
	
	private boolean reachedFirstBase = false;

	private Direction followDir;

	public SoldierAI(RobotController rc) {
		super(rc);
		combat = new CombatSystem(controllers);
	}

	public void proceed() {				
		
		while (true) {
			
			try {processMessages();}
			catch (GameActionException e1) {}


			
			combat.senseNearby();
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
			
			int before = Clock.getBytecodeNum();
			if (controllers.comm != null){
				if (combat.enemyInfosSet.size() > 0) {
//					String s = "";
//					for (int i = 0; i < combat.enemyInfos.size(); ++i)
//						s += combat.enemyInfos.get(i).location + " ";
//					controllers.myRC.setIndicatorString(1,"Broadcast:" + s);
					msgHandler.clearOutQueue();
					msgHandler.queueMessage(new EnemyInformationMessage(combat.enemyInfosSet));
					msgHandler.process();
				} 
//				else {
//					if (Clock.getRoundNum() % 3 == 0) {
//						msgHandler.queueMessage(new FollowMeMessage(controllers.myRC.getDirection()));
//						controllers.myRC.setIndicatorString(1, "Follow Me Message Sent");
//					}
//				}
			}
			int after = Clock.getBytecodeNum();

			rc.setIndicatorString(0, "bytecode: " + (after - before));
			rc.setIndicatorString(1, "bytecode: " + (after - before));
			rc.setIndicatorString(2, "bytecode: " + (after - before));
			
			if (combat.enemyInfosSet.size() > 0) {
				attackRoundCounter = 2;
			} else if (attackRoundCounter > 0) {
				attackRoundCounter--;
			}
			
			
			
			if (attackRoundCounter == 0) {
				try {navigate();}
	//			controllers.myRC.setIndicatorString(2, "navigate");}
				catch (GameActionException e) {}
			}
			
			sense_border();
			yield();

		}
	}

	public void yield() {
		super.yield();
		combat.reset();
		controllers.myRC.setIndicatorString(0, controllers.myRC.getLocation() + "");

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
				controllers.myRC.setIndicatorString(1, "Border Message got" + handler.getRoundNum());
				break;
				
//			case FOLLOW_ME_MESSAGE:
//				// System.out.println("follow me message");
//				FollowMeMessage fhandler = new FollowMeMessage(msg);
//				MapLocation loc = fhandler.getSourceLocation();
//				followDir = fhandler.getFollowDirection();
//				if (leaderLoc != null) {
//					int curdist = rc.getLocation().distanceSquaredTo(leaderLoc);
//					int newdist = rc.getLocation().distanceSquaredTo(loc);
//					if (newdist < curdist)
//						leaderLoc = loc.add(followDir, 5);
//				} else
//					leaderLoc = loc.add(followDir, 5);
//				navigator.setDestination(leaderLoc);
//				controllers.myRC.setIndicatorString(1, "Follow me message got" + fhandler.getRoundNum());
//				break;
//				
			case ENEMY_INFORMATION_MESSAGE:
				EnemyInformationMessage ehandler = new EnemyInformationMessage(msg);
				if (Clock.getRoundNum() - ehandler.getRoundNum() <= 1) {
					for (EnemyInfo e: ehandler.getInfos()) {
						if (Clock.getRoundNum() - e.roundNum <= 1) {
							combat.enemyInfosSet.remove(e);
							combat.enemyInfosSet.add(e);
						}
					}	
//					String s = "";
//					for (int i = 0; i < combat.enemyInfosInbox.size(); ++i)
//						s += combat.enemyInfosInbox.get(i).location + " ";
//					controllers.myRC.setIndicatorString(1, "Mess:" + s + ehandler.getRoundNum());
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

	private void navigate() throws GameActionException {
		if (leaderLoc == null && enemyBaseLoc[0] != null) {
			if (reachedFirstBase)
				navigator.setDestination(enemyBaseLoc[controllers.myRC.getRobot().getID() % 2 + 1]);
			else
				navigator.setDestination(enemyBaseLoc[0]);
		}
		Direction nextDir = navigator.getNextDir(0);
		if (nextDir != Direction.OMNI) {
			if (!motor.isActive() && motor.canMove(nextDir)) {
				if (rc.getDirection() == nextDir) {
					motor.moveForward();
				} else {
					motor.setDirection(nextDir);
				}
			}
			if (enemyBaseLoc[0] != null && controllers.myRC.getLocation().distanceSquaredTo(enemyBaseLoc[0]) < 25)
				reachedFirstBase = true;
		} else if (enemyBaseLoc[0] != null) {
			if (reachedFirstBase)
				navigator.setDestination(enemyBaseLoc[controllers.myRC.getRobot().getID() % 2 + 1]);
			else
				navigator.setDestination(enemyBaseLoc[0]);

			leaderLoc = null;
		} else {
			leaderLoc = null;
			roachNavigate();
		}
	}

}
