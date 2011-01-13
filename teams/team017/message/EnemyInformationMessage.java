package team017.message;

import java.util.ArrayList;
import java.util.Set;

import java.util.List;

import team017.util.EnemyInfo;
import battlecode.common.Clock;
import battlecode.common.Message;
import battlecode.common.RobotLevel;

public class EnemyInformationMessage extends GenericMessage{
	private List<EnemyInfo> infos;

	public EnemyInformationMessage (Set<EnemyInfo> infos) {
		super (MessageType.ENEMY_INFORMATION_MESSAGE);
		
		int sizePointer = intCounter++;
		int size = 0;
		for (EnemyInfo i : infos) {
			if (Clock.getRoundNum() - i.roundNum > 0)
				continue;
			size++;
			
			msg.ints[intCounter++] = i.roundNum;
			msg.ints[intCounter++] = i.id;
			msg.ints[intCounter++] = (int) (i.hp * 100);
			msg.locations[locCounter++] = i.location;
			msg.ints[intCounter++] = i.level.ordinal();
			msg.ints[intCounter++] = i.mobile ? 1 : 0;
		}
		msg.ints[sizePointer] = size;
	}
	
	
	public EnemyInformationMessage(Message msg) {
		super(msg);
//		int id, double hp, MapLocation location, RobotLevel level, boolean mobile		
		infos = new ArrayList<EnemyInfo>();
		int size = msg.ints[intCounter++];
		for (int i = 0; i < size; ++i) {
			EnemyInfo enemy = new EnemyInfo (msg.ints[intCounter++], 
					msg.ints[intCounter++],
					((double) msg.ints[intCounter++]) / 100,
					msg.locations[locCounter++],
					RobotLevel.values()[msg.ints[intCounter++]], 
					msg.ints[intCounter++] == 1); 
			infos.add(enemy);
		}
		
	}
	
	public List<EnemyInfo> getInfos() {
		return infos;
	}

}
