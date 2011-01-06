package team017.AI;

import team017.construction.UnitType;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class FactoryAI extends AI {

	public FactoryAI(RobotController rc) {
		super(rc);
	}

	@Override
	public void yield() throws GameActionException {
		myRC.yield();
		updateComponents();
		updateFluxRate();
	}
	
	@Override
	public void proceed() {

		while (true) {
			try {
				if (fluxRate > 0 && myRC.getTeamResources() > 120)
					buildingSystem.randomConstructUnit(UnitType.TANK_KILLER);
				yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}

	}

}
