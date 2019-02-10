package to.etc.domui.subinjector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.dom.html.SubPage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Default re-injector of subpages.
 *
 * FIXME Uncached implementation.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 1-12-18.
 */
public class SubPageInjector implements ISubPageInjector {
	static public final Logger LOG = LoggerFactory.getLogger(SubPageInjector.class);

	private Map<Class<?>, List<ISubPageInjector>> m_injectorMap = new WeakHashMap<>();

	private List<ISubPageInjectorFactory> m_factoryList = Collections.emptyList();

	public SubPageInjector() {
		register(new SubPageFieldInjectorFactory());
	}

	public synchronized List<ISubPageInjector> getInjectors(Class<? extends SubPage> clz) {
		List<ISubPageInjector> list = m_injectorMap.get(clz);
		if(null == list) {
			list = calculateInjectors(clz);
			m_injectorMap.put(clz, list);
		}
		return list;
	}

	private List<ISubPageInjector> calculateInjectors(Class<? extends SubPage> clz) {
		List<ISubPageInjector> list = new ArrayList<>();
		for(ISubPageInjectorFactory factory : getFactoryList()) {
			list.addAll(factory.calculateInjectors(clz));
		}
		return list;
	}

	private synchronized List<ISubPageInjectorFactory> getFactoryList() {
		return m_factoryList;
	}

	public synchronized void register(ISubPageInjectorFactory factory) {
		List<ISubPageInjectorFactory> list = new ArrayList<>(m_factoryList);
		list.add(factory);
		m_factoryList = list;
	}

	@Override
	public void inject(SubPage page) throws Exception {
		List<ISubPageInjector> injectors = getInjectors(page.getClass());
		for(ISubPageInjector injector : injectors) {
			injector.inject(page);
		}
	}
}
