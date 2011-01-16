package team017.AI;

import team017.combat.CombatSystem;
import team017.message.EnemyInformationMessage;
import team017.util.UnitInfo;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;

public class TowerAI extends AI {

	private CombatSystem combat;
	private int enemyNum;
	public TowerAI(RobotController rc) {
		super(rc);
		combat = new CombatSystem(controllers);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void proceed() {
		while(true) {
			try {
				controllers.senseNearby();
				enemyNum = controllers.enemyMobile.size();

				
				processMessages();
				enemyNum = controllers.enemyMobile.size();
				if (enemyNum == 0 && !controllers.motor.isActive()) {
					controllers.motor.setDirection(controllers.myRC.getDirection().opposite());
					yield();
				} else {
					MapLocation nextLoc = combat.attack();
					if (nextLoc != null) {
						Direction nextDir = controllers.myRC.getLocation().directionTo(combat.attack());
						if (nextDir != Direction.OMNI && !controllers.motor.isActive()) {
							controllers.motor.setDirection(nextDir);
						}
					}
					yield();
				}
				controllers.updateComponents();
				if (enemyNum > 0) {

					msgHandler.clearOutQueue();
					msgHandler.queueMessage(new EnemyInformationMessage(controllers.enemyMobile));
					msgHandler.process();
					}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void yield() {
		super.yield();
		controllers.reset();
	}
	
	@Override
	protected void processMessages() throws GameActionException {
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			switch (msgHandler.getMessageType(msg)) {
			case ENEMY_INFORMATION_MESSAGE:
				if (enemyNum == 0) {
					EnemyInformationMessage ehandler = new EnemyInformationMessage(msg);
					if (Clock.getRoundNum() - ehandler.getRoundNum() <= 1) {
						for (UnitInfo e: ehandler.getInfos()) {
							controllers.enemyMobile.remove(e);
							controllers.enemyMobile.add(e);
						}	
					}
				}
				break;		
			}
		}	
	}
}
