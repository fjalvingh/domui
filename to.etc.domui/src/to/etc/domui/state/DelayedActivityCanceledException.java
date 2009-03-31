package to.etc.domui.state;

public class DelayedActivityCanceledException extends RuntimeException {
	public DelayedActivityCanceledException() {
		super("Activity was canceled");
	}
}
