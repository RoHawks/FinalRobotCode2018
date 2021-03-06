package constants;

import edu.wpi.first.wpilibj.DoubleSolenoid.Value;

public class HingeConstants {
	
	public static class Motor {
		public static final boolean 
			LEFT_REVERSED = true,
			RIGHT_REVERSED = false,
			LEFT_ENCODER_REVERSED = true,
			RIGHT_ENCODER_REVERSED = false;
		
		public static final double
			LEFT_OFFSET = 0,
			RIGHT_OFFSET = 80;
		
		public static final double //ticks, up is negative
			LEFT_UP = 220,//-22950,
			LEFT_DOWN = 108,//-21860,
			RIGHT_UP = 244,//2420,
			RIGHT_DOWN = 345,//3500;
			MOVE_TOLERANCE = 3;
		
		public static final double MAX_CURRENT = 5;
		
		public static final double
			LEFT_UP_SPEED = -0.3,
			LEFT_DOWN_SPEED = 0.3,
			RIGHT_UP_SPEED = -0.25,
			RIGHT_DOWN_SPEED = 0.3;
		
		public static class PID{ //we don't set hinge by position -- unused
			public static final double
				HINGE_P_DOWN = 0.1,
				HINGE_I_DOWN = 0.001,
				HINGE_D_DOWN = 100,	
				HINGE_P_UP = 0.8,
				HINGE_I_UP = 0.005,
				HINGE_D_UP = 0;
		
			public static final int
				LEFT_HINGE_TOLERANCE_DOWN = 50,
				LEFT_HINGE_IZONE_DOWN = 100,
				RIGHT_HINGE_TOLERANCE_DOWN = 50,
				RIGHT_HINGE_IZONE_DOWN= 100,
				LEFT_HINGE_TOLERANCE_UP = 100,
				LEFT_HINGE_IZONE_UP = 100,
				RIGHT_HINGE_TOLERANCE_UP = 50,
				RIGHT_HINGE_IZONE_UP = 100;
		}
	}
	
	public static class Piston {
		public static final Value 
			UP = Value.kReverse, 
			DOWN = Value.kForward;
	}
}