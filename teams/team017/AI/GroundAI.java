package team017.AI;

import team017.util.Util;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;

abstract public class GroundAI extends AI {

	private MapLocation jumpLoc = null;
	
	public GroundAI(RobotController rc) {
		super(rc);
	}

	@Override
	abstract public void proceed();

	@Override
	abstract protected void processMessages() throws GameActionException;
	
	public void yield(){
		super.yield();
	}
	
	public boolean walkingNavigateToDestination(MapLocation des, int tolerance){
		navigator.setDestination(des);
		Direction nextDir = navigator.getNextDir(tolerance);
		
		if (nextDir == Direction.OMNI){
			return true;
		}
		
		if (controllers.motor.isActive())
			return false;
		
		try{
			if (controllers.myRC.getDirection() == nextDir) {
				if (controllers.motor.canMove(nextDir)) {
					controllers.motor.moveForward();
				}
			} else {
				controllers.motor.setDirection(nextDir);
			}
		} 
		catch (GameActionException e){
			e.printStackTrace();
			return false;
		}
		
		return false;
		
	}
	
	public boolean jumpingNavigateToDestination(MapLocation des, int tolerance){
		
		MapLocation currentLoc = controllers.myRC.getLocation();
		Direction desDir = currentLoc.directionTo(des);
		int distance = currentLoc.distanceSquaredTo(des);
		
		
		if (distance < tolerance)
			return true;
		
		Direction nextDir;
		// if target is not traversable, back-tracing the destination
		while (!navigator.isTraversable(des)) {
			nextDir = currentLoc.directionTo(des);
			des = des.subtract(nextDir);
			
			// beside the source
			if (currentLoc.distanceSquaredTo(des) <= tolerance) {
				return true;
			}
		}

		controllers.myRC.setIndicatorString(1, "destination location: " + des);
		try{
			navigator.setDestination(des);
			
			if (jumpLoc == null)
				jumpLoc = navigator.getNextJumpingLoc(tolerance);
	
			controllers.myRC.setIndicatorString(1, "destination location: " + des + "jump location: " + jumpLoc);
			
			if (jumpLoc == null){
				return walkingNavigateToDestination(des, tolerance);
			}
			else{
				if (controllers.myRC.getDirection() != currentLoc.directionTo(jumpLoc)) {
					if (!controllers.motor.isActive())
						controllers.motor.setDirection(currentLoc.directionTo(jumpLoc));
				}
				else if (!controllers.jumper.isActive()){
					
					if (controllers.sensor.canSenseSquare(jumpLoc)){
						if (controllers.sensor.senseObjectAtLocation(jumpLoc, RobotLevel.ON_GROUND) == null){
							controllers.jumper.jump(jumpLoc);
							jumpLoc = null;
						}
						else {
							if(!controllers.motor.isActive())
								controllers.motor.moveForward();
						}
					}
					
					
				}
				
			}
		} catch (GameActionException e) {
//			e.printStackTrace();
			jumpLoc = null;
			return false;
		}
		return false;
	}
	
	public boolean navigateToDestination(MapLocation des, int tolerance){
		if (controllers.jumper == null)
			return walkingNavigateToDestination(des, tolerance);
		else
			return jumpingNavigateToDestination(des, tolerance);
	}

}
