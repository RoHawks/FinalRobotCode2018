package autonomous.commands;

import robotcode.systems.IntakeHingeMotor;

public class HingeCommand extends BaseAutonomousCommand {
	IntakeHingeMotor mHinge;
	boolean mUp;
	
	public HingeCommand(IntakeHingeMotor pHinge, boolean pUp) {
		mHinge = pHinge;
		mUp = pUp;
	}

	@Override
	public boolean RunCommand() {
		if(mUp) {
			mHinge.up();
		}
		else {
			mHinge.down();
		}
		
		return true;
	}
}
