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

import java.util.*;

import org.slf4j.*;

/**
 * <p>Singleton AJAX event manager. This allows Java code to post an event to browsers
 * waiting for them. This singleton uses JSON to communicate with the browser; it
 * uses a specific CometContext implementation to allow asynchronous IO where the
 * servlet container supports it.</p>
 * <p>This implementation has support for "lossless" event retrieval. This means that
 * the browser will see all events, even when it's comet connection has been used and
 * returned. When events occur while the browser has abn outstanding Comet call then
 * all is well, and the events can be sent using that call. If however the call has
 * just been returned and a new event occurs then the browser would loose that event
 * because no comet call is present to sent it on.</p>
 *
 * <p>This gets fixed by assigning all events an unique number that gets incremented
 * for every event. Every time a browser issues a new Comet call it passes in the last
 * event number it has seen. The event manager will queue the events from the last
 * few minutes and will sent all of the events queued thus far to the browser.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 25, 2006
 */
public class AjaxEventManager {
	static private final Logger LOG = LoggerFactory.getLogger(AjaxEventManager.class);

	static private final int MIN_LINGER = 300;

	//	static private final String		ALL_CHANNELS = "$%all%$";

	static private final AjaxEventManager m_instance = new AjaxEventManager();

	private boolean m_initialized;

	private Timer m_timer = new Timer(true);

	/** The NEXT message number that will be assigned. This number must be [1..MAX_INT] */
	private int m_nextMessageNumber = 1;

	/** The first message number that is in the queue. [1..MAX_INT] */
	private int m_firstMessageNumber = 1;

	/** The circular event queue. */
	private QueuedEvent[] m_eventQueue = new QueuedEvent[1024];

	/** The event queue put index: the first FREE index to put an event in. */
	private int m_qput_ix;

	/** The event queue GET index: points to the first entry to GET. */
	private int m_qget_ix;

	/** The current #of entries in the queue. */
	private int m_qlength;

	/** Expire events in one minute. */
	private final long m_expiry = 60 * 1000;

	/** The max #of events that gets returned per call. */
	private final int m_maxevents = 20;

	/** Default browser timeout is 90 seconds. */
	private final long m_browserTimeout = 20 * 1000;

	/**
	 * This-instance's identification key. This gets initialized when the instance starts to
	 * an unique random number. Callers must specify the key of the last instance they talk
	 * with; if a caller specifies a key different from this one it means that that caller has
	 * talked with a different (older) incarnation of the event handler and needs to reinitialize
	 * it's user interface.
	 */
	private int m_key;

	/**
	 * This maps channels to the set of EventCometContexts waiting for an event
	 * on that channel.
	 */
	private Map<String, Set<EventCometContext>> m_channelMap = new HashMap<String, Set<EventCometContext>>();

	private final Map<String, Set<AjaxEventFilterFactory>> m_eventFilters = new HashMap<String, Set<AjaxEventFilterFactory>>();

	static public AjaxEventManager getInstance() {
		synchronized(m_instance) {
			int tries = 5;
			while(tries-- > 0) {
				if(m_instance.m_initialized)
					return m_instance;
				System.out.println("AjaxEventManager: waiting for initialization");
				try {
					m_instance.wait(5000);
				} catch(InterruptedException x) {}
			}
			throw new IllegalStateException("The Ajax Event Manager did not initialize in a reasonable amount of time");
		}
	}

	static public void initialize() {
		synchronized(m_instance) {
			if(m_instance.m_initialized)
				throw new IllegalStateException("Already initialized");
			m_instance.init();
			m_instance.m_initialized = true;
			m_instance.notifyAll();
		}
	}

