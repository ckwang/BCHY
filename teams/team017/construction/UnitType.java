package team017.construction;

import java.util.ArrayList;
import java.util.List;

import battlecode.common.BuildMappings;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;

public enum UnitType {
	CONSTRUCTOR(Chassis.LIGHT, ComponentType.CONSTRUCTOR, ComponentType.SIGHT, ComponentType.ANTENNA),
	MEDIUM_CONSTRUCTOR(Chassis.MEDIUM, ComponentType.CONSTRUCTOR, ComponentType.RADAR, ComponentType.ANTENNA),
	GRIZZLY (Chassis.LIGHT, ComponentType.BLASTER,ComponentType.BLASTER, ComponentType.PROCESSOR, ComponentType.SIGHT),
	HAMMER_TANK (Chassis.LIGHT, ComponentType.HAMMER,ComponentType.HAMMER, ComponentType.PLATING, ComponentType.SIGHT),//weak!!!
	COMMANDER (Chassis.LIGHT, ComponentType.RADAR, ComponentType.ANTENNA, ComponentType.PLATING, ComponentType.PLATING),
	TANK_KILLER (Chassis.MEDIUM, ComponentType.RAILGUN,ComponentType.TELESCOPE),
	BATTLE_FORTRESS(Chassis.HEAVY, ComponentType.BLASTER, ComponentType.BLASTER, ComponentType.BLASTER, ComponentType.BLASTER, ComponentType.BLASTER, ComponentType.BLASTER, ComponentType.SIGHT, ComponentType.PLATING),
	APOCALYPSE(Chassis.HEAVY, ComponentType.RAILGUN,ComponentType.RAILGUN, ComponentType.RADAR),
	RECYCLER (Chassis.BUILDING, ComponentType.RECYCLER),
	ARMORY(Chassis.BUILDING, ComponentType.ARMORY),
	FACTORY (Chassis.BUILDING, ComponentType.FACTORY);
		
	public final Chassis chassis;
	public final ComponentType[] recyclerComs;
	public final ComponentType[] armoryComs;
	public final ComponentType[] factoryComs;
	public final ComponentType[] constructorComs;
	public final ComponentType[] allComs;
	public final ComponentType[] requiredBuilders;
	
	public final double totalCost;
	public final boolean selfBuild;
	public boolean shouldBuild;
	
	UnitType(Chassis chassis, ComponentType...coms) {
		this.chassis = chassis;
		double t = chassis.cost;
		allComs = coms;
		
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
		armoryComs = new ComponentType[armoryList.size()];
		factoryComs = new ComponentType[factoryList.size()];
		constructorComs = new ComponentType[constructorList.size()];
		
		recyclerList.toArray(recyclerComs);
		armoryList.toArray(armoryComs);
		factoryList.toArray(factoryComs);
		constructorList.toArray(constructorComs);
		
		List<ComponentType> requiredBuilderList = new ArrayList<ComponentType>();
		
		if(recyclerComs.length != 0)	requiredBuilderList.add(ComponentType.RECYCLER);
		if(armoryComs.length != 0)	requiredBuilderList.add(ComponentType.ARMORY);
		if(factoryComs.length != 0)	requiredBuilderList.add(ComponentType.FACTORY);
		if(constructorComs.length != 0)	requiredBuilderList.add(ComponentType.CONSTRUCTOR);
		
		requiredBuilders = new ComponentType[requiredBuilderList.size()];
		requiredBuilderList.toArray(requiredBuilders);
		
		selfBuild = requiredBuilders.length == 1;
		
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
