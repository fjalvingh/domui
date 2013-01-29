package to.etc.domui.util.modelcopier;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.webapp.annotations.*;

/**
 * Defines search and create/ignore rules for an entity.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 9, 2013
 */
public class EntityDef<T> {
	final private ModelCopier m_model;

	@Nonnull
	final private Class<T> m_entityClass;

	@Nullable
	private String[] m_searchKey;

	@Nonnull
	private ClassMetaModel m_mm;

	private boolean m_copy = true;

	private boolean m_creatable = true;

	private boolean m_createAlways;

	private boolean m_updateExisting = false;

	public EntityDef(@Nonnull ModelCopier model, @Nonnull Class<T> entityClass) {
		m_model = model;
		m_entityClass = entityClass;
		m_mm = MetaManager.findClassMeta(entityClass);
	}

	public T createInstance() throws Exception {
		return m_entityClass.newInstance();
	}

	public Class<T> getEntityClass() {
		return m_entityClass;
	}

	@Nonnull
	public ClassMetaModel getMetaModel() {
		return m_mm;
	}

	/**
	 * Define the search key set for this entity.
	 * @param k
	 */
	public EntityDef<T> key(@GProperty String... k) {
		if(k.length == 0)
			throw new IllegalStateException(this + ": key set cannot be empty");
		m_searchKey = k;
		return this;
	}

	public String[] getSearchKey() {
		if(m_searchKey == null)
			throw new IllegalStateException(this + ": search key not known");
		return m_searchKey;
	}

	/**
	 * Get the search key for the specified existing entity instance.
	 * @param instance
	 * @return
	 */
	@Nonnull
	public InstanceKey<T> getInstanceKey(T instance) throws Exception {
		Object[] vals = new Object[getSearchKey().length];
		int ix = 0;
		for(String prop : getSearchKey()) {
			PropertyMetaModel< ? > pmm = m_mm.getProperty(prop);
			Object kval = pmm.getValue(instance);
			if(kval == null)
				throw new IllegalStateException(this + ": entity " + instance + " key field " + prop + " is null - cannot locate");


			if(!isEntityInstance(kval)) {
				//-- Just some value- store.
				vals[ix] = kval;
			} else {
				//-- This by itself is an entity to locate.
				EntityDef<Object> altd = (EntityDef<Object>) m_model.getDefinition(kval.getClass());
				InstanceKey<Object> altk = altd.getInstanceKey(kval);
				vals[ix] = altk;
			}
			ix++;
		}
		return new InstanceKey<T>(this, vals, instance);
	}

	/**
	 *
	 * @param t
	 * @return
	 */
	static public <X> boolean isEntityInstance(@Nullable X t) {
		if(t == null)
			return false;
		Class<X> tclz = (Class<X>) t.getClass();
		ClassMetaModel cmm = MetaManager.findClassMeta(tclz);
		return cmm.isPersistentClass();
	}

	public boolean isCopy() {
		return m_copy;
	}

	public EntityDef<T> setCopy(boolean ok) {
		m_copy = ok;
		return this;
	}

	public EntityDef<T> nocopy() {
		m_copy = false;
		return this;
	}

	public boolean isCreatable() {
		return m_creatable;
	}

	public boolean isCreateAlways() {
		return m_createAlways;
	}

	public EntityDef<T> nocreate() {
		m_creatable = false;
		return this;
	}

	public EntityDef<T> createAlways() {
		m_createAlways = true;
		return this;
	}

	public boolean isUpdateExisting() {
		return m_updateExisting || m_model.isUpdateExisting();
	}

	@Override
	public String toString() {
		return getEntityClass().getName();
	}

	public EntityDef<T> update() {
		m_updateExisting = true;
		return this;
	}

}
