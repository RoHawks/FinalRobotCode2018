package constants;

public class ElevatorConstants {
	public static class PID {
		public static final double 
			ELEVATOR_UP_P = 0.07, 
			ELEVATOR_UP_I = 0.0003,
			ELEVATOR_UP_D = 0,
			ELEVATOR_DOWN_P = 0.02, 
			ELEVATOR_DOWN_I = 0.0003, 
			ELEVATOR_DOWN_D = 0;
		
		public static final int 
			ELEVATOR_TOLERANCE = 15, 
			IZONE = 20000;
	}

	public static class Heights {
		public static final double
				GROUND = 0,
				SCALE_HEIGHT_HIGH = 75,
				SCALE_HEIGHT_MID = 60,
				SCALE_HEIGHT_LOW = 48,
				SWITCH_HEIGHT = 24,
				HINGE_HEIGHT = 20,
				BOX_HEIGHT = 4.75,
				TOP = 75;
	}
	
	public static final boolean
			ENCODER_REVERSED = true,
			REVERSED = false;
	
	public static final double MAX_CURRENT = 100;

	public static final long MAX_CURRENT_TIME = 1000;
}
