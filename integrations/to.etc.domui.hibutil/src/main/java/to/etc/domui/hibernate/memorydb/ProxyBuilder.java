package to.etc.domui.hibernate.memorydb;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-09-19.
 */
class ProxyBuilder {
	static private final Set<String> IGNOREMETHODS = new HashSet<>(Arrays.asList("finalize", "toString"));

	private final Map<Class<?>, Class<Proxy>> m_proxyByClassMap = new HashMap<>();

	@Nullable
	private Class<Proxy> m_listProxyClass;

	/**
	 * This creates a lazy proxy for the parent. The proxy intercepts all calls to the
	 * parent class, and causes that parent to be loaded first before forwarding the
	 * call to the loaded instance.
	 */
	public synchronized <T> T createParentProxy(MemoryDataContext dc, AttributeMeta attribute, T originalValue) throws Exception {
		Class<?> actualType = attribute.getActualType();
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
					m_copy = copy = (T) dc.loadCopyButDoNotRegister(attribute.getRelationEntity(), originalValue);
				}
				return thisMethod.invoke(copy, args);
			}
		});
		return (T) proxy;
	}

	/**
	 * Create a proxy for this List implementation.
	 */
	public synchronized  <M> List<M> createListProxy(MemoryDataContext dc, AttributeMeta attribute, List<M> sourceValue) throws Exception {
		Class<Proxy> listProxyClass = m_listProxyClass;
		if(null == listProxyClass) {
			ProxyFactory pf = new ProxyFactory();
			pf.setSuperclass(ArrayList.class);
			pf.setFilter(new MethodFilter() {
				@Override public boolean isHandled(Method m) {
					if(IGNOREMETHODS.contains(m.getName()))
						return false;
					return true;
				}
			});
			m_listProxyClass = listProxyClass = pf.createClass();
		}
		Proxy proxy = (Proxy) listProxyClass.newInstance();
		proxy.setHandler(new MethodHandler() {
			@Nullable
			private List<Object> m_copy;

			@Override public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
				System.out.println(">> proxying " + thisMethod.getName());

				List<Object> copy = m_copy;
				if(null == copy) {
					//-- We need to instantiate the copy.
					m_copy = copy = new ArrayList<>();

					//-- Walk through all source values and instantiate them here.
					for(M m : sourceValue) {
						M itemCopy = dc.loadHere(attribute.getRelationEntity(), m);
						copy.add(itemCopy);
					}
				}
				return proceed.invoke(copy, args);
			}
		});
		return (List<M>) proxy;
	}
}
