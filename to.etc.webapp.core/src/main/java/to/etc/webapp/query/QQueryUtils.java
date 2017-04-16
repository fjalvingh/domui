package to.etc.webapp.query;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

import to.etc.util.*;
import to.etc.webapp.qsql.*;

final public class QQueryUtils {
	private QQueryUtils() {}

	@Nonnull
	static public <R> List<R> mapSelectionQuery(@Nonnull QDataContext dc, @Nonnull Class<R> resultInterface, @Nonnull QSelection< ? > sel) throws Exception {
		if(!resultInterface.isInterface())
			throw new IllegalArgumentException(resultInterface + " must be an interface");

		List<Object[]> resl = dc.query(sel);
		if(resl.size() == 0)
			return new ArrayList<R>();						// Return empty modifyable list

		//-- Create mapping proxies for all thingies.
		ProxyFactory<R> pf = createResultFactory(resultInterface);
		List<R> result = new ArrayList<R>(resl.size());
		for(Object[] rv : resl) {
			if(rv == null)
				continue;
			result.add(pf.createInstance(rv));
		}
		return result;
	}

	@Nullable
	static public <R> R mapSelectionOneQuery(@Nonnull QDataContext dc, @Nonnull Class<R> resultInterface, @Nonnull QSelection< ? > sel) throws Exception {
		if(!resultInterface.isInterface())
			throw new IllegalArgumentException(resultInterface + " must be an interface");

		Object[] resl = dc.queryOne(sel);
		if(null == resl)
			return null;
		ProxyFactory<R> pf = createResultFactory(resultInterface);
		return pf.createInstance(resl);
	}

	private static class ProxyFactory<T> {
		final private ClassLoader m_cl;

		final private Map<Method, Integer> m_imap;

		final private Class<T>[] m_resultInterface;

		public ProxyFactory(@Nonnull ClassLoader cl, @Nonnull Map<Method, Integer> imap, @Nonnull Class<T> resultInterface) {
			m_cl = cl;
			m_imap = imap;
			m_resultInterface = new Class[1];
			m_resultInterface[0] = resultInterface;
		}

		@Nonnull
		public T createInstance(@Nonnull final Object[] row) {
			return (T) Proxy.newProxyInstance(m_cl, m_resultInterface, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					Integer index = m_imap.get(method);
					if(null == index)
						throw new IllegalArgumentException("Unexpected method " + method + " not found in map");
					Object rv = row[index.intValue()];
					return RuntimeConversions.convertTo(rv, method.getReturnType());
				}
			});
		}
	}

	static private <T> ProxyFactory<T> createResultFactory(@Nonnull Class<T> resultInterface) {
		//-- Get all defined methods in order.
		Map<Method, Integer> imap = new HashMap<Method, Integer>();
		Method[] mar = resultInterface.getDeclaredMethods();
		for(int index = 0; index < mar.length; index++) {
			Method m = mar[index];
			QFld fix = m.getAnnotation(QFld.class);
			if(null == fix)
				throw new QQuerySyntaxException(m + " must be annotated with @QFld to define the location in the result set");
			imap.put(m, Integer.valueOf(fix.value()));
		}

		ClassLoader cl = resultInterface.getClassLoader();
		return new ProxyFactory<T>(cl, imap, resultInterface);
	}

	/**
	 * Util that enables easier retrieving of rows count on specified QCriteria.
	 *
	 * @param dc
	 * @param q
	 * @return
	 * @throws Exception
	 */
	public static <K, T extends IIdentifyable<K>> int queryCount(@Nonnull QDataContext dc, @Nonnull QCriteria<T> q) throws Exception {
		QSelection<T> rest = QSelection.create(q.getBaseClass());
		rest.setRestrictions(q.getRestrictions());
		rest.count("id");
		Object[] count = dc.queryOne(rest.testId(q.getTestId()));
		if(count != null && count.length > 0) {
			return ((Number) count[0]).intValue();
		}
		return 0;
	}

//	interface myData {
//		@QFld(1)
//		int count();
//
//		@QFld(2)
//		String name();
//	}
//
//	public static void main(String[] args) throws Exception {
//		createResultFactory(myData.class);
//
//	}


}
