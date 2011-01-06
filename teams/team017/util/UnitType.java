package team017.util;

import battlecode.common.Chassis;
import battlecode.common.ComponentType;

public enum UnitType {
	GRIZZLY (Chassis.LIGHT, ComponentType.BLASTER,ComponentType.BLASTER, ComponentType.PROCESSOR, ComponentType.SIGHT),
	HAMMER_TANK (Chassis.LIGHT, ComponentType.HAMMER,ComponentType.HAMMER, ComponentType.PLATING, ComponentType.SIGHT),//weak!!!
	TANK_KILLER (Chassis.MEDIUM, ComponentType.RAILGUN,ComponentType.TELESCOPE),
	RECYCLER (Chassis.BUILDING, ComponentType.RECYCLER),
	FACTORY (Chassis.BUILDING, ComponentType.FACTORY);

	
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

