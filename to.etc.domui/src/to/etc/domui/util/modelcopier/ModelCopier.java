package to.etc.domui.util.modelcopier;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.util.*;
import to.etc.webapp.query.*;

/**
 * This helps with copying models by using natural/defined keys instead of just PK's.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 9, 2013
 */
public class ModelCopier {
//	@Nonnull
//	final private QDataContext m_sds;

	@Nonnull
	final private QDataContext m_dds;

	@Nullable
	final private ILogSink m_sink;

	@Nonnull
	final Map<Class< ? >, EntityDef< ? >> m_defMap = new HashMap<>();

	private StringBuilder m_pathSb = new StringBuilder();

	private String m_currentPath;

	private Set<String> m_ignorePathSet = new HashSet<String>();

	private boolean m_updateExisting;

	public ModelCopier(@Nullable ILogSink sink, @Nonnull QDataContext sds, @Nonnull QDataContext dds) throws Exception {
		m_sink = sink;
//		m_sds = sds;
		m_dds = dds;
		m_dds.startTransaction();
	}

	private void log(String s) {
		if(null != m_sink)
			m_sink.log(s);
	}

	@Nonnull
	public ModelCopier ignorePath(String path) {
		m_ignorePathSet.add(path);
		return this;
	}

	public ModelCopier updateExisting() {
		m_updateExisting = true;
		return this;
	}

