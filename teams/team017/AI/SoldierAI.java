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
	
	public List<EnemyInfo> enemyInfoInbox = new ArrayList <EnemyInfo>();
	
	private boolean reachedFirstBase = false;

	private Direction followDir;

	public SoldierAI(RobotController rc) {
		super(rc);
		combat = new CombatSystem(controllers);
	}

	public void proceed() {
		
		outer:
		while (true) {
			
			try {processMessages();}
			catch (GameActionException e1) {}


			
			combat.senseNearby();
			combat.attack();
			if (controllers.comm != null){
				if (combat.enemyInfos.size() > 0) {
					String s = "";
					for (int i = 0; i < combat.enemyInfos.size(); ++i)
						s += combat.enemyInfos.get(i).location + " ";
					controllers.myRC.setIndicatorString(1, s);
					msgHandler.queueMessage(new EnemyInformationMessage(combat.enemyInfos));
				} else {
					controllers.myRC.setIndicatorString(1, "Follow Me Message Sent");
					msgHandler.queueMessage(new FollowMeMessage(controllers.myRC.getDirection()));
				}
			}

			try {navigate();}
//			controllers.myRC.setIndicatorString(2, "navigate");}
			catch (GameActionException e) {}
			
			sense_border();
			yield();

		}
	}

	public void yield() {
		combat.reset();
		super.yield();
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
				break;
				
			case FOLLOW_ME_MESSAGE:
				// System.out.println("follow me message");
				FollowMeMessage fhandler = new FollowMeMessage(msg);
				MapLocation loc = fhandler.getSourceLocation();
				followDir = fhandler.getFollowDirection();
				if (leaderLoc != null) {
					int curdist = rc.getLocation().distanceSquaredTo(leaderLoc);
					int newdist = rc.getLocation().distanceSquaredTo(loc);
					if (newdist < curdist)
						leaderLoc = loc.add(followDir, 5);
				} else
					leaderLoc = loc.add(followDir, 5);
				navigator.setDestination(leaderLoc);
				break;
				
			case ENEMY_INFORMATION_MESSAGE:
				EnemyInformationMessage ehandler = new EnemyInformationMessage(msg);
				if (ehandler.getRoundNum() == Clock.getRoundNum()) {
					for (EnemyInfo e: ehandler.getInfos()) {
						enemyInfoInbox.add(e);
					}	
				}
				
//				String s = "";
//				for (int i = 0; i < combat.enemyInfos.size(); ++i)
//					s += combat.enemyInfos.get(i).location + " ";
//				controllers.myRC.setIndicatorString(1, "1" + s);
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
			if (enemyBaseLoc[0] != null
					&& controllers.myRC.getLocation().distanceSquaredTo(
							enemyBaseLoc[0]) < 25)
				reachedFirstBase = true;
		} else if (enemyBaseLoc[0] != null) {
			if (reachedFirstBase)
				navigator.setDestination(enemyBaseLoc[controllers.myRC
						.getRobot().getID() % 2 + 1]);
			else
				navigator.setDestination(enemyBaseLoc[0]);

			leaderLoc = null;
		} else {
			leaderLoc = null;
			roachNavigate();
		}
	}

}
