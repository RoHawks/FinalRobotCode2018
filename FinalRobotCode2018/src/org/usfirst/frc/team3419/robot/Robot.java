package org.usfirst.frc.team3419.robot;

import java.util.ArrayList;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.kauailabs.navx.frc.AHRS;

import autonomous.AutonomousRoutines;
import autonomous.AutonomousSequence;
import autonomous.commands.AutonomousCommand;
import autonomous.routines.DoNothingRoutine;
import autonomous.routines.DriveForwardRoutine;
import autonomous.routines.ScaleRoutineStartOnLeft;
import autonomous.routines.ScaleRoutineStartOnRight;
import autonomous.routines.SwitchRoutine;
import autonomous.routines.SwitchRoutineNoElevator;
import autonomous.sequence.DoNothingSequence;
import autonomous.sequence.DriveForwardNoElevatorSequence;
import autonomous.sequence.SwitchSequence;
import constants.AutoConstants;
import constants.DriveConstants;
import constants.ElevatorConstants;
import constants.GrabberConstants;
import constants.HingeConstants;
import constants.IntakeConstants;
import constants.JoystickConstants;
import constants.Ports;
import constants.RunConstants;
import constants.States;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import resource.ResourceFunctions;
import robotcode.driving.DriveTrain;
import robotcode.driving.Wheel;
import robotcode.systems.Elevator;
import robotcode.systems.Grabber;
import robotcode.systems.Intake;
import robotcode.systems.IntakeHingeMotor;
import robotcode.systems.IntakeHingePiston;
import robotcode.systems.SingleSolenoidReal;
import sensors.ElevatorEncoder;
import sensors.RobotAngle;
import sensors.TalonAbsoluteEncoder;
import simulator.BreakbeamSimulator;
import simulator.DigitalInputActualImplementation;
import simulator.DigitalInputInterface;
import simulator.LimitSwitchSimulator;
import simulator.gyro.GyroActualImplementation;
import simulator.gyro.GyroInterface;
import simulator.gyro.GyroSimulator;
import simulator.solenoid.SingleSolenoidActualImplementation;
import simulator.solenoid.SingleSolenoidSimulator;
import simulator.talon.TalonActualImplementation;
import simulator.talon.TalonInterface;

@SuppressWarnings("deprecation")
public class Robot extends SampleRobot {

	private XboxController mController = new XboxController(Ports.XBOX);
	private Joystick mJoystick = new Joystick(Ports.JOYSTICK);

	private DriveTrain mDriveTrain;

	private Wheel[] mWheel = new Wheel[4];
	private TalonInterface[] mTurn = new TalonInterface[4];
	private TalonInterface[] mDrive = new TalonInterface[4];
	private TalonAbsoluteEncoder[] mEncoder = new TalonAbsoluteEncoder[4];

	private GyroInterface mNavX;
	private RobotAngle mRobotAngle;

	private PowerDistributionPanel mPDP;
	private Compressor mCompressor;
	// private boolean mShouldRunCompressor = false;

	private simulator.solenoid.SolenoidInterface mLeftPiston, mRightPiston;
	private DigitalInputInterface mLimitSwitch, mBreakbeam;

	private TalonInterface mLeftIntakeWheel, mRightIntakeWheel;
	private simulator.solenoid.SolenoidInterface mHingePiston;
	private IntakeHingePiston mHinge;
	private Intake mIntake;
	private TalonInterface mRightIntakeHinge, mLeftIntakeHinge;
	//private IntakeHingeMotor mHinge;

	private TalonInterface mElevatorTalon;
	private ElevatorEncoder mElevEncoder;
	private simulator.solenoid.SolenoidInterface mGrab, mExtend;
	private Grabber mGrabber;
	private Elevator mElevator;

	private boolean mInGame = false;

	private long mGameStartMillis;
	private States mCurrentState = States.Initial_If_Holding_Box;
	private States mInitialState = States.Initial_If_Holding_Box;

	private long mTimeLastCycleStarted;
	
	private AutonomousRoutines mAutonomousRoutine = AutonomousRoutines.SWITCH_SCORE;
	
	public Robot() {
		
	}

	@Override
	public void robotInit() {
		UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
		camera.setResolution(240, 180);
		camera.setFPS(30);

		mNavX = GetGyroObject(Ports.NAVX);
		mCompressor = new Compressor(Ports.COMPRESSOR);
		mPDP = new PowerDistributionPanel();

		if (RunConstants.RUNNING_DRIVE) {
			DriveInit();
		}
		if (RunConstants.RUNNING_INTAKE) {
			IntakeAndHingeInit();
		}
		if (RunConstants.RUNNING_ELEVATOR) {
			ElevInit();
		}
		if (RunConstants.RUNNING_GRABBER) {
			GrabberInit();
		}
		
	}

	@Override
	public void autonomous() {

		int currentStep = 0;
		int previousStep = -1;

		startGame();
		ArrayList<AutonomousCommand> autonomousCommands = getAutoSequence();
		long autoTimeStart = System.currentTimeMillis();

		// Do not do anything until data from FMS arrives:
		while ((isAutonomous() && isEnabled())
				&& (System.currentTimeMillis() - autoTimeStart > AutoConstants.SwitchMiddle.INITIAL_PAUSE)
				|| (autonomous.PlateAssignmentReader.GetNearSwitchSide() == 'U')) {
			Timer.delay(0.005);
		}

		boolean isComplete = false;

		while (isAutonomous() && isEnabled() && !isComplete) {
			log();
			//isComplete = mAutonomousSequence.run();

			SmartDashboard.putNumber("Autonomous step", currentStep);
			if (currentStep < autonomousCommands.size()) {
				AutonomousCommand command = autonomousCommands.get(currentStep);
				if (currentStep != previousStep) {
					command.Startup();
					previousStep = currentStep;
				}

				boolean moveToNextStep = command.RunCommand();
				if (moveToNextStep) {
					currentStep++;
				}
			} // else we're done with auto.

			Timer.delay(0.005);
		}
		
		SetNewState(mInitialState);
	}

