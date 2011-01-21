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
		int distance = currentLoc.distanceSquaredTo(des);
		
		if (distance < tolerance)
			return true;

			
		MapLocation jumpLoc = controllers.myRC.getLocation();
		Direction nextDir;
		
		do{
			nextDir = jumpLoc.directionTo(des);
			
			// arrive at destination
			if (nextDir == Direction.OMNI){
				break;
			}
				
			jumpLoc = jumpLoc.add(nextDir);
		}while (jumpLoc.distanceSquaredTo(currentLoc) <= 16);
		
		jumpLoc = jumpLoc.subtract(nextDir);
		
		MapLocation bestAlternativeJumpLoc = null;
		if ( !navigator.isTraversable(jumpLoc) ){
			
			MapLocation temp = null;
			
			for (int i = 0; i < 8; i++){
				temp = jumpLoc.add(Util.dirs[i]);
				if( navigator.isTraversable(temp) 
					&& temp.distanceSquaredTo(currentLoc) <= 18
					&& temp.distanceSquaredTo(des) < distance ){
					bestAlternativeJumpLoc = temp;
					distance = temp.distanceSquaredTo(des);
				}
			}
			
			for (int i = 0; i < 8; i+=2){
				temp = jumpLoc.add(Util.dirs[i], 2);
				if( navigator.isTraversable(temp) 
					&& temp.distanceSquaredTo(currentLoc) <= 18 
					&& temp.distanceSquaredTo(des) < distance ){
					bestAlternativeJumpLoc = temp;
					distance = temp.distanceSquaredTo(des);
				}
			}
			
			if(bestAlternativeJumpLoc != null)
				jumpLoc = bestAlternativeJumpLoc;
		} else{
			try {
				controllers.jumper.jump(jumpLoc);
			} catch (GameActionException e) {
				e.printStackTrace();
			}
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
