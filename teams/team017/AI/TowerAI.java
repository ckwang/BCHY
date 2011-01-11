package team017.AI;

import team017.combat.CombatSystem;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class TowerAI extends AI {

	private CombatSystem combat;
	
	public TowerAI(RobotController rc) {
		super(rc);
		combat = new CombatSystem(controllers);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void proceed() {
		while(true) {
			try {
				combat.senseNearby();
				
				if (combat.enemyNum() == 0 && !controllers.motor.isActive()) {
					controllers.motor.setDirection(controllers.myRC.getDirection().rotateRight().rotateRight().rotateRight().rotateRight());
					yield();
				} else {
					combat.chaseTarget();
					combat.attack();
					yield();
				}
				controllers.updateComponents();
				controllers.myRC.setIndicatorString(0, combat.enemyNum() +"");
				
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void processMessages() throws GameActionException {
		// TODO Auto-generated method stub
		
	}

}