	private ArrayList<AutonomousCommand> getAutoSequence() {
		
		switch (mAutonomousRoutine) {
			case SCALE_SCORE_START_ON_LEFT:
				return new ScaleRoutineStartOnLeft(mDriveTrain, mElevator, mGrabber, mIntake, mHinge).getAutonomousCommands();
			case SCALE_SCORE_START_ON_RIGHT:
				return new ScaleRoutineStartOnRight(mDriveTrain, mElevator, mGrabber, mIntake, mHinge).getAutonomousCommands();
			case SWITCH_SCORE:
				mInitialState = States.Hunting;
				return new SwitchRoutineNoElevator(mDriveTrain, mElevator, mGrabber, mHinge, mIntake).getAutonomousCommands();
			case DRIVE_FORWARD:
				mInitialState = States.Initial_If_Holding_Box;
				return new DriveForwardRoutine(mDriveTrain, mElevator, mGrabber, mIntake, mHinge).getAutonomousCommands();			
			default:
				mInitialState = States.Initial_If_Holding_Box;
				return new DoNothingRoutine().getAutonomousCommands();
		}
	}

	public void operatorControl() {
		startGame();
		mTimeLastCycleStarted = System.currentTimeMillis();
		//SmartDashboard.putBoolean("breakbeam", mBreakbeam.get());
		while (isOperatorControl() && isEnabled()) {
			long timeCycleStart = System.currentTimeMillis();
			long lastCycleTime = timeCycleStart = mTimeLastCycleStarted;
			SmartDashboard.putNumber("Cycle Time", lastCycleTime);
			mTimeLastCycleStarted = timeCycleStart;
			log();
			
			if(RunConstants.RUNNING_DRIVE) {
				SwerveDrive();
				for (int i = 0; i < 4; i++) {
					SmartDashboard.putNumber("motor current " + i, mDrive[i].getMotorOutputPercent());
				}
			}
			
			if (RunConstants.RUNNING_EVERYTHING) {
				chooseModeMethod();
			}
			
			SmartDashboard.putString("Current State", mCurrentState.toString());

			if(RunConstants.RUNNING_ELEVATOR) {
				mElevator.setCurrentPosition();
				monitorElevatorCurrent();
				if (mJoystick.getRawButton(JoystickConstants.ENABLE_ELEVATOR) && !mElevator.IsEnabled()) {
					SetNewState(States.Manual_Elevator_Control);
					mElevator.enable(true);
				}
				SmartDashboard.putNumber("operatorcontrol: CURRENT TICKS", mElevEncoder.getRawTicks());
				SmartDashboard.putNumber("operatorcontrol: CURRENT HEIGHT INCHES??", mElevEncoder.getHeightInInchesFromElevatorBottom());
				SmartDashboard.putNumber("operatorcontrol: PID ERROR", mElevatorTalon.getClosedLoopError(0));
				SmartDashboard.putNumber("operatorcontrol: MOTOR OUTPUT", mElevatorTalon.getMotorOutputPercent());
				SmartDashboard.putNumber("operatorcontrol: JOYSTICK Y", mJoystick.getY());
			}
			
			if(RunConstants.RUNNING_INTAKE) {
				SmartDashboard.putBoolean("test limit", mLimitSwitch.get());
				SmartDashboard.putBoolean("breakbeam", mBreakbeam.get());
				SmartDashboard.putString("Intake State", mIntake.getIntakeState().toString());
				SmartDashboard.putString("Left Intake Piston",
						mLeftPiston.get().equals(Value.kForward) ? "Open" : "Closed");
				SmartDashboard.putString("Right Intake Piston",
						mRightPiston.get().equals(Value.kForward) ? "Open" : "Closed");
				SmartDashboard.putNumber("Left Intake Wheel", mLeftIntakeWheel.getMotorOutputPercent());
				SmartDashboard.putNumber("Right Intake Wheel", mRightIntakeWheel.getMotorOutputPercent());
			}
	
			if(RunConstants.RUNNING_GRABBER) {
				SmartDashboard.putString("Grabber L-R", mGrabber.getGrab().equals(Value.kReverse) ? "Grab" : "Release");
				SmartDashboard.putString("Grabber F-B", mGrabber.getExtend().equals(Value.kReverse) ? "In" : "Out");
			}
			
			for (int i = 1; i < 12; i++) {
				SmartDashboard.putBoolean("Button" + i, mJoystick.getRawButton(i));
			}
			
			Timer.delay(0.005); // wait for a motor update time
		}
	}

	private boolean mStartedMonitoringElevCurrent = false;
	private long mTimeStartedMonitoringElevCurrent;

	private void monitorElevatorCurrent() {

		if (mElevatorTalon.getOutputCurrent() > ElevatorConstants.MAX_CURRENT) {
			if (mStartedMonitoringElevCurrent
					&& mTimeStartedMonitoringElevCurrent > ElevatorConstants.MAX_CURRENT_TIME) {
				mElevator.enable(false);
			} 
			else {
				mStartedMonitoringElevCurrent = true;
				mTimeStartedMonitoringElevCurrent = System.currentTimeMillis();
			}
		} 
		else {
			mStartedMonitoringElevCurrent = false;
		}
		SmartDashboard.putBoolean("Started monitoring elevator current", mStartedMonitoringElevCurrent);
		SmartDashboard.putNumber("Time Started monitoring elev current", mTimeStartedMonitoringElevCurrent);
	}

