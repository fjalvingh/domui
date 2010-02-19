package to.etc.util;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

/**
 * Information on properties on a class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 9, 2007
 */
final public class PropertyInfo {
	private String	m_name;

	private Method	m_getter;

	private Method	m_setter;

	public PropertyInfo(String name, Method getter, Method setter) {
		if(getter == null || name == null)
			throw new IllegalStateException("Name or getter null not allowed");
		m_name = name;
		m_getter = getter;
		m_setter = setter;
	}

	@Nonnull
	public String getName() {
		return m_name;
	}

	@Nonnull
	public Method getGetter() {
		return m_getter;
	}

	@Nullable
	public Method getSetter() {
		return m_setter;
	}
	@Nonnull
	public Class< ? > getActualType() {
		return m_getter.getReturnType();
}
	public Type getGenericActualType() {
		return m_getter.getGenericReturnType();
	}

	/**
	 * Returns T if this is either a collection or an array.
	 * @return
	 */
	public boolean isCollectionOrArrayType() {
		return getActualType().isArray() || Collection.class.isAssignableFrom(getActualType());
	}

	public boolean isCollectionType() {
		return Collection.class.isAssignableFrom(getActualType());
	}

	public boolean isArrayType() {
		return getActualType().isArray();
	}

	public Class< ? > getCollectionValueType() {
		if(getActualType().isArray())
			return getActualType().getComponentType();
		else if(Collection.class.isAssignableFrom(getActualType())) {
			Type t = getGenericActualType();
			if(t == null)
				return null;
			return ClassUtil.findCollectionType(t);
		} else
			return null;
	}
}
