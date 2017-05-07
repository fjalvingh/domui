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
package to.etc.webapp.ajax.eventmanager;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import to.etc.webapp.ajax.comet.*;
import to.etc.webapp.ajax.renderer.*;
import to.etc.webapp.ajax.renderer.json.*;
import to.etc.xml.*;

/**
 * Comet context for the Ajax Event manager. All browsers waiting on events
 * have one of these open.
 * The browser request is a JSON message which looks like this:
 * <pre>
 * 	{
 * 		key: 121648764328,
 * 		nexteventid: 56785,
 * 		channels: [ "tehchannel", "morechannel", "thingydoodle" ],
 * 		lingertime: 1500,	// Linger time in ms, optional.
 *  }
 * </pre>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 25, 2006
 */
public class EventCometContext implements CometContext {
	static JSONRegistry m_registry = new JSONRegistry();

	private Continuation m_co;

	private String m_remoteUser;

	private Set<String> m_channels;

	private List<QueuedEvent> m_eventList;

	private int m_key;

	private int m_lingerTime = 1000;

	private TimerTask m_timerTask;

	enum CompletionState {
		/** This thingy is waiting for an event to occur. */
		csWAITING,

		/** The thingy has completed with events and is waiting for the OK result to be returned. */
		csOKAY,

		/** The thingy has timed out, and is waiting for that fact to be returned to the caller. */
		csTIMEOUT,

		/** This thingy has been determined to have an invalid key or an expired/outdated request and is waiting for that fact to be returned to the caller. */
		csBADKEY,

		/** This object is completed fully; the response has been rendered hence this object is mostly invalid. */
		csFINISHED
	}

	/** The current state of this thingy. */
	private CompletionState m_completionState = CompletionState.csWAITING;

	/** When true, this context is in "lingering" mode already. */
	private boolean m_lingering;

	/** This contains the thing to return as a JSON object to the caller. */
	private EventResult m_result;

	/**
	 * This locks this context when > 0. Locking means that any call that terminates
	 * this instance does not release it to the client but merely posts it's intention
	 * to do so. Only when the lock count reaches 0 again will the actual continue be
	 * done. While this count is > 0 the context is "owned". This gets used to allow
	 * ajax event filters to run outside of any locks.
	 */
	private int m_lockCount;

	private HttpServletRequest m_httpServletRequest;

	public HttpServletRequest getHttpServletRequest() {
		return m_httpServletRequest;
	}

	@Override
	public void begin(final HttpServlet slet, final HttpServletRequest req, final Continuation cont) throws Exception {
		m_httpServletRequest = req;
		m_co = cont;
		String json = req.getParameter("json");
		if(json == null)
			throw new ServletException("Missing servlet parameter 'json' containing the event subscription");
		//		System.out.println("AjaxEventServlet: json="+json);
		Object o = JSONParser.parseJSON(json); // Convert to Java map and lists
		if(!(o instanceof Map< ? , ? >))
			throw new ServletException("Expecting an 'object' as the top-level JSON element");
		Map<Object, Object> map = (Map<Object, Object>) o;
		o = map.get("key");
		if(o == null || !(o instanceof Number))
			throw new ServletException("Missing 'key' field");
		int key = ((Number) o).intValue();

		o = map.get("nexteventid");
		if(o == null || !(o instanceof Number))
			throw new ServletException("Missing 'nexteventid' field");
		int nexteventid = ((Number) o).intValue();

		o = map.get("lingertime");
		if(o != null) {
			if(!(o instanceof Number))
				throw new ServletException("'lingertime' must be an integer.");
			m_lingerTime = ((Number) o).intValue();
		}

		o = map.get("remoteUser");
		if(o == null) {
			o = req.getRemoteUser();
			if(o == null)
				throw new ServletException("The COMET request does not contain the remoteUser field.");
		}
		if(!(o instanceof String))
			throw new ServletException("'remoteUser' must be a String.");
		m_remoteUser = (String) o;

		Set<String> channels = new HashSet<String>();
		o = map.get("channels");
		if(o == null || !(o instanceof List< ? >))
			throw new ServletException("The 'channels' field must be an array of strings");
		for(Object co : (List<Object>) o) {
			if(!(co instanceof String))
				throw new ServletException("The 'channels' field must be an array of strings");
			channels.add((String) co);
		}
		if(channels.size() == 0)
			throw new ServletException("The 'channels' field cannot be empty");

		m_channels = channels;
		m_key = key;
		AjaxEventManager.getInstance().registerWaiter(this, nexteventid);
	}

