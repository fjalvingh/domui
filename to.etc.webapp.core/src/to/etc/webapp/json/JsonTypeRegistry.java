package to.etc.webapp.json;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

import to.etc.util.*;

public class JsonTypeRegistry {
	static private class Entry {
		final private int m_order;

		@Nonnull
		final private IJsonTypeFactory m_factory;

		public Entry(int order, @Nonnull IJsonTypeFactory factory) {
			m_order = order;
			m_factory = factory;
		}

		public int getOrder() {
			return m_order;
		}

		@Nonnull
		public IJsonTypeFactory getFactory() {
			return m_factory;
		}
	}

	private Set<Entry> m_list = new TreeSet<Entry>(new Comparator<Entry>() {
		@Override
		public int compare(Entry a, Entry b) {
			return a.getOrder() - b.getOrder();
		}
	});

	public JsonTypeRegistry() {
		register(1000, new JsonIntFactory());
		register(1000, new JsonStringTypeFactory());
	}

	public synchronized void register(int order, @Nonnull IJsonTypeFactory factory) {
		m_list.add(new Entry(order, factory));
	}

	@Nullable
	public synchronized ITypeMapping findFactory(@Nonnull Class< ? > typeClass, @Nullable Type type) {
		for(Entry e: m_list) {
			ITypeMapping mapper = e.getFactory().createMapper(typeClass, type);
			if(null != mapper)
				return mapper;
		}
		return null;
	}

	public <T> ITypeMapping createMapping(@Nonnull Class<T> clz, @Nullable Type type) {
		ITypeMapping tm = findFactory(clz, type);
		if(null != tm)
			return tm;

		if(clz.isPrimitive())
			throw new IllegalStateException("No renderer for " + clz);

		//-- Create a class type mapper.
		List<PropertyInfo> props = ClassUtil.calculateProperties(clz);
		Map<String, PropertyMapping> res = new HashMap<String, PropertyMapping>();
		for(PropertyInfo pi : props) {
			PropertyMapping pm = createPropertyMapper(clz, pi);
			if(null != pm)
				res.put(pm.getName(), pm);
		}

		return new JsonClassType<T>(clz, res);
	}

	@Nullable
	private <T> PropertyMapping createPropertyMapper(@Nonnull Class<T> type, @Nonnull PropertyInfo pi) {
		ITypeMapping pm = findFactory(pi.getActualType(), pi.getActualGenericType());
		if(null == pm)
			return null;
		return new PropertyMapping(pi.getGetter(), pi.getSetter(), pi.getName(), pm);
	}
}
