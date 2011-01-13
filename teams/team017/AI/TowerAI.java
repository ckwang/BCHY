package team017.AI;

import team017.combat.CombatSystem;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
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
				
				if (combat.enemyInfosSet.size() == 0 && !controllers.motor.isActive()) {
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
				controllers.myRC.setIndicatorString(0, combat.enemyNum() +"");
				
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void yield() {
		super.yield();
		combat.reset();
	}
	
	@Override
	protected void processMessages() throws GameActionException {
		// TODO Auto-generated method stub
		
	}

}
