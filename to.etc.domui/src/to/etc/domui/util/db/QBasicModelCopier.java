package to.etc.domui.util.db;

import java.lang.reflect.*;
import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.query.*;

abstract public class QBasicModelCopier implements IModelCopier {
	abstract protected <T> boolean isUnloadedParent(T source, PropertyMetaModel pmm) throws Exception;

	abstract protected <T> boolean isUnloadedChildList(T source, PropertyMetaModel pmm) throws Exception;

	abstract protected QPersistentObjectState getObjectState(QDataContext dc, Object instance) throws Exception;

	/**
	 * Make sure the data context is not a shared one.
	 * @param dc
	 */
	static public void assertPrivateContext(QDataContext dc) {
	// FIXME Need to find a way to mark the context as shared
	}

	static public class CopyInfo {
		private QDataContext m_sourcedc;

		private QDataContext m_targetdc;

		private Map<Object, Object> m_sourceToTargetMap = new HashMap<Object, Object>();

		/**
		 * Objects that are determined to be new need to be fully initialized before they are saved, especially
		 * when saving objects that are part of a bidirectional relation.
		 */
		private List<Object> m_saveList = new ArrayList<Object>();

		private int m_nCopied;

		private int m_nChanged;

		private int m_nNew;

		private int m_level;

		public CopyInfo(QDataContext target, QDataContext source) {
			m_sourcedc = source;
			m_targetdc = target;
		}

		public QDataContext getSourceDC() {
			return m_sourcedc;
		}

		public QDataContext getTargetDC() {
			return m_targetdc;
		}

		public void put(Object src, Object tgt) {
			m_sourceToTargetMap.put(src, tgt);
		}

		public Object get(Object src) {
			return m_sourceToTargetMap.get(src);
		}

		/**
		 * Add the object to the pending save list, to be saved in order of addition.
		 * @param o
		 */
		public void save(Object o) {
			if(m_saveList.contains(o))
				return;
			//				throw new IllegalStateException("Duplicate saved object: " + identify(o)); // Indicative of a save order error.
			m_saveList.add(o);
		}

		public List<Object> getSaveList() {
			return m_saveList;
		}

		public void incCopies() {
			m_nCopied++;
		}

		public void incChanges() {
			m_nChanged++;
		}

		public void incNew() {
			m_nNew++;
		}

		public int getNChanged() {
			return m_nChanged;
		}

		public int getNCopied() {
			return m_nCopied;
		}

		public void inc() {
			m_level++;
		}

		public void dec() {
			m_level--;
		}

		public void log(String s) {
			StringBuilder sb = new StringBuilder(128);
			for(int i = 0; i < m_level; i++)
				sb.append(' ');
			sb.append(s);
			System.out.println(sb.toString());
		}
	}

	static private String identify(Object t) {
		if(t == null)
			return "null";
		ClassMetaModel cmm = MetaManager.findClassMeta(t.getClass());
		if(!cmm.isPersistentClass() || cmm.getPrimaryKey() == null)
			return t.toString();
		try {
			Object k = cmm.getPrimaryKey().getAccessor().getValue(t);
			return t.getClass().getName() + "#" + k + " @" + System.identityHashCode(t);
			//			return t.getClass().getName() + "#null@" + System.identityHashCode(t);
		} catch(Exception x) {
			return t.toString();
		}
	}

	/**
	 * Do a shallow copy of the instance. This only copies all public fields.
	 * @see to.etc.domui.util.db.IModelCopier#copyInstanceShallow(to.etc.webapp.query.QDataContext, java.lang.Object)
	 */
	public <T> T copyInstanceShallow(QDataContext dc, T source) throws Exception {
		if(source == null)
			return null;
		assertPrivateContext(dc);
		ClassMetaModel cmm = MetaManager.findClassMeta(source.getClass());
		if(!cmm.isPersistentClass())
			throw new IllegalStateException("The class " + cmm + " is not a persistent class");
		Class<T> clz = (Class<T>) cmm.getActualClass(); // Use this as the class indicator - source can be a proxy class
		Object pk = cmm.getPrimaryKey().getAccessor().getValue(source);

		//-- Create a new instance as the copy
		T copy;
		if(pk == null) {
			//-- We have a new instance. Create one.
			copy = clz.newInstance();
		} else {
			//-- Load the instance
			copy = dc.find(clz, pk);
			if(copy == null)
				throw new IllegalStateException("INTERNAL: probably a concurrency problem? Instance " + pk + " of class=" + clz + " cannot be loaded");
		}

		//-- Property value copy. We copy all properties including "up" relations but excluding "child" lists.
		for(PropertyMetaModel pmm : cmm.getProperties()) {
			switch(pmm.getRelationType()){
				default:
					break;
				case NONE:
				case UP:
					//-- We must copy.
					Object v = pmm.getAccessor().getValue(source);
					((IValueAccessor<Object>) pmm.getAccessor()).setValue(copy, v);
					break;
			}
		}
		return copy;
	}

