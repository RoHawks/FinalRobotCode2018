package constants;

public class RunConstants {
	public static boolean
		RUNNING_DRIVE = true,
		RUNNING_PNEUMATICS = true,
		RUNNING_INTAKE = true,
		RUNNING_ELEVATOR = true,
		RUNNING_GRABBER = true,
		SIMULATOR = false,
		IS_PROTOTYPE = false,
		RUNNING_EVERYTHING = RUNNING_DRIVE && RUNNING_PNEUMATICS && RUNNING_INTAKE && RUNNING_ELEVATOR
			&& RUNNING_GRABBER;
	// USING_CUSTOM_JOYSTICK = false,
	// RUN_COMPRESSOR_ALWAYS = false,
}