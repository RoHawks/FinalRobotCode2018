package autonomous.routines;

import java.util.ArrayList;

import autonomous.AutonomousRoutine;
import autonomous.commands.AutonomousCommand;
import autonomous.commands.IntakeCubeCommand;
import autonomous.commands.ScoreCommand;
import autonomous.commands.SetElevatorHeightCommand;
import autonomous.commands.SetGrabberCommand;
import autonomous.commands.StopCommand;
import autonomous.commands.StraightLineDriveCombinedCommand;
import autonomous.commands.TurnRobotToAngle;
import autonomous.commands.TurnWheelsToAngle;
import constants.AutoConstants;
import constants.ElevatorConstants;
import robotcode.driving.DriveTrain;
import robotcode.systems.Elevator;
import robotcode.systems.Grabber;
import robotcode.systems.Grabber.GrabberState;
import robotcode.systems.Intake;
import robotcode.systems.IntakeHingeMotor;
import robotcode.systems.IntakeHingePiston;

public class SwitchRoutineNoElevator implements AutonomousRoutine {
	
	private DriveTrain mDriveTrain;
	private Elevator mElevator;
	private Grabber mGrabber;
	private IntakeHingePiston mHinge;
	private Intake mIntake;
	
	public SwitchRoutineNoElevator(DriveTrain pDriveTrain, Elevator pElevator, Grabber pGrabber, IntakeHingePiston mHinge2, Intake pIntake) {
		mDriveTrain = pDriveTrain;
		mElevator = pElevator;
		mGrabber = pGrabber;
		mHinge = mHinge2;
		mIntake = pIntake;
	}

	@Override
	public ArrayList<AutonomousCommand> getAutonomousCommands() {

		double actualAngle;
		long actualDriveTime;
		double goBackAngle;
		double scaleAngle;
		if (autonomous.PlateAssignmentReader.GetNearSwitchSide() == 'L') {
			actualAngle = AutoConstants.SwitchMiddle.LEFT_ANGLE;
			actualDriveTime = AutoConstants.SwitchMiddle.SIDEWAYS_FORWARD_FULL_SPEED_DRIVE_TIME_LEFT;
			goBackAngle = - AutoConstants.SwitchMiddle.GO_BACK_ANGLE;
			scaleAngle = -90.0;
		} 
		else {
			actualAngle = AutoConstants.SwitchMiddle.RIGHT_ANGLE;
			actualDriveTime = AutoConstants.SwitchMiddle.SIDEWAYS_FORWARD_FULL_SPEED_DRIVE_TIME_RIGHT;
			goBackAngle = AutoConstants.SwitchMiddle.GO_BACK_ANGLE;
			scaleAngle = -270.0;
		}


		ArrayList<AutonomousCommand> returnValue = new ArrayList<AutonomousCommand>();

		// point wheels towards correct plate
		returnValue.add(new TurnWheelsToAngle(mDriveTrain, actualAngle));

		returnValue.add(new StraightLineDriveCombinedCommand(
				mDriveTrain, 
				mElevator, 
				ElevatorConstants.Heights.SWITCH_HEIGHT,
				mGrabber, 
				GrabberState.OUT_GRAB, 
				actualAngle, 
				AutoConstants.SwitchMiddle.FORWARD_SPEED, 
				AutoConstants.SwitchMiddle.FORWARD_ACCELERATION_TIME, 
				actualDriveTime, 
				AutoConstants.SwitchMiddle.FORWARD_DECELERATION_TIME,
				mIntake,
				false,
				mHinge, 
				true, 
				20000)); //drive to plate

		returnValue.add(new StopCommand(mDriveTrain));
		
		returnValue.add(new ScoreCommand(mGrabber));

//		returnValue.add(new HingeCommand(mHinge, false));
		
		returnValue.add(new StraightLineDriveCombinedCommand(
				mDriveTrain, 
				mElevator, 
				ElevatorConstants.Heights.SWITCH_HEIGHT,
				mGrabber, 
				GrabberState.IN_RELEASE, 
				goBackAngle, 
				AutoConstants.SwitchMiddle.BACKWARDS_SPEED, 
				AutoConstants.SwitchMiddle.BACKWARDS_ACCELERATION_TIME, 
				AutoConstants.SwitchMiddle.BACKWARDS_FULL_SPEED_DRIVE_TIME, 
				AutoConstants.SwitchMiddle.BACKWARDS_DECELERATION_TIME,
				mIntake,
				false,
				mHinge, 
				false, 
				1000));
		//drive backwards to align with cube pyramid
		
		returnValue.add(new TurnRobotToAngle(mDriveTrain, 0)); // make sure going in straight
		
		returnValue.add(new StopCommand(mDriveTrain));
		
//		returnValue.add(new StraightLineDriveCombinedCommand(
//				mDriveTrain, 
//				mElevator, 
//				ElevatorConstants.Heights.BOX_HEIGHT,
//				mGrabber, 
//				GrabberState.IN_RELEASE, 
//				0, 
//				AutoConstants.SwitchMiddle.INTAKE_FORWARD_SPEED, 
//				0, 
//				AutoConstants.SwitchMiddle.INTAKE_FULL_SPEED_DRIVE_TIME, 
//				AutoConstants.SwitchMiddle.INTAKE_DECELERATION_TIME,
//				mIntake,
//				true,
//				mHinge, 
//				false, 
//				0));
//		// go forward
//		
//		returnValue.add(new StopCommand(mDriveTrain));
//		
//		returnValue.add(new IntakeCubeCommand(mIntake));
//		//intake
//		
//		returnValue.add(new SetElevatorHeightCommand(mElevator, ElevatorConstants.Heights.GROUND));
//		//elevator on ground
//		
//		returnValue.add(new SetGrabberCommand(mGrabber, GrabberState.IN_GRAB));
//		//grab it
//		
//		returnValue.add(new TurnWheelsToAngle(mDriveTrain, scaleAngle));
//		// turn wheels to scale
//		
//		returnValue.add(new StraightLineDriveCombinedCommand(
//				mDriveTrain, 
//				mElevator, 
//				ElevatorConstants.Heights.GROUND,
//				mGrabber, 
//				GrabberState.IN_GRAB, 
//				scaleAngle, 
//				AutoConstants.SwitchMiddle.FORWARD_SPEED, 
//				AutoConstants.SwitchMiddle.FORWARD_ACCELERATION_TIME, 
//				AutoConstants.SwitchMiddle.SIDEWAYS_TO_SCALE_FULL_SPEED_DRIVE_TIME, 
//				AutoConstants.SwitchMiddle.FORWARD_DECELERATION_TIME,
//				mIntake,
//				true,
//				mHinge, 
//				false, 
//				0));
//		//go sideways
//		
//		returnValue.add(new StopCommand(mDriveTrain));
//		
//		returnValue.add(new TurnRobotToAngle(mDriveTrain, -scaleAngle));
//				
//		returnValue.add(new StopCommand(mDriveTrain));
		
		return returnValue;
		
	}
	
}