	public boolean isUpdateExisting() {
		return m_updateExisting;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Defining.											*/
	/*--------------------------------------------------------------*/

	public <T> EntityDef<T> define(@Nonnull Class<T> eclass) {
		EntityDef<T> ed = (EntityDef<T>) m_defMap.get(eclass);
		if(null == ed) {
			ed = new EntityDef<T>(this, eclass);
			m_defMap.put(eclass, ed);
		}
		return ed;
	}

	@Nullable
	public <T> EntityDef<T> findDefinition(@Nonnull Class<T> eclass) {
		return (EntityDef<T>) m_defMap.get(eclass);
	}

	@Nonnull
	public <T> EntityDef<T> getDefinition(@Nonnull Class<T> eclass) {
		ClassMetaModel cmm = MetaManager.findClassMeta(eclass);

		EntityDef<T> ed = findDefinition((Class<T>) cmm.getActualClass());
		if(null == ed)
			throw new IllegalStateException(eclass + ": no entity definition");
		return ed;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Copying.											*/
	/*--------------------------------------------------------------*/

	/** Maps known/located/created instances in dest by key. */
	@Nonnull
	private Map<InstanceKey< ? >, Object> m_destInstanceMap = new HashMap<>();

	/** Maps known/located/created instances in src by key. */
	@Nonnull
	private Map<InstanceKey< ? >, Object> m_srcInstanceMap = new HashMap<>();

	@Nonnull
	private Stack<InstanceKey< ? >> m_currentFindSet = new Stack<InstanceKey< ? >>();

	/**
	 * This tries to make a copy of the object, by recursively implementing the search rules for each of
	 * it's relation instances.
	 *
	 * @param src
	 * @param except
	 * @return
	 */
	@Nullable
	public <T> T copy(@Nonnull T src, Object... except) throws Exception {
		Set<Object> exceptSet = new HashSet<Object>();
		for(Object xc : except)
			exceptSet.add(xc);

		return copyInstance(src);
	}

	@Nullable
	private <T> T findKnownDestInstance(@Nonnull InstanceKey<T> k) {
		return (T) m_destInstanceMap.get(k);
	}

	@Nullable
	private <T> T copyInstance(@Nonnull T src) throws Exception {
//		if(except.contains(instancePath))
//			return null;

		//-- Create the instance key.
		EntityDef<T>	ed = (EntityDef<T>) getDefinition(src.getClass());
		if(! ed.isCopy())								// May forget copy?
			return null;
		InstanceKey<T> key = ed.getInstanceKey(src);

		//-- Have we already "done this one"?
		return destCreate(key);
	}

	public <T> T destLocate(InstanceKey<T> key) throws Exception {
		T dv = findKnownDestInstance(key);
		if(null != dv)
			return dv;
		if(key.getEntity().isCreateAlways())
			return null;

		dv = dbfind(key);
		if(null == dv)
			return null;

		if(key.getEntity().isUpdateExisting()) {
			updateProperties(key, dv);
		}

		return dv;
	}

	private <T> void updateProperties(InstanceKey<T> key, T di) throws Exception {
		EntityDef<T> ed = key.getEntity();
		List<PropertyMetaModel< ? >> pl = ed.getMetaModel().getProperties();
		List<PropertyMetaModel< ? >> childList = new ArrayList<>();
		T si = key.getSourceInstance();
		if(null == si)
			throw new IllegalStateException("No source instance for key " + key);

		for(PropertyMetaModel< ? > pmm : pl) {
			switch(pmm.getRelationType()){
				default:
					break;

				case DOWN:
					childList.add(pmm);
					break;

				case NONE:
					//-- Just copy the value.
					copyValue(ed, di, si, pmm);
					break;

//				case UP:
//					copyParent(ed, di, si, pmm);
//					break;
			}
		}

		for(PropertyMetaModel< ? > pmm : childList) {

		}

	}

	/**
	 * This either locates or creates the specified instance in dest.
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public <T> T destCreate(@Nonnull InstanceKey<T> key) throws Exception {
		T di = destLocate(key);
		if(null != di)
			return di;

		//-- We should not get here recursively for the same instance...
		int xi = m_currentFindSet.indexOf(key);
		if(xi >= 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("Find loop: ");
			for(int ix = xi; ix < m_currentFindSet.size(); ix++) {
				sb.append(">");
				sb.append(m_currentFindSet.get(ix));
			}
			throw new IllegalStateException(sb.toString());
		}

		//-- We need to actually create this instance.
		EntityDef<T> ed = key.getEntity();
		if(!ed.isCopy())
			return null;
		if(!ed.isCreatable())
			throw new IllegalStateException(key + ": not allowed to create instances of " + ed);

		T src = key.getSourceInstance();
		if(src == null)
			throw new IllegalStateException(key + ": source instance is null??");

		m_currentFindSet.add(key);
		System.out.println("mc: creating " + key + " (" + m_currentPath + ")");
		di = ed.createInstance();					// Create a new, empty instance
		m_destInstanceMap.put(key, di);				// Store created one
		copyProperties(ed, di, src, key);
		m_currentFindSet.remove(key);
		return di;
	}

	private <T, I> void copyProperties(@Nonnull EntityDef<T> ed, @Nonnull T di, @Nonnull T si, @Nonnull InstanceKey<T> key) throws Exception {
		List<PropertyMetaModel< ? >> pl = ed.getMetaModel().getProperties();
		List<PropertyMetaModel< ? >> childList = new ArrayList<>();

		for(PropertyMetaModel< ? > pmm : pl) {
			switch(pmm.getRelationType()){
				default:
					throw new IllegalStateException(pmm.getRelationType() + ": ??");

				case DOWN:
					childList.add(pmm);
					break;

				case NONE:
					//-- Just copy the value.
					copyValue(ed, di, si, pmm);
					break;

				case UP:
					copyParent(ed, di, si, pmm);
					break;
			}
		}

		//-- Object created except for child relations. Save it now.
		m_dds.save(di);
//		log("Flushing after save of " + key + ": " + MetaManager.identify(di));
//		DbUtil.flush(m_dds);

		//-- Check
//		ClassMetaModel cmm = ed.getMetaModel();
//		PropertyMetaModel< ? > pk = cmm.getPrimaryKey();
//		if(pk == null)
//			throw new IllegalStateException("Undefined pk on " + cmm);
//		String sql = "select count(1) from " + cmm.getTableName() + " where " + pk.getColumnNames()[0] + " = " + pk.getValue(di);
//		Connection dbc = m_dds.getConnection();
//		PreparedStatement ps = null;
//		ResultSet rs = null;
//		try {
//			ps	= dbc.prepareStatement(sql);
//			rs	= ps.executeQuery();
//			int c = 0;
//			if(rs.next()) {
//				c = rs.getInt(1);
//			}
//			if(c != 1)
//				throw new IllegalStateException("Cannot find " + cmm + " after save:\n" + sql);
//		} finally {
//			try { if(rs != null) rs.close(); } catch(Exception x){}
//			try { if(ps != null) ps.close(); } catch(Exception x){}
//		}

		//-- Now handle all child relations.
		for(PropertyMetaModel< ? > pmm : childList) {
			//-- Only works for list.
			if(!List.class.isAssignableFrom(pmm.getActualType()))
				throw new IllegalStateException(pmm + ": unsupported child relation container type");

			PropertyMetaModel<List<I>> npm = (PropertyMetaModel<List<I>>) pmm;
			copyChildren(ed, di, si, npm);
		}
	}

	private <T, I, X extends List<I>> void copyChildren(@Nonnull EntityDef<T> ed, @Nonnull T di, @Nonnull T si, @Nonnull PropertyMetaModel<X> pmm) throws Exception {
		int sbl = m_pathSb.length();
		adjustPath(sbl, pmm.getName());
		if(isIgnoredPath()) {
			resetPath(sbl);
			return;
		}

		X sval = pmm.getValue(si);					// Get value of parent in instance
		X dval = pmm.getValue(di);

		if(sval != null) {
			//-- We need to create dest instances, so make sure a dval list is present
			if(dval == null) {
				dval = (X) new ArrayList<I>();		// Oh brother.
				pmm.setValue(di, dval);				// Set a value in dest.
			}

			//-- Find the contained entity and it's definition
			Type	ct = pmm.getGenericActualType();		// Get contained type (I)
			Class<I> itemtype = (Class<I>) MetaManager.findCollectionType(ct);
			if(null == itemtype)
				throw new IllegalStateException("Cannot get collection type");
			getDefinition(itemtype);

			//-- Party time....
			for(I srci: sval) {
				I dsti = copyInstance(srci);
				dval.add(dsti);
			}
		} else {
			dval = null;
		}
		resetPath(sbl);
		pmm.setValue(di, dval);
	}

	private <T, X> void copyParent(@Nonnull EntityDef<T> ed, @Nonnull T di, @Nonnull T si, @Nonnull PropertyMetaModel<X> pmm) throws Exception {
		int sbl = m_pathSb.length();
		adjustPath(sbl, pmm.getName());
		if(isIgnoredPath()) {
			resetPath(sbl);
			return;
		}

		X val = pmm.getValue(si);					// Get value of parent in instance
		if(val != null) {
			//-- Find the entity definition for this
			EntityDef<X>	ped = getDefinition(pmm.getActualType());
			InstanceKey<X> vk = ped.getInstanceKey(val);
			val = destCreate(vk);
		}
		pmm.setValue(di, val);
		resetPath(sbl);
//		System.out.println("P: " + pmm + " = " + MetaManager.identify(val));
	}

	private <T, X> void copyValue(@Nonnull EntityDef<T> ed, @Nonnull T di, @Nonnull T si, @Nonnull PropertyMetaModel<X> pmm) throws Exception {
		if(pmm.isTransient() || pmm.getReadOnly() == YesNoType.YES)
			return;
		if(pmm.isPrimaryKey())
			return;

		ClassMetaModel cmm = MetaManager.findClassMeta(pmm.getActualType());
		if(cmm.isPersistentClass())
			throw new IllegalStateException("Attempt to copy-by-value property " + pmm + " containing a persistent " + cmm);

		X val = pmm.getValue(si);
		pmm.setValue(di, val);
//		System.out.println("V: " + pmm + "=" + val);
	}

	/**
	 * Locate the direct target in the dest db, only create key instances. If the thing
	 * is not found in dest returns null.
	 * @param key
	 * @return
	 * @throws Exception
	 */
	@Nullable
	public <T> T dbfind(InstanceKey<T> key) throws Exception {
		QCriteria<T> q = QCriteria.create(key.getEntity().getEntityClass());
		int ix = 0;
		int sbl = m_pathSb.length();
		for(String name : key.getEntity().getSearchKey()) {
			adjustPath(sbl, name);

			Object kv = key.getValue(ix);

			if(kv instanceof InstanceKey) {
				InstanceKey< ? > altk = (InstanceKey< ? >) kv;
				kv = destCreate(altk);
				if(null == kv) {
					throw new IllegalStateException("Cannot locate key entity for field '" + name + "': " + altk);
				}
			}
			if(kv == null)
				throw new IllegalStateException("Logic: null key fragment " + name + " in " + key);
			q.eq(name, kv);
			ix++;
		}
		resetPath(sbl);

		log("FIND: " + key + " using " + q);
		T di = m_dds.queryOne(q);
		if(null != di) {
			m_destInstanceMap.put(key, di);
		}

		return di;
	}

	private void adjustPath(int sbl, String name) {
		m_pathSb.setLength(sbl);
		if(m_pathSb.length() > 0)
			m_pathSb.append('.');
		m_pathSb.append(name);
		m_currentPath = m_pathSb.toString();
	}

	private void resetPath(int sbl) {
		m_pathSb.setLength(sbl);
		m_currentPath = m_pathSb.toString();
	}

	public boolean isIgnoredPath() {
		return m_ignorePathSet.contains(m_currentPath);
	}

	private static boolean isExcepted(@Nonnull Set<Object> exceptSet, @Nonnull PropertyMetaModel< ? > frpmm) {
		if(exceptSet.contains(frpmm.getName()))
			return true;
		for(Object t : exceptSet) {
			if(t == Class.class) {
				Class< ? > rc = (Class< ? >) t;

				if(rc.isAssignableFrom(frpmm.getActualType()))
					return true;
			}
		}
		return false;
	}


}
