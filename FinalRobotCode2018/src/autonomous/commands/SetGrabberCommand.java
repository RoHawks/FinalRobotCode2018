package autonomous.commands;

import robotcode.systems.Grabber;
import robotcode.systems.Grabber.GrabberState;

public class SetGrabberCommand extends BaseAutonomousCommand {

	Grabber mGrabber;
	GrabberState mGrabberState;
	
	public SetGrabberCommand(Grabber pGrabber, GrabberState pGrabberState) {
		mGrabber = pGrabber;
		mGrabberState = pGrabberState;
	}
	
	@Override
	public boolean RunCommand() {
		mGrabber.setState(mGrabberState);
		return true;
	}

}
