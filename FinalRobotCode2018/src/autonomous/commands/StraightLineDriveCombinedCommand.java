package autonomous.commands;

import constants.IntakeConstants;
import robotcode.driving.DriveTrain;
import robotcode.driving.DriveTrain.LinearVelocity;
import robotcode.driving.DriveTrain.RotationalVelocity;
import robotcode.systems.Elevator;
import robotcode.systems.Grabber;
import robotcode.systems.Grabber.GrabberState;
import robotcode.systems.Intake;
import robotcode.systems.IntakeHingeMotor;
import robotcode.systems.IntakeHingePiston;

public class StraightLineDriveCombinedCommand extends BaseAutonomousCommand {

	
	private Elevator mElevator;
	private double mElevatorHeight;
	
	private Grabber mGrabber;
	private GrabberState mGrabberState;

	private DriveTrain mDriveTrain;	
	private double mAngle;
	private double mFinalVelocity;
	private long mTotalMillisecondsFull;
	private long mTotalMillisecondsAccel;
	private long mTotalMillisecondsDecel;
	
	private Intake mIntake;
	private boolean mRunningWheels;

	private IntakeHingePiston mHinge;
	private boolean mUp;
	private long mHingeDelay;
	
	public StraightLineDriveCombinedCommand(DriveTrain pDriveTrain, Elevator pElevator, double pElevatorHeight,
			Grabber pGrabber, GrabberState pGrabberState, double pAngle, double pFinalVelocity, long pTotalMillisecondsAccel,
			long pTotalMillisecondsFull, long pTotalMillisecondsDecel, Intake pIntake, boolean pRunningWheels,
			IntakeHingePiston mHinge2, boolean pUp, long pHingeDelay) {

		mElevator = pElevator;
		mElevatorHeight = pElevatorHeight;
		
		mGrabber = pGrabber;
		mGrabberState = pGrabberState;
		
		mDriveTrain = pDriveTrain;
		mAngle = pAngle;
		mFinalVelocity = pFinalVelocity;
		mTotalMillisecondsAccel = pTotalMillisecondsAccel;
		mTotalMillisecondsFull = pTotalMillisecondsFull;
		mTotalMillisecondsDecel = pTotalMillisecondsDecel;
		
		mIntake = pIntake;
		mRunningWheels = pRunningWheels;

		mHinge = mHinge2;
		mUp = pUp;
		mHingeDelay = pHingeDelay;
	}
	
	@Override
	public boolean RunCommand() {
		mElevator.moveElevator(mElevatorHeight);
		mGrabber.setState(mGrabberState);
		if(mRunningWheels) {
			mIntake.setWheelSpeed(IntakeConstants.INTAKE_WHEEL_SPEED);
		}
		else {
			mIntake.setWheelSpeed(0);
		}
		// compute what percent into the command we are:
		double speed;
		if (GetMillisecondsSinceStart() <= mTotalMillisecondsAccel) {
			double percentComplete = ((float) GetMillisecondsSinceStart()) / mTotalMillisecondsFull;
			speed = mFinalVelocity * percentComplete; // positive in acceleration, negative in deccel, 0 in constant
		}
		else if (GetMillisecondsSinceStart() <= mTotalMillisecondsFull + mTotalMillisecondsAccel) {
			speed = mFinalVelocity;
		}
		else if (GetMillisecondsSinceStart() <= mTotalMillisecondsFull + mTotalMillisecondsAccel
				+ mTotalMillisecondsDecel) {
			double percentComplete = ((float) (GetMillisecondsSinceStart() - mTotalMillisecondsAccel
					- mTotalMillisecondsFull)) / mTotalMillisecondsFull;
			speed = mFinalVelocity - (mFinalVelocity * percentComplete);
		}
		else {
			speed = 0;
		}

		boolean isThisCommandDone = GetMillisecondsSinceStart() > mTotalMillisecondsFull + mTotalMillisecondsAccel + mTotalMillisecondsDecel;

		mDriveTrain.enactMovement(mDriveTrain.getRobotAngle(), mAngle, LinearVelocity.NORMAL, RotationalVelocity.NONE,
				speed);
		
		if (GetMillisecondsSinceStart() > mHingeDelay) {
			if (mUp) {
				mHinge.up();
			} 
			else {
				mHinge.down();
			}
		}

		return isThisCommandDone;
	}

}
