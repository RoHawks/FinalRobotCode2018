package autonomous.commands;

import constants.IntakeConstants;
import robotcode.systems.Intake;

public class IntakeCubeCommand extends BaseAutonomousCommand {
	
	public static final long INTAKE_TIME = 600;
	private Intake mIntake;
	
	public IntakeCubeCommand(Intake pIntake) {
		mIntake = pIntake;
	}

	@Override
	public boolean RunCommand() {
		mIntake.closeMovePistons();
		mIntake.setWheelSpeed(IntakeConstants.INTAKE_WHEEL_SPEED);
		return System.currentTimeMillis() - mTimeStartMillis > INTAKE_TIME;
	}
}