	private void chooseModeMethod() {
		switch (mCurrentState) {
			case Hunting:
				Hunting();
				break;
			case Breakbeam_Tripped:
				Breakbeam_Tripped();
				break;
			case On_Way_To_Exchange:
				On_Way_To_Exchange();
				break;
			case Exchange_Score:
				Exchange_Score();
				break;
			case On_Way_To_Score_Switch:
				On_Way_To_Score_Switch();
				break;
			case Score:
				Score();
				break;
			case Defense:
				Defense();
				break;
			case Manual_Elevator_Control:
				Manual_Elevator_Control();
				break;
			case Initial_If_Holding_Box:
				InitialIfHoldingBox();
				break;
			default:
				throw new RuntimeException("Unknown state");
		}

		SmartDashboard.putString("Current State", mCurrentState.name());
	}

	private boolean mHasBegunScoringSequence = false;
	private long mScoringSequenceStartingTime = 0;

	private void Score() {
		mHinge.up();
		mIntake.disable();
		SmartDashboard.putBoolean("Close to Target -- score", mElevator.isCloseToTargetUsingPID());
		SmartDashboard.putBoolean("Has begun scoring-- score", mHasBegunScoringSequence);

		long scoringSequenceElapsedMilliseconds = System.currentTimeMillis() - mScoringSequenceStartingTime;
		SmartDashboard.putBoolean("mHasBegunScoringSequence", mHasBegunScoringSequence);
		SmartDashboard.putNumber("scoringSequenceElapsedMilliseconds", scoringSequenceElapsedMilliseconds);
		if (mHasBegunScoringSequence) {

			if (scoringSequenceElapsedMilliseconds < GrabberConstants.EXTEND_PISTON_OUT_TIME) {
				mGrabber.out();
				mGrabber.grab();
			} 
			else if (scoringSequenceElapsedMilliseconds < GrabberConstants.EXTEND_PISTON_OUT_TIME
					+ GrabberConstants.GRAB_PISTON_OUT_TIME) {
				mGrabber.out();
				mGrabber.release();
			} 
			else if (scoringSequenceElapsedMilliseconds < GrabberConstants.EXTEND_PISTON_OUT_TIME
					+ GrabberConstants.GRAB_PISTON_OUT_TIME + GrabberConstants.EXTEND_PISTON_IN_TIME) {
				mGrabber.in();
				mGrabber.release();
			} 
			else if (mJoystick.getRawButton(JoystickConstants.HUNTING)) {
				mHasBegunScoringSequence = false;
				SetNewState(States.Hunting);
			}
		} 
		else {
			mHasBegunScoringSequence = true;
			mScoringSequenceStartingTime = System.currentTimeMillis();
		}
	}

	private long mPickingUpBoxStartedGrabTime;
	private boolean mPickingUpBoxHasStartedGrab = false;
	
	private void On_Way_To_Score_Switch() {

		if (mElevator.IsAtBottom()) {
			SmartDashboard.putNumber("Grab time", System.currentTimeMillis());
			mGrabber.grab();
			if (!mPickingUpBoxHasStartedGrab) {
				mPickingUpBoxStartedGrabTime = System.currentTimeMillis();
			}
			mPickingUpBoxHasStartedGrab = true;
			if(GetMillisIntoState() > GrabberConstants.GRAB_PISTON_IN_TIME) {
				mIntake.disable();
			}
		} 
		else if (!mPickingUpBoxHasStartedGrab) {
			mElevator.setSpeed(-0.3);
		}
		

		if (mPickingUpBoxHasStartedGrab && System.currentTimeMillis() - mPickingUpBoxStartedGrabTime 
				> GrabberConstants.GRAB_PISTON_IN_TIME + 200) {
			SmartDashboard.putNumber("Change height time", System.currentTimeMillis());
			mElevator.setSwitch();
			if (mElevator.getHeightInches() > ElevatorConstants.Heights.HINGE_HEIGHT) {
				mHinge.up();
			}
			mIntake.disable();
			if (mJoystick.getRawButton(JoystickConstants.SCORE)) {
				SetNewState(States.Score);
				mPickingUpBoxHasStartedGrab = false;
			}
		}
		if (mJoystick.getRawButton(JoystickConstants.MANUAL_ELEVATOR_CONTROL)) {
			SetNewState(States.Manual_Elevator_Control);
			mPickingUpBoxHasStartedGrab = false;
		} 
		else if (mJoystick.getRawButton(JoystickConstants.HUNTING)) {
			SetNewState(States.Hunting);
			mPickingUpBoxHasStartedGrab = false;
		}
	}

	private void InitialIfHoldingBox() {
		mHinge.up();
		mIntake.disable();
		mGrabber.grab();
		mElevator.setSwitch();

		if (mJoystick.getRawButton(JoystickConstants.SCORE)) {
			SetNewState(States.Score);
		} 
		else if (mJoystick.getRawButton(JoystickConstants.MANUAL_ELEVATOR_CONTROL)) {
			SetNewState(States.Manual_Elevator_Control);
		} 
		else if (mJoystick.getRawButton(JoystickConstants.HUNTING)) { //TZ
			SetNewState(States.Hunting);
		}
	}

	private void Manual_Elevator_Control() {
		mIntake.disable();
		mElevator.setJoystickAdvanced();

		if (mJoystick.getRawButton(JoystickConstants.SCORE)) {
			SetNewState(States.Score);
		}
		else if (mJoystick.getRawButton(JoystickConstants.HUNTING)) {
			SetNewState(States.Hunting);
		}
	}

	private void Exchange_Score() {
		mGrabber.release();
		mHinge.down();
		mIntake.eject();

		if (mJoystick.getRawButton(JoystickConstants.HUNTING)) {
			SetNewState(States.Hunting);
		}
	}

	private boolean mHasSeenGroundInOnWayToExchange = false;
	private boolean mDriveHasHitOnWayToExchange_ExchangeScoreButtonPressed = false;
	private boolean mDriveHasHitOnWayToExchange_OnWayToScoreSwitchButtonPressed = false;
	private boolean mDriveHasHitOnWayToExchange_HuntingButtonPressed = false;
	
