package team017.AI;

import team017.combat.CombatSystem;
import team017.message.EnemyInformationMessage;
import team017.message.FollowMeMessage;
import team017.util.UnitInfo;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.Message;
import battlecode.common.MovementController;
import battlecode.common.RobotController;

public class CommanderAI extends AI {

	private CombatSystem combat;
	private Direction previousDir;
	private MovementController motor = controllers.motor;
	private RobotController rc = controllers.myRC;
	
	private boolean reachedFirstBase = false;

	
	public CommanderAI(RobotController rc) {
		super(rc);
		combat = new CombatSystem(controllers);

	}

	@Override
	public void proceed() {
		while (true) {
			try {
				
				if (controllers.sensor != null)
					combat.senseNearby();
				processMessages();
				
//				rc.setIndicatorString(0, combat.enemyInfosSet.size() + "");
				if (controllers.enemyMobile.size() > 0) {
					msgHandler.clearOutQueue();
					msgHandler.queueMessage(new EnemyInformationMessage(controllers.enemyMobile));
					msgHandler.process();
				} else {
					navigate();
					if (controllers.myRC.getDirection() != previousDir || Clock.getRoundNum() % 3 == 0) {
						msgHandler.queueMessage(new FollowMeMessage(controllers.myRC.getDirection(), controllers.comm.type().range));
					}
				}
				yield();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void processMessages() throws GameActionException {
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			switch (msgHandler.getMessageType(msg)) {
			case ENEMY_INFORMATION_MESSAGE:
					EnemyInformationMessage ehandler = new EnemyInformationMessage(msg);
					if (Clock.getRoundNum() - ehandler.getRoundNum() <= 1) {
						for (UnitInfo e: ehandler.getInfos()) {
							controllers.enemyMobile.remove(e);
							controllers.enemyMobile.add(e);
						}	
					}
				break;
			}
		}
	}

	public void yield() {
		previousDir = controllers.myRC.getDirection();
		super.yield();
		controllers.reset();
		navigator.updateMap();
	}

	private void navigate() throws GameActionException {
//		if (leaderLoc == null && enemyBaseLoc[0] != null) {
//			if (reachedFirstBase)
//				navigator.setDestination(enemyBaseLoc[controllers.myRC.getRobot().getID() % 2 + 1]);
//			else
//				navigator.setDestination(enemyBaseLoc[0]);
//		}
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
//			leaderLoc = null;
		} else {
//			leaderLoc = null;
			roachNavigate();
		}
	}
}
