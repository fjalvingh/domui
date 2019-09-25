package to.etc.domui.hibernate.memorydb;


import org.eclipse.jdt.annotation.Nullable;
import to.etc.util.WrappedException;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-09-19.
 */
final class AttributeMeta {
	private final EntityMeta m_entity;

	private final Method m_getter;

	private final Method m_setter;

	private final String m_name;

	private final Class<?> m_actualType;

	private final MetaRelType m_relType;

	@Nullable
	private EntityMeta m_relationEntity;

	public AttributeMeta(EntityMeta entity, Method getter, Method setter, String name, Class<?> actualType, MetaRelType relType) {
		m_entity = entity;
		m_getter = getter;
		m_setter = setter;
		m_name = name;
		m_actualType = actualType;
		m_relType = relType;
	}

	public EntityMeta getEntity() {
		return m_entity;
	}

	public synchronized EntityMeta getRelationEntity() {
		EntityMeta relationEntity = m_relationEntity;
		if(null == relationEntity) {
			relationEntity = m_relationEntity = m_entity.getCache().getEntity(m_actualType);
		}
		return relationEntity;
	}

	public Method getGetter() {
		return m_getter;
	}

	public Method getSetter() {
		return m_setter;
	}

	public String getName() {
		return m_name;
	}

	public Class<?> getActualType() {
		return m_actualType;
	}

	public MetaRelType getRelType() {
		return m_relType;
	}

	public Object getValue(Object instance) {
		try {
			return m_getter.invoke(instance);
		} catch(Exception x) {
			x = WrappedException.unwrap(x);
			throw new MemoryDataException(x, "Cannot get value of " + this + ": " + x);
		}
	}

	public void setValue(Object instance, Object value) {
		try {
			m_setter.invoke(instance, value);
		} catch(Exception x) {
			x = WrappedException.unwrap(x);
			throw new MemoryDataException(x, "Cannot set value of " + this + " to " + value + ": " + x);
		}
	}

	@Override public String toString() {
		return m_entity + "." + getName();
	}
}
