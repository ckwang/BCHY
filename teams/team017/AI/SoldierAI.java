package team017.AI;

import java.util.ArrayList;
import java.util.List;

import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.common.WeaponController;

public class SoldierAI extends AI {

	public SoldierAI(RobotController rc) {
		super(rc);
	}

	public void yield() throws GameActionException {
		
	}
	
	public void proceed() {

		while (true) {
			// MessageHandler encoder = new BorderMessage(myRC, comm,
			// Direction.NORTH);
			// encoder.send();
			//
			// Message incomingMsg;
			// MessageHandler decoder = new BorderMessage(incomingMsg);
			// decoder.getMessageType();
			// ((BorderMessage) decoder).getBorderDirection();

			try {
				updateComponents();

				/*** beginning of main loop ***/
				if (motor != null) {
					navigate();
				}

				attack();

				sense_border();

				myRC.yield();

				/*** end of main loop ***/
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}

	private void sense_border() {
		try {

			Direction[] addDirs = new Direction[3];

			if (myRC.getDirection().isDiagonal()) {
				addDirs[0] = myRC.getDirection().rotateLeft();
				addDirs[1] = myRC.getDirection().rotateRight();
			} else {
				addDirs[0] = myRC.getDirection();
			}

			int j = -1;
			while (addDirs[++j] != null) {
				MapLocation currentLoc = myRC.getLocation();

				int i;
				for (i = 3; i > 0; i--) {
					if (myRC.senseTerrainTile(currentLoc.add(addDirs[j], i)) != TerrainTile.OFF_MAP)
						break;
				}

				// i == 3 means no OFF_MAP sensed
				if (i != 3) {
					switch (addDirs[j]) {
					case NORTH:
						borders[0] = currentLoc.y - (i + 1);
						break;
					case EAST:
						borders[1] = currentLoc.x + (i + 1);
						break;
					case SOUTH:
						borders[2] = currentLoc.y + (i + 1);
						break;
					case WEST:
						borders[3] = currentLoc.x - (i + 1);
						break;
					}
				}
			}
		} catch (Exception e) {
			System.out.println("caught exception:");
			e.printStackTrace();
		}

	}

	private boolean attack() {
		Robot[] robots = sensor.senseNearbyGameObjects(Robot.class);
		List<MapLocation> enemylocs = new ArrayList<MapLocation>(robots.length);
		List<MapLocation> allylocs = new ArrayList<MapLocation>(robots.length);
		double leasthp1 = Chassis.HEAVY.maxHp, leasthp2 = Chassis.HEAVY.maxHp;
		int index1 = 0, index2 = 0;
		for (int i = 0; i < robots.length; ++i) {
			Robot r = robots[i];
			if (r.getTeam() == myRC.getTeam()) {
				MapLocation loc;
				try {
					loc = sensor.senseLocationOf(r);
					allylocs.add(loc);
				} catch (GameActionException e) {
					e.printStackTrace();
					continue;
				}

			} else if (r.getTeam() != Team.NEUTRAL) {
				try {
					RobotInfo info = sensor.senseRobotInfo(r);
					MapLocation loc = sensor.senseLocationOf(r);
					enemylocs.add(loc);
					if (!info.on)
						continue;
					if (info.hitpoints < leasthp1) {
						leasthp2 = leasthp1;
						index2 = index1;
						index1 = i;
						leasthp1 = info.hitpoints;
					}
					if (info.hitpoints < leasthp2) {
						index2 = i;
						leasthp2 = info.hitpoints;
					}
				} catch (GameActionException e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		boolean canfire = false, attacked = false;
		if (enemylocs.size() == 0)
			return false;
		for (WeaponController w : weapons) {
			if (w.isActive())
				continue;
			try {
				MapLocation weakest = sensor.senseLocationOf(robots[index1]);
				w.attackSquare(weakest, robots[index1].getRobotLevel());
				attacked = true;
			} catch (GameActionException e) {
				canfire = true;
				break;
			}
		}
		if (enemylocs.size() > 1 && canfire) {
			for (WeaponController w : weapons) {
				if (w.isActive())
					continue;
				try {
					MapLocation weakest = sensor
							.senseLocationOf(robots[index2]);
					w.attackSquare(weakest, robots[index2].getRobotLevel());
					attacked = true;
				} catch (GameActionException e) {
					return attacked;
				}
			}
		}
		return attacked;
	}

	private void navigate() throws GameActionException {

		if (!motor.isActive()) {
			roachNavigate();
		}

	}

	private void roachNavigate() throws GameActionException {
		// navigate();
		if (motor.canMove(myRC.getDirection())) {
			// System.out.println("about to move");
			motor.moveForward();
		} else {
			if ((Clock.getRoundNum() / 10) % 2 == 0)
				motor.setDirection(myRC.getDirection().rotateRight());
			else
				motor.setDirection(myRC.getDirection().rotateLeft());
		}
	}

}
