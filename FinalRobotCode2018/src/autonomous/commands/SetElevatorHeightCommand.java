package autonomous.commands;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import robotcode.systems.Elevator;

public class SetElevatorHeightCommand extends BaseAutonomousCommand{

	private Elevator mElevator;
	private double mHeight;
	private long mTimeClose;
	private boolean mIsClose = false;
	
	public SetElevatorHeightCommand(Elevator pElevator, double pHeight) {
		mElevator = pElevator;
		mHeight = pHeight;
	}
	
	@Override
	public boolean RunCommand() {
		mElevator.moveElevator(mHeight);
		SmartDashboard.putBoolean("AUTO_TEST is close to target ", mElevator.isCloseToTargetUsingPID());
		if(mElevator.isCloseToTarget(mHeight, 0.5) && !mIsClose) {
			mIsClose = true;
			mTimeClose = System.currentTimeMillis();
		}
		return mElevator.isCloseToTargetUsingPID() && System.currentTimeMillis() - mTimeClose > 500;
	}

}
