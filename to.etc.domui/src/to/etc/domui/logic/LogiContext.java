package to.etc.domui.logic;

import java.util.*;

import javax.annotation.*;

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

	final private Map<String, QDataContext> m_dataContextMap = new HashMap<String, QDataContext>();

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

	@Nonnull
	public <T> T get(@Nonnull T instance) throws Exception {
		T xmap = (T) m_storeMap.get(instance);
		if(null != xmap)
			return xmap;
		m_storeMap.put(instance, instance);
		return instance;
	}

	public <T> void addRoot(T root) {
		m_model.addRoot(root);
	}

	public void updateCopy() throws Exception {
		m_model.updateCopy();
	}
}