	/**
	 * Lock the context for use. If the context is no longer available return false.
	 * synchronized on AjaxEventManager
	 */
	boolean lock() {
		if(m_completionState != CompletionState.csWAITING)
			return false;
		m_lockCount++;
		return true;
	}

	/**
	 * Unlock the context.
	 * synchronized on AjaxEventManager
	 */
	void unlock() {
		if(m_lockCount <= 0)
			throw new IllegalStateException("Bad lock count");
		AjaxEventManager.getInstance().notifyAll();
		m_lockCount--;
	}

	void waitForZeroLock() {
		AjaxEventManager m = AjaxEventManager.getInstance();
		synchronized(m) {
			for(;;) {
				if(m_lockCount < 0)
					throw new IllegalStateException("Lockcount < 0: object has been released!!");
				if(m_lockCount == 0) {
					m_lockCount = -1; // Ensure an error next time,
					return;
				}
				try {
					m.wait();
				} catch(InterruptedException x) {
					return;
				}
			}
		}
	}

	static public class EventResult {
		private final String m_status;

		private final int m_key;

		private final int m_nexteventid;

		private final List<QueuedEvent> m_eventlist;

		public EventResult(final int key, final int nexteventid, final String status, final List<QueuedEvent> eventlist) {
			m_key = key;
			m_nexteventid = nexteventid;
			m_status = status;
			m_eventlist = eventlist;
		}

		public String getStatus() {
			return m_status;
		}

		public int getKey() {
			return m_key;
		}

		public int getNexteventid() {
			return m_nexteventid;
		}

		public List<QueuedEvent> getEventlist() {
			return m_eventlist;
		}
	}

	/**
	 * Called when the condition we're waiting for has been fulfilled. This
	 * takes the event list and returns it as an event response structure. This
	 * structure is a JSON structure. The returned base structure is an object
	 * with status and key fields. If events are returned this base object has
	 * a field 'eventlist' which is an array of event objects and the associated
	 * data.
	 * <pre>
	 * 	{
	 * 		key: 2131244,			// This server's key. Must be repeated in the next request
	 * 		nexteventid: 2131,		// The last event ID seen. Must be passed in the next request,
	 * 		status: "eventcode",	// The status of the call (see below)
	 * 		eventlist: [			// Optional, only if events are found
	 * 			{
	 * 				channel: "channelname",
	 * 				eventid: 2131,
	 * 				data: .... 		// Whatever event data was present
	 * 			},
	 * 			// repeatedly
	 * 		]
	 * 	}
	 * </pre>
	 *
	 *
	 *
	 * @see to.etc.webapp.ajax.comet.CometContext#respond(javax.servlet.http.HttpServletResponse, boolean)
	 */
	@Override
	public void respond(final HttpServletResponse resp, final boolean timeout) throws Exception {
		if(timeout) {
			// Cancelled by the comet provider. Send a "TIMEOUT" or "EVENTS" depending on the list contents
			AjaxEventManager.getInstance().removeWaiting(this);
		}
		if(m_result == null)
			throw new IllegalStateException("Activated but response is not defined!?");
		//		System.out.println("AjaxEvent: sending "+m_result.getStatus()+", nxtid="+m_result.getNexteventid());
		renderJSON(resp, m_result);
	}

	private void renderJSON(final HttpServletResponse response, final Object res) throws Exception {
		response.setContentType("text/html"); // Jal 20060922 Do not change to text/javascript!! This makes Prototype eval() the response as a JS program which it is not.
		response.setCharacterEncoding("utf-8");
		//		getResponse().addHeader("X-JSON", "1.0");
		Writer w = response.getWriter();
		XmlWriter xw = new XmlWriter(w);
		JSONRenderer xr = new JSONRenderer(m_registry, xw, true);
		xr.render(res);
	}


	void sendResponse() {
		m_co.resume();
	}

	void append(final List<QueuedEvent> list) {
		if(m_eventList == null)
			m_eventList = new ArrayList<QueuedEvent>(list);
		else
			m_eventList.addAll(list);
	}

