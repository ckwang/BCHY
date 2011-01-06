package team017.AI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import team017.construction.UnitType;
import team017.message.BorderMessage;
import team017.message.MessageHandler;


import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.common.WeaponController;

public class GroundAI extends AI {

	Set<MapLocation> mineLocations = new HashSet<MapLocation>();
	private boolean isSoldier;
	private boolean isConstructor;

	public GroundAI(RobotController rc) {
		super(rc);
	}

	public void proceed() {

		// Initial movement
		if (Clock.getRoundNum() == 0) {
			init();
			init_revolve();
			// updateComponents();
			// init_return();
		} else {
			myRC.turnOff();
		}

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

				if (isSoldier) {
					attack();
				}

				if (isConstructor) {
					updateMineSet();
					build();
				}

				evaluateNextState();
				sense_border();
				// myRC.setIndicatorString(0, borders[0] + "," + borders[1] +
				// "," + borders[2] + "," + borders[3]);
				// myRC.setIndicatorString(1,myRC.getLocation() + "");

				myRC.yield();

				/*** end of main loop ***/
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}

	private void init() {
		try {
			for (int i = 0; i < 4; ++i) {
				sense_border();
				// Rotate twice Right for a 90 degrees turn
				motor.setDirection(myRC.getDirection().rotateRight()
						.rotateRight());
				updateMineSet();
				myRC.yield();
				myRC.setIndicatorString(0, borders[0] + "," + borders[1] + ","
						+ borders[2] + "," + borders[3]);
				myRC.setIndicatorString(1, myRC.getLocation() + "");
			}
		} catch (GameActionException e) {
			System.out.println("caught exception:");
			e.printStackTrace();
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

	private void init_revolve() {

		MapLocation[] locationList = {
				homeLocation.add(Direction.NORTH_EAST, 2),
				homeLocation.add(Direction.SOUTH_EAST, 2),
				homeLocation.add(Direction.SOUTH_WEST, 2),
				homeLocation.add(Direction.NORTH_WEST, 2) };
		int index = 0;

		while (true) {
			try {

				navigator.setDestination(locationList[index]);
				myRC.setIndicatorString(2, myRC.getLocation().toString()
						+ locationList[index].toString());

				Direction nextDir = navigator.getNextDir(0);
				if (nextDir == Direction.OMNI) {
					index++;
					if (index == 4)
						return;

					continue;
				}

				if (!motor.isActive() && motor.canMove(nextDir)) {
					if (myRC.getDirection() == nextDir) {
						motor.moveForward();
					} else {
						motor.setDirection(nextDir);
					}
				}

				myRC.yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}

	private void init_return() {
		navigator.setDestination(homeLocation);

		while (true) {
			try {
				Direction nextDir = navigator.getNextDir(0);

				if (nextDir == Direction.OMNI)
					break;

				if (!motor.isActive() && motor.canMove(nextDir)) {
					if (myRC.getDirection() == nextDir)
						motor.moveForward();
					else
						motor.setDirection(nextDir);
				}
				myRC.yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}

		MessageHandler msgHandler = new BorderMessage(myRC, comm, borders);
		msgHandler.send();
		myRC.yield();

	}

	private void updateMineSet() throws GameActionException {
		Mine[] minelist = sensor.senseNearbyGameObjects(Mine.class);
		for (Mine mine : minelist) {
			if (sensor.senseObjectAtLocation(mine.getLocation(),
					RobotLevel.ON_GROUND) != null) {
				if (mineLocations.contains(mine.getLocation()))
					mineLocations.remove(mine.getLocation());
			} else {
				mineLocations.add(mine.getLocation());
			}

		}
	}

	private void evaluateNextState() {
		if (weapons != null)
			isSoldier = true;
		if (builder != null)
			isConstructor = true;
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

	private void build() throws GameActionException {
		myRC.setIndicatorString(0, mineLocations.toString());

		for (MapLocation mineLoc : mineLocations) {
			if (myRC.getLocation().isAdjacentTo(mineLoc)) {
				if (sensor.canSenseSquare(mineLoc)) {
					if (sensor.senseObjectAtLocation(mineLoc,
							RobotLevel.ON_GROUND) != null)
						continue;
				}
				if (myRC.getDirection() != myRC.getLocation().directionTo(
						mineLoc)) {
					while (motor.isActive())
						myRC.yield();
					motor.setDirection(myRC.getLocation().directionTo(mineLoc));
					myRC.yield();
				}
				if (sensor.senseObjectAtLocation(mineLoc, RobotLevel.ON_GROUND) == null) {
					while (!constructUnit(
							myRC.getLocation().add(myRC.getDirection()),
							UnitType.RECYCLER))
						myRC.yield();
				}
			}
		}
		if(Clock.getRoundNum()>200 && Clock.getRoundNum() < 1500 &&myRC.getTeamResources()>95){
			constructUnit(myRC.getLocation().add(myRC.getDirection()),UnitType.FACTORY);
		}
	}


	private void navigate() throws GameActionException {

		if (!motor.isActive()) {
			if (isConstructor) {
				if (!mineLocations.isEmpty()) {
					MapLocation currentLoc = myRC.getLocation();
					MapLocation nearest = currentLoc.add(Direction.NORTH, 100);
					for (MapLocation loc : mineLocations) {
						if (currentLoc.distanceSquaredTo(loc) < currentLoc
								.distanceSquaredTo(nearest))
							nearest = loc;
					}

					myRC.setIndicatorString(0, currentLoc + "," + nearest);

					navigator.setDestination(nearest);
					Direction nextDir = navigator.getNextDir(0);

					if (nextDir != Direction.OMNI) {
						if (myRC.getDirection() == nextDir) {
							if (motor.canMove(nextDir)) {
								motor.moveForward();
							}
						} else {
							motor.setDirection(nextDir);
						}
					}

				} else {
					roachNavigate();
				}
			} else {
				roachNavigate();
			}
		}

	}

	private void roachNavigate() throws GameActionException {
		// navigate();
		if (motor.canMove(myRC.getDirection())) {
			// System.out.println("about to move");
			motor.moveForward();
		} else {
			if ((Clock.getRoundNum()/10) % 2 == 0)
				motor.setDirection(myRC.getDirection().rotateRight());
			else
				motor.setDirection(myRC.getDirection().rotateLeft());
		}
	}
}
