package autonomous.commands;

import org.usfirst.frc.team3419.robot.Robot;

import constants.States;

public class SetStateCommand extends BaseAutonomousCommand {
	
	private States mState;
	private Robot mRobot;
	
	public SetStateCommand(Robot pRobot, States pState) {
		mState = pState;
	}

	@Override
	public boolean RunCommand() {
		return false;
	}

}
