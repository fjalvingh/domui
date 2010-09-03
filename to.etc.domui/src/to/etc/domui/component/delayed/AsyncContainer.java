package to.etc.domui.component.delayed;

import to.etc.domui.component.buttons.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.util.*;

public class AsyncContainer extends Div {
	private IActivity m_activity;

	private DelayedActivityInfo m_scheduledActivity;

	private Div m_progress;

	public AsyncContainer(IActivity activity) {
		m_activity = activity;
	}

	@Override
	public void createContent() throws Exception {
		if(m_scheduledActivity == null) {
			m_scheduledActivity = getPage().getConversation().scheduleDelayed(this, m_activity);
		}

		//-- Render a thingy containing a spinner
		setCssClass("ui-asc");
		Img img = new Img();
		img.setSrc(PageContext.getRequestContext().getRelativeThemePath("asy-container-busy.gif"));
		add(img);
		DefaultButton db = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_CANCEL), new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton b) throws Exception {
				cancel();
				b.setDisabled(true);
			}
		});
		m_progress = new Div();
		add(m_progress);
		add(db);
	}

	void cancel() {
		if(m_scheduledActivity != null)
			m_scheduledActivity.cancel();
	}

	/**
	 * Update the progress report.
	 * @param pct
	 * @param msg
	 */
	public void updateProgress(int pct, String msg) {
		StringBuilder sb = new StringBuilder();
		sb.append(pct);
		sb.append("%");
		if(msg != null) {
			sb.append(' ');
			sb.append(msg);
		} else {
			sb.append(" " + Msgs.BUNDLE.getString(Msgs.ASYNC_CONTAINER_COMPLETE_INDICATOR));
		}
		m_progress.setText(sb.toString());
	}

	public void updateCompleted(DelayedActivityInfo dai) {
		//-- If we've got an exception replace the contents with the exception message.
		if(dai.getException() != null) {
			StringBuilder sb = new StringBuilder(8192);
			StringTool.strStacktrace(sb, dai.getException());
			this.setText(sb.toString()); // Discard everything && replace
			return;
		}

		//-- Replace THIS node with the new thingy.
		if(dai.getExecutionResult() == null) {
			if(dai.getMonitor().isCancelled()) {
				setText(Msgs.BUNDLE.getString(Msgs.ASYNC_CONTAINER_CANCELLED_MSG));
			} else {
				setText(Msgs.BUNDLE.getString(Msgs.ASYNC_CONTAINER_NO_RESULTS_MSG));
			}
			return;
		}
		replaceWith(dai.getExecutionResult()); // Replace this node with another one.

		//		removeAllChildren();
		//		add(dai.getExecutionResult());
	}

	public void confirmCancelled() {
		setText(Msgs.BUNDLE.getString(Msgs.ASYNC_CONTAINER_CANCELLED));
	}
}
