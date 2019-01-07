package robotcode.systems;

import constants.HingeConstants;

public class IntakeHingePiston {

	private simulator.solenoid.SolenoidInterface mPiston;

	public IntakeHingePiston(simulator.solenoid.SolenoidInterface pPiston) {
		mPiston = pPiston;
	}

	public void up() {
		mPiston.set(HingeConstants.Piston.UP);
	}

	public void down() {
		mPiston.set(HingeConstants.Piston.DOWN);
	}
}
