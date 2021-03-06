package constants;

import edu.wpi.first.wpilibj.DoubleSolenoid.Value;

public class IntakeConstants {

	public static final Value //CHANGED AT NY QF
		OPEN = Value.kForward,//Value.kReverse, 
		CLOSED = Value.kReverse;//Value.kForward;

	// break beam normally open or closed
	public static final boolean 
		BOX_PRESENT_BREAK_BEAM = false, 
		BOX_PRESENT_LIMIT_SWITCH = true;

	public static final boolean
		RIGHT_WHEEL_REVERSED = true,
		LEFT_WHEEL_REVERSED = false;

	public static final double 
		INTAKE_WHEEL_SPEED = 1,
		EJECT_WHEEL_SPEED = -0.6,
		INTAKE_HOLD_SPEED = 0.3;

	public static final long FLIP_TIME_MS = 800;

}
