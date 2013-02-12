package to.etc.domui.logic;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.errors.*;
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

	private Map<Object, Object> m_storeMap = new HashMap<Object, Object>();

	private LogiModel m_model = new LogiModel();

	public LogiContext(@Nonnull QDataContext dataContext) {
		m_dataContextMap.put(QContextManager.DEFAULT, dataContext);
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
		Object[] callv = new Object[10];
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

	public <T> void addRoot(T root) {
		m_model.addRoot(root);
	}

	public void updateCopy() throws Exception {
		m_model.updateCopy();
	}


	/**
	 * @param m
	 */
	public void addMessage(@Nonnull UIMessage m) {

	}


}
