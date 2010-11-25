package to.etc.util;

import java.lang.ref.*;
import java.util.*;

import javax.annotation.*;
import javax.annotation.concurrent.*;

/**
 * Like ClassUtil, but this caches the retrieved information.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 13, 2010
 */
public class ClassUtilCached {
	static private final ClassUtilCached	m_instance	= new ClassUtilCached();

	@Immutable
	public static class ClassInfo {
		@Nonnull
		private Class<?>	m_theClass;

		private Map<String, PropertyInfo>	m_propertyMap;

		private List<PropertyInfo>	m_propertyList;

		ClassInfo(@Nonnull Class< ? > theClass) {
			m_theClass = theClass;
		}

		synchronized void initialize() {
			if(null != m_propertyList)
				return;

			//-- Decode
			List<PropertyInfo> plist = ClassUtil.getProperties(m_theClass);
			m_propertyList = Collections.unmodifiableList(plist);

			Map<String, PropertyInfo> pmap = new HashMap<String, PropertyInfo>();
			for(PropertyInfo pi : plist)
				pmap.put(pi.getName(), pi);
			m_propertyMap = Collections.unmodifiableMap(pmap);
		}

		@Nonnull
		public List<PropertyInfo> getPropertyList() {
			return m_propertyList;
		}

		@Nonnull
		public Map<String, PropertyInfo> getPropertyMap() {
			return m_propertyMap;
		}

		@Nullable
		public PropertyInfo findProperty(String name) {
			return m_propertyMap.get(name);
		}
	}

	/** The class discovery cache. */
	private Map<Class< ? >, Reference<ClassInfo>>	m_classMap	= new HashMap<Class< ? >, Reference<ClassInfo>>();

	public ClassUtilCached() {
	}

	/**
	 * Get an instance to use for retrieving cached information.
	 * @return
	 */
	@Nonnull
	static public ClassUtilCached getInstance() {
		return m_instance;
	}

	/**
	 * Get all information for a given class from cache if possible, create and cache it if needed.
	 * @param clz
	 * @return
	 */
	@Nonnull
	public ClassInfo getClassInfo(@Nonnull Class< ? > clz) {
		ClassInfo ci = null;
		synchronized(m_classMap) {
			Reference<ClassInfo> rci = m_classMap.get(clz);
			if(null != rci)
				ci = rci.get();
			if(null == ci) {
				ci = new ClassInfo(clz);
				m_classMap.put(clz, new WeakReference<ClassInfo>(ci));
			}
		}
		ci.initialize();
		return ci;
	}

	/**
	 * Return a cached variant of properties for a class.
	 * @param clz
	 * @return
	 */
	@Nonnull
	public List<PropertyInfo> getProperties(Class< ? > clz) {
		return getClassInfo(clz).getPropertyList();
	}

	/**
	 * Locates a property using cached class property info.
	 * @param clz
	 * @param name
	 * @return
	 */
	@Nullable
	public PropertyInfo findProperty(Class< ? > clz, String name) {
		return getClassInfo(clz).findProperty(name);
	}
}