	/**
	 *
	 * @param <T>
	 * @param targetdc
	 * @param sourcedc
	 * @param source
	 * @return
	 * @throws Exception
	 */
	public <T> T copyInstanceDeep(QDataContext targetdc, QDataContext sourcedc, T source) throws Exception {
		CopyInfo ci = new CopyInfo(targetdc, sourcedc);
		long ts = System.nanoTime();
		T res = internalCopy(ci, source);

		//-- Now save all objects;
		for(Object o : ci.getSaveList()) {
			ci.log("Save new object: " + identify(o));
			targetdc.save(o);
		}

		ts = System.nanoTime() - ts;
		System.out.println("Q: created 'save' copy of " + identify(source) + " by copying " + ci.getNCopied() + ", adding " + ci.m_nNew + " records in " + StringTool.strNanoTime(ts));
		return res;
	}

	/**
	 * Main recursively called workhorse of copying the node graph. It copies an object and it's properties, and takes care to
	 * "save" records that were new. It must do so in the proper order or Hibernate barfs up:
	 * <ol>
	 *	<li>Any 'parent' record of this object must be saved</li>
	 *	<li>The record itself, if new, must be saved</li>
	 *	<li>Any child record must be saved.</li>
	 * </ol>
	 * @param <T>
	 * @param targetdc
	 * @param donemap
	 * @param source
	 * @return
	 * @throws Exception
	 */
	private <T> T internalCopy(CopyInfo donemap, T source) throws Exception {
		//-- 1. If the instance we're doing is part of the done set exit to prevent infinite loop
		T copy = (T) donemap.get(source); // Already mapped before?
		if(copy != null) {
			donemap.log("returning existing copy for " + identify(source));
			return copy; // Yes-> return earlier copy.
		}

		//-- Get the target instance to use.
		assertPrivateContext(donemap.getTargetDC());
		ClassMetaModel cmm = MetaManager.findClassMeta(source.getClass());
		if(!cmm.isPersistentClass())
			throw new IllegalStateException("The class " + cmm + " is not a persistent class");
		Class<T> clz = (Class<T>) cmm.getActualClass(); // Use this as the class indicator - source can be a proxy class
		Object pk = cmm.getPrimaryKey().getAccessor().getValue(source);

		//-- Create a new instance as the copy
		if(pk == null) {
			//-- We have a new instance. Create one.
			copy = clz.newInstance();
			donemap.incNew();
		} else {
			//-- Load the instance
			copy = donemap.getTargetDC().find(clz, pk);
			if(copy == null)
				throw new IllegalStateException("INTERNAL: probably a concurrency problem? Instance " + pk + " of class=" + clz + " cannot be loaded");
			donemap.incCopies();
		}
		donemap.put(source, copy); // Save as mapping
		System.out.println();
		donemap.log("Copying " + identify(source) + (pk == null ? " (new)" : ""));
		//		if(null == pk) {
		//			donemap.log("Post for save " + identify(copy) + " (copy of " + identify(source) + ")");
		//			donemap.save(copy);
		//		}
		donemap.inc();
		copyProperties(donemap, source, copy, cmm);
		donemap.dec();
		donemap.log("Finished copy of " + identify(source) + (pk == null ? " (new)" : ""));
		return copy;
	}

	static private String dumpValue(Object o) {
		if(o == null)
			return "null";

		String s = String.valueOf(o);
		if(s.length() > 20)
			return s.substring(0, 20) + "...";
		return s;
	}

