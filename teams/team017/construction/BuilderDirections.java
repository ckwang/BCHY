package team017.construction;

import battlecode.common.ComponentType;
import battlecode.common.Direction;

public class BuilderDirections {
	public Direction recyclerDirection;
	public Direction armoryDirection;
	public Direction factoryDirection;
	
	public Direction getDirections(ComponentType type){
		switch(type){
		case RECYCLER:
			return recyclerDirection;
		case ARMORY:
			return armoryDirection;
		case FACTORY:
			return factoryDirection;
		default:
			return null;
		}
	}
	
	public void setDirections(ComponentType type, Direction dir) {
		switch(type){
		case RECYCLER:
			recyclerDirection = dir;
		case ARMORY:
			armoryDirection = dir;
		case FACTORY:
			factoryDirection = dir;
		}
	}
	
}
