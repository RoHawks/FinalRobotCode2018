package robotcode.systems;

import com.ctre.phoenix.motorcontrol.ControlMode;

import constants.ElevatorConstants;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import sensors.ElevatorEncoder;
import simulator.talon.TalonInterface;

public class Elevator {

	private TalonInterface mMotor;
	private ElevatorEncoder mEncoder;
	private Joystick mJoystick;

	private ElevatorState mElevatorState = ElevatorState.BOX_HEIGHT;

	private ElevatorDirection mElevatorDir = ElevatorDirection.NONE;

	private boolean mIsEnabled = true;
	
	private double mCurrentPosition;

	public boolean IsEnabled() {
		return mIsEnabled;
	}

	public void enable(boolean pIsEnabled) {
		mIsEnabled = pIsEnabled;
	}

	public Elevator(TalonInterface pMotor, ElevatorEncoder pEncoder, Joystick pJoystick) {
		mEncoder = pEncoder;
		mMotor = pMotor;
		mJoystick = pJoystick;
		mCurrentPosition = mEncoder.getRawTicks();
	}

	public enum ElevatorState {
		GROUND,
		JOYSTICK,
		SCALE_HIGH,
		SCALE_MID,
		SCALE_LOW,
		SWITCH,
		BOX_HEIGHT,
		TOP;
	}

	public enum ElevatorDirection {
		UP,
		DOWN,
		NONE;
	}

	public void setPIDConstants() {
		SmartDashboard.putString("Elevator Dir", mElevatorDir.toString());
		switch (mElevatorDir) {
			case UP:
				mMotor.config_kP(0, ElevatorConstants.PID.ELEVATOR_UP_P, 10);
				mMotor.config_kI(0, ElevatorConstants.PID.ELEVATOR_UP_I, 10);
				mMotor.config_kD(0, ElevatorConstants.PID.ELEVATOR_UP_D, 10);
				break;
			case DOWN:
				mMotor.config_kP(0, ElevatorConstants.PID.ELEVATOR_DOWN_P, 10);
				mMotor.config_kI(0, ElevatorConstants.PID.ELEVATOR_DOWN_I, 10);
				mMotor.config_kD(0, ElevatorConstants.PID.ELEVATOR_DOWN_D, 10);
				break;
			default:
				break;
		}
	}

	/**
	 * set elevator speed
	 * 
	 * @param speed
	 */
	public void setSpeed(double speed) {
		if (mIsEnabled) {
			SmartDashboard.putNumber("set speed", speed);
			mMotor.set(ControlMode.PercentOutput, speed);
		} 
		else {
			mMotor.set(ControlMode.PercentOutput, 0);
		}
	}

	public boolean IsAtBottom() {
		return mMotor.getSensorCollection_isRevLimitSwitchClosed();
	}

	public void setCurrentPosition() {
		mCurrentPosition = mEncoder.getRawTicks();
	}
	
	public void stop() {
		setSpeed(0);
	}

	public void setGround() {
		mElevatorState = ElevatorState.GROUND;
		//moveElevator(ElevatorConstants.Heights.GROUND);
		if(IsAtBottom()) {
			mMotor.set(0);
		}
		else if(isCloseToTarget(ElevatorConstants.Heights.GROUND, 7)) {
			mMotor.set(-0.3);
		}
		else {
			mMotor.set(-0.8);
		}
	}

	public void setSwitch() {
		mElevatorState = ElevatorState.SWITCH;
		moveElevator(ElevatorConstants.Heights.SWITCH_HEIGHT);
	}

	public void setScaleLow() {
		mElevatorState = ElevatorState.SCALE_LOW;
		moveElevator(ElevatorConstants.Heights.SCALE_HEIGHT_LOW);
	}

	public void setScaleMid() {
		mElevatorState = ElevatorState.SCALE_MID;
		moveElevator(ElevatorConstants.Heights.SCALE_HEIGHT_MID);
	}

	public void setScaleHigh() {
		mElevatorState = ElevatorState.SCALE_HIGH;
		moveElevator(ElevatorConstants.Heights.SCALE_HEIGHT_HIGH);
	}

	public void setBox() {
		mElevatorState = ElevatorState.BOX_HEIGHT;
		moveElevator(ElevatorConstants.Heights.BOX_HEIGHT);
	}
	
	public void setTop() {
		mElevatorState = ElevatorState.TOP;
		moveElevator(ElevatorConstants.Heights.TOP);
	}

	public void setJoystickBasic() {
		mElevatorState = ElevatorState.JOYSTICK;
		if (Math.abs(mJoystick.getY()) > 0.25 && mIsEnabled) {
			mMotor.set(-mJoystick.getY() * mJoystick.getY() * Math.signum(mJoystick.getY()));
		}
		else {
			mMotor.set(0);
		}
	}

