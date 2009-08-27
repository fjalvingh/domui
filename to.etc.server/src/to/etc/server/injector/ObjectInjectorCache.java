package to.etc.server.injector;

import java.lang.reflect.*;
import java.util.*;

import to.etc.util.*;

/**
 * This caches the rules that are used to "inject" values into some 
 * object or some method. Indexed by the class of the object to inject
 * into this caches the rules calculated for injecting.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 11, 2006
 */
abstract public class ObjectInjectorCache {
	static final private class MapKey {
		private Object						m_sourcecl;

		private Class< ? extends Object>	m_targetcl;

		public MapKey(Object s, Class< ? extends Object> t) {
			m_sourcecl = s;
			m_targetcl = t;
		}

		@Override
		public int hashCode() {
			final int PRIME = 31;
			int result = PRIME + m_sourcecl.hashCode();
			result = PRIME * result + m_targetcl.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			MapKey a = (MapKey) obj;
			return m_sourcecl == a.m_sourcecl && m_targetcl == a.m_targetcl;
		}
	}

	final private Map<MapKey, Object>	m_map;

	abstract protected InjectorSet calcInjectors(Class< ? extends Object> sourcecl, Class< ? extends Object> cl, Map<String, Method> doneset) throws Exception;

	public ObjectInjectorCache(int maxItems) {
		m_map = new LRUHashMap<MapKey, Object>(maxItems);
	}

	public ObjectInjectorCache() {
		this(1024);
	}

	/**
	 * Calculate an injector set for the source and target passed.
	 *
	 * @param sourcecl      The type of the class which sources the values
	 * @param targetcl      The target class needing to be injected
	 * @param allmandatory  When true all setters must have been set using this call or an exception occurs.
	 * @return
	 * @throws Exception
	 */
	public InjectorSet getInjectorSet(Class< ? extends Object> sourcecl, Class< ? extends Object> targetcl, boolean allmandatory) throws Exception {
		synchronized(this) {
			MapKey k = new MapKey(sourcecl, targetcl);
			InjectorSet set = (InjectorSet) m_map.get(k);
			if(set != null)
				return set;
			Map<String, Method> donemap = new HashMap<String, Method>();
			set = calcInjectors(sourcecl, targetcl, donemap);
			if(allmandatory) {
				Map<String, Method> map = Injector.getObjectSetterMap(targetcl);
				if(map.size() != donemap.size()) {
					map.keySet().removeAll(donemap.keySet());
					StringBuilder sb = new StringBuilder(256);
					sb.append("No injector provider for the propert");
					sb.append(map.size() > 1 ? "ies" : "y");
					for(String s : map.keySet()) {
						sb.append("'");
						sb.append(s);
						sb.append("':");
						Method m = map.get(s);
						sb.append(m.getParameterTypes()[0].getCanonicalName());
						sb.append(' ');
					}
					sb.append("\nof class " + targetcl.getCanonicalName() + "\nusing a source class '" + sourcecl.getCanonicalName() + "'");

					throw new ParameterException(sb.toString()).setHandlerClass(targetcl);
				}
			}
			m_map.put(k, set); // Save it,
			return set;
		}
	}

	/**
	 * Uses a list of possible sources to calculate injectors for properties of the
	 * object class passed. For each property, the first source class that generates
	 * a hit is taken as the injector.
	 *
	 * @param sourcecl
	 * @param targetcl
	 * @param allmandatory
	 * @return
	 * @throws Exception
	 */
	public List<InjectorSet> getInjectorSet(String setkey, List<Class< ? extends Object>> slist, Class<Object> targetcl, boolean allmandatory) throws Exception {
		synchronized(this) {
			MapKey k = new MapKey(setkey, targetcl);
			List<InjectorSet> setlist = (List<InjectorSet>) m_map.get(k);
			if(setlist != null)
				return setlist;
			setlist = new ArrayList<InjectorSet>();

			//-- Get a combined injector for this source set.
			Map<String, Method> donemap = new HashMap<String, Method>();
			for(Class< ? extends Object> sourcecl : slist) {
				InjectorSet set = calcInjectors(sourcecl, targetcl, donemap);
				setlist.add(set);
			}

			//-- Now check if all thingies are done,
			if(allmandatory) {
				Map<String, Method> map = Injector.getObjectSetterMap(targetcl);
				if(map.size() != donemap.size()) {
					map.keySet().removeAll(donemap.keySet());
					StringBuilder sb = new StringBuilder(256);
					sb.append("No injector provider for the propert");
					sb.append(map.size() > 1 ? "ies" : "y");
					for(String s : map.keySet()) {
						sb.append("'");
						sb.append(s);
						sb.append("':");
						Method m = map.get(s);
						sb.append(m.getParameterTypes()[0].getCanonicalName());
						sb.append(' ');
					}
					sb.append("\nof class " + targetcl.getCanonicalName() + "\nusing source classes:\n");
					for(Class< ? extends Object> cl : slist) {
						sb.append(cl.getName());
						sb.append("\n");
					}
					throw new ParameterException(sb.toString()).setHandlerClass(targetcl);
				}
			}
			m_map.put(k, setlist); // Save it,
			return setlist;
		}
	}
}
