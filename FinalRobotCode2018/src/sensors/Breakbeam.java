package sensors;

import edu.wpi.first.wpilibj.DigitalInput;

public class Breakbeam extends DigitalInput {

	private long mTimeLastChanged;
	private boolean mPreviousValue;
	
	private final long mThreshold = 250;
	
	public Breakbeam(int channel) {
		super(channel);
	}
	
	@Override
	public boolean get() {
		if(System.currentTimeMillis() - mTimeLastChanged > mThreshold) {
			mPreviousValue = super.get();
			return mPreviousValue;
		}
		else {
			return mPreviousValue;
		}
		
	}

}
