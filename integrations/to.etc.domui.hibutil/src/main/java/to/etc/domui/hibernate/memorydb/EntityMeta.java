package to.etc.domui.hibernate.memorydb;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata from Hibernate entities.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-09-19.
 */
final class EntityMeta {
	private final Class<?> m_entityClass;

	private final List<AttributeMeta> m_attributes = new ArrayList<>();

	private AttributeMeta m_id;

	public EntityMeta(Class<?> entityClass) {
		m_entityClass = entityClass;
	}

	public Class<?> getEntityClass() {
		return m_entityClass;
	}

	public List<AttributeMeta> getAttributes() {
		return m_attributes;
	}

	public void setId(AttributeMeta id) {
		m_id = id;
	}

	public AttributeMeta getId() {
		return m_id;
	}

	@Override public String toString() {
		return m_entityClass.getSimpleName();
	}
}