	private void init() {
		m_key = (int) ((System.currentTimeMillis() + System.nanoTime()) & 0x7fffffff);
		m_firstMessageNumber = m_nextMessageNumber = 1;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Event queue management.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Put an event in the event queue. When called it expires old
	 * events before posting the new'un.
	 * @param channels
	 * @param data
	 */
	private synchronized QueuedEvent putEvent(final String channel, final Object data) {
		long ts = System.currentTimeMillis();
		expire(ts - m_expiry);

		/*
		 * 20101207 jal Bugfix: when enlarging the queue, we need to normalize it so
		 * the get pointer is at 0 and the put pointer is at the old length.
		 */
		if(m_qlength >= m_eventQueue.length) { // We must grow the queue
			/*
			 * Old queue:
			 *             v- qget
			 * [xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx]
			 *            ^ qput
			 *  ----a----- --------b----------
			 * The new, enlarged queue must look like:
			 *
			 *  v- qget
			 * [bbbbbbbbbbbbbbbaaaaaaaaaaaaaaa--------------------]
			 *                                ^qput
			 */
			//-- Allocate the bigger array
			int sz = m_eventQueue.length * 2;
			QueuedEvent[] ar = new QueuedEvent[sz];

			//-- Copy all elements at and after the GET pointer to the start
			int elen = m_eventQueue.length - m_qget_ix;
			if(elen > 0)
				System.arraycopy(m_eventQueue, m_qget_ix, ar, 0, elen); // Copy "b" segment to start of array (get ptr is inclusive)
			System.arraycopy(m_eventQueue, 0, ar, elen, m_qput_ix); // PUT pointer is exclusive
			m_qget_ix = 0;
			m_qput_ix = m_qlength;
			m_eventQueue = ar;
		}
		m_qlength++;
		int id = m_nextMessageNumber++;
		if(id >= Integer.MAX_VALUE) {
			m_nextMessageNumber = id = 1; // Wrap if max reached
		}
		QueuedEvent e = new QueuedEvent(id, ts, channel, data);
		m_eventQueue[m_qput_ix++] = e; // Queue
		if(m_qput_ix >= m_eventQueue.length)
			m_qput_ix = 0;
		return e;
	}

	/**
	 * Collects all of the events since the event passed. If no events have
	 * occured this returns null.
	 *
	 * @param startingeventid	The first event ID to return, if present
	 * @return
	 */
	private synchronized List<QueuedEvent> getEventsSince(final int startingeventid, int max, final Set<String> channels) {
		expire(System.currentTimeMillis() - m_expiry);
		if(startingeventid < m_firstMessageNumber || startingeventid >= m_nextMessageNumber)// Too old or at current level
			return null;
		int delta = (startingeventid - m_firstMessageNumber); // Get index within queue
		if(m_eventQueue == null)
			throw new IllegalStateException("Eventqueue NULL??");
		if(channels == null)
			throw new IllegalStateException("Channels null????");

		List<QueuedEvent> list = null;
		int ix = m_qget_ix + delta;
		if(ix >= m_eventQueue.length)
			ix -= m_eventQueue.length; // Wrap around
		int len = m_qlength - delta; // #entries to handle,
		while(len-- > 0) {
			QueuedEvent e = m_eventQueue[ix++]; // Get item,
			if(e == null)
				throw new IllegalStateException("NULL EVENT encountered??");
			if(channels.contains(e.getChannel())) {
				if(list == null)
					list = new ArrayList<QueuedEvent>();
				list.add(e);
				if(--max <= 0)
					break;
			}
			if(ix >= m_eventQueue.length)
				ix = 0;
		}
		return list;
	}

	/**
	 * Walks the event queue, and removes all events that are older than
	 * the timestamp passed.
	 */
	private synchronized void expire(final long ts) {
		if(m_eventQueue == null)
			throw new IllegalStateException("Eventqueue NULL??");
		while(m_qlength > 0) {
			QueuedEvent e = m_eventQueue[m_qget_ix];
			if(e == null)
				throw new IllegalStateException("NULL EVENT encountered??");
			if(e.getEventTS() > ts) // This event is still valid?
				return; // Then we're done (the queue is sorted)

			//-- Discard this event.
			m_qget_ix++;
			if(m_qget_ix >= m_eventQueue.length)
				m_qget_ix = 0;
			m_qlength--;
			m_firstMessageNumber++;
			if(m_firstMessageNumber >= Integer.MAX_VALUE)
				m_firstMessageNumber = 1;
		}
	}

	/**
	 * Registers a context as waiting for an event. The context gets added to
	 * all channels.
	 * in
	 * @param ectx
	 * @param timeout The #of ms after which this should be timed out, either because the
	 * 					linger period expired or the browser timeout expired.
	 */
	private synchronized void registerWaiting(final EventCometContext ectx, final long timeout) {
		Set<String> channels = ectx.getChannels();
		for(String ch : channels) {
			Set<EventCometContext> wset = m_channelMap.get(ch);
			if(wset == null) { // Not yet a map there? Then create one
				wset = new HashSet<EventCometContext>();
				m_channelMap.put(ch, wset);
			}
			wset.add(ectx); // Add to this-channels's listener list
		}

		//- Register an expiry time.
		schedule(ectx, timeout);
	}

	private synchronized void schedule(final EventCometContext ectx, long timeout) {
		TimerTask tt = ectx.getTimerTask();
		if(tt != null) {
			tt.cancel();
		}
		if(timeout <= 0)
			timeout = 100;
		tt = new TimerTask() {
			@Override
			public void run() {
				/*
				 *
				 */
				handleTimeout(ectx);
			}
		};
		ectx.setTimerTask(tt);
		m_timer.schedule(tt, timeout);
	}

	/**
	 * Remove the context from all it's channels.
	 * @param ectx
	 */
	private synchronized void deregisterChannels(final EventCometContext ectx) {
		Set<String> channels = ectx.getChannels();
		for(String ch : channels) {
			Set<EventCometContext> wset = m_channelMap.get(ch);
			if(wset != null)
				wset.remove(ectx);
		}
	}

	/**
	 * Called when a connection has a timeout. This determines the timeout cause
	 * then sends back a response.
	 *
	 * @param ectx
	 */
	void handleTimeout(final EventCometContext ectx) {
		boolean sendcompletion = false;
		synchronized(this) {
			if(ectx.hasCompleted())
				return;
			int nextid = m_nextMessageNumber; // Save the first new message ID that will arrive,
			if(ectx.getEventCount() == 0) { // No events? Then we have expired because of the browser timeout
				//-- A browser timeout has occured. Set the completion status
				ectx.setBrowserTimeout(nextid); // Set the status to "browser timed out",
			} else {
				//-- We timed out but there are events (we were lingering). Just return the result.
				ectx.setCompletedOK();
			}

			//-- We're going to terminate, so unregister,
			deregisterChannels(ectx); // Remove from all event channels
			if(ectx.acceptForCompletion()) // If we may return this dude do so,
				sendcompletion = true;
		}

		if(sendcompletion) // If we own this one
			ectx.sendResponse(); // return the response outside of the lock
	}

	/**
	 * Cancel the request because the container wants it. When called the response handler
	 * is active and we cannot prevent the response from being sent except by blocking. So
	 * we do that while the lock count is > 0 to prevent trouble in the filter chain. When
	 * the lock count is > 0 it should be reduced to 0 quickly by the postEvent call in
	 * execution. At that time a signal will release this blocker.
	 *
	 * @param ectx
	 */
	synchronized void removeWaiting(final EventCometContext ectx) {
		TimerTask tt = ectx.getTimerTask();
		if(tt != null)
			tt.cancel();

		// Force this thingy completed if necessary.
		if(!ectx.hasCompleted()) {
			//-- If this thingy has events post it as a response, else post it as a timeout
			if(ectx.getEventCount() == 0)
				ectx.setBrowserTimeout(m_nextMessageNumber);
			else
				ectx.setCompletedOK();
		}
		deregisterChannels(ectx);

		/*
		 * Now loop while the lock count is > 0 before returning.
		 */
		ectx.waitForZeroLock();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Accepting Comet...									*/
	/*--------------------------------------------------------------*/
	static private final AjaxEventFilter[] NO_FILTERS = new AjaxEventFilter[0];

	/**
	 * Registers a browser that starts to wait for events. Called when a comet call
	 * begins and has been decoded.
	 *
	 * @param ectx
	 * @param channels
	 * @param nexteventid	The next event ID that we wait for.
	 * @param lingertime	The #of milliseconds that this server may wait for new events
	 * 						so that multiple events can be returned in one request.
	 */
	void registerWaiter(final EventCometContext ectx, int nexteventid) {
		/*
		 * If the last event ID is too old, or if the key is different from the current
		 * instance's key we have a browser whose's session is too old. In that case we
		 * return immediately and post a 'too old' general event. This should force the
		 * browser to reset it's state immediately.
		 */
		boolean sentdone = false; // When set to T this calls ectx.done() outside of the lock.
		boolean needlinger = false;
		try {
			/*
			 * Loop: try to register with the event manager. After validity checks we check if there
			 * are pending events for this connection. If not then we set the connection
			 * to WAITING and we're done (we exit the call). All of this within a single
			 * synchronized block (fast).
			 *
			 * If however events are pending we need to leave the synchronized block to
			 * call the event filters for the events to forward; then we add all events
			 * to the context. If that causes the context to be full we do not queue but
			 * return the data immediately.
			 * If more events fit we loop, starting the process anew; if no events have
			 * arrived in the meantime we enter WAITING mode as described above. If events
			 * did arrive we just do the same again.
			 */
			List<QueuedEvent> newlist = null;
			for(;;) {
				/*
				 * Try to synchronize and wait; we only exit this block if events were pending.
				 */
				List<QueuedEvent> list = null;
				synchronized(this) {
					/*
					 * If this is the second or later time entry within this loop then
					 * newlist may hold filtered events that MUST be queued for this
					 * event. Do that first before continuing.
					 */
					if(newlist != null && newlist.size() > 0) {
						/*
						 * The earlier loop has prepared filtered events to send... Do so,
						 */
						ectx.append(newlist); // Append the filtered events,
						newlist.clear(); // Discard,

						/*
						 * Check if we need to return immediately; this is the case if the
						 * max. #of events is reached, or when we're not allowed to linger for
						 * new events.
						 */
						if(ectx.getEventCount() >= m_maxevents || ectx.getLingerTime() < MIN_LINGER) {
							ectx.setCompletedOK();
							sentdone = true;
							return;
						}

						/*
						 * We have room to spare- loop again to check for messages...
						 */
						needlinger = true; // If we sleep we need a lingering sleep
					}

					/*
					 * The browser has talked with an event manager before. If that was not *this*
					 * event manager OR if it has taken too long to call us back sent an expired
					 * packet back.
					 * Since the eventID wraps around we need to check both for unwrapped event (begin < end) and
					 * a wrapped id (begin > end).
					 */
					if(!ectx.setKey(m_key) || (m_firstMessageNumber <= m_nextMessageNumber && (nexteventid < m_firstMessageNumber || nexteventid > m_nextMessageNumber))
						|| (m_firstMessageNumber > m_nextMessageNumber && (nexteventid > m_nextMessageNumber || nexteventid < m_firstMessageNumber))) {
						//-- This browser was talking with an earlier instance. Sent it a RESET command.
						ectx.setRegistrationExpired(m_nextMessageNumber); // return the current state,
						sentdone = true; // May return immediately with sent because no client can have obtained this context.
						return;
					}

					/*
					 * The browser has a valid previous registration. Check if events I wait for
					 * have occured since it's last call.
					 */
					list = getEventsSince(nexteventid, m_maxevents, ectx.getChannels());
					if(list == null) {
						//-- No pending events- register as WAITING with the browser timeout /linger timeout and be done
						if(needlinger)
							ectx.setLingering(true);
						registerWaiting(ectx, needlinger ? ectx.getLingerTime() : m_browserTimeout);
						return;
					} else
						nexteventid = list.get(list.size() - 1).getEventID() + 1;
				} // synchronized

				/*
				 * Ok; we're not locked now and we have pending events. The context is not queued yet
				 * so it is not visible to all other tasks running. First pass all events thru all
				 * event filters. Do this as fast as possible, caching all filters.
				 */
				Map<String, AjaxEventFilter[]> chainmap = new HashMap<String, AjaxEventFilter[]>(); // channel's filter list

				//-- Filter resources protection block
				if(newlist == null)
					newlist = new ArrayList<QueuedEvent>(list.size() + 20);
				try {
					for(int i = 0; i < list.size(); i++) { // Walk all events to filter,
						//-- Get the appropriate filter chain,
						QueuedEvent e = list.get(i);
						AjaxEventFilter[] far = chainmap.get(e.getChannel());
						if(far == null) {
							far = createEventFilters(e.getChannel());
							if(far == null)
								far = NO_FILTERS;
							chainmap.put(e.getChannel(), far); // Cache
						}
						Object res = e.getData();
						if(far != NO_FILTERS)
							res = filterEvent(far, ectx, res);
						newlist.add(e.createCopy(res)); // Clone original with filtered data
					}
				} finally {
					for(AjaxEventFilter[] far : chainmap.values())
						releaseEventFilters(far);
				}

				/*
				 * Ok: we're done... These need to be queued/sent; this gets done at entry of
				 * the above block, so loop on!!
				 */
			}
		} finally {
			if(sentdone)
				ectx.sendResponse();
		}
	}


	/**
	 * Onderstaande moet synchronized zijn, omdat er anders bijvoorbeeld
	 * een filter toegevoegd kan worden tijdens het copieren.
	 * We maken een copie om te voorkomen dat er tijdens de verwerking
	 * een filter toegevoegd kan worden.
	 *
	 * @param channel
	 * @return
	 */
	private synchronized Set<AjaxEventFilterFactory> getEventFilters(final String channel) {
		// Make a shallow copy of the set of filters
		Set<AjaxEventFilterFactory> filterSet = m_eventFilters.get(channel);
		if(filterSet == null)
			return null;
		return new TreeSet<AjaxEventFilterFactory>(filterSet);
	}

	/**
	 * Get the list of event filter factories and create and initialize an array of
	 * event filters, suitable for filtering the event data passed. If no filters
	 * are needed this returns null.
	 *
	 * @param channel
	 * @return
	 */
	private AjaxEventFilter[] createEventFilters(final String channel) {
		Set<AjaxEventFilterFactory> fset = getEventFilters(channel); // Get all factories
		if(fset == null || fset.size() == 0)
			return null;

		AjaxEventFilter[] far = new AjaxEventFilter[fset.size()];
		boolean okay = false;
		try {
			int ix = 0;
			for(AjaxEventFilterFactory ff : fset) {
				far[ix++] = ff.createAjaxEventFilter();
			}
			okay = true;
			return far;
		} finally {
			if(!okay)
				releaseEventFilters(far);
		}
	}

	//	private void	reinitializeEventFilters(AjaxEventFilter[] far, Object data) {
	//		for(AjaxEventFilter f : far) {
	//			try {
	//				f.prepare(data);
	//			} catch(Exception x) {
	//				LOG.log(Level.INFO, "Exception in AjaxEventFilter "+f, x);
	//				x.printStackTrace();
	//				//-- FIXME This filter should not be called after this error.... Not very important because it goes wrong anyway
	//			}
	//		}
	//	}

	/**
	 * Release all filters by calling their close() method. Allows for partially filled array.
	 * @param far
	 */
	private void releaseEventFilters(final AjaxEventFilter[] far) {
		if(far == null)
			return;
		for(AjaxEventFilter ef : far) {
			try {
				if(ef != null)
					ef.close();
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	public synchronized void registerEventFilter(final String channel, final AjaxEventFilterFactory ajaxEventFilterFactory) {
		Set<AjaxEventFilterFactory> wset = m_eventFilters.get(channel);
		if(wset == null) { // Als er nog geen set aan filters bestaat voor
			// dit event creer deze dan.
			wset = new HashSet<AjaxEventFilterFactory>();
			m_eventFilters.put(channel, wset);
		}
		wset.add(ajaxEventFilterFactory); // Toevoegen aan de filters voor dit event
	}

	private Object filterEvent(final AjaxEventFilter[] filters, final EventCometContext ctx, Object res) {
		for(AjaxEventFilter ef : filters) {
			if(ef != null) {
				try {
					Object tres = ef.filterEvent(ctx, res);
					if(tres == null)
						throw new IllegalStateException("The event filter " + ef + " returned null!?!");
					res = tres;
				} catch(Exception x) {
					/*
					 * Exceptions within the filter leave the data as-is, i.e. as if the filter has not run.
					 */
					LOG.error("Exception in AjaxEventFilter " + ef, x);
					x.printStackTrace();
				}
			}
		}
		return res;
	}

	/**
	 * Sends an event to the specified channels. All listerers to the event get the
	 * event, and the event gets queued.
	 *
	 * @param channels
	 * @param data
	 */
	public void postEvent(final String channel, final Object data) {
		//-- 1. Obtain a set of all waiting contexts that accept this event
		List<EventCometContext> doset = new ArrayList<EventCometContext>(); // All contexts that accept this change,
		QueuedEvent e;
		synchronized(this) {
			//-- 1. Queue the event, obtaining an QueuedEvent.
			e = putEvent(channel, data);

			//-- 2. Get all contexts listening for this event and *own* them,
			Set<EventCometContext> cset = m_channelMap.get(channel);
			if(cset == null)
				return;
			for(EventCometContext cc : cset) {
				if(cc.lock()) // Is this still usable?
					doset.add(cc); // Then add it to the set
			}
			if(doset.size() == 0) // No listeners?
				return; // Then begone.
		}

		/*
		 * Start filtering: create unique results for each listener. Filtering *must* occur
		 * outside of locks.
		 */
		Object[] results = new Object[doset.size()]; // Intermediary result list,
		AjaxEventFilter[] filters = null;
		try {
			filters = createEventFilters(channel); // Create filters and initialize 'm for this event
			for(int j = doset.size(); --j >= 0;) {
				EventCometContext ctx = doset.get(j);
				Object res = data; // Start with original

				//-- Call all filters, if applicable
				if(filters != null)
					res = filterEvent(filters, ctx, res);
				results[j] = res; // The event for this connection, after filtering
			}
		} finally {
			//-- Force all filters to release their resources
			releaseEventFilters(filters);
			filters = null;
		}

		/*
		 * We have called all filters and the results are in. Now at least unlock
		 * all requests, and append the event to the ones that needed it.
		 */
		List<EventCometContext> clist = new ArrayList<EventCometContext>(doset.size());
		synchronized(this) {
			for(int i = doset.size(); --i >= 0;) {
				EventCometContext ctx = doset.get(i); // Get context,
				ctx.unlock(); // Make sure it's released,
				if(!ctx.hasCompleted()) { // Has completed in the meantime -> throw away event for this dude.
					//-- The event is accepted, so post it for sending with the new data
					if(!ctx.append(e.createCopy(results[i])))
						throw new IllegalStateException("!? Programmer error - context does not accept event!?");

					//-- Do we have to send immediately?
					if(ctx.getEventCount() >= m_maxevents || ctx.getLingerTime() < MIN_LINGER) {
						//-- Send response immediately, if the timer has not finished
						ctx.setCompletedOK(); // Set "completed" with "OK" status
						ctx.getTimerTask().cancel(); // Stop any timeout
						deregisterChannels(ctx); // Remove from all listeners
					} else {
						//-- Do we need to set a linger timeout?
						if(!ctx.isLingering()) {
							schedule(ctx, ctx.getLingerTime());
						}
					}
				}

				/*
				 * For all of the contexts we have had locked we need to check if
				 * they are currently ready for completion, because at filter time
				 * events like timeouts may have occured that could not send the
				 * response due to the lock.
				 */
				if(ctx.acceptForCompletion()) // May we send a response?
					clist.add(ctx); // Yes=> cause it to be sent at the end,
			}
		} // synchronized

		//-- Now notify all thingies outside the lock
		for(EventCometContext ectx : clist)
			ectx.sendResponse();
	}

	/**
	 * Called when the application will die. This releases all requests and cancels the timers.
	 */
	public void destroy() {
		m_timer.cancel();
		m_timer = null;
		List<EventCometContext> list = new ArrayList<EventCometContext>();
		synchronized(this) {
			for(Set<EventCometContext> ctx : m_channelMap.values()) {
				list.addAll(ctx);
			}
			m_channelMap = null;
		}
		for(EventCometContext c : list) {
			try {
				c.sendResponse();
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}
}
