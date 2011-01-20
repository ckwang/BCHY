package team017.message;

import battlecode.common.Message;

public class NotEnoughSpaceMessage extends GenericMessage {
	public NotEnoughSpaceMessage() {
		super(MessageType.NOT_ENOUGH_SPACE_MESSAGE);
		
	}

	protected NotEnoughSpaceMessage(Message msg) {
		super(msg);
	}

}
