/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.component.delayed;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Img;
import to.etc.domui.state.DelayedActivityInfo;
import to.etc.domui.state.DelayedActivityInfo.State;
import to.etc.domui.themes.Theme;
import to.etc.domui.util.Msgs;
import to.etc.util.Progress;
import to.etc.util.StringTool;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final public class AsyncContainer extends Div {
	@Nonnull
	final private IAsyncRunnable m_runnable;

	@Nullable
	final private IAsyncCompletionListener m_resultListener;

	private DelayedActivityInfo m_scheduledActivity;

	private Div m_progress;

	/**
	 * Defines if async action can be cancelled. T by default.
	 */
	private boolean m_abortable = true;

	/**
	 * Defines busy image src. If not set uses default framework resource.
	 */
	private String m_busyMarkerSrc = "THEME/asy-container-busy.gif";

	public AsyncContainer(@Nonnull IAsyncRunnable arunnable) {
		this(arunnable, null);
	}

	public AsyncContainer(@Nonnull IAsyncRunnable arunnable, @Nullable IAsyncCompletionListener listener) {
		m_runnable = arunnable;
		m_resultListener = listener;
	}

	public AsyncContainer inline() {
		setDisplay(DisplayType.INLINE_BLOCK);
		setAbortable(false);
		setBusyMarkerSrc("THEME/io-blk-wait.gif");
		return this;
	}

	public AsyncContainer(@Nonnull IActivity activity) {
		Div[] resultLocator = new Div[1];

		m_runnable = new IAsyncRunnable() {
			@Nullable
			private Div m_result;

			@Override
			public void run(@Nonnull Progress p) throws Exception {
				setResult(activity.run(p));
			}

			private synchronized void setResult(Div result) {
				resultLocator[0] = result;
			}
		};

		m_resultListener = new IAsyncCompletionListener() {
			@Override public void onCompleted(boolean cancelled, @Nullable Exception errorException) throws Exception {
				//-- If we've got an exception replace the contents with the exception message.
				if(errorException != null) {
					errorException.printStackTrace();
					StringBuilder sb = new StringBuilder(8192);
					StringTool.strStacktrace(sb, errorException);
					String s = sb.toString();
					s = s.replace("\n", "<br/>\n");

					MsgBox.error(AsyncContainer.this.getParent(), "Exception while creating result for asynchronous task:<br/>" + s);
					return;
				}

				//-- If there is no result- either we were cancelled OR there are no results..
				Div res = resultLocator[0];
				if(res == null) {
					if(cancelled) {
						setText(Msgs.BUNDLE.getString(Msgs.ASYNC_CONTAINER_CANCELLED_MSG));
					} else {
						setText(Msgs.BUNDLE.getString(Msgs.ASYNC_CONTAINER_NO_RESULTS_MSG));
					}
					return;
				}

				//-- Now replace AsyncContainer with the result
				replaceWith(res); 					// Replace this node with another one.
			}
		};
	}

	@Override
	public void createContent() throws Exception {
		if(m_scheduledActivity == null) {
			m_scheduledActivity = getPage().getConversation().scheduleDelayed(this, m_runnable);
		}

		//-- Render a thingy containing a spinner
		setCssClass("ui-asc");
		Img img = new Img();
		img.setSrc(getBusyMarkerSrc());
		add(img);
		m_progress = new Div();
		add(m_progress);
		if(isAbortable()) {
			DefaultButton db = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_CANCEL), Theme.BTN_CANCEL, b -> {
				cancel();
				b.setDisabled(true);
			});
			add(db);
		}
	}

	void cancel() {
		if(m_scheduledActivity != null)
			m_scheduledActivity.cancel();
	}

	/**
	 * Update the progress report.
	 */
	public void updateProgress(DelayedActivityInfo dai) throws Exception {
		if(dai.getState() == State.DONE) {
			updateCompleted(dai);
		} else {
			Progress progress = dai.getMonitor();
			StringBuilder sb = new StringBuilder();
			sb.append(progress.getPercentage());
			sb.append("%");

			sb.append(' ');
			String actionPath = progress.getActionPath(3);
			sb.append(actionPath);

			//if(msg != null) {
			//	sb.append(' ');
			//	sb.append(msg);
			//} else {
			//	sb.append(" " + Msgs.BUNDLE.getString(Msgs.ASYNC_CONTAINER_COMPLETE_INDICATOR));
			//}
			m_progress.setText(sb.toString());
		}
	}

	private void updateCompleted(DelayedActivityInfo dai) throws Exception {
		//-- Call the node's update handler *before* removing myself.
		try {
			IAsyncCompletionListener resultListener = m_resultListener;
			if(null != resultListener)
				resultListener.onCompleted(dai.getMonitor().isCancelled(), dai.getException());
			else {
				new DefaultAsyncCompletionListener(getParent()).onCompleted(dai.getMonitor().isCancelled(), dai.getException());
			}
			//dai.getActivity().onCompleted(dai.getMonitor().isCancelled(), dai.getException());
		} finally {
			try {
				remove();								// Remove myself *after* this all.
			} catch(Exception x) {
				System.err.println("Could not remove AsyncContainer: " + x);
				x.printStackTrace();
			}
		}
	}

	public void confirmCancelled() {
		setText(Msgs.BUNDLE.getString(Msgs.ASYNC_CONTAINER_CANCELLED));
	}

	public boolean isAbortable() {
		return m_abortable;
	}

	public void setAbortable(boolean abortable) {
		m_abortable = abortable;
	}

	public String getBusyMarkerSrc() {
		return m_busyMarkerSrc;
	}

	public void setBusyMarkerSrc(String busyMarkerSrc) {
		m_busyMarkerSrc = busyMarkerSrc;
	}
}
