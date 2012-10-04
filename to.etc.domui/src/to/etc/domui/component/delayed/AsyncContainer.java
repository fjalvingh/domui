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

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.util.*;

public class AsyncContainer extends Div {
	@Nonnull
	final private IAsyncRunnable m_runnable;

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
		m_runnable = arunnable;
	}

	public AsyncContainer(@Nonnull final IActivity activity) {
		m_runnable = new IAsyncRunnable() {
			@Nullable
			private Div m_result;

			@Override
			public void run(@Nonnull IProgress p) throws Exception {
				setResult(activity.run(p));
			}

			private synchronized Div getResult() {
				return m_result;
			}

			private synchronized void setResult(Div result) {
				m_result = result;
			}

			@Override
			public void onCompleted(boolean cancelled, @Nullable Exception errorException) throws Exception {
				//-- If we've got an exception replace the contents with the exception message.
				if(errorException != null) {
					StringBuilder sb = new StringBuilder(8192);
					StringTool.strStacktrace(sb, errorException);
					setText(sb.toString()); 						// Just fill myself with the error stack trace.
					return;
				}

				//-- If there is no result- either we were cancelled OR there are no results..
				Div res = getResult();
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
			DefaultButton db = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_CANCEL), new IClicked<DefaultButton>() {
				@Override
				public void clicked(DefaultButton b) throws Exception {
					cancel();
					b.setDisabled(true);
				}
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

	public void updateCompleted(DelayedActivityInfo dai) throws Exception {
		//-- Call the node's update handler *before* removing myself.
		try {
			dai.getActivity().onCompleted(dai.getMonitor().isCancelled(), dai.getException());
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

	/**
	 * @see AsyncContainer#m_abortable
	 * @return
	 */
	public boolean isAbortable() {
		return m_abortable;
	}

	/**
	 * @see AsyncContainer#m_abortable
	 * @return
	 */
	public void setAbortable(boolean abortable) {
		m_abortable = abortable;
	}

	/**
	 * @see AsyncContainer#m_busyMarkerSrc
	 * @return
	 */
	public String getBusyMarkerSrc() {
		return m_busyMarkerSrc;
	}

	/**
	 * @see AsyncContainer#m_busyMarkerSrc
	 * @return
	 */
	public void setBusyMarkerSrc(String busyMarkerSrc) {
		m_busyMarkerSrc = busyMarkerSrc;
	}

}
