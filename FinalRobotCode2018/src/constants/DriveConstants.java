
package constants;

public class DriveConstants {

	// speed mins, when lower than these don't do anything
	public static final double  
			MIN_LINEAR_VEL = 0.02,
			MIN_DIRECTION_MAG = 0.25, // refers to joystick magnitudes
			MAX_INDIVIDUAL_VELOCITY = 1.0;
	
	public static final double
			EMERGENCY_VOLTAGE = 10000,
			MAX_EMERGENCY_VOLTAGE = 0.5;

	public static final double 
			MAX_ANGULAR_VEL = 1.0,
			MAX_LINEAR_VEL = 0.5;

	public static class SwerveSpeeds {
		public static final double 
			SPEED_MULT = 1.0,
			ANGULAR_SPEED_MULT = 1.0,
			NUDGE_MOVE_SPEED = 0.3,
			NUDGE_TURN_SPEED = 0.3;
	}

	public static class Modules {
		public static final boolean[]
			TURN_INVERTED = new boolean[] { false, false, false, false }, //old values {false, false, false, true}
			DRIVE_INVERTED = new boolean[] { false, false, false, true }, // assuming this is DRIVE_REVERSED old values {false, false, false, false }
			ENCODER_REVERSED = new boolean[] { true, true, true, true }; //old values {true, true, true, false}
		
		public static final double[] 
			X_OFF = new double[] { -27.438/2.0, 27.438/2.0 , 27.438/2.0 , -27.438/2.0 },
			Y_OFF = new double[] { 22.563/2.0, 22.563/2.0 , -22.563/2.0 , -22.563/2.0 };

		public static final int[] OFFSETS = new int[] { 75, 2269, 3056, 1252 };
	}

	public static class PID_Constants {

		public static final double[] 
			ROTATION_P = new double[] { 1, 1, 1, 1 },//{ 0.91, 0.91, 0.91, 0.91 }, //{ 1.0, 1.0, 1.0, 1.0 },
			ROTATION_I = new double[] { 0.001, 0.001, 0.001, 0.001 },//{ 0.002, 0.002, 0.002, 0.002 },//{ 0.023, 0.0025, 0.0025, 0.03 },
			ROTATION_D = new double[] { 0, 0, 0, 0 };

		public static final int[]
			ROTATION_IZONE = new int[] { 500, 500, 500, 500 },
			ROTATION_TOLERANCE = new int[] { 3, 3, 3, 3 };

		// for turning robot to an angle
		public static final double
			GYRO_P = 0.0085, //0.01 -- prototype
			GYRO_I = 0, //0.0003 -- prototype
			GYRO_D = 0,
			GYRO_TOLERANCE = 5,
			GYRO_MAX_SPEED = 0.7; //1 -- prototype
		
		public static final double
			DRIFT_COMP_P = 0.08,
			DRIFT_COMP_I = 0.0008,
			DRIFT_COMP_D = 0,
			DRIFT_COMP_MAX = 0.3;
	}
	
	public static class Prototype { //SW, SE, NE, NW
		public static final boolean[]
				TURN_INVERTED = new boolean[] {false, false, false, true},
				DRIVE_INVERTED = new boolean[] { false, false, false, false },
				ENCODER_REVERSED = new boolean[] {true, true, true, false};
		
		public static final double[] 
				X_OFF = new double[] { -19.0 / 2.0, -19.0 / 2.0, 19.0 / 2.0, 19.0 / 2.0 },
				//{-19.0 / 2.0, 19.0 / 2.0, 19.0 / 2.0, -19.0 / 2.0},//SE, NE, NW, SW
				Y_OFF = new double[] { -22.0 / 2.0, 22.0 / 2.0, 22.0 / 2.0, -22.0 / 2.0 };
		//{22.0 / 2.0, 22.0 / 2.0, -22.0 / 2.0, -22.0 / 2.0};

		public static final int[] OFFSETS = new int[] { 3721 , 767, 3402, 2527 };
		
		public static final double[] 
			ROTATION_P = new double[] { 0.7, 0.7, 0.7, 0.7 },
			ROTATION_I = new double[] { 0.007, 0.007, 0.007, 0.007 },
			ROTATION_D = new double[] { 0, 0, 0, 0 };
	}

}