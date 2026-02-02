/*
 * DomUI Java User Interface - shared code
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.util;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Like ClassUtil, but this caches the retrieved information.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 13, 2010
 */
public class ClassUtilCached {
	static private final ClassUtilCached	m_instance	= new ClassUtilCached();

	//@Immutable
	public static class ClassInfo {
		@NonNull
		private Class<?>	m_theClass;

		private Map<String, PropertyInfo>	m_propertyMap;

		private List<PropertyInfo>	m_propertyList;

		ClassInfo(@NonNull Class< ? > theClass) {
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

		@NonNull
		public List<PropertyInfo> getPropertyList() {
			return m_propertyList;
		}

		@NonNull
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
	@NonNull
	static public ClassUtilCached getInstance() {
		return m_instance;
	}

	/**
	 * Get all information for a given class from cache if possible, create and cache it if needed.
	 * @param clz
	 * @return
	 */
	@NonNull
	public ClassInfo getClassInfo(@NonNull Class< ? > clz) {
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
	@NonNull
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
