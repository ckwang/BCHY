package team017.AI;

import java.util.ArrayDeque;
import java.util.Deque;

import team017.construction.UnitType;
import team017.message.BuildingRequestMessage;
import team017.message.ConstructUnitMessage;
import team017.message.ConstructionCompleteMessage;
import team017.util.Util;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;

public class ArmoryAI extends BuildingAI{
	MapLocation currentLoc = controllers.myRC.getLocation();
	int buildIdleRound = 0;
	private Deque<UnitType> constructingQueue = new ArrayDeque<UnitType>(50);
	
	public ArmoryAI(RobotController rc) {
		super(rc);
	}
	
	@Override
	public void yield() {
		super.yield();
		if (buildIdleRound != 0)
			buildIdleRound--;
	}

	@Override
	public void proceed() {
		while (controllers.motor.isActive() || controllers.comm == null)
			yield();
		msgHandler.queueMessage(new ConstructionCompleteMessage(controllers.myRC.getLocation(), UnitType.ARMORY));

		
		while (true) {
			try {
				controllers.myRC.setIndicatorString(0, constructingQueue.toString() + Clock.getRoundNum());

				processMessages();
				constructing();
				yield();
				} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}

	private void constructing() {
		try {
			if (constructingQueue.size() != 0) {
				UnitType type = constructingQueue.getFirst();
				MapLocation buildLoc = buildingLocs.constructableLocation(Util.ARMORY_CODE, type.requiredBuilders);
				if (buildLoc != null && buildingSystem.constructUnit(buildLoc,type, buildingLocs))
					constructingQueue.pop();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	@Override
	protected void processMessages() throws GameActionException {
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			switch (msgHandler.getMessageType(msg)) {
			case BUILDING_REQUEST:{
				BuildingRequestMessage handler = new BuildingRequestMessage(msg);
				if (handler.getBuilderLocation().equals(controllers.myRC.getLocation())) {

					while(!buildingSystem.constructComponent(handler.getBuildingLocation(),handler.getUnitType())){
						if(controllers.sensor.senseObjectAtLocation(handler.getBuilderLocation(),handler.getUnitType().chassis.level).getTeam() != controllers.myRC.getTeam())
							break;
						yield();
					}	
				}
				break;
			}
			case CONSTRUCTION_COMPLETE: {
				ConstructionCompleteMessage handler = new ConstructionCompleteMessage(msg);
				MapLocation currentLoc = controllers.myRC.getLocation();
				if (handler.getBuildingLocation().distanceSquaredTo(currentLoc) < 5) {
					buildingLocs.setLocations(handler.getBuildingType(), handler.getBuildingLocation());
				}
				break;
			}
			
			case CONSTRUCT_UNIT_MESSAGE: {
				ConstructUnitMessage handler = new ConstructUnitMessage(msg);
				if (controllers.myRC.getLocation().equals(handler.getBuilderLocation())) {
					if (handler.isList()) {
						if (handler.isUrgent()) {
							for (int i = handler.getTypes().size() - 1; i >= 0; i--)
								constructingQueue.addFirst(handler.getTypes().get(i));
						} else {
							constructingQueue.addAll(handler.getTypes());
						}	
					} else {
						if (handler.isUrgent())
							constructingQueue.addFirst(handler.getType());
						else
							constructingQueue.addLast(handler.getType());
					}

				}
				break;
			}
			}
		}
	}
}
