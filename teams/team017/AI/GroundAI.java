package team017.AI;

import team017.util.Util;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

abstract public class GroundAI extends AI {

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

		controllers.myRC.setIndicatorString(2, "searching for jump location");
		try{
			navigator.setDestination(des);
			MapLocation jumpLoc = navigator.getNextJumpingLoc(tolerance);
	
			controllers.myRC.setIndicatorString(2, "jump location: " + jumpLoc);
			
			if (jumpLoc == null){
				return walkingNavigateToDestination(des, tolerance);
			}
			else{
				if (controllers.myRC.getDirection() != currentLoc.directionTo(jumpLoc))
					controllers.motor.setDirection(currentLoc.directionTo(jumpLoc));
				else if (!controllers.jumper.isActive())
					controllers.jumper.jump(jumpLoc);
				
			}
		} catch (GameActionException e) {
			e.printStackTrace();
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