	/**
	 * Copies properties, recursively traversing the graph by following parent and child relations. This
	 * code takes care to ensure that parent properties are saved BEFORE the class they occur in, and that
	 * child properties are saved ONLY when their parent has been saved. It fails when the graph has a cycle
	 * of new nodes - which should not normally occur.
	 *
	 * @param <T>
	 * @param targetdc
	 * @param donemap
	 * @param source
	 * @param copy
	 * @param cmm
	 * @throws Exception
	 */
	private <T> void copyProperties(CopyInfo donemap, T source, T copy, ClassMetaModel cmm) throws Exception {
		/*
		 * Deep copy of (database) property values. All properties that refer to some relation will be copied *if* their
		 * value is instantiated (not lazy and clean). All other properties are just copied by reference. We first handle
		 * all normal properties and all "UP" relations. By handling UP relations first we ensure that parent records
		 * that are "new" are always saved <i>before</i> this one. After these we check if the "current" record needs to
		 * be saved; we can do that at this time because all it's parents are saved. Only then we'll pass over all
		 * child relations, because these can save their data <i>only</i> if this record has been saved.
		 */
		QDataContext targetdc = donemap.getTargetDC();
		QPersistentObjectState pos = getObjectState(targetdc, source);
		boolean docopy = false;
		switch(pos){
			default:
				throw new IllegalStateException("Unexpected source object state " + pos + " on " + identify(source));

			case DELETED:
				//-- FIXME What to do now!?
				System.out.println("HELP! Deleted object in object graph " + identify(source));
				return;

			case UNKNOWN:
			case NEW:
			case DIRTY:
				docopy = true;

			case PERSISTED: // The object is unchanged and persisted
				donemap.log("Not copying fields for clean object " + identify(source));
				docopy = false;
				break;
		}

		//-- Unknowns are treated as new,
		List<PropertyMetaModel> childpropertylist = null;

		for(PropertyMetaModel pmm : cmm.getProperties()) {
			//-- We cannot copy readonly properties, so skip those
			if(pmm.getReadOnly() == YesNoType.YES)
				continue;

			switch(pmm.getRelationType()){
				default:
					throw new IllegalStateException("Unexpected relation type: " + pmm.getRelationType() + " in " + pmm);
				case NONE:
					//-- Normal non-relation field. Just copy the value or the reference.
					if(docopy) {
						Object v = pmm.getAccessor().getValue(source);
						((IValueAccessor<Object>) pmm.getAccessor()).setValue(copy, v);
						//					donemap.log("value property " + pmm.getName() + " of " + pmm.getClassModel() + ": " + dumpValue(v));
					}
					break;

				case UP:
					//-- Traverse these immediately.
					donemap.log("UP relation " + pmm.getName() + " of " + pmm.getClassModel());
					donemap.inc();
					copyParentProperty(donemap, source, copy, pmm);
					donemap.dec();
					break;

				case DOWN:
					if(childpropertylist == null)
						childpropertylist = new ArrayList<PropertyMetaModel>();
					childpropertylist.add(pmm);
					break;
			}
		}

		//-- 2. If the current record is unsaved save it now;
		if(cmm.getPrimaryKey().getAccessor().getValue(copy) == null) {
			donemap.log("Post for save " + identify(copy) + " (copy of " + identify(source) + ")");
			donemap.save(copy);
		}

		//-- Now traverse all child relations - this record (parent of those children) has been saved.
		if(childpropertylist != null) {
			for(PropertyMetaModel pmm : childpropertylist) {
				donemap.log("DOWN relation " + pmm.getName() + " of " + pmm.getClassModel());
				copyChildListProperty(donemap, source, copy, pmm);
			}
		}
	}

