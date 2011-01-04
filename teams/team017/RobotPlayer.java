package team017;

import team017.AI.*;
import battlecode.common.*;
import static battlecode.common.GameConstants.*;

public class RobotPlayer implements Runnable {

	private final RobotController myRC;

    public RobotPlayer(RobotController rc) {
        myRC = rc;
    }

	public void run() {
		switch (myRC.getChassis()){
			case BUILDING:
				new BuildingAI(myRC).proceed();
				break;
			
			case FLYING:
				new AirAI(myRC).proceed();
				break;
			
			default:
				new GroundAI(myRC).proceed();
				break;
		}
	}
//
//	public void testit(MovementController m) {
//		m.withinRange(myRC.getLocation());
//	}
//
//	public void runBuilder(MovementController motor, BuilderController builder) {
//	
//		while (true) {
//            try {
//
//				myRC.yield();
//
//				if(!motor.canMove(myRC.getDirection()))
//					motor.setDirection(myRC.getDirection().rotateRight());
//				else if(myRC.getTeamResources()>=2*Chassis.LIGHT.cost)
//					builder.build(Chassis.LIGHT,myRC.getLocation().add(myRC.getDirection()));
//
//            } catch (Exception e) {
//                System.out.println("caught exception:");
//                e.printStackTrace();
//            }
//        }
//	}
//
//    public void runMotor(MovementController motor) {
//        
//        while (true) {
//            try {
//                /*** beginning of main loop ***/
//                while (motor.isActive()) {
//                    myRC.yield();
//                }
//
//                if (motor.canMove(myRC.getDirection())) {
//                    //System.out.println("about to move");
//                    motor.moveForward();
//                } else {
//                    motor.setDirection(myRC.getDirection().rotateRight());
//                }
//
//                /*** end of main loop ***/
//            } catch (Exception e) {
//                System.out.println("caught exception:");
//                e.printStackTrace();
//            }
//        }
//    }
}
