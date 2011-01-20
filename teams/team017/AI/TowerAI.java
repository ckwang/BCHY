package team017.AI;

import team017.combat.CombatSystem;
import team017.util.Util;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.WeaponController;

/*
 * assume first weapon has longest range
 */
public class TowerAI extends AI {

	private CombatSystem combat;
	private MapLocation myloc;
	private int maxRange = 0;
	private boolean hasWeapon = false;

	// private boolean attacked = false;
	// private double prevHp = 0;

	public TowerAI(RobotController rc) {
		super(rc);
		combat = new CombatSystem(controllers);
		myloc = controllers.myRC.getLocation();
	}

	@Override
	public void proceed() {
		RobotInfo target;
		proceed: while (true) {
			if (controllers.weapons.size() > 0) {
				while (controllers.mobileEnemyNum() > 0) {
					Util.sortLocation(controllers.enemyMobile, myloc);
					target = controllers.enemyMobile.get(0);
					if (myloc.distanceSquaredTo(target.location) > maxRange)
						break;
					attack(target);
				}
				while (controllers.immobileEnemyNum() > 0) {
					Util.sortLocation(controllers.enemyImmobile, myloc);
					target = controllers.enemyImmobile.get(0);
					if (myloc.distanceSquaredTo(target.location) > maxRange)
						break;
					attack(target);
					if (controllers.mobileEnemyNum() > 0)
						continue proceed;
				}
				if (controllers.debrisNum() > 0) {
					Util.sortLocation(controllers.debris, myloc);
					target = controllers.debris.get(0);
					combat.shoot(target);

				}
			}

			if (!controllers.motor.isActive()) {
				Direction toTurn = controllers.myRC.getDirection();
				switch (Clock.getBytecodeNum() % 3) {
				case 0:
					toTurn = toTurn.opposite();
					break;
				case 1:
					toTurn = toTurn.rotateLeft().rotateLeft();
					break;
				case 2:
					toTurn = toTurn.rotateRight().rotateRight();
				}
				try {
					controllers.motor.setDirection(toTurn);
				} catch (GameActionException e) {
					e.printStackTrace();
				}
			}
			yield();

			// if (enemyNum > 0) {
			// msgHandler.clearOutQueue();
			// msgHandler.queueMessage(new
			// EnemyInformationMessage(controllers.enemyMobile));
			// msgHandler.process();
			// }
		}
	}

	public void yield() {
		super.yield();
		// attacked = controllers.myRC.getHitpoints() < prevHp;
		// prevHp = controllers.myRC.getHitpoints();
		controllers.updateComponents();
		if (!hasWeapon) {
			if (controllers.weapons.size() > 0) {
				combat.primary = controllers.weapons.get(0);
				hasWeapon = true;
				maxRange = combat.primary.type().range;
			}
		}

		for (WeaponController w : controllers.weapons) {
			combat.primary = controllers.weapons.get(0);
			if (w.type().range > maxRange)
				maxRange = w.type().range;
		}
		controllers.senseAll();
		combat.heal();
		processMessages();
	}

	public boolean attack(RobotInfo target) {
		Direction edir = myloc.directionTo(target.location);
		// controllers.myRC.setIndicatorString(0, target.robot.getID() + "");
		if (!combat.primary.withinRange(target.location)) {
			while (!combat.setDirection(edir)) {
				combat.shoot(target);
				yield();
			}
		}
		int i;
		for (i = 0; i < 3 && !combat.shoot(target);) {
			// controllers.myRC.setIndicatorString(1, "i: "+i);
			try {
				target = controllers.sensor.senseRobotInfo(target.robot);
			} catch (GameActionException e) {
				++i;
				edir = myloc.directionTo(target.location);
				combat.setDirection(edir);
				continue;
			}
			yield();
		}
		// controllers.myRC.setIndicatorString(0, "");
		if (i == 3)
			return false;
		return true;
	}

	protected void processMessages() {
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			switch (msgHandler.getMessageType(msg)) {
			// case ENEMY_INFORMATION_MESSAGE:
			// if (enemyNum == 0) {
			// EnemyInformationMessage ehandler = new
			// EnemyInformationMessage(msg);
			// if (Clock.getRoundNum() - ehandler.getRoundNum() <= 1) {
			// for (UnitInfo e: ehandler.getInfos()) {
			// controllers.enemyMobile.remove(e);
			// controllers.enemyMobile.add(e);
			// }
			// }
			// }
			// break;
			}
		}
	}
}
