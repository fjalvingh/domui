package to.etc.domui.hibernate.memorydb;

import to.etc.util.ClassUtil;
import to.etc.util.PropertyInfo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates and caches entity metadata.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-09-19.
 */
final class MetaCache {
	private Map<Class<?>, EntityMeta> m_entityMap = new HashMap<>();

	public EntityMeta getEntity(Class<?> clz) {
		EntityMeta entityMeta = m_entityMap.get(clz);
		if(null == entityMeta) {
			//-- Create the thingy and register it
			entityMeta = create(clz);
		}
		return entityMeta;
	}

	private EntityMeta create(Class<?> clz) {
		//-- 1. Make sure this is an entity
		if(null == clz.getAnnotation(Entity.class))
			throw new IllegalStateException("The class " + clz.getName() + " is not an entity class (no @Entity annotation)");

		EntityMeta em = new EntityMeta(clz);
		m_entityMap.put(clz, em);

		AttributeMeta id = null;
		List<PropertyInfo> properties = ClassUtil.calculateProperties(clz, true);
		for(PropertyInfo property : properties) {
			Method getter = property.getGetter();
			Method setter = property.getSetter();
			if(null != getter && null != setter && null == getter.getAnnotation(Transient.class)) {
				MetaRelType rt = MetaRelType.NONE;
				if(getter.getAnnotation(ManyToOne.class) != null) {
					rt = MetaRelType.PARENT;
				}
				if(getter.getAnnotation(OneToMany.class) != null) {
					if(rt != MetaRelType.NONE)
						throw new IllegalStateException(clz.getName() + " has both @ManyToOne and @OneToMany");
					rt = MetaRelType.CHILDLIST;
				}

				//-- Real thingy.
				AttributeMeta am = new AttributeMeta(em, getter, setter, property.getName(), property.getActualType(), rt);
				em.getAttributes().add(am);
				if(getter.getAnnotation(Id.class) != null) {
					if(id != null)
						throw new IllegalStateException("The entity " + clz.getName() + " has multiple properties annotated with @Id");
					id = am;
				}
			}
		}
		if(null == id)
			throw new IllegalStateException(clz.getName() + " has no @Id property");
		em.setId(id);
		return em;
	}
}
