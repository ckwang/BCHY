package team017.AI;

import java.util.List;

import team017.combat.CombatSystem;
import team017.message.BorderMessage;
import team017.message.EnemyInformationMessage;
import team017.message.FollowMeMessage;
import team017.util.EnemyInfo;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.JumpController;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.MovementController;
import battlecode.common.RobotController;
import battlecode.common.SensorController;
import battlecode.common.WeaponController;

public class SoldierAI extends AI {

	private CombatSystem combat;
	private RobotController rc = controllers.myRC;
	private MovementController motor = controllers.motor;
	private SensorController sensor = controllers.sensor;
	private List<WeaponController> weapons = controllers.weapons;
	private JumpController jump = controllers.jump;
	
	private MapLocation leaderLoc = null;
	private MapLocation destionation;

	private double prevHp = 50;
	private boolean attacked = false;
	private int attackRoundCounter = 0;
	private int leaderMessageRoundCounter = 0;
	int enemyNum = 0;

	private boolean reachedFirstBase = false;
	private boolean hasLeader = false;

	private Direction followDir;
	private Direction previousDir = null;

	public SoldierAI(RobotController rc) {
		super(rc);
		combat = new CombatSystem(controllers);
		navigator.updateMap();
	}

	public void proceed() {
		while (true) {

			combat.senseNearby();
			enemyNum = combat.enemyInfosSet.size();

			try {processMessages();}
			catch (Exception e1) {}

			enemyNum = combat.enemyInfosSet.size();
			MapLocation nextLoc = combat.attack();
			
			if (combat.enemyInfosSet.size() == 0 && combat.debrisLoc.size() != 0)
				try {combat.attackDebris();}
				catch (Exception e1) {e1.printStackTrace();}

			if (nextLoc != null && !controllers.motor.isActive()) {
				navigator.setDestination(nextLoc);
				try {
					Direction nextDir = navigator.getNextDir(0);
					if (rc.getDirection() == nextDir) {
						if (controllers.motor.canMove(controllers.myRC
								.getDirection()))
							controllers.motor.moveForward();
					} else if (nextDir == Direction.OMNI) {
						if (controllers.motor.canMove(controllers.myRC
								.getDirection().opposite()))
							controllers.motor.moveBackward();
					} else {
						controllers.motor.setDirection(nextDir);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			broadcast();

			if (enemyNum > 0)
				attackRoundCounter = 0;
			else
				attackRoundCounter++;
			try {
				if (leaderMessageRoundCounter < 4) {
					if (!motor.isActive()) {
						MapLocation estimatedLeaderLoc = leaderLoc.add(
								followDir, leaderMessageRoundCounter);
						// Move in the same direction as the leader if being
						// near enough to the leader
						if (rc.getLocation().distanceSquaredTo(
								estimatedLeaderLoc) <= 9) {
							if (motor.canMove(followDir)) {
								if (rc.getDirection() == followDir) {
									motor.moveForward();
								} else {
									motor.setDirection(followDir);
								}
								// Move to the front of the leader if cant move
								// in the same direction
							} else {
								navigator.setDestination(leaderLoc.add(
										followDir, 3));
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
			} catch (Exception e) {
			}

			if (attackRoundCounter > 2 && leaderMessageRoundCounter > 3) {
				try {navigate();}
				catch (Exception e) {}
			}
			senseBorder();
			yield();

		}
	}

	public void yield() {
		previousDir = controllers.myRC.getDirection();
		super.yield();
		combat.reset();
		navigator.updateMap();
		if (controllers.myRC.getHitpoints() < prevHp) {
			prevHp = controllers.myRC.getHitpoints();
			attacked = true;
		} else {
			attacked = false;
		}
	}

	public void broadcast() {
		if (controllers.comm == null)
			return;
		if (enemyNum > 0) {
			msgHandler.clearOutQueue();
			msgHandler.queueMessage(new EnemyInformationMessage(
					combat.enemyInfosSet));
			msgHandler.process();
		} else if (!hasLeader) {
			if (previousDir != controllers.myRC.getDirection()
					|| Clock.getRoundNum() % 3 == 0) {
				msgHandler.queueMessage(new FollowMeMessage(
						controllers.myRC.getDirection(),
						controllers.comm.type().range));
			}
		}
	}
	
	@Override
	protected void processMessages() throws GameActionException {
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
				FollowMeMessage fhandler = new FollowMeMessage(msg);
				/*
				 *  If 2 commanders meet, follow the one with a longer range of
				 * broadcast
				 * If the range is the same, follow the one with a smaller ID
				 */
				if (controllers.comm != null) {
					if (controllers.comm.type().range < fhandler.getCommRange())
						break;
					else if (controllers.comm.type().range == fhandler
							.getCommRange()
							&& fhandler.getSourceID() < rc.getRobot().getID())
						break;
				}
				leaderMessageRoundCounter = 0;
				hasLeader = true;
				MapLocation loc = fhandler.getSourceLocation();
				followDir = fhandler.getFollowDirection();
				if (leaderLoc != null) {
					int curdist = rc.getLocation().distanceSquaredTo(leaderLoc);
					int newdist = rc.getLocation().distanceSquaredTo(loc);
					if (newdist < curdist)
						leaderLoc = loc;
				} else
					leaderLoc = loc;
				break;

			case ENEMY_INFORMATION_MESSAGE:
				if (enemyNum == 0) {
					EnemyInformationMessage ehandler = new EnemyInformationMessage(
							msg);
					if (Clock.getRoundNum() - ehandler.getRoundNum() <= 1) {
						for (EnemyInfo e : ehandler.getInfos()) {
							combat.enemyInfosSet.remove(e);
							combat.enemyInfosSet.add(e);
						}
					}
				}
				break;
			}
		}
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
			controllers.myRC.setIndicatorString(0, controllers.myRC
					.getLocation()
					+ ", e0: " + enemyBaseLoc[0]);
		} else if (enemyBaseLoc[1] != null) {
			navigator.setDestination(enemyBaseLoc[1]);
			nextDir = navigator.getNextDir(9);
			if (nextDir == Direction.OMNI) {
				enemyBaseLoc[1] = null;
				navigator.setDestination(enemyBaseLoc[2]);
				nextDir = navigator.getNextDir(9);
			}
			controllers.myRC.setIndicatorString(0, controllers.myRC
					.getLocation()
					+ ", e1: " + enemyBaseLoc[1]);
		} else if (enemyBaseLoc[2] != null) {
			navigator.setDestination(enemyBaseLoc[2]);
			nextDir = navigator.getNextDir(9);
			if (nextDir == Direction.OMNI) {
				enemyBaseLoc[2] = null;
			}
			controllers.myRC.setIndicatorString(0, controllers.myRC
					.getLocation()
					+ ", e2: " + enemyBaseLoc[2]);
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
