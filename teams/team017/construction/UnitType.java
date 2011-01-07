package team017.construction;

import java.util.ArrayList;
import java.util.List;

import battlecode.common.BuildMappings;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;

public enum UnitType {
	CONSTRUCTOR(Chassis.LIGHT, ComponentType.CONSTRUCTOR, ComponentType.SIGHT, ComponentType.ANTENNA),
	GRIZZLY (Chassis.LIGHT, ComponentType.BLASTER,ComponentType.BLASTER, ComponentType.PROCESSOR, ComponentType.SIGHT),
	HAMMER_TANK (Chassis.LIGHT, ComponentType.HAMMER,ComponentType.HAMMER, ComponentType.PLATING, ComponentType.SIGHT),//weak!!!
	TANK_KILLER (Chassis.MEDIUM, ComponentType.RAILGUN,ComponentType.TELESCOPE),
	APOCALYPSE(Chassis.HEAVY, ComponentType.RAILGUN,ComponentType.RAILGUN, ComponentType.TELESCOPE),
	RECYCLER (Chassis.BUILDING, ComponentType.RECYCLER),
	ARMORY(Chassis.BUILDING, ComponentType.ARMORY),
	FACTORY (Chassis.BUILDING, ComponentType.FACTORY);
	
	
	public final Chassis chassis;
	public final ComponentType[] recyclerComs;
	public final ComponentType[] armoryComs;
	public final ComponentType[] factoryComs;
	public final ComponentType[] constructorComs;
	public final double totalCost;
	public final boolean selfBuild;
	public boolean shouldBuild;
	
	UnitType(Chassis chassis, ComponentType...coms) {
		this.chassis = chassis;
		double t = chassis.cost;
		int countNulls = 0;
		
		List<ComponentType> recyclerList = new ArrayList<ComponentType>();
		List<ComponentType> armoryList = new ArrayList<ComponentType>();
		List<ComponentType> factoryList = new ArrayList<ComponentType>();
		List<ComponentType> constructorList = new ArrayList<ComponentType>();
		
		for (ComponentType com : coms) {
			t += com.cost;
			if(BuildMappings.canBuild(ComponentType.RECYCLER, com))
				recyclerList.add(com);
			if(BuildMappings.canBuild(ComponentType.ARMORY, com))
				armoryList.add(com);
			if(BuildMappings.canBuild(ComponentType.FACTORY, com))
				factoryList.add(com);
			if(BuildMappings.canBuild(ComponentType.CONSTRUCTOR, com))
				constructorList.add(com);
		}
	
		recyclerComs = new ComponentType[recyclerList.size()];
		recyclerList.toArray(recyclerComs);
		if(recyclerComs.length == 0)
			++countNulls;
		
		armoryComs = new ComponentType[armoryList.size()];
		armoryList.toArray(armoryComs);
		if(armoryComs.length == 0)
			++countNulls;
		
		factoryComs = new ComponentType[factoryList.size()];
		factoryList.toArray(factoryComs);
		if(factoryComs.length == 0)
			++countNulls;
		
		constructorComs = new ComponentType[constructorList.size()];
		constructorList.toArray(constructorComs);
		if(constructorComs.length == 0)
			++countNulls;
		
		if(countNulls == 3)
			selfBuild = true;
		else
			selfBuild = false;
		
		totalCost = t;
		shouldBuild = false;
	}
	
	public ComponentType[] getComponentList(ComponentType type){
		switch(type){
			case RECYCLER:
				return recyclerComs;
			case ARMORY:
				return armoryComs;
			case FACTORY:
				return factoryComs;
			case CONSTRUCTOR:
				return constructorComs;
			default:
				return null;
		}
	}
	
}

