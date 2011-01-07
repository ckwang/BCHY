package team017.construction;

import team017.util.Controllers;
import battlecode.common.ComponentType;
import battlecode.common.Direction;

public class BuilderDirections {
	
	private Controllers controllers;
	
	public Direction recyclerDirection;
	public Direction armoryDirection;
	public Direction factoryDirection;
	/*
	 * 7 0 1
	 * 6 * 2
	 * 5 4 3
	 */
	public boolean[] emptyDirections;
	
	public BuilderDirections(Controllers controllers) {
		this.controllers = controllers;
		emptyDirections = new boolean[8];
	}
	
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
	
//	public Direction consecutiveTwoEmpty() {
//		int i = 0;
//		
//	}
	
	public boolean isComplete() {
		switch(controllers.builder.type()) {
		case RECYCLER:
			return (armoryDirection != null && factoryDirection != null);
		case ARMORY:
			return (recyclerDirection != null && factoryDirection != null);
		case FACTORY:
			return (recyclerDirection != null && armoryDirection != null);
		default:
			return false;
		}
	}
	
}