	private void On_Way_To_Exchange() {
		mIntake.hold();

		if (mHasSeenGroundInOnWayToExchange) {
			mElevator.stop();
			SmartDashboard.putNumber("Time Breakbeam", System.currentTimeMillis());
		} 
		else if (GetMillisIntoState() > 800) {
			mElevator.setGround();
			mHasSeenGroundInOnWayToExchange = mElevator.IsAtBottom();
		}
		mGrabber.release();
		mHinge.down();

		mIntake.setWheelSpeed(IntakeConstants.INTAKE_HOLD_SPEED);

		if (mJoystick.getRawButton(JoystickConstants.SCORE)) {
			mDriveHasHitOnWayToExchange_ExchangeScoreButtonPressed = true;
		}
		else if (mJoystick.getRawButton(JoystickConstants.PREPARE_TO_SCORE_ON_SWITCH)) { //TZ
			mDriveHasHitOnWayToExchange_OnWayToScoreSwitchButtonPressed = true;
		} 
		else if (mJoystick.getRawButton(JoystickConstants.HUNTING)) {
			SetNewState(States.Hunting);
		}

		if (mHasSeenGroundInOnWayToExchange) {
			if (mDriveHasHitOnWayToExchange_ExchangeScoreButtonPressed) {
				SetNewState(States.Exchange_Score);
			} 
			else if (mDriveHasHitOnWayToExchange_OnWayToScoreSwitchButtonPressed) {
				SetNewState(States.On_Way_To_Score_Switch);
			} 
			else if (mJoystick.getRawButton(JoystickConstants.HUNTING)) {
				SetNewState(States.Hunting);
			}
		}
	}

	private void Breakbeam_Tripped() {
		mElevator.setBox();
		mHinge.down();
		mIntake.flap();

		if (mElevator.isCloseToTargetUsingPID()) {
			mGrabber.HuntingMode();
		}

		if (mJoystick.getRawButton(JoystickConstants.ACQUIRED_BOX)) {
			SetNewState(States.On_Way_To_Exchange);
		} 
		else if (mBreakbeam.get()) {
			SetNewState(States.Hunting);
		}
	}

	private long mTimeAtStateStart;
	private String mStateLog = "";

	private void SetNewState(States pState) {
		mCurrentState = pState;
		mTimeAtStateStart = System.currentTimeMillis();

		mHasSeenGroundInOnWayToExchange = false;
		mDriveHasHitOnWayToExchange_ExchangeScoreButtonPressed = false;
		mDriveHasHitOnWayToExchange_OnWayToScoreSwitchButtonPressed = false;
		mDriveHasHitOnWayToExchange_HuntingButtonPressed = false;
		mHasBegunScoringSequence = false;

		mStateLog = mStateLog + ", " + (mTimeAtStateStart - mGameStartMillis) + ": " + mCurrentState.toString();
		SmartDashboard.putString("StateLog", mStateLog);
	}

	private long GetMillisIntoState() {
		return System.currentTimeMillis() - mTimeAtStateStart;
	}

	private void Hunting() {
		mElevator.setBox();
		mHinge.down();
		mIntake.hunt();
		
		if (mElevator.isCloseToTargetUsingPID()) {
			mGrabber.HuntingMode();
		}

		if (mJoystick.getRawButton(JoystickConstants.ACQUIRED_BOX)) {
			SetNewState(States.On_Way_To_Exchange);
		}
		else if (!mBreakbeam.get() && GetMillisIntoState() > 3000) {
			SetNewState(States.Breakbeam_Tripped);
		}
		else if (mJoystick.getRawButton(JoystickConstants.DEFENSE)) {
			SetNewState(States.Defense);
		}
	}

	private void Defense() {
		mGrabber.grab();
		mGrabber.in();
		mElevator.setSwitch();
		mIntake.disable();
		mHinge.up();
		if (mJoystick.getRawButton(JoystickConstants.DEFENSE)) {
			SetNewState(States.Hunting);
		}
	}

	public void startGame() {
		if (!mInGame) {
			
			mGameStartMillis = System.currentTimeMillis();
			SmartDashboard.putString ("DashboardCommand", "StartRecording");
			createHeaderString();
			if (RunConstants.RUNNING_PNEUMATICS) {
				mCompressor.start();
			}
			else {
				mCompressor.stop();
			}

			if (RunConstants.RUNNING_ELEVATOR) {
				while (!mElevatorTalon.getSensorCollection_isRevLimitSwitchClosed()) {
					mElevatorTalon.set(-0.4);
				}

				mElevatorTalon.set(0);
				mElevatorTalon.setSelectedSensorPosition(0, 0, 10);
				mElevEncoder.TriggerAtElevatorBottom();
			}
			
			if(RunConstants.RUNNING_GRABBER) {
				mGrabber.grab();
			}
			
			if(RunConstants.RUNNING_INTAKE) {
				
			}
			mInGame = true;
		}
	}

	public void endGame() {
		SmartDashboard.putString ("DashboardCommand", "EndRecording");
	}
	
	@Override
	public void disabled() {
		long timeDisabledStarted = System.currentTimeMillis();
		boolean ended = false;
		
		while (this.isDisabled()) {
			long timeElapsed = System.currentTimeMillis() - timeDisabledStarted;
			if (timeElapsed > 3000 && !ended)
			{
				endGame();
				ended = true;
				SmartDashboard.putString("CURRENT ROBOT MODE:", "DISABLED");
				
			}
			if (mJoystick.getTriggerPressed()) {
				// rotate autonomous routine:
				if (mAutonomousRoutine == AutonomousRoutines.SWITCH_SCORE) {
					mAutonomousRoutine = AutonomousRoutines.SCALE_SCORE_START_ON_LEFT;
				} 
				else if (mAutonomousRoutine == AutonomousRoutines.SCALE_SCORE_START_ON_LEFT) {
					mAutonomousRoutine = AutonomousRoutines.SCALE_SCORE_START_ON_RIGHT;
				} 
				else if (mAutonomousRoutine == AutonomousRoutines.SCALE_SCORE_START_ON_RIGHT) {
					mAutonomousRoutine = AutonomousRoutines.DRIVE_FORWARD;
				} 
				else if (mAutonomousRoutine == AutonomousRoutines.DRIVE_FORWARD) {
					mAutonomousRoutine = AutonomousRoutines.DO_NOTHING;
				}
				else if (mAutonomousRoutine == AutonomousRoutines.DO_NOTHING) {
					mAutonomousRoutine = AutonomousRoutines.SWITCH_SCORE;
				}
			}
			
			SmartDashboard.putString("!AUTONOMOUS_ROUTINE!", mAutonomousRoutine.toString());
			

			Timer.delay(0.005); // wait for a motor update time
		}
	}

