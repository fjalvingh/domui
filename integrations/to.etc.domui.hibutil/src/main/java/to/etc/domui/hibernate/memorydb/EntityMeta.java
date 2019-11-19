package to.etc.domui.hibernate.memorydb;

import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata from Hibernate entities.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-09-19.
 */
final class EntityMeta {
	private final MetaCache m_cache;

	private final Class<?> m_entityClass;

	private final List<AttributeMeta> m_attributes = new ArrayList<>();

	private AttributeMeta m_id;

	public EntityMeta(MetaCache cache, Class<?> entityClass) {
		m_cache = cache;
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

	public MetaCache getCache() {
		return m_cache;
	}

	@Nullable
	public <I> Object getIdValue(I instance) {
		return getId().getValue(instance);
	}
}
