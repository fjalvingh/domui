package to.etc.server.injector;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import to.etc.server.ajax.*;
import to.etc.util.*;

/**
 * This caches the rules that are used to "inject" values into a
 * method's parameters. Indexed by the method to inject this caches
 * the rules calculated for injecting all of the parameters of the
 * method off a given source type.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 20, 2006
 */
abstract public class ParameterInjectorCache {
	abstract protected ParameterInjectorSet calcInjectors(Class< ? extends Object> sourcecl, Method m, boolean[] done) throws Exception;

	/**
	 * The key class for items, consisting of a source class and the method
	 * to inject.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Oct 20, 2006
	 */
	static final private class MapKey {
		private Object	m_sourcekey;

		private Method	m_targetMethod;

		public MapKey(Object sourcekey, Method t) {
			assert (t != null);
			assert (sourcekey != null);
			m_sourcekey = sourcekey;
			m_targetMethod = t;
		}

		@Override
		public int hashCode() {
			final int PRIME = 31;
			int result = PRIME + m_sourcekey.hashCode();
			result = PRIME * result + m_targetMethod.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			MapKey a = (MapKey) obj;
			return m_sourcekey == a.m_sourcekey && m_targetMethod.equals(a.m_targetMethod);
		}
	}

	final private Map<MapKey, Object>	m_map;

	public ParameterInjectorCache(int maxItems) {
		m_map = new LRUHashMap<MapKey, Object>(maxItems);
	}

	public ParameterInjectorCache() {
		this(1024);
	}

	//    /**
	//     * Retrieve an injector set for the source and target passed. It tries to return a cached
	//     * value but calculates and caches a new set if not found.
	//     *
	//     * @param sourcecl      The type of the class which sources the values
	//     * @param targetmethod  The method whose parameters must be injected.
	//     * @return
	//     * @throws Exception
	//     */
	//    public MethodCallInjector getInjectorSet(Class sourcecl, Method targetmethod) throws Exception {
	//    	return getInjectorSet(sourcecl, targetmethod, true);
	//    }
	//
	//    /**
	//     * Retrieve an injector set for the source and target passed. It tries to return a cached
	//     * value but calculates and caches a new set if not found.
	//     *
	//     * @param sourcecl      The type of the class which sources the values
	//     * @param targetmethod  The method whose parameters must be injected.
	//     * @param allmandatory  When true all setters must have been set using this call or an exception occurs.
	//     * @return
	//     * @throws Exception
	//     */
	//    public MethodCallInjector  getInjectorSet(Class sourcecl, Method targetmethod, boolean allmandatory) throws Exception {
	//        synchronized(this) {
	//            MapKey  k = new MapKey(sourcecl, targetmethod);
	//            MethodCallInjector set = (MethodCallInjector)m_map.get(k);
	//            if(set != null)
	//                return set;
	//            set = calcInjectors(sourcecl, targetmethod);
	//
	//            if(allmandatory) {
	//            	//-- FIXME Check if all parameters have a setter.
	////                    throw new ParameterException(sb.toString())
	////                        .setHandlerClass(targetcl)
	////                    ;
	//            }
	//            m_map.put(k, set);                              // Save it,
	//            return set;
	//        }
	//    }

	/**
	 * Uses a list of possible sources to calculate injectors for the parameters of the
	 * method passed. For each parameter, the first source class that generates
	 * a hit is taken as the injector.
	 *
	 * @param sourcecl
	 * @param targetcl
	 * @param allmandatory
	 * @return
	 * @throws Exception
	 */
	public List<ParameterInjectorSet> getInjectorSet(String setkey, List<Class< ? extends Object>> slist, Method targetmethod, boolean[] alreadydone) throws Exception {
		synchronized(this) {
			MapKey k = new MapKey(setkey, targetmethod);
			List<ParameterInjectorSet> setlist = (List<ParameterInjectorSet>) m_map.get(k);
			if(setlist != null)
				return setlist;
			setlist = new ArrayList<ParameterInjectorSet>();

			//-- Get a combined injector for this source set.
			Class<Object>[] formals = (Class<Object>[]) targetmethod.getParameterTypes();
			int count = formals.length; // The #of parameters to fill,
			boolean[] doneset = new boolean[formals.length];
			if(alreadydone != null) {
				for(int i = alreadydone.length; --i >= 0;) {
					doneset[i] = alreadydone[i];
					if(doneset[i])
						count--;
				}
			}
			if(count == 0) // Nothin' to do?
				return setlist; // Return the empty set

			//-- Find all
			for(Class< ? extends Object> sourcecl : slist) {
				ParameterInjectorSet pis = calcInjectors(sourcecl, targetmethod, doneset);
				if(pis != null) {
					setlist.add(pis);
					count = 0;
					for(int i = doneset.length; --i >= 0;) {
						if(!doneset[i])
							count++;
					}
					if(count == 0) // Nothing to do anymore?
						break;
				}
			}

			//-- Now report problems for all undone parameters.
			if(count != 0) {
				Annotation[][] aset = targetmethod.getParameterAnnotations();
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < doneset.length; i++) {
					if(!doneset[i]) {
						String name = "(unknown name)";
						for(int j = aset[i].length; --j >= 0;) {
							Annotation a = aset[i][j];
							if(a instanceof AjaxParam) {
								name = ((AjaxParam) a).value();
							}
						}

						sb.append("No value for parameter #" + (i + 1) + ", " + name + ", type " + formals[i].getSimpleName() + "\n");
					}
				}
				sb.append("for method " + targetmethod.toString());
				throw new ParameterException(sb.toString()).setHandlerClass(targetmethod.getDeclaringClass()).setHandlerMethod(targetmethod);
			}
			m_map.put(k, setlist); // Save it,
			return setlist;
		}
	}

}
