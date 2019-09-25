package to.etc.domui.hibernate.memorydb;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-09-19.
 */
class ProxyBuilder {
	static private final Set<String> IGNOREMETHODS = new HashSet<>(Arrays.asList("finalize", "toString"));

	private final Map<Class<?>, Class<Proxy>> m_proxyByClassMap = new HashMap<>();

	/**
	 * This creates a lazy proxy for the parent. The proxy intercepts all calls to the
	 * parent class, and causes that parent to be loaded first before forwarding the
	 * call to the loaded instance.
	 */
	public synchronized <T> T createParentProxy(MemoryDataContext dc, AttributeMeta meta, T originalValue) throws Exception {
		Class<?> actualType = meta.getActualType();
		Class<Proxy> proxyClass = m_proxyByClassMap.get(actualType);
		if(null == proxyClass) {
			//-- Create the parent proxy class
			ProxyFactory pf = new ProxyFactory();
			pf.setSuperclass(actualType);
			pf.setFilter(new MethodFilter() {
				@Override public boolean isHandled(Method m) {
					if(IGNOREMETHODS.contains(m.getName()))
						return false;
					return true;
				}
			});
			proxyClass = pf.createClass();
			m_proxyByClassMap.put(actualType, proxyClass);
		}

		//-- Now create the instance using the method handler for loading
		Proxy proxy = (Proxy) proxyClass.newInstance();
		proxy.setHandler(new MethodHandler() {
			private T m_copy;

			@Override public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
				System.out.println(">> proxying " + thisMethod.getName());
				T copy = m_copy;
				if(null == copy) {
					//-- We need to instantiate the copy.
					m_copy = copy = (T) dc.loadHere(self);
				}
				return proceed.invoke(copy, args);
			}
		});
		return (T) proxy;
	}



}
