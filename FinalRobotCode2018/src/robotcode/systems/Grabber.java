package robotcode.systems;

import constants.GrabberConstants;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;

public class Grabber {

	private simulator.solenoid.SolenoidInterface mGrab, mExtend;
	private GrabberState mGrabberState;

	public Grabber(simulator.solenoid.SolenoidInterface pGrab, simulator.solenoid.SolenoidInterface pExtend) {
		mGrab = pGrab;
		mExtend = pExtend;
		setState();
	}

	public enum GrabberState {
		OUT_GRAB,
		OUT_RELEASE,
		IN_GRAB,
		IN_RELEASE
	}

	public void out() {
		mExtend.set(GrabberConstants.OUT);
		setState();
	}

	public void in() {
		mExtend.set(GrabberConstants.IN);
		setState();
	}

	public void release() {
		mGrab.set(GrabberConstants.RELEASE);
		setState();
	}

	public void grab() {
		mGrab.set(GrabberConstants.GRAB);
		setState();
	}

	public void grabOpposite() {
		mGrab.setOpposite();
		setState();
	}

	public void extendOpposite() {
		mExtend.setOpposite();
		setState();
	}
	
	private void setState() {
		if (mGrab.get() == GrabberConstants.GRAB && mExtend.get() == GrabberConstants.IN) {
			mGrabberState = GrabberState.IN_GRAB;
		}
		else if (mGrab.get() == GrabberConstants.GRAB && mExtend.get() == GrabberConstants.OUT) {
			mGrabberState = GrabberState.OUT_GRAB;
		}
		else if (mGrab.get() == GrabberConstants.RELEASE && mExtend.get() == GrabberConstants.IN) {
			mGrabberState = GrabberState.IN_RELEASE;
		}
		else if (mGrab.get() == GrabberConstants.RELEASE && mExtend.get() == GrabberConstants.OUT) {
			mGrabberState = GrabberState.OUT_RELEASE;
		}
	}
	
	public void setState(GrabberState pGrabberState) {
		switch(pGrabberState) {
		case IN_GRAB:
			in();
			grab();
			break;
		case IN_RELEASE:
			in();
			release();
			break;
		case OUT_GRAB:
			out();
			grab();
			break;
		case OUT_RELEASE:
			out();
			release();
			break;
		default:
			break;
		}
		mGrabberState = pGrabberState;
	}

	public Value getGrab() {
		return mGrab.get();
	}

	public Value getExtend() {
		return mExtend.get();
	}

	public GrabberState getGrabberState() {
		return mGrabberState;
	}

	public void HuntingMode() {
		in();
		release();
	}
}
