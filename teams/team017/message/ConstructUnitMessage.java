package team017.message;

import java.util.LinkedList;
import java.util.List;

import team017.construction.UnitType;
import battlecode.common.MapLocation;
import battlecode.common.Message;

public class ConstructUnitMessage extends GenericMessage {

	private MapLocation builderLocation;
	private UnitType type;
	private List<UnitType> types = new LinkedList<UnitType>();
	private int queueSize;
	private boolean isUrgent;
	
	public ConstructUnitMessage(MapLocation builderLocation, UnitType type, boolean isUrgent) {
		super(MessageType.CONSTRUCT_UNIT_MESSAGE);
		queueSize = 0;
		
		msg.locations[locCounter++] = builderLocation;
		msg.ints[intCounter++] = queueSize;
		msg.ints[intCounter++] = type.ordinal();
		msg.ints[intCounter++] = isUrgent? 1 : 0;
	}
	
	public ConstructUnitMessage(MapLocation builderLocation, List<UnitType> types, boolean isUrgent) {
		super(MessageType.CONSTRUCT_UNIT_MESSAGE);
		queueSize = types.size();
		
		msg.locations[locCounter++] = builderLocation;
		msg.ints[intCounter++] = queueSize;
		for (int i = 0; i < queueSize; i++)
			msg.ints[intCounter++] = types.get(i).ordinal();
		msg.ints[intCounter++] = isUrgent? 1 : 0;
	}
	
	public ConstructUnitMessage(MapLocation builderLocation, UnitType[] types, boolean isUrgent) {
		super(MessageType.CONSTRUCT_UNIT_MESSAGE);
		queueSize = types.length;
		
		msg.locations[locCounter++] = builderLocation;
		msg.ints[intCounter++] = queueSize;
		for (int i = 0; i < queueSize; i++)
			msg.ints[intCounter++] = types[i].ordinal();
		msg.ints[intCounter++] = isUrgent? 1 : 0;
	}


	public ConstructUnitMessage(Message msg) {
		super(msg);
		builderLocation = msg.locations[locCounter++];
		queueSize = msg.ints[intCounter++];
		if (queueSize == 0) {
			type = UnitType.values()[msg.ints[intCounter++]];
		} else {
			for (int i = 0; i < queueSize; i++)
				types.add(UnitType.values()[msg.ints[intCounter++]]);
		}
			
		
		isUrgent = msg.ints[intCounter++] == 1;
		
	}
	
	public boolean isList() {
		return (queueSize != 0);
	}
	
	public UnitType getType() {
		return type;
	}
	
	public List<UnitType> getTypes() {
		return types;
	}
	
	
	
	public MapLocation getBuilderLocation() {
		return builderLocation;
	}
	
	public boolean isUrgent() {
		return isUrgent;
	}
	
}
