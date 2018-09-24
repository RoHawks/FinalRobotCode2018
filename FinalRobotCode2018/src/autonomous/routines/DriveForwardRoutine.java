package autonomous.routines;

import java.util.ArrayList;

import autonomous.AutonomousRoutine;
import autonomous.commands.AutonomousCommand;
import autonomous.commands.StopCommand;
import autonomous.commands.StraightLineDriveCombinedCommand;
import autonomous.commands.TurnWheelsToAngle;
import constants.AutoConstants;
import constants.ElevatorConstants;
import robotcode.driving.DriveTrain;
import robotcode.systems.Elevator;
import robotcode.systems.Grabber;
import robotcode.systems.Grabber.GrabberState;
import robotcode.systems.Intake;
import robotcode.systems.IntakeHingeMotor;

public class DriveForwardRoutine implements AutonomousRoutine {

	private DriveTrain mDriveTrain;
	private Elevator mElevator;
	private Grabber mGrabber;
	private Intake mIntake;
	private IntakeHingeMotor mHinge;
	
	public DriveForwardRoutine(DriveTrain pDriveTrain, Elevator pElevator, Grabber pGrabber, Intake pIntake, IntakeHingeMotor pHinge) {
		mDriveTrain = pDriveTrain;
		mElevator = pElevator;
		mGrabber = pGrabber;
		mIntake = pIntake;
		mHinge = pHinge;
	}

	@Override
	public ArrayList<AutonomousCommand> getAutonomousCommands() {
		
		ArrayList<AutonomousCommand> returnValue = new ArrayList<AutonomousCommand>();
	
		returnValue.add(new TurnWheelsToAngle(mDriveTrain, 0)); //point wheels forward

		returnValue.add(new StraightLineDriveCombinedCommand(
				mDriveTrain, 
				mElevator, 
				ElevatorConstants.Heights.SWITCH_HEIGHT,
				mGrabber, 
				GrabberState.IN_GRAB, 
				0, 
				AutoConstants.DriveForward.FORWARD_SPEED, 
				AutoConstants.DriveForward.FORWARD_ACCELERATION_TIME, 
				AutoConstants.DriveForward.FORWARD_DRIVE_TIME, 
				AutoConstants.DriveForward.FORWARD_DECELERATION_TIME,
				mIntake,
				false,
				mHinge,
				true,
				20000)); //20000ms > 15 sec
		//go forward
		
		returnValue.add(new StopCommand(mDriveTrain));
		//stop
//		//Accelerate
//		returnValue.add(new StraightLineDriveCommand(
//				mDriveTrain,
//				mElevator, ElevatorConstants.Heights.SWITCH_HEIGHT,
//				WHEEL_ANGLE,
//				0.0,
//				ACCELERATE_TO_SPEED,
//				ACCELERATION_TIME));
//
//		//Drive full speed
//		returnValue.add(new StraightLineDriveCommand(
//				mDriveTrain,
//				mElevator, ElevatorConstants.Heights.SWITCH_HEIGHT,
//				WHEEL_ANGLE,
//				ACCELERATE_TO_SPEED,
//				ACCELERATE_TO_SPEED,
//				DRIVE_FULL_SPEED_TIME));
//
//		//Slow down then stop
//		returnValue.add(new StraightLineDriveCommand(
//				mDriveTrain,
//				mElevator, ElevatorConstants.Heights.SWITCH_HEIGHT,
//				WHEEL_ANGLE,
//				ACCELERATE_TO_SPEED,
//				0.0,
//				DECELERATION_TIME));


		return returnValue;
	}

}
