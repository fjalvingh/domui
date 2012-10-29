package to.etc.domui.logic;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

import to.etc.webapp.query.*;

/**
 * Root logic context class.
 *
 * Vladimir: I removed your copy code because it cannot be done that way:
 * - we cannot make copies in a "separate" Hibernate context because it would be very, very expensive to do that multiple times (because of all of the lookups).
 * - you require a set PK in all entities, and that is clearly not the case in most models.
 * I created the copy logic into LogiModel.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 15, 2012
 */
final public class LogiContext {
	@Nonnull
	private QDataContext m_dataContext;

	private Map<Object, Object> m_storeMap = new HashMap<Object, Object>();

	private LogiModel m_model = new LogiModel();

	public LogiContext(@Nonnull QDataContext dataContext) {
		m_dataContext = dataContext;
	}

	@Nonnull
	public <T> T get(@Nonnull Class<T> clz, Object... params) throws Exception {
		Constructor<T> realc = null;
		Object[] callv = new Object[10];
		for(Constructor< ? > c : clz.getConstructors()) {
			//-- All arguments must map to something, then we use this constructor.
			Class< ? >[] formalar = c.getParameterTypes();
			int actix = 0;
			boolean matched = true;
			for(int i = 0; i < formalar.length; i++) {
				Class< ? > fa = formalar[i];
				if(fa.isAssignableFrom(LogiContext.class))
					callv[i] = this;
				else if(fa.isAssignableFrom(QDataContext.class)) {
					callv[i] = m_dataContext;
				} else {
					if(actix >= params.length) {
						//-- Out of parameters -> no match
						matched = false;
						break;
					}
					Object val = params[actix++];
					if(val == null) {
						//-- Null cannot be for primitives
						if(fa.isPrimitive()) {
							matched = false;
							break;
						}
						callv[i] = null;
					} else if(fa.isAssignableFrom(val.getClass())) {
						callv[i] = val;
					} else {
						matched = false;
						break;
					}
				}
			}
			if(matched) {
				if(realc != null)
					throw new IllegalStateException(clz + ": matched by 2 constructors: " + c + " and " + realc);
				realc = (Constructor<T>) c;
			}
		}

		if(realc == null)
			throw new IllegalStateException("No constructor found for " + clz + " with arguments " + params);
		T keyi = realc.newInstance(callv);
		T vali = (T) m_storeMap.get(keyi);
		if(null == vali) {
			m_storeMap.put(keyi, keyi);
			vali = keyi;
		}
		return vali;
	}

	public <T> void addRoot(T root) {
		m_model.addRoot(root);
	}

	public void updateCopy() throws Exception {
		m_model.updateCopy();
	}


}