	/**
	 *
	 * @param <T>
	 * @param targetdc
	 * @param donemap
	 * @param source
	 * @param copy
	 * @param pmm
	 * @throws Exception
	 */
	private <T> void copyChildListProperty(CopyInfo donemap, T source, T copy, PropertyMetaModel pmm) throws Exception {
		//-- If this list is not yet present (lazily loaded and not-loaded) exit- it cannot have changed
		if(isUnloadedChildList(source, pmm))
			return;
		Object schild = pmm.getAccessor().getValue(source);
		if(schild == null) {
			//-- We ignore NULL lists
			return;
		}
		Collection<Object> slist = (List<Object>) schild;

		Object dchild = pmm.getAccessor().getValue(copy);
		if(dchild == null) {
			//-- We need an actual instantiated target thingerydoo to add objects to.
			dchild = new ArrayList<Object>(slist.size());
			((IValueAccessor<Object>) pmm.getAccessor()).setValue(copy, dchild);
		}
		Collection<Object> dlist = (List<Object>) dchild;

		//-- Locate the child class's type
		Type vtype = pmm.getGenericActualType();
		if(vtype == null)
			throw new IllegalStateException("Unable to determine the generic TYPE of the child record in DOWN relation property "+pmm);
		Class< ? > childclz = MetaManager.findCollectionType(vtype); // Try to get value class type.

		/*
		 * Ohh shit. We need to do an exhaustive compare between source and target lists,
		 * mapping "source" to "target" object then making sure both lists are equal.
		 */
		//-- 1. Make a map of all known PK's in the DEST list.
		ClassMetaModel	childmm = MetaManager.findClassMeta(childclz);
		if(!childmm.isPersistentClass())
			throw new IllegalStateException("The class " + childmm + " is not a persistent class");
		PropertyMetaModel childpkpmm = childmm.getPrimaryKey();
		if(childpkpmm == null)
			throw new IllegalStateException("The class " + childmm + " has an unknown primary key");

		Map<Object, Object>	dpkmap = new HashMap<Object, Object>();
		for(Object o: dlist) {
			Object dpk = childpkpmm.getAccessor().getValue(o);
			if(dpk == null)
				throw new IllegalStateException("Unexpected record with null primary key in child list " + pmm);
			dpkmap.put(dpk, o);
		}

		//-- 1. For every source element locate destination and remove from map
		for(Object si : slist) {
			Object spk = childpkpmm.getAccessor().getValue(si);
			if(spk == null) {
				//-- A new record @ source. Map it to dest && add to dlist
				Object di = internalCopy(donemap, si);
				//				donemap.log("Post for save " + identify(di)); Already done by internalCopy.
				//				donemap.save(di);
				dlist.add(di);
			} else {
				Object di = dpkmap.remove(spk); // Does this same record exist @ destination?
				if(di != null) {
					copyProperties(donemap, si, di, childmm);
				} else {
					//-- This did not exist @ destination. Map it to a destination object then add it there.
					di = internalCopy(donemap, si);
					//					donemap.log("Post for save " + identify(di)); Already done by internalCopy.
					//					donemap.save(di);
					dlist.add(di);
					//					donemap.incNew();
				}
			}
		}

		//-- 2. What's left in dpkmap is destination thingerydoos that do not exist in the source anymore.
		for(Object di : dpkmap.values()) {
			dlist.remove(di);

			//-- FIXME We may need to DELETE those things too - we need to ask Hibernate if they are deleted
		}
	}

	/**
	 * Handles the deep copy of a parent property.
	 * @param dc
	 * @param donemap
	 * @param source
	 * @param pmm
	 * @throws Exception
	 */
	private <T> void copyParentProperty(CopyInfo donemap, T source, T copy, PropertyMetaModel pmm) throws Exception {
		//-- Upward reference. If this is a lazy proxy that is NOT instantiated (clean) we do nothing, else we load and copy.
		Object sparent = pmm.getAccessor().getValue(source); // We are instantiated so get the property
		Object dparent;
		if(sparent == null) {
			donemap.log("parent property is null."); // Nothing to see here, begone
			dparent = null;
		} else if(isUnloadedParent(source, pmm)) {
			//-- This *will* be an existing thing but it is not 'loaded' in this session. Just get a PK reference to it and use that.
			ClassMetaModel pcmm = MetaManager.findClassMeta(sparent.getClass()); // Get the type of the parent,
			if(!pcmm.isPersistentClass())
				throw new IllegalStateException("parent instance pointed to by " + pmm + " is not a persistent class");
			PropertyMetaModel pkpm = pcmm.getPrimaryKey();
			if(pkpm == null)
				throw new IllegalStateException("parent instance pointed to by " + pmm + " has no primary key defined");
			Object pk = pkpm.getAccessor().getValue(sparent); // Get PK of parent;
			if(pk == null)
				throw new IllegalStateException("undirtied and existing parent instance has a null PK!?");

			//-- Get an instance /reference/ in the target datacontext
			dparent = donemap.getTargetDC().getInstance(pcmm.getActualClass(), pk);
			donemap.log("property is not instantiated (lazy loaded and unused in this session), loading instance");
		} else {
			//-- Fsuck. Load the appropriate copy from the database;
			dparent = internalCopy(donemap, sparent); // Make a deep copy of the source parent object instance
		}
		((IValueAccessor<Object>) pmm.getAccessor()).setValue(copy, dparent);
		donemap.log("parent property set to " + identify(dparent) + " (source was " + identify(sparent) + ")");
	}
}
