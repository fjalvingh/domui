package to.etc.json;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.util.ClassUtil;
import to.etc.util.PropertyInfo;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class JsonTypeRegistry {
	static private class Entry {
		final private int m_order;

		@NonNull
		final private IJsonTypeFactory m_factory;

		public Entry(int order, @NonNull IJsonTypeFactory factory) {
			m_order = order;
			m_factory = factory;
		}

		public int getOrder() {
			return m_order;
		}

		@NonNull
		public IJsonTypeFactory getFactory() {
			return m_factory;
		}
	}

	private Map<Class< ? >, ITypeMapping> m_classMap = new HashMap<Class< ? >, ITypeMapping>();

	private Set<Entry> m_list = new TreeSet<Entry>(new Comparator<Entry>() {
		@Override
		public int compare(Entry a, Entry b) {
			int ct = a.getOrder() - b.getOrder();
			if(ct != 0)
				return ct;
			return a.hashCode() - b.hashCode();
		}
	});

	public JsonTypeRegistry() {
		register(1000, new JsonIntFactory());
		register(1000, new JsonStringFactory());
		register(1000, new JsonLongFactory());
		register(1000, new JsonBooleanFactory());
		register(1000, new JsonUtcDateFactory());
		register(1000, new JsonEnumFactory());
		register(1000, new JsonListFactory());
		register(1000, new JsonSetFactory());
		register(1000, new JsonArrayFactory());
//		register(1000, new JsonFactory());
	}

	public synchronized void register(int order, @NonNull IJsonTypeFactory factory) {
		m_list.add(new Entry(order, factory));
	}

	@Nullable
	public synchronized ITypeMapping findFactory(@NonNull Class< ? > typeClass, @Nullable Type type) {
		for(Entry e: m_list) {
			ITypeMapping mapper = e.getFactory().createMapper(this, typeClass, type);
			if(null != mapper)
				return mapper;
		}
		return null;
	}

	static private Set<String> IGNORESET = new HashSet<String>(Arrays.asList("class"));

	@Nullable
	public synchronized <T> ITypeMapping createMapping(@NonNull Class<T> clz, @Nullable Type type) {
		ITypeMapping cm = m_classMap.get(clz);
		if(null != cm)
			return cm;

		ITypeMapping tm = findFactory(clz, type);
		if(null != tm)
			return tm;

		if(clz.isPrimitive())
			throw new IllegalStateException("No renderer for " + clz);

		//-- Create/get a class type mapper.
		JsonClassType<T> ct = new JsonClassType<T>(clz);
		m_classMap.put(clz, ct);

		List<PropertyInfo> props = ClassUtil.calculateProperties(clz);
		Map<String, PropertyMapping> res = new TreeMap<String, PropertyMapping>();
		for(PropertyInfo pi : props) {
			if(IGNORESET.contains(pi.getName()))
				continue;
			PropertyMapping pm = createPropertyMapper(clz, pi);
			if(null != pm)
				res.put(pm.getName(), pm);
		}
		ct.setMap(res);
		return ct;
	}

	@Nullable
	private <T> PropertyMapping createPropertyMapper(@NonNull Class<T> type, @NonNull PropertyInfo pi) {
		try {
			ITypeMapping pm = createMapping(pi.getActualType(), pi.getActualGenericType());
			if(null == pm)
				return null;
			return new PropertyMapping(pi.getGetter(), pi.getSetter(), pi.getName(), pm);
		} catch(Exception x) {
			throw new RuntimeException("In mapping " + pi.getName() + " of class " + type.getName() + ": " + x, x);
		}

	}
}