	/**
	 * Runs during test mode
	 */
	@Override
	public void test() {

	}

	public void TankDrive() {
		if (RunConstants.RUNNING_DRIVE) {
			mDriveTrain.driveTank();
		}
	}

	public void CrabDrive() {
		if (RunConstants.RUNNING_DRIVE) {
			mDriveTrain.driveCrab();
		}
	}

	public void SwerveDrive() {
		if (RunConstants.RUNNING_DRIVE) {
			mDriveTrain.driveSwerve();
		}
	}

	public void DriveInit() {
		int turnPort, turnOffset, drivePort;
		double D_PID;
		double I_PID;
		double P_PID;
		boolean turnEncoderReversed, turnReversed, driveReversed; 
		for (int i = 0; i < 4; i++) {
			if(RunConstants.IS_PROTOTYPE) {
				turnPort = Ports.Prototype.TURN[i];
				turnEncoderReversed = DriveConstants.Prototype.ENCODER_REVERSED[i];
				turnReversed = DriveConstants.Prototype.TURN_INVERTED[i];
				turnOffset = DriveConstants.Prototype.OFFSETS[i];
				driveReversed = DriveConstants.Prototype.DRIVE_INVERTED[i];
				drivePort = Ports.Prototype.DRIVE[i];
				P_PID = DriveConstants.Prototype.ROTATION_P[i];
				I_PID = DriveConstants.Prototype.ROTATION_I[i];
				D_PID = DriveConstants.Prototype.ROTATION_D[i];
			}
			else {
				turnPort = Ports.TURN[i];
				turnEncoderReversed = DriveConstants.Modules.ENCODER_REVERSED[i];
				turnReversed = DriveConstants.Modules.TURN_INVERTED[i];
				turnOffset = DriveConstants.Modules.OFFSETS[i];
				driveReversed = DriveConstants.Modules.DRIVE_INVERTED[i];
				drivePort = Ports.DRIVE[i];
				P_PID = DriveConstants.PID_Constants.ROTATION_P[i];
				I_PID = DriveConstants.PID_Constants.ROTATION_I[i];
				D_PID = DriveConstants.PID_Constants.ROTATION_D[i];
			}
			mTurn[i] = GetTalonObject(turnPort);
			mTurn[i].configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Absolute, 0, 10);
			mTurn[i].setNeutralMode(NeutralMode.Brake);

			mTurn[i].setSensorPhase(turnEncoderReversed);
			mTurn[i].setInverted(turnReversed);

			mTurn[i].config_kP(0, DriveConstants.PID_Constants.ROTATION_P[i], 10);
			mTurn[i].config_kI(0, DriveConstants.PID_Constants.ROTATION_I[i], 10);
			mTurn[i].config_kD(0, DriveConstants.PID_Constants.ROTATION_D[i], 10);
			mTurn[i].config_IntegralZone(0, DriveConstants.PID_Constants.ROTATION_IZONE[i], 10);
			mTurn[i].configAllowableClosedloopError(0, DriveConstants.PID_Constants.ROTATION_TOLERANCE[i], 10);

			mTurn[i].configPeakOutputForward(1, 10);
			mTurn[i].configPeakOutputReverse(-1, 10);

			mDrive[i] = GetTalonObject(drivePort);
			mDrive[i].setInverted(driveReversed);
			mDrive[i].setNeutralMode(NeutralMode.Brake);

			mDrive[i].configPeakOutputForward(1, 10);
			mDrive[i].configPeakOutputReverse(-1, 10);

			mDrive[i].configPeakCurrentDuration(1000, 10);
			mDrive[i].configPeakCurrentLimit(150, 10);
			mDrive[i].configContinuousCurrentLimit(80, 10);
			mDrive[i].enableCurrentLimit(true);

			// mDrive[i].configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Absolute, 0, 10);
			// mDrive[i].setSensorPhase(DriveConstants.DRIVE_ENCODER_INVERTED[i]);

			mEncoder[i] = new TalonAbsoluteEncoder(mTurn[i], ResourceFunctions.tickToAngle(turnOffset));
			mWheel[i] = new Wheel(mTurn[i], mDrive[i], mEncoder[i]);
		}

