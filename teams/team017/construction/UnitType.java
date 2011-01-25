package team017.construction;

import java.util.ArrayList;
import java.util.List;

import team017.util.Util;

import battlecode.common.BuildMappings;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;

public enum UnitType {
// Recycler
	CONSTRUCTOR(Chassis.LIGHT, ComponentType.CONSTRUCTOR, ComponentType.SIGHT, ComponentType.ANTENNA),
	COMMANDER (Chassis.LIGHT, ComponentType.RADAR, ComponentType.DISH),
	GRIZZLY (Chassis.LIGHT, ComponentType.BLASTER, ComponentType.BLASTER, ComponentType.PLATING, ComponentType.SIGHT),
	RADARGUN (Chassis.LIGHT, ComponentType.BLASTER, ComponentType.ANTENNA, ComponentType.RADAR),
	HAMMER_TANK (Chassis.LIGHT, ComponentType.HAMMER,ComponentType.HAMMER, ComponentType.PLATING, ComponentType.SIGHT),//weak!!!

// Armory + Recycler
	JUMPING_CONSTRUCTOR (Chassis.MEDIUM, ComponentType.CONSTRUCTOR, ComponentType.JUMP, ComponentType.RADAR, ComponentType.ANTENNA, ComponentType.SHIELD),
	FLYING_CONSTRUCTOR (Chassis.FLYING, ComponentType.CONSTRUCTOR, ComponentType.ANTENNA),
	NETWORK_COMMANDER (Chassis.MEDIUM, ComponentType.RADAR, ComponentType.NETWORK, ComponentType.PROCESSOR, ComponentType.PROCESSOR),
	MEDIUM_BEAM (Chassis.MEDIUM, ComponentType.RADAR, ComponentType.JUMP, ComponentType.BEAM, ComponentType.SMG, ComponentType.SHIELD),
	HAMMER_JUMP(Chassis.MEDIUM, ComponentType.HAMMER, ComponentType.HAMMER, ComponentType.HAMMER, ComponentType.RADAR, ComponentType.JUMP),
	
//	Factory + Recycler + Armory
	WAR_MINER (Chassis.HEAVY, ComponentType.CONSTRUCTOR, ComponentType.JUMP, ComponentType.BEAM, ComponentType.RADAR, ComponentType.SHIELD, ComponentType.SHIELD, ComponentType.SHIELD, ComponentType.SHIELD),
	TELESCOPER (Chassis.FLYING, ComponentType.TELESCOPE, ComponentType.ANTENNA),
	HEAVY_CONSTRUCTOR(Chassis.HEAVY, ComponentType.CONSTRUCTOR, ComponentType.RADAR, ComponentType.DISH, ComponentType.REGEN),
	MEDIUM_KILLER (Chassis.MEDIUM, ComponentType.RAILGUN, ComponentType.RADAR, ComponentType.JUMP, ComponentType.SHIELD),
	
// Factory
	TANK_KILLER (Chassis.MEDIUM, ComponentType.RAILGUN, ComponentType.TELESCOPE),
	

// Factory + Recycler	
	RHINO_TANK (Chassis.MEDIUM, ComponentType.RADAR, ComponentType.BLASTER, ComponentType.SMG, ComponentType.SMG, ComponentType.SHIELD, ComponentType.SHIELD, ComponentType.SHIELD, ComponentType.SHIELD, ComponentType.SHIELD),
	MEDIUM_CONSTRUCTOR(Chassis.MEDIUM, ComponentType.CONSTRUCTOR, ComponentType.RADAR, ComponentType.ANTENNA, ComponentType.PLATING),
	MEDIUM_COMMANDER (Chassis.MEDIUM, ComponentType.RADAR, ComponentType.DISH, ComponentType.PROCESSOR, ComponentType.PROCESSOR),
	BATTLE_FORTRESS(Chassis.HEAVY, ComponentType.RADAR, ComponentType.REGEN, ComponentType.SHIELD, ComponentType.SMG,ComponentType.SMG,ComponentType.SMG,ComponentType.SMG,ComponentType.SMG,ComponentType.SMG,ComponentType.SMG,ComponentType.SMG,ComponentType.SMG,ComponentType.SMG),
	APOCALYPSE(Chassis.HEAVY, ComponentType.REGEN, ComponentType.HARDENED, ComponentType.SHIELD, ComponentType.SMG, ComponentType.SMG, ComponentType.RAILGUN, ComponentType.RADAR),
	CHRONO_APOCALYPSE(Chassis.HEAVY, ComponentType.RAILGUN, ComponentType.REGEN, ComponentType.JUMP, ComponentType.RADAR, ComponentType.SHIELD, ComponentType.SHIELD, ComponentType.SMG),
	BEAMER (Chassis.HEAVY, ComponentType.PLASMA, ComponentType.PLASMA, ComponentType.PLASMA, ComponentType.JUMP, ComponentType.RADAR, ComponentType.BEAM, ComponentType.SMG, ComponentType.SMG),
// Buildings
	RECYCLER (Chassis.BUILDING, ComponentType.RECYCLER),
	ARMORY(Chassis.BUILDING, ComponentType.ARMORY, ComponentType.ANTENNA),
	FACTORY (Chassis.BUILDING, ComponentType.FACTORY, ComponentType.ANTENNA),
	RAILGUN_TOWER (Chassis.BUILDING, ComponentType.RADAR, ComponentType.ANTENNA, ComponentType.MEDIC, ComponentType.RAILGUN, ComponentType.RAILGUN, ComponentType.SMG, ComponentType.SMG),
	TOWER (Chassis.BUILDING, ComponentType.ANTENNA, ComponentType.RADAR, ComponentType.BLASTER, ComponentType.BLASTER, ComponentType.BLASTER, ComponentType.BLASTER, ComponentType.SMG, ComponentType.SMG, ComponentType.SHIELD, ComponentType.SHIELD, ComponentType.SMG, ComponentType.SMG);
		
	public final Chassis chassis;
	public final ComponentType[] recyclerComs;
	public final ComponentType[] armoryComs;
	public final ComponentType[] factoryComs;
	public final ComponentType[] constructorComs;
	public final ComponentType[] allComs;
	public final int requiredBuilders;	// constructor, factory, armory, recycler
//	public final ComponentType[] requiredBuilders;
	
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
		
//		List<ComponentType> requiredBuilderList = new ArrayList<ComponentType>();
		int required = 0;
		if(recyclerComs.length != 0)	required |= Util.RECYCLER_CODE;
		if(armoryComs.length != 0)	required |= Util.ARMORY_CODE;
		if(factoryComs.length != 0)	required |= Util.FACTORY_CODE;
		if(constructorComs.length != 0)	required |= Util.CONSTRUCTOR_CODE;
		requiredBuilders = required;
		
		selfBuild = requiredBuilders % 2 == 0;
		
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
	
	public ComponentType getChassisBuilder () {
		if (BuildMappings.canBuild(ComponentType.RECYCLER, chassis))
			return ComponentType.RECYCLER;
		else if (BuildMappings.canBuild(ComponentType.FACTORY, chassis))
			return ComponentType.FACTORY;
		else if (BuildMappings.canBuild(ComponentType.ARMORY, chassis))
			return ComponentType.ARMORY;
		else
			return null;
		
	}
	
}
