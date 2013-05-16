package to.etc.domui.logic;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.misc.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.logic.events.*;
import to.etc.webapp.*;
import to.etc.webapp.query.*;

/**
 * This context class encapsulates instantiated business logic classes, and cache data used by those
 * classes.
 *
 * Root logic context class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 15, 2012
 */
final public class LogiContext {
	@Nonnull
	final private QDataContext m_dc;

	@Nonnull
	final private Map<String, QDataContext> m_dataContextMap = new HashMap<String, QDataContext>();

	@Nonnull
	final private Map<Class< ? >, Map<Object, ILogic>> m_instanceMap = new HashMap<Class< ? >, Map<Object, ILogic>>();

	@Nonnull
	final private Map<Class< ? >, ILogic> m_allMap = new HashMap<Class< ? >, ILogic>();


	@Nonnull
	final private LogiModel m_model = new LogiModel();

	@Nonnull
	final private List<UIMessage> m_actionMessageList = new ArrayList<UIMessage>();

	@Nonnull
	final private List<IMessageListener> m_actionMsgListenerList = new ArrayList<IMessageListener>();

	@Nonnull
	final private List<ILogiEventListener> m_eventListenerList = new ArrayList<ILogiEventListener>();

	/**
	 * Create and set the default data context to use.
	 * @param dataContext
	 */
	public LogiContext(@Nonnull QDataContext dataContext) {
		m_dataContextMap.put("", dataContext);
		m_dc = dataContext;
	}

	/**
	 * Return the default QDataContext.
	 * @return
	 */
	@Nonnull
	public QDataContext dc() {
		return m_dc;
	}

	/**
	 * Find/create the specified logic wrapper for instance.
	 * @param clz
	 * @param instance
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public <L extends ILogic, K, T extends IIdentifyable<K>> L get(@Nonnull Class<L> clz, @Nonnull T instance) throws Exception {
		//-- Already exists in this context?
		Map<Object, ILogic> cmap = m_instanceMap.get(clz);
		if(null == cmap) {
			cmap = new HashMap<Object, ILogic>();
			m_instanceMap.put(clz, cmap);
		}

		/*
		 * To support unsaved object, we try to use the identity hashcode 1st. Only if that fails we'll use the ID.
		 */
		int ihc = System.identityHashCode(instance);
		Object key = "ihc-" + ihc;
		ILogic logic = cmap.get(key);
		if(null == logic) {
			key = instance.getId();												// Get the ID for this instance.
			if(null != key) {
				logic = cmap.get(key);
			}
		}
		if(null != logic)
			return (L) logic;

		//-- Nothing there. We need to create an instance.
		for(Constructor< ? > c : clz.getConstructors()) {
			Class< ? >[] formalar = c.getParameterTypes();						// We only accept constructor(LogiContext, T)
			if(formalar.length != 2)
				continue;
			if(!formalar[0].isAssignableFrom(LogiContext.class))
				continue;
			if(!formalar[1].isAssignableFrom(instance.getClass()))
				continue;

			//-- We got L(LogiContext, T). Instantiate the object using it.
			L ni = (L) c.newInstance(this, instance);
			if(null == ni)
				throw new IllegalStateException("Java sucks balls error");

			cmap.put(key, ni);
			return ni;
		}

		throw new ProgrammerErrorException("Could not create an instance of " + clz + ": constructor(LogiContext, " + instance.getClass().getName() + ") not found");
	}

	/**
	 * Find an undecorating instance (all-xxxx instance) for logic.
	 * @param clz
	 * @param instance
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public <L extends ILogic> L get(@Nonnull Class<L> clz) throws Exception {
		//-- Already exists in this context?
		ILogic il = m_allMap.get(clz);
		if(null != il)
			return (L) il;

		//-- Nothing there. We need to create an instance.
		for(Constructor< ? > c : clz.getConstructors()) {
			Class< ? >[] formalar = c.getParameterTypes();						// We only accept constructor(LogiContext, T)
			if(formalar.length != 1)
				continue;
			if(!formalar[0].isAssignableFrom(LogiContext.class))
				continue;

			//-- We got L(LogiContext). Instantiate the object using it.
			L ni = (L) c.newInstance(this);
			if(null == ni)
				throw new IllegalStateException("Java sucks balls error");
			m_allMap.put(clz, ni);
			return ni;
		}

		throw new ProgrammerErrorException("Could not create an instance of " + clz + ": constructor(LogiContext) not found");
	}

	public <T> void addRoot(T root) {
		m_model.addRoot(root);
	}

	public void updateCopy() throws Exception {
		m_model.updateCopy();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Phase handling.										*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 */
	public void startPhase() throws Exception {
		m_actionMessageList.clear();
		m_model.updateCopy();
	}

	/**
	 * Should be called @ user interaction end time.
	 */
	public void endPhase() throws Exception {
		LogiEventSet eventSet = m_model.compareCopy();
//		System.out.println("model: eventSet=" + eventSet);

		LogiEvent le = eventSet.createEvent();
		sendEvent(le);

		if(m_actionMessageList.size() > 0) {
			for(IMessageListener l : m_actionMsgListenerList) {
				l.actionMessages(m_actionMessageList);
			}
			m_actionMessageList.clear();
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Error and action error events.						*/
	/*--------------------------------------------------------------*/

	/**
	 * Add a listener for Action messages. The listener will be called at the end of a "phase" if
	 * message(s) were posted during it.
	 * @param l
	 */
	public void addActionMessageListener(@Nonnull IMessageListener l) {
		m_actionMsgListenerList.add(l);
	}

	/**
	 * Add a message to be displayed as the result of an "action". This message type is different from a "state" message: it is caused by an action
	 * that needs to send some message, which is related to the action only and transient. This differs from messages that represent an error in the
	 * current state of the model. Messages like these are usually displayed as a {@link MsgBox}.
	 * @param m
	 */
	public void addActionMessage(@Nonnull UIMessage m) {
		m_actionMessageList.add(m);
	}

	public void addEventListener(@Nonnull ILogiEventListener listener) {
		m_eventListenerList.add(listener);
	}

	public void removeEventListener(@Nonnull ILogiEventListener listener) {
		m_eventListenerList.remove(listener);
	}

	private void sendEvent(@Nonnull LogiEvent event) throws Exception {
		for(ILogiEventListener lel : m_eventListenerList)
			lel.logicEvent(event);
	}

}
