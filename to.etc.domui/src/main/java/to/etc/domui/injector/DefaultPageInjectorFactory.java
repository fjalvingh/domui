package to.etc.domui.injector;

import to.etc.domui.dom.html.*;
import to.etc.util.*;

import javax.annotation.*;
import java.util.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-2-17.
 */
public class DefaultPageInjectorFactory implements IPageInjectorCalculator {
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

	public DefaultPageInjectorFactory() {
		registerFactory(0, new SimplePropertyInjectorFactory());
		registerFactory(100, new EntityPropertyInjectorFactory());
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

	@Nonnull
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
	}

	/**
	 * Tries to find an injector to inject a value for the specified property.
	 *
	 * @param pi
	 * @return
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
