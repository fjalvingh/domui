package to.etc.util;

import java.util.*;

import javax.annotation.concurrent.*;

/**
 * Cached information on class properties detected in ClassUtil.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 24, 2009
 */
@Immutable
final public class ClassInfo {
	private Class< ? >	m_theClass;

	private List<PropertyInfo>	m_properties;

	public ClassInfo(Class< ? > theClass, List<PropertyInfo> prop) {
		m_theClass = theClass;
		Collections.sort(prop, new Comparator<PropertyInfo>() {
			@Override
			public int compare(PropertyInfo a, PropertyInfo b) {
				return a.getName().compareTo(b.getName());
			}
		});
		m_properties = Collections.unmodifiableList(prop);
	}

	public Class< ? > getTheClass() {
		return m_theClass;
	}

	public List<PropertyInfo> getProperties() {
		return m_properties;
	}

	static private final Comparator<Object>	C_COMP	= new Comparator<Object>() {
		@Override
		public int compare(Object a, Object b) {
			if(a instanceof PropertyInfo) {
				return ((PropertyInfo) a).getName().compareTo((String) b);
			} else {
				return ((PropertyInfo) b).getName().compareTo((String) a);
			}
		}
	};

	public PropertyInfo	findProperty(String name) {
		int ix = Collections.binarySearch(m_properties, name, C_COMP);
		if(ix < 0)
			return null;
		return m_properties.get(ix);
	}

}
