package to.etc.domui.util.modelcopier;

import java.util.*;

import javax.annotation.*;

/**
 * A specific (search) key for a specific entity instance.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 9, 2013
 */
public class InstanceKey<T> {
	@Nonnull
	final private EntityDef<T> m_entity;

	@Nonnull
	final private Object[] m_keyValues;

	final private T m_sourceInstance;

	public InstanceKey(EntityDef<T> entity, Object[] keyValues, @Nullable T sourceInstance) {
		m_entity = entity;
		m_keyValues = keyValues;
		m_sourceInstance = sourceInstance;
	}

	public EntityDef<T> getEntity() {
		return m_entity;
	}

	public Object[] getKeyValues() {
		return m_keyValues;
	}

	@Nullable
	public T getSourceInstance() {
		return m_sourceInstance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_entity == null) ? 0 : m_entity.hashCode());
		result = prime * result + Arrays.hashCode(m_keyValues);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		InstanceKey< ? > other = (InstanceKey< ? >) obj;
		if(m_entity == null) {
			if(other.m_entity != null)
				return false;
		} else if(!m_entity.equals(other.m_entity))
			return false;
		return Arrays.equals(m_keyValues, other.m_keyValues);
	}

	@Nonnull
	public Object getValue(int ix) {
		return m_keyValues[ix];
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getEntity()).append("[");
		for(int i = 0; i < m_keyValues.length; i++) {
			Object v = m_keyValues[i];
			String n = getEntity().getSearchKey()[i];
			if(i > 0)
				sb.append(',');
			sb.append(n).append('=').append(v);
		}
		sb.append("]");
		return sb.toString();
	}
}
