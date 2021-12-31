package to.etc.domui.injector;

import org.apache.poi.ss.formula.functions.T;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.AbstractPage;
import to.etc.util.PropertyInfo;
import to.etc.webapp.ProgrammerErrorException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Access checker which uses the type of the value to check access to it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-12-18.
 */
public class TypedPropertyAccessChecker implements IInjectedPropertyAccessChecker {
	private Map<Class<?>, ITypedValueAccessChecker<?>> m_checkerMap = Collections.emptyMap();

	private List<ITypedValueAccessChecker<Object>> m_anyCheckerList = new CopyOnWriteArrayList<>();

	public synchronized <T> void register(Class<T> clz, ITypedValueAccessChecker<T> check) {
		Map<Class<?>, ITypedValueAccessChecker<?>> map = new HashMap<>(m_checkerMap);
		if(map.put(clz, check) != null)
			throw new ProgrammerErrorException("Duplicate typed access checker for class " + clz.getName());
		m_checkerMap = Collections.unmodifiableMap(map);
	}

	public void registerAny(ITypedValueAccessChecker<Object> checker) {
		m_anyCheckerList.add(checker);
	}

	private synchronized Map<Class<?>, ITypedValueAccessChecker<?>> getCheckerMap() {
		return m_checkerMap;
	}

	@Override
	public void checkAccessAllowed(PropertyInfo info, AbstractPage page, @Nullable Object value) throws Exception {
		checkAccessInternal(info, page, value);
	}

	@Override
	public boolean checks(Class<?> parameterType) {
		ITypedValueAccessChecker<T> checker = findClassChecker(parameterType);
		return checker != null;
	}

	private <T> void checkAccessInternal(PropertyInfo info, AbstractPage page, @Nullable Object value) throws Exception {
		if(null == value)
			return;

		ITypedValueAccessChecker<T> checker = findClassChecker(value.getClass());
		if(null != checker) {
			checker.checkAccessAllowed(info, page, (T) value);
			return;
		}

		//Checks if any of registered access checkers can grant the access. If none can grant it, we abort with first access check exception that was delivered by checkers.
		AccessCheckException firstException = null;
		for(ITypedValueAccessChecker<Object> any : m_anyCheckerList) {
			try {
				any.checkAccessAllowed(info, page, value);
				return;
			} catch(AccessCheckException x) {
				if(null == firstException) {
					firstException = x;
				}
			}
		}
		if(null != firstException) {
			throw firstException;
		}
	}

	@Nullable
	private <T> ITypedValueAccessChecker<T> findClassChecker(Class<?> clz) {
		Map<Class<?>, ITypedValueAccessChecker<?>> map = getCheckerMap();
		Class<?> curr = clz;
		for(; ; ) {
			if(curr == Object.class || curr == null)
				return null;
			ITypedValueAccessChecker<?> checker = map.get(curr);
			if(null != checker) {
				return (ITypedValueAccessChecker<T>) checker;
			}

			curr = curr.getSuperclass();
		}
	}
}