	/**
	 * Appends the event to the list *only* if it is not already there. This
	 * can easily be seen since event ids are always ascending in calls, so
	 * if the lists last item has a higher or equal event # the event was seen.
	 *
	 * @param e
	 * @return true if the event was accepted (added), false if it was seen earlier.
	 */
	boolean append(final QueuedEvent e) {
		if(m_eventList == null) {
			m_eventList = new ArrayList<QueuedEvent>();
			m_eventList.add(e);
			return true;
		}
		if(m_eventList.size() == 0) {
			m_eventList.add(e);
			return true;
		}

		if(m_eventList.get(m_eventList.size() - 1).getEventID() >= e.getEventID())
			return false;
		m_eventList.add(e);
		return true;
	}

	int getEventCount() {
		return m_eventList == null ? 0 : m_eventList.size();
	}

	boolean setKey(final int key) {
		boolean wassame = key == m_key;
		m_key = key;
		return wassame;
	}

	Set<String> getChannels() {
		return m_channels;
	}

	/**
	 * Called when the state in the request is no longer valid. This returns
	 * an "EXPIRED" packet containing the new key and nexteventid. The browser
	 * must refresh, then repost.
	 * @param lastmsg
	 */
	void setRegistrationExpired(final int nexteventid) {
		if(m_completionState != CompletionState.csWAITING)
			throw new IllegalStateException("Completion state is alread " + m_completionState);
		if(m_eventList != null && m_eventList.size() > 0)
			throw new IllegalStateException("EXPIRING a context what HAS events pending!?!");
		m_completionState = CompletionState.csBADKEY;
		m_result = new EventResult(m_key, nexteventid, "EXPIRED", null);
	}

	/**
	 * Set the data to send as an event, and use the lasteventid passed as the
	 * lastid.
	 * @param nexteventid
	 */
	private void sendEvents(final int nexteventid) {
		if(m_eventList == null)
			throw new IllegalStateException("sendEvents() called without events present!?");
		m_result = new EventResult(m_key, nexteventid, "EVENTS", m_eventList);
		m_eventList = null;
	}

	private void sendEvents() {
		if(m_eventList == null)
			throw new IllegalStateException("sendEvents() called without events present!?");
		sendEvents(m_eventList.get(m_eventList.size() - 1).getEventID() + 1);
	}

	int getLingerTime() {
		return m_lingerTime;
	}

	TimerTask getTimerTask() {
		return m_timerTask;
	}

	void setTimerTask(final TimerTask timerTask) {
		m_timerTask = timerTask;
	}

	boolean isLingering() {
		return m_lingering;
	}

	void setLingering(final boolean lingering) {
		m_lingering = lingering;
	}

	public String getRemoteUser() {
		return m_remoteUser;
	}

	/**
	 * Sets this thing as "completed OK". This prepares the response object.
	 */
	void setCompletedOK() {
		if(m_completionState != CompletionState.csWAITING)
			throw new IllegalStateException("The completion state is ALREADY set to " + m_completionState);
		m_completionState = CompletionState.csOKAY;
		sendEvents(); // Prepare the result
	}

	/**
	 * Called when the browser timeout has been reached.
	 *
	 * @param nexteventid
	 */
	void setBrowserTimeout(final int nexteventid) {
		if(m_completionState != CompletionState.csWAITING)
			throw new IllegalStateException("Programmer error: completion state is not WAITING but " + m_completionState);
		m_completionState = CompletionState.csTIMEOUT;
		m_result = new EventResult(m_key, nexteventid, "TIMEOUT", null);
	}


	boolean hasCompleted() {
		return m_completionState != CompletionState.csWAITING;
	}

	/**
	 * This tries to accept this response for completion, i.e. the returning
	 * of this response to the caller. When called this checks to see if
	 * we may respond currently which is the case if the lock count is 0 and
	 * the state is not waiting or finished. If responding is not allowed
	 * this returns false and nothing is changed.
	 * If responding is allowed we change this objects' state to finished and
	 * return true. All of this means that this call returns 'true' at most
	 * once.
	 * The data in here is protected by the lock on the event manager handling this.
	 * The caller must call "sendResponse" for every object he has called this
	 * routine for and got a 'true' to force the response to be sent.
	 *
	 * @return
	 */
	boolean acceptForCompletion() {
		if(m_completionState == CompletionState.csWAITING || m_completionState == CompletionState.csFINISHED || m_lockCount > 0)
			return false; // Still waiting/already finished/still locked.
		m_completionState = CompletionState.csFINISHED;
		return true;
	}
}
