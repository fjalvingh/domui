package to.etc.domui.injector;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.dom.html.UrlPage;
import to.etc.util.ClassUtil;
import to.etc.util.PropertyInfo;
import to.etc.webapp.ProgrammerErrorException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Scans all properties of a page class, and tries to find a way to inject a value
 * in them by using {@link IPagePropertyFactory} instances registered with
 * this class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-2-17.
 */
public class DefaultPagePropertyInjectorFactory implements IPageInjectorCalculator {
	static final private class PropFactoryRef {
		private final int m_priority;

		private final IPagePropertyFactory m_propertyFactory;

		public PropFactoryRef(int priority, IPagePropertyFactory propertyFactory) {
			m_priority = priority;
			m_propertyFactory = propertyFactory;
		}

		public int getPriority() {
			return m_priority;
		}

		public IPagePropertyFactory getPropertyFactory() {
			return m_propertyFactory;
		}
	}

	private List<PropFactoryRef> m_orderedList = Collections.emptyList();

	private List<IPagePropertyFactory> m_list = Collections.emptyList();

	public DefaultPagePropertyInjectorFactory() {
		registerFactory(0, new SimplePropertyInjectorFactory());
		registerFactory(100, new EntityPropertyInjectorFactory());
		registerFactory(120, new UrlContextPropertyInjector());
	}

	public synchronized void registerFactory(int urgency, IPagePropertyFactory injector) {
		ArrayList<PropFactoryRef> list = new ArrayList<>(m_orderedList);
		list.add(new PropFactoryRef(urgency, injector));
		Collections.sort(list, (a, b) -> b.getPriority() - a.getPriority());
		m_orderedList = list;

		List<IPagePropertyFactory> res = new ArrayList<>(list.size());
		list.forEach(item -> res.add(item.getPropertyFactory()));
		m_list = Collections.unmodifiableList(res);
	}

	@NonNull
	private synchronized List<IPagePropertyFactory> getFactoryList() {
		return m_list;
	}

	/**
	 * Checks all properties of a page and returns a list of Injectors to use to inject values into
	 * those properties, if needed.
	 */
	@Override public void calculatePageInjectors(Map<String, PropertyInjector> registrationMap, Class<? extends UrlPage> pageClass) {
		List<PropertyInfo> propertyList = ClassUtil.getProperties(pageClass);
		for(PropertyInfo pi : propertyList) {
			if(! registrationMap.containsKey(pi.getName())) {
				PropertyInjector pij = calculateInjector(pi);
				if(null != pij)
					registrationMap.put(pi.getName(), pij);
			}
		}
		checkUnreachableAnnotations(registrationMap, pageClass);
	}

	/**
	 * A property exists only when it has a getter, so an {@link UIUrlParameter} on a setter
	 * of a write-only property is never seen by the code above: the page would silently keep
	 * the default value of the field. Report that instead of ignoring it.
	 */
	private void checkUnreachableAnnotations(Map<String, PropertyInjector> registrationMap, Class<? extends UrlPage> pageClass) {
		for(Method m : pageClass.getMethods()) {
			if(Modifier.isStatic(m.getModifiers()) || m.getParameterCount() != 1)
				continue;
			String name = propertyNameOfSetter(m);
			if(null == name || registrationMap.containsKey(name) || hasGetter(pageClass, name))
				continue;
			if(null == ClassUtil.findAnnotationIncludingSuperClasses(m, UIUrlParameter.class))
				continue;
			throw new ProgrammerErrorException(UIUrlParameter.class.getSimpleName() + " on " + pageClass.getName() + "." + m.getName()
				+ "() cannot be injected: the property '" + name + "' has no getter, so nothing knows it exists. Add a getter for it.");
		}
	}

	private static boolean hasGetter(Class<?> pageClass, String propertyName) {
		for(PropertyInfo pi : ClassUtil.getProperties(pageClass)) {
			if(pi.getName().equals(propertyName))
				return true;
		}
		return false;
	}

	/**
	 * Return the property name for a setter method, or null if it is not a setter.
	 */
	@Nullable
	private static String propertyNameOfSetter(Method m) {
		String name = m.getName();
		if(!name.startsWith("set") || name.length() <= 3)
			return null;
		if(Character.isUpperCase(name.charAt(3)) && (name.length() == 4 || !Character.isUpperCase(name.charAt(4))))
			return Character.toLowerCase(name.charAt(3)) + name.substring(4);
		return name.substring(3);
	}

	/**
	 * Tries to find an injector to inject a value for the specified property.
	 */
	@Nullable
	protected PropertyInjector calculateInjector(final PropertyInfo pi) {
		for(IPagePropertyFactory factory : getFactoryList()) {
			PropertyInjector injector = factory.calculateInjector(pi);
			if(null != injector)
				return injector;
		}
		return null;
	}
}
