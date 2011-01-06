package team017.util;

import battlecode.common.Chassis;
import battlecode.common.ComponentType;

public enum UnitType {
	GRIZZLY (Chassis.LIGHT, ComponentType.BLASTER,ComponentType.BLASTER, ComponentType.PROCESSOR, ComponentType.SIGHT),
	RECYCLER (Chassis.BUILDING, ComponentType.RECYCLER);
	
	public final Chassis chassis;
	public final ComponentType[] coms;
	public final double totalCost;
	
	UnitType(Chassis chassis, ComponentType...coms) {
		this.chassis = chassis;
		this.coms = coms;
		double t = chassis.cost;
		for (ComponentType com : coms) {
			t += com.cost;
		}
		totalCost = t;
	}
	
	
}

