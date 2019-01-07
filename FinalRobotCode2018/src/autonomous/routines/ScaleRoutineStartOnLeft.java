package autonomous.routines;

import java.util.ArrayList;

import autonomous.AutonomousRoutine;
import autonomous.PlateAssignmentReader;
import autonomous.commands.AutonomousCommand;
import autonomous.commands.ScoreCommand;
import autonomous.commands.SetElevatorHeightCommand;
import autonomous.commands.SetGrabberCommand;
import autonomous.commands.StopCommand;
import autonomous.commands.StraightLineDriveCombinedCommand;
import autonomous.commands.StraightLineDriveCommand;
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

public class ScaleRoutineStartOnLeft implements AutonomousRoutine {
	
	private DriveTrain mDriveTrain;
	private Elevator mElevator;	
	private Grabber mGrabber;
	private Intake mIntake;
	private IntakeHingePiston mHinge;

	
	public ScaleRoutineStartOnLeft(DriveTrain pDriveTrain, Elevator pElevator, Grabber pGrabber, Intake pIntake, IntakeHingePiston mHinge2) {
		mDriveTrain = pDriveTrain;
		mElevator = pElevator;
		mGrabber = pGrabber;
		mIntake = pIntake;
		mHinge = mHinge2;
	}
	
	@Override
	public ArrayList<AutonomousCommand> getAutonomousCommands() {

		ArrayList<AutonomousCommand> returnValue = new ArrayList<AutonomousCommand>();

		//if scale is on our side, go for SCALE -- LLL, RLR -- then hunting
		if(PlateAssignmentReader.GetScaleSide() == 'L') 
		{
			returnValue.add(new TurnWheelsToAngle(mDriveTrain, 0)); //make sure wheels are forward
						
			returnValue.add(new StraightLineDriveCombinedCommand(
					mDriveTrain,
					mElevator, 
					ElevatorConstants.Heights.SWITCH_HEIGHT,
					mGrabber, 
					GrabberState.IN_GRAB,
					0,
					AutoConstants.Side.Scale.FINAL_VELOCITY,
					AutoConstants.Side.Scale.ACCELERATION_TIME,
					AutoConstants.Side.Scale.FULL_SPEED_TIME, 
					AutoConstants.Side.Scale.DECELERATION_TIME,
					mIntake, 
					false, 
					mHinge,
					true,
					20000)); //drive up to side of scale -- do we want out or no
		
			returnValue.add(new TurnRobotToAngle(mDriveTrain, 90)); //turn to face scale
			
			returnValue.add(new StopCommand(mDriveTrain)); // just in case
			
			returnValue.add(new SetElevatorHeightCommand(mElevator, ElevatorConstants.Heights.TOP)); // up the elevator
			
			returnValue.add(new StraightLineDriveCommand(
					mDriveTrain,
					mElevator, 
					ElevatorConstants.Heights.TOP,
					0, // angle
					AutoConstants.Side.Scale.SHORT_FINAL_VELOCITY, // initial velocity 
					AutoConstants.Side.Scale.SHORT_FINAL_VELOCITY, 
					AutoConstants.Side.Scale.SHORT_DRIVE_TIME)); // tiny bit forward
			
			returnValue.add(new ScoreCommand(mGrabber)); //S C O R E !
			
			returnValue.add(new SetGrabberCommand(mGrabber, GrabberState.IN_RELEASE));
			
			returnValue.add(new StraightLineDriveCommand(
					mDriveTrain,
					mElevator, 
					ElevatorConstants.Heights.TOP,
					180, // angle
					AutoConstants.Side.Scale.SHORT_FINAL_VELOCITY, // initial velocity 
					AutoConstants.Side.Scale.SHORT_FINAL_VELOCITY, 
					AutoConstants.Side.Scale.SHORT_DRIVE_TIME)); // tiny bit backwards
			
			returnValue.add(new StopCommand(mDriveTrain));
			
			returnValue.add(new SetElevatorHeightCommand(mElevator, ElevatorConstants.Heights.SWITCH_HEIGHT));
		}
		
		//if scale is not on our side but switch is, go for SWITCH -- LRL -- then hunting
		else if(PlateAssignmentReader.GetNearSwitchSide() == 'L') 
		{
			returnValue.add(new TurnWheelsToAngle(mDriveTrain, 0)); //make sure wheels are forward
			
			returnValue.add(new StraightLineDriveCombinedCommand(
					mDriveTrain,
					mElevator, 
					ElevatorConstants.Heights.SWITCH_HEIGHT,
					mGrabber, 
					GrabberState.IN_GRAB,
					0,
					AutoConstants.Side.Switch.FINAL_VELOCITY,
					AutoConstants.Side.Switch.ACCELERATION_TIME,
					AutoConstants.Side.Switch.FULL_SPEED_TIME, 
					AutoConstants.Side.Switch.DECELERATION_TIME,
					mIntake,
					false,
					mHinge, 
					true, 
					20000)); //drive up to side of switch
		
			returnValue.add(new TurnRobotToAngle(mDriveTrain, 90)); //turn to face switch
			
			returnValue.add(new StopCommand(mDriveTrain)); //just in case
			
			returnValue.add(new StraightLineDriveCommand(
					mDriveTrain,
					mElevator, ElevatorConstants.Heights.SWITCH_HEIGHT,
					0, // angle
					AutoConstants.Side.Switch.SHORT_FINAL_VELOCITY, // initial velocity 
					AutoConstants.Side.Switch.SHORT_FINAL_VELOCITY, 
					AutoConstants.Side.Switch.SHORT_DRIVE_TIME)); // tiny bit forward
			
			returnValue.add(new ScoreCommand(mGrabber)); //S C O R E !

			returnValue.add(new SetGrabberCommand(mGrabber, GrabberState.IN_RELEASE));

			returnValue.add(new StraightLineDriveCommand(
					mDriveTrain,
					mElevator, 
					ElevatorConstants.Heights.SWITCH_HEIGHT,
					180, // angle
					AutoConstants.Side.Switch.SHORT_FINAL_VELOCITY, // initial velocity 
					AutoConstants.Side.Switch.SHORT_FINAL_VELOCITY, 
					AutoConstants.Side.Switch.SHORT_DRIVE_TIME)); // tiny bit backwards
			
			returnValue.add(new StopCommand(mDriveTrain));
			
		}
		//if neither scale nor switch are on our side, just go forward -- RRR -- then on_way_to_exchange? how do diff :O
		else {
			returnValue.add(new TurnWheelsToAngle(mDriveTrain, 0));
			
			returnValue.add(new StraightLineDriveCombinedCommand(
					mDriveTrain,
					mElevator, 
					ElevatorConstants.Heights.SWITCH_HEIGHT,
					mGrabber, 
					mGrabber.getGrabberState(), 
					0,
					AutoConstants.Side.Mobility.FINAL_VELOCITY,
					AutoConstants.Side.Mobility.ACCELERATION_TIME, 
					AutoConstants.Side.Mobility.FULL_SPEED_TIME, 
					AutoConstants.Side.Mobility.DECELERATION_TIME,
					mIntake,
					false,
					mHinge, 
					true, 
					20000));

			returnValue.add(new StopCommand(mDriveTrain));
		}
		
		return returnValue;
	}

}