	public void setJoystickAdvanced() {
		mElevatorState = ElevatorState.JOYSTICK;
		setElevatorDirection();
		ElevatorDirection dir = getElevatorDirection();
		SmartDashboard.putNumber("Joystick2 Y", mJoystick.getY());
		if(mMotor.getSensorCollection_isFwdLimitSwitchClosed() || Math.abs(mJoystick.getY()) < 0.25) {
			mMotor.set(ControlMode.Position, mCurrentPosition - 60);
			SmartDashboard.putString("Is in Advanced joystick PID loop", "yes");
		}
		else if (Math.abs(mJoystick.getY()) > 0.25 && mIsEnabled) {
			double speed = -mJoystick.getY() * mJoystick.getY() * Math.signum(mJoystick.getY());
			if (dir == ElevatorDirection.UP && isCloseToTarget(ElevatorConstants.Heights.TOP, 5)) {
				speed *= 0.5;
			}
			else if (dir == ElevatorDirection.DOWN && isCloseToTarget(ElevatorConstants.Heights.GROUND, 5)) {
				speed *= 0.5;
			}
			mMotor.set(speed);
			mCurrentPosition = mEncoder.getRawTicks();
			SmartDashboard.putString("Is in Advanced joystick PID loop", "no");
		}
		else {
			mMotor.set(0);
			mCurrentPosition = mEncoder.getRawTicks();
			SmartDashboard.putString("Is in Advanced joystick PID loop", "no");
		}
	}

	/**
	 * sets the elevator to go to a certain height
	 * 
	 * @param height
	 */
	public void moveElevator(double height) {
		if (mIsEnabled) {
			double goal = ElevatorEncoder.InchToTick(height);
			setElevatorDirection();
			setPIDConstants();
			SmartDashboard.putNumber("goal", goal);
			mMotor.set(ControlMode.Position, goal);
			SmartDashboard.putNumber("Elevator Raw Ticks", mEncoder.getRawTicks());
			SmartDashboard.putNumber("Elevator Error", ElevatorEncoder.TickToInch(mEncoder.getRawTicks() - goal));
			SmartDashboard.putNumber("Elevator Error from Talon", mMotor.getClosedLoopError(0)); //TZDC Check
			SmartDashboard.putNumber("Talon error value", mMotor.getClosedLoopError(0));
			SmartDashboard.putNumber("Elevator motor output", mMotor.getMotorOutputVoltage());
			SmartDashboard.putNumber("Elevator Talon Current", mMotor.getOutputCurrent());
		} 
		else {
			mMotor.set(ControlMode.PercentOutput, 0);
		}
	}

	/**
	 * finds direction the elevator will move
	 */
	private void setElevatorDirection() {
		if (mEncoder.getHeightInInchesFromElevatorBottom() > getHeightGoal(mElevatorState)) {
			mElevatorDir = ElevatorDirection.DOWN;
		} 
		else if (mEncoder.getHeightInInchesFromElevatorBottom() < getHeightGoal(mElevatorState)) {
			mElevatorDir = ElevatorDirection.UP;
		} 
		else {
			mElevatorDir = ElevatorDirection.NONE;
		}
	}

	/**
	 * height of the "target"
	 * 
	 * @param mode
	 *            Elevator mode
	 * @return height
	 */
	private double getHeightGoal(ElevatorState mode) {
		switch (mode) {
		case GROUND:
			return ElevatorConstants.Heights.GROUND;
		case JOYSTICK:
			return Integer.MIN_VALUE * Math.signum(mJoystick.getY());
		case SCALE_HIGH:
			return ElevatorConstants.Heights.SCALE_HEIGHT_HIGH;
		case SCALE_MID:
			return ElevatorConstants.Heights.SCALE_HEIGHT_MID;
		case SCALE_LOW:
			return ElevatorConstants.Heights.SCALE_HEIGHT_LOW;
		case SWITCH:
			return ElevatorConstants.Heights.SWITCH_HEIGHT;
		case BOX_HEIGHT:
			return ElevatorConstants.Heights.BOX_HEIGHT;
		case TOP:
			return ElevatorConstants.Heights.TOP;
		default:
			return mEncoder.getHeightInInchesFromElevatorBottom();
		}
	}

	/**
	 * @return elevator mode
	 */
	public ElevatorState getElevatorMode() {
		return mElevatorState;
	}

	/**
	 * @return elevator direction
	 */
	public ElevatorDirection getElevatorDirection() {
		return mElevatorDir;
	}

	/**
	 * @return speed of elevator
	 */
	public double getSpeed() {
		return mMotor.getMotorOutputVoltage();
	}

	/**
	 * @return height of the elevator in inches
	 */
	public double getHeightInches() {
		return mEncoder.getHeightInInchesFromElevatorBottom();
	}

	public boolean isAboveTarget() {
		return getHeightInches() > this.getHeightGoal(mElevatorState);
	}
	
	public boolean isCloseToTargetUsingPID() {
		return isCloseToTargetUsingPID(200);
	}
	
	public boolean isCloseToTargetUsingPID(double pTickTolerance) {
		return mMotor.getClosedLoopError(0) < pTickTolerance;
	}

	public boolean isCloseToTarget() {
		SmartDashboard.putNumber("Height Goal -- close to target", this.getHeightGoal(mElevatorState));
		SmartDashboard.putNumber("Height Inches -- close to target", getHeightInches());
		return isCloseToTarget(getHeightGoal(mElevatorState), 1);
	}

	public boolean isCloseToTarget(double pHeight, double pTolerance) { // Everything in inches
		return Math.abs(pHeight - getHeightInches()) < pTolerance;
	}
}