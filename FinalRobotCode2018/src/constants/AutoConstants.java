package constants;

public class AutoConstants {

	public static class Mobility {
		public static final long DRIVE_TIME = 3250;
		public static final double DRIVE_SPEED = 0.3;
	}

	public static class SwitchMiddle {

		public static final double
			FORWARD_SPEED = 0.5,
			BACKWARDS_SPEED = 0.3,//0.5,
			INTAKE_FORWARD_SPEED = 0.3;
		
		public static final long
			INITIAL_PAUSE = 500,
			SIDEWAYS_FORWARD_FULL_SPEED_DRIVE_TIME_LEFT = 1450,
			SIDEWAYS_FORWARD_FULL_SPEED_DRIVE_TIME_RIGHT = 1550,
			FORWARD_ACCELERATION_TIME = 980,
			FORWARD_DECELERATION_TIME = 980,
			BACKWARDS_FULL_SPEED_DRIVE_TIME = 1850,
			BACKWARDS_ACCELERATION_TIME = 896,
			BACKWARDS_DECELERATION_TIME = 546,
			INTAKE_DECELERATION_TIME = 300,
			INTAKE_FULL_SPEED_DRIVE_TIME = 800,
			
			SIDEWAYS_TO_SCALE_FULL_SPEED_DRIVE_TIME = 1014,
			PISTON_WORK_TIME = GrabberConstants.EXTEND_PISTON_OUT_TIME + GrabberConstants.GRAB_PISTON_OUT_TIME
				+ GrabberConstants.EXTEND_PISTON_IN_TIME + 200;
		
		public static final double 
			MIDDLE_ANGLE = 28, 
			LEFT_ANGLE = -30.23, 
			RIGHT_ANGLE = 26.0,
			GO_BACK_ANGLE = -143.5;
	}
	
	public static class Side{
		public static class Scale{
			public static final double
				FINAL_VELOCITY = 0.5;
			public static final long
				ACCELERATION_TIME = 1000,
				FULL_SPEED_TIME = 3234,
				DECELERATION_TIME = 1000;
		
			public static final double
				SHORT_FINAL_VELOCITY = 0.2;
			public static final long
				SHORT_DRIVE_TIME = 500,
				SHORT_DRIVE_TIME_BACK = 1500;
		}
		public static class Switch{
			public static final double
				FINAL_VELOCITY = 0.5;
			public static final long
				ACCELERATION_TIME = 700,
				FULL_SPEED_TIME = 1408,
				DECELERATION_TIME = 700;
			
			public static final double
				SHORT_FINAL_VELOCITY = 0.2;
			public static final long
				SHORT_DRIVE_TIME = 500;
		}
		public static class Mobility{
			public static final double
				FINAL_VELOCITY = 0.5;
			public static final long
				ACCELERATION_TIME = 1000,
				FULL_SPEED_TIME = 2000,
				DECELERATION_TIME = 1000;
		}
	}
	
	public static class DriveForward{
			public static final double
				FORWARD_SPEED = 0.6;
			
			public static final long
				INITIAL_PAUSE = 500,
				FORWARD_ACCELERATION_TIME = 1000,
				FORWARD_DRIVE_TIME = 1400,
				FORWARD_DECELERATION_TIME = 1000;
	}
}
