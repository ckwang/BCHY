package team017.AI;

import team017.combat.CombatSystem;
import team017.util.Util;
import battlecode.common.GameActionException;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class TowerAI extends AI {

	private CombatSystem combat;
	private int enemyNum;
	public TowerAI(RobotController rc) {
		super(rc);
		combat = new CombatSystem(controllers);
	}

	@Override
	public void proceed() {
		while(true) {
			try {
				controllers.senseAll();
//				enemyNum = controllers.enemyMobile.size();

				processMessages();
				enemyNum = controllers.enemyMobile.size() + controllers.enemyImmobile.size();

				combat.heal();
				if (enemyNum == 0 && !controllers.motor.isActive()) {
					controllers.motor.setDirection(controllers.myRC.getDirection().opposite());
					yield();
				} else {
//					MapLocation nextLoc = combat.attack();
					if (controllers.weapons.size() > 0) {
						combat.w = controllers.weapons.get(0);
						attack();
					}
//					if (nextLoc != null) {
//						Direction nextDir = controllers.myRC.getLocation().directionTo(combat.attack());
//						if (nextDir != Direction.OMNI && !controllers.motor.isActive()) {
//							controllers.motor.setDirection(nextDir);
//						}
//					}
					yield();
				}
				
				
				controllers.updateComponents();
				if (enemyNum > 0) {
					msgHandler.clearOutQueue();
//					msgHandler.queueMessage(new EnemyInformationMessage(controllers.enemyMobile));
					msgHandler.process();
					}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

//	public void yield() {
//		super.yield();
//		controllers.reset();
//	}
	
	public void attack() {
		RobotInfo target;
		if (controllers.mobileEnemyNum() > 0) {
			Util.sortHp(controllers.enemyMobile);
			target = combat.getTarget();
			if (target == null) return;
			for (int i = 0; i < 3 && !combat.canAttack(target); ++i) {
				try {
					target = controllers.sensor.senseRobotInfo(target.robot);
				} catch (GameActionException e) {
					e.printStackTrace();
					return;
				}
				yield();
			}
			while (!combat.attackTarget(target)) {
				try {
					target = controllers.sensor.senseRobotInfo(target.robot);
				} catch (GameActionException e) {
					return;
				}
				yield();
			}
		}
		else if (controllers.immobileEnemyNum() > 0) {
			Util.sortHp(controllers.enemyImmobile);
			target = combat.getImmobile();
			if (target == null) return;
			for (int i = 0; i < 3 && !combat.canAttack(target); ++i) {
				if (i != 0) {
					try {
						target = controllers.sensor.senseRobotInfo(target.robot);
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
				}
				yield();
			}
			while (!combat.shoot(target)) {
				try {
					target = controllers.sensor.senseRobotInfo(target.robot);
				} catch (GameActionException e) {
					return;
				}
			}
			yield();
		}
	}
	
	@Override
	protected void processMessages() throws GameActionException {
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			switch (msgHandler.getMessageType(msg)) {
//			case ENEMY_INFORMATION_MESSAGE:
//				if (enemyNum == 0) {
//					EnemyInformationMessage ehandler = new EnemyInformationMessage(msg);
//					if (Clock.getRoundNum() - ehandler.getRoundNum() <= 1) {
//						for (UnitInfo e: ehandler.getInfos()) {
//							controllers.enemyMobile.remove(e);
//							controllers.enemyMobile.add(e);
//						}	
//					}
//				}
//				break;		
			}
		}	
	}
}
