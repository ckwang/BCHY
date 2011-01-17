package team017.AI;

import team017.combat.CombatSystem;
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
				if (controllers.enemyInfosSet.size() > 0) {
					msgHandler.clearOutQueue();
					msgHandler.queueMessage(new EnemyInformationMessage(controllers.enemyInfosSet));
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
						for (EnemyInfo e: ehandler.getInfos()) {
							controllers.enemyInfosSet.remove(e);
							controllers.enemyInfosSet.add(e);
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
		Direction nextDir = Direction.OMNI;
		if (enemyBaseLoc[0] != null) {
			navigator.setDestination(enemyBaseLoc[0]);
			nextDir = navigator.getNextDir(9);
			if (nextDir == Direction.OMNI) {
				enemyBaseLoc[0] = null;
				navigator.setDestination(enemyBaseLoc[1]);
				nextDir = navigator.getNextDir(9);
			}
//			controllers.myRC.setIndicatorString(0, controllers.myRC.getLocation() + ", e0: " + enemyBaseLoc[0]);
		} else if (enemyBaseLoc[1] != null) {
			navigator.setDestination(enemyBaseLoc[1]);
			nextDir = navigator.getNextDir(9);
			if (nextDir == Direction.OMNI) {
				enemyBaseLoc[1] = null;
				navigator.setDestination(enemyBaseLoc[2]);
				nextDir = navigator.getNextDir(9);
			}
//			controllers.myRC.setIndicatorString(0, controllers.myRC.getLocation() + ", e1: " + enemyBaseLoc[1]);
		} else if (enemyBaseLoc[2] != null) {
			navigator.setDestination(enemyBaseLoc[2]);
			nextDir = navigator.getNextDir(9);
			if (nextDir == Direction.OMNI) {
				enemyBaseLoc[2] = null;
			}
//			controllers.myRC.setIndicatorString(0, controllers.myRC.getLocation() + ", e2: " + enemyBaseLoc[2]);
		} else {
			navigator.setDestination(gridMap.getScoutLocation());
			nextDir = navigator.getNextDir(4);
		}
		if (nextDir != Direction.OMNI) {
			if (!controllers.motor.isActive()) {
				if (controllers.myRC.getDirection() == nextDir) {
					if (controllers.motor.canMove(nextDir)) {
						controllers.motor.moveForward();

						MapLocation currentLoc = controllers.myRC.getLocation();
						if (!gridMap.isScouted(currentLoc)) {
							gridMap.setScouted(currentLoc);
							gridMap.updateScoutLocation(currentLoc);
						}
					}
				} else {
					controllers.motor.setDirection(nextDir);
				}
			}
		} else {
			if (!controllers.motor.isActive())
				roachNavigate();
		}
	}
}