		mRobotAngle = new RobotAngle(mNavX, false, 0);
		mDriveTrain = new DriveTrain(mWheel, mController, mRobotAngle);
	}

	public void IntakeAndHingeInit() {

		mLeftPiston = GetSolenoidObject(Ports.Intake.LEFT_INTAKE);
		mRightPiston = GetSolenoidObject(Ports.Intake.RIGHT_INTAKE);

		mRightIntakeWheel = GetTalonObject(Ports.Intake.RIGHT_INTAKE_WHEEL);
		mRightIntakeWheel.setInverted(IntakeConstants.RIGHT_WHEEL_REVERSED);
		mRightIntakeWheel.setNeutralMode(NeutralMode.Brake);

		mLeftIntakeWheel = GetTalonObject(Ports.Intake.LEFT_INTAKE_WHEEL);
		mLeftIntakeWheel.setInverted(IntakeConstants.LEFT_WHEEL_REVERSED);
		mLeftIntakeWheel.setNeutralMode(NeutralMode.Brake);

		mLimitSwitch = GetDigitalInputObject(Ports.Intake.LIMITSWITCH);
		mBreakbeam = GetDigitalInputObject(Ports.Intake.BREAKBEAM);

		mIntake = new Intake(mLeftIntakeWheel, mRightIntakeWheel, mLeftPiston, mRightPiston, mLimitSwitch, mBreakbeam,
				mJoystick);

//		mRightIntakeHinge = GetTalonObject(Ports.Hinge.RIGHT_INTAKE_HINGE);
//		mRightIntakeHinge.setInverted(HingeConstants.Motor.RIGHT_REVERSED);
//		mRightIntakeHinge.setNeutralMode(NeutralMode.Brake);
//		mRightIntakeHinge.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Absolute, 0, 10);
//		mRightIntakeHinge.setSensorPhase(HingeConstants.Motor.RIGHT_ENCODER_REVERSED);
//		mRightIntakeHinge.config_kP(0, HingeConstants.Motor.PID.HINGE_P_DOWN, 10);
//		mRightIntakeHinge.config_kI(0, HingeConstants.Motor.PID.HINGE_I_DOWN, 10);
//		mRightIntakeHinge.config_kD(0, HingeConstants.Motor.PID.HINGE_D_DOWN, 10);
//		mRightIntakeHinge.config_IntegralZone(0, HingeConstants.Motor.PID.RIGHT_HINGE_IZONE_DOWN, 10);
//		mRightIntakeHinge.configAllowableClosedloopError(0, HingeConstants.Motor.PID.RIGHT_HINGE_TOLERANCE_DOWN, 10);
//
//		mLeftIntakeHinge = GetTalonObject(Ports.Hinge.LEFT_INTAKE_HINGE);
//		mLeftIntakeHinge.setInverted(HingeConstants.Motor.LEFT_REVERSED);
//		mLeftIntakeHinge.setNeutralMode(NeutralMode.Brake);
//		mLeftIntakeHinge.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Absolute, 0, 10);
//		mLeftIntakeHinge.setSensorPhase(HingeConstants.Motor.LEFT_ENCODER_REVERSED);
//		mLeftIntakeHinge.config_kP(0, HingeConstants.Motor.PID.HINGE_P_DOWN, 10);
//		mLeftIntakeHinge.config_kI(0, HingeConstants.Motor.PID.HINGE_I_DOWN, 10);
//		mLeftIntakeHinge.config_kD(0, HingeConstants.Motor.PID.HINGE_D_DOWN, 10);
//		mLeftIntakeHinge.config_IntegralZone(0, HingeConstants.Motor.PID.LEFT_HINGE_IZONE_DOWN, 10);
//		mLeftIntakeHinge.configAllowableClosedloopError(0, HingeConstants.Motor.PID.LEFT_HINGE_TOLERANCE_DOWN, 10);
//
//		mHinge = new IntakeHingePisto(mLeftIntakeHinge, mRightIntakeHinge);
		mHingePiston = GetSolenoidObject(Ports.Hinge.HINGE_PISTON);
		mHinge = new IntakeHingePiston(mHingePiston);
	}

	public void ElevInit() {
		mElevatorTalon = GetTalonObject(Ports.Elevator.ELEVATOR);
		mElevatorTalon.setInverted(ElevatorConstants.REVERSED);
		mElevatorTalon.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);
		mElevatorTalon.setSensorPhase(ElevatorConstants.ENCODER_REVERSED);
		mElevatorTalon.setNeutralMode(NeutralMode.Brake);

		mElevatorTalon.config_kP(0, ElevatorConstants.PID.ELEVATOR_UP_P, 10);
		mElevatorTalon.config_kI(0, ElevatorConstants.PID.ELEVATOR_UP_I, 10);
		mElevatorTalon.config_kD(0, ElevatorConstants.PID.ELEVATOR_UP_D, 10);
		mElevatorTalon.configAllowableClosedloopError(0, ElevatorConstants.PID.ELEVATOR_TOLERANCE, 10);
		mElevatorTalon.config_IntegralZone(0, ElevatorConstants.PID.IZONE, 10);

		mElevEncoder = new ElevatorEncoder(mElevatorTalon);
		mElevator = new Elevator(mElevatorTalon, mElevEncoder, mJoystick);
	}

	public void GrabberInit() {
		mGrab = GetSolenoidObject(Ports.Grabber.GRAB);
		mExtend = GetSolenoidObject(Ports.Grabber.EXTEND);

		mGrabber = new Grabber(mGrab, mExtend);
	}

	private TalonInterface GetTalonObject(int pPortNumber) {
		if (RunConstants.SIMULATOR) {
			if (pPortNumber == Ports.Elevator.ELEVATOR) {
				return new simulator.talon.ElevatorTalonSimulator();
			} 
			else if (pPortNumber == Ports.Intake.LEFT_INTAKE_WHEEL
					|| pPortNumber == Ports.Intake.RIGHT_INTAKE_WHEEL) {
				return new simulator.talon.IntakeWheelSimulator();
			} 
			else {
				throw new RuntimeException("Simulator class for port " + pPortNumber + " not created yet");
			}
		} 
		else {
			return new TalonActualImplementation(new WPI_TalonSRX(pPortNumber));
		}
	}

	private simulator.solenoid.SolenoidInterface GetSolenoidObject(int pPortNumber) {
		if (RunConstants.SIMULATOR) {
			return new SingleSolenoidSimulator();
		} else {
			return new SingleSolenoidActualImplementation(new SingleSolenoidReal(pPortNumber));
		}
	}

	private GyroInterface GetGyroObject(SerialPort.Port port) {
		if (RunConstants.SIMULATOR) {
			return new GyroSimulator();
		} else {
			return new GyroActualImplementation(new AHRS(port));
		}
	}

	private DigitalInputInterface GetDigitalInputObject(int pPortNumber) {
		if (RunConstants.SIMULATOR) {
			if (pPortNumber == Ports.Intake.BREAKBEAM) {
				return new BreakbeamSimulator(mJoystick);
			} else if (pPortNumber == Ports.Intake.LIMITSWITCH) {
				return new LimitSwitchSimulator(mJoystick);
			} else {
				throw new RuntimeException("Simulator class for port " + pPortNumber + " not created yet");
			}
		} else {
			return new DigitalInputActualImplementation(new DigitalInput(pPortNumber));
		}
	}

	private long mLastSimulationTime = 0;

	private void simulate() {
		if (RunConstants.SIMULATOR) {
			if (mLastSimulationTime == 0) {
				// first time only
				mLastSimulationTime = System.currentTimeMillis();
			}

			long deltaTime = System.currentTimeMillis() - mLastSimulationTime;
			mLastSimulationTime = System.currentTimeMillis();

			mElevatorTalon.simulate(deltaTime);

			mLimitSwitch.simulate();
			mBreakbeam.simulate();
		}

	}

	public void addLogValueDouble(StringBuilder pLogString, double pVal) {
		pLogString.append(pVal);
		pLogString.append(",");
	}

	public void addLogValueInt(StringBuilder pLogString, int pVal) {
		pLogString.append(pVal);
		pLogString.append(",");
	}

	public void addLogValueLong(StringBuilder pLogString, long pVal) {
		pLogString.append(pVal);
		pLogString.append(",");
	}

	public void addLogValueBoolean(StringBuilder pLogString, boolean pVal) {
		pLogString.append(pVal ? "1" : "0");
		pLogString.append(",");
	}

	public void addLogValueString(StringBuilder pLogString, String pVal) {
		pLogString.append(pVal);
		pLogString.append(",");
	}

	public void addLogValueEndDouble(StringBuilder pLogString, double pVal) {
		pLogString.append(pVal);
		pLogString.append("\n");
	}

	public void addLogValueEndInt(StringBuilder pLogString, int pVal) {
		pLogString.append(pVal);
		pLogString.append("\n");
	}

	public void addLogValueEndLong(StringBuilder pLogString, long pVal) {
		pLogString.append(pVal);
		pLogString.append("\n");
	}

	public void addLogValueEndBoolean(StringBuilder pLogString, boolean pVal) {
		pLogString.append(pVal ? "1" : "0");
		pLogString.append("\n");
	}

	public void addLogValueEndString(StringBuilder pLogString, String pVal) {
		pLogString.append(pVal);
		pLogString.append("\n");
	}

	public void createHeaderString() {
		StringBuilder headerString = new StringBuilder();

		// for now it is one frame per line
		addLogValueString(headerString, "Time Elapsed");

		for(int i = 0; i<16; i++) {
			addLogValueString(headerString, "PDP Current: " + i);
		}
		addLogValueString(headerString, "PDP Voltage");
		addLogValueString(headerString, "PDP Total Current");
		
		if (RunConstants.RUNNING_PNEUMATICS) {
			addLogValueString(headerString, "Compressor Enabled");
			addLogValueString(headerString, "Compressor Current");
		}

		if (RunConstants.RUNNING_DRIVE) {
			for (int i = 0; i < 4; i++) {
				addLogValueString(headerString, "Turn Motor Current " + i);
				addLogValueString(headerString, "Drive Motor Current " + i);

				addLogValueString(headerString, "Turn Motor Voltage " + i);
				addLogValueString(headerString, "Drive Motor Voltage " + i);

				addLogValueString(headerString, "Encoder Angle " + i);
			}

			addLogValueString(headerString, "Linear Vel Magnitude");
			addLogValueString(headerString, "Linear Vel Angle");
			addLogValueString(headerString, "Angular vel");
		}

		if (RunConstants.RUNNING_ELEVATOR) {
			addLogValueString(headerString, "Elevator Current");
			addLogValueString(headerString, "Elevator Voltage");

			addLogValueString(headerString, "Elevator Height (inches)");

			addLogValueString(headerString, "Elevator Mode");
			addLogValueString(headerString, "Elevator Direction");
			
			addLogValueString(headerString, "Elevator Fwd Limit Switch");
			addLogValueString(headerString, "Elevator Rev Limit Switch");
		}

		if (RunConstants.RUNNING_INTAKE) {
			addLogValueString(headerString, "Left Intake Wheel Current");
			addLogValueString(headerString, "Left Intake Wheel Voltage");

			addLogValueString(headerString, "Right Intake Wheel Current");
			addLogValueString(headerString, "Right Intake Wheel Voltage");

			// addLogValueString(logString, mHingePiston.get().toString());

			addLogValueString(headerString, "Left Intake Piston");
			addLogValueString(headerString, "Right Intake Piston");

			addLogValueString(headerString, "Intake State");

			addLogValueString(headerString, "Left Intake Hinge Current");
			addLogValueString(headerString, "Left Intake Hinge Voltage");

			addLogValueString(headerString, "Right Intake Hinge Current");
			addLogValueString(headerString, "Right Intake Hinge Voltage");
			
			addLogValueString(headerString, "Hinge State");

			addLogValueString(headerString, "Intake Break Beam");
			addLogValueString(headerString, "Intake Limit Switch :(");
		}

		if (RunConstants.RUNNING_GRABBER) {
			addLogValueString(headerString, "Grab Piston");
			addLogValueString(headerString, "Extend Piston");

			addLogValueString(headerString, "Grabber State");
		}

		addLogValueString(headerString, "Robot State");

		addLogValueString(headerString, "Robot Angle");

		addLogValueString(headerString, "xBox Y Button");
		addLogValueString(headerString, "xBox B Button");
		addLogValueString(headerString, "xBox A Button");
		addLogValueString(headerString, "xBox X Button");
		addLogValueString(headerString, "xBox Left Bumper");
		addLogValueString(headerString, "xBox Right Bumper");
		addLogValueString(headerString, "xBox Left Trigger");
		addLogValueString(headerString, "xBox Right Trigger");
		addLogValueString(headerString, "xBox POV");
		addLogValueString(headerString, "xBox Start Button");
		addLogValueString(headerString, "xBox Back Button");
		addLogValueString(headerString, "xBox Left Joystick x-value");
		addLogValueString(headerString, "xBox Left Joystick y-value");
		addLogValueString(headerString, "xBox Right Joystick x-value");
		addLogValueString(headerString, "xBox Right Joystick y-value");

		for (int i = 1; i < 12; i++) {
			addLogValueString(headerString, "Joystick Button: " + i);
		}

		addLogValueEndString(headerString, "");
		SmartDashboard.putString("HeaderString", headerString.toString());
	}
	
	public void log() {
		long time = System.currentTimeMillis();
		long timeElapsed = time - mGameStartMillis;

		SmartDashboard.putNumber("Time Elapsed:", timeElapsed);

		StringBuilder logString = new StringBuilder();

		// for now it is one frame per line
		addLogValueLong(logString, timeElapsed);

		for(int i = 0; i<16; i++) {
			addLogValueDouble(logString, mPDP.getCurrent(i));
		}
		addLogValueDouble(logString, mPDP.getVoltage());
		addLogValueDouble(logString, mPDP.getTotalCurrent());
		
		if (RunConstants.RUNNING_PNEUMATICS) {
			addLogValueBoolean(logString, mCompressor.enabled());
			addLogValueDouble(logString, mCompressor.getCompressorCurrent());
		}

		if (RunConstants.RUNNING_DRIVE) {
			for (int i = 0; i < 4; i++) {
				addLogValueDouble(logString, mTurn[i].getOutputCurrent());
				addLogValueDouble(logString, mDrive[i].getOutputCurrent());

				addLogValueDouble(logString, mTurn[i].getMotorOutputVoltage());
				addLogValueDouble(logString, mDrive[i].getMotorOutputVoltage());

				addLogValueDouble(logString, mEncoder[i].getAngleDegrees());
			}

			addLogValueDouble(logString, mDriveTrain.getDesiredRobotVel().getMagnitude());
			addLogValueDouble(logString, mDriveTrain.getDesiredRobotVel().getAngle());
			addLogValueDouble(logString, mDriveTrain.getDesiredAngularVel());
		}

		if (RunConstants.RUNNING_ELEVATOR) {
			addLogValueDouble(logString, mElevatorTalon.getOutputCurrent());
			addLogValueDouble(logString, mElevatorTalon.getMotorOutputVoltage());

			addLogValueDouble(logString, mElevEncoder.getHeightInInchesFromElevatorBottom());

			addLogValueString(logString, mElevator.getElevatorMode().toString());
			addLogValueString(logString, mElevator.getElevatorDirection().toString());
			
			addLogValueBoolean(logString, mElevatorTalon.getSensorCollection_isFwdLimitSwitchClosed());
			addLogValueBoolean(logString, mElevatorTalon.getSensorCollection_isRevLimitSwitchClosed());
		}

		if (RunConstants.RUNNING_INTAKE) {
			addLogValueDouble(logString, mLeftIntakeWheel.getOutputCurrent());
			addLogValueDouble(logString, mLeftIntakeWheel.getMotorOutputVoltage());
			
			addLogValueDouble(logString, mRightIntakeWheel.getOutputCurrent());
			addLogValueDouble(logString, mRightIntakeWheel.getMotorOutputVoltage());

			addLogValueString(logString, mHingePiston.get().toString());

			addLogValueString(logString, mLeftPiston.get().toString());
			addLogValueString(logString, mRightPiston.get().toString());

			addLogValueString(logString, mIntake.getIntakeState().toString());
			
//			addLogValueDouble(logString, mLeftIntakeHinge.getOutputCurrent());
//			addLogValueDouble(logString, mLeftIntakeHinge.getMotorOutputVoltage());
//			
//			addLogValueDouble(logString, mRightIntakeHinge.getOutputCurrent());
//			addLogValueDouble(logString, mRightIntakeHinge.getMotorOutputVoltage());
			
//			addLogValueString(logString, mHinge.getHingeState().name());
			
			addLogValueBoolean(logString, mBreakbeam.get());
			addLogValueBoolean(logString, mLimitSwitch.get());
		}

		if (RunConstants.RUNNING_GRABBER) {
			addLogValueString(logString, mGrab.get().toString());
			addLogValueString(logString, mExtend.get().toString());

			addLogValueString(logString, mGrabber.getGrabberState().toString());
		}

		addLogValueString(logString, mCurrentState.toString());
		
		addLogValueDouble(logString, mRobotAngle.getAngleDegrees());
		
		
		
		addLogValueBoolean(logString, mController.getYButton());
		addLogValueBoolean(logString, mController.getBButton());
		addLogValueBoolean(logString, mController.getAButton());
		addLogValueBoolean(logString, mController.getXButton());
		addLogValueBoolean(logString, mController.getBumper(Hand.kLeft));
		addLogValueBoolean(logString, mController.getBumper(Hand.kRight));
		addLogValueDouble(logString, mController.getTriggerAxis(Hand.kLeft));
		addLogValueDouble(logString, mController.getTriggerAxis(Hand.kRight));
		addLogValueInt(logString, mController.getPOV());
		addLogValueBoolean(logString, mController.getStartButton());
		addLogValueBoolean(logString, mController.getBackButton());
		addLogValueDouble(logString, mController.getX(Hand.kLeft));
		addLogValueDouble(logString, mController.getY(Hand.kLeft));
		addLogValueDouble(logString, mController.getX(Hand.kRight));
		addLogValueDouble(logString, mController.getY(Hand.kRight));

		for (int i = 1; i < 12; i++) {
			addLogValueBoolean(logString, mJoystick.getRawButton(i));
		}
		
		addLogValueEndString(logString, "");

		SmartDashboard.putNumber("TimeLeft", 150000 - timeElapsed);
		SmartDashboard.putString("LogString", logString.toString());
	}
}
