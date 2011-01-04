package team017.util;

import battlecode.common.ComponentClass;
import battlecode.common.ComponentController;
import battlecode.common.RobotController;

public class Util {

	public static ComponentController getComponentController(ComponentClass component, RobotController rc) {
		ComponentController[] components = rc.components();
		for (ComponentController cc: components) {
			if (cc.componentClass() == ComponentClass.SENSOR)
				return cc;
		}
		return null;
	}
	
}
