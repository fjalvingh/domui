package to.etc.server.ajax.eventmanager;

/**
 * <p>Filters Ajax events for each individual Comet connection/user. When an event
 * is generated all current connections to the comet handler must receive the
 * event.</p>
 *
 * <p>Logically, the event filter's "filterEvent" method gets called for <i>every</i>
 * event when it gets passed to <i>every</i> comet listener. This means that the
 * filter has the ability to change each event for each listener individually.</p>
 *
 * <p>For performance reasons the filters must be able to operate in bulk mode. In this
 * mode the filter initializes for one specific event, then it's filterEvent() method
 * gets called for every listener that needs the event. After this the system calls
 * the filter's close() method allowing it to release any resources it has allocated.</p>
 *
 * <p>When filtering the filter is <i>not</i> allowed to change the input data because that
 * is the single copy of the data that was passed to the postEvent() call. If the filter
 * needs to change data it has to create a copy, change the copy and return that as it's
 * result</p>
 */
public interface AjaxEventFilter {
	/**
	 * Filter the data.
	 *
	 * @param eventCometContext
	 * @return
	 */
	public Object filterEvent(EventCometContext eventCometContext, Object eventdata) throws Exception;

	/**
	 * Eventueel opruimen van allerlei zooi.
	 *
	 */
	public void close();
}
