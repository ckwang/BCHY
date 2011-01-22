package team017.message;

import team017.construction.UnitType;
import battlecode.common.Message;

public class UnitReadyMessage extends GenericMessage {

	UnitType type;
	
	public UnitReadyMessage(UnitType type) {
		super(MessageType.UNIT_READY);
		
		msg.ints[intCounter++] = type.ordinal();
	}

	public UnitReadyMessage(Message msg) {
		super(msg);
		
		type = UnitType.values()[msg.ints[intCounter++]];
	}
	
	public UnitType getUnitType() {
		return type;
	}
	
}
