package to.etc.domui.util.db;

import java.lang.reflect.*;
import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;
import to.etc.webapp.query.*;

abstract public class QBasicModelCopier implements IModelCopier {
	abstract protected <T> boolean isUnloadedParent(T source, PropertyMetaModel pmm) throws Exception;

	abstract protected <T> boolean isUnloadedChildList(T source, PropertyMetaModel pmm) throws Exception;

	/**
	 * Make sure the data context is not a shared one.
	 * @param dc
	 */
	static public void assertPrivateContext(QDataContext dc) {
	// FIXME Need to find a way to mark the context as shared
	}

	static public class CopyInfo {
		private Map<Object, Object> m_sourceToTargetMap = new HashMap<Object, Object>();

		private int m_nCopied;

		private int m_nChanged;

		private int m_nNew;

		public CopyInfo() {}

		public void put(Object src, Object tgt) {
			m_sourceToTargetMap.put(src, tgt);
		}

		public Object get(Object src) {
			return m_sourceToTargetMap.get(src);
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

	public <T> T copyInstanceDeep(QDataContext dc, T source) throws Exception {
		CopyInfo ci = new CopyInfo();
		return internalCopy(dc, ci, source);
	}

	private <T> T internalCopy(QDataContext dc, CopyInfo donemap, T source) throws Exception {
		//-- 1. If the instance we're doing is part of the done set exit to prevent infinite loop
		T copy = (T) donemap.get(source); // Already mapped before?
		if(copy != null)
			return copy; // Yes-> return earlier copy.

		//-- Get the target instance to use.
		assertPrivateContext(dc);
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
			copy = dc.find(clz, pk);
			if(copy == null)
				throw new IllegalStateException("INTERNAL: probably a concurrency problem? Instance " + pk + " of class=" + clz + " cannot be loaded");
			donemap.incCopies();
		}
		donemap.put(source, copy); // Save as mapping
		copyProperties(dc, donemap, source, copy, cmm);
		//		/*
		//		 * Deep copy of (database) property values. All properties that refer to some relation will be copied *if* their
		//		 * value is instantiated (not lazy and clean). All other properties are just copied by reference.
		//		 */
		//		for(PropertyMetaModel pmm : cmm.getProperties()) {
		//			//-- We cannot copy readonly properties, so skip those
		//			if(pmm.getReadOnly() == YesNoType.YES)
		//				continue;
		//
		//			switch(pmm.getRelationType()){
		//				default:
		//					throw new IllegalStateException("Unexpected relation type: " + pmm.getRelationType() + " in " + pmm);
		//				case NONE:
		//					//-- Normal non-relation field. Just copy the value or the reference.
		//					Object v = pmm.getAccessor().getValue(source);
		//					((IValueAccessor<Object>) pmm.getAccessor()).setValue(copy, v);
		//					break;
		//
		//				case UP:
		//					copyParentProperty(dc, donemap, source, copy, pmm);
		//					break;
		//
		//				case DOWN:
		//					copyChildListProperty(dc, donemap, source, copy, pmm);
		//					break;
		//			}
		//		}

		return copy;
	}

	private <T> void copyProperties(QDataContext dc, CopyInfo donemap, T source, T copy, ClassMetaModel cmm) throws Exception {
		/*
		 * Deep copy of (database) property values. All properties that refer to some relation will be copied *if* their
		 * value is instantiated (not lazy and clean). All other properties are just copied by reference.
		 */
		for(PropertyMetaModel pmm : cmm.getProperties()) {
			//-- We cannot copy readonly properties, so skip those
			if(pmm.getReadOnly() == YesNoType.YES)
				continue;

			switch(pmm.getRelationType()){
				default:
					throw new IllegalStateException("Unexpected relation type: " + pmm.getRelationType() + " in " + pmm);
				case NONE:
					//-- Normal non-relation field. Just copy the value or the reference.
					Object v = pmm.getAccessor().getValue(source);
					((IValueAccessor<Object>) pmm.getAccessor()).setValue(copy, v);
					break;

				case UP:
					copyParentProperty(dc, donemap, source, copy, pmm);
					break;

				case DOWN:
					copyChildListProperty(dc, donemap, source, copy, pmm);
					break;
			}
		}
	}

	/**
	 *
	 * @param <T>
	 * @param dc
	 * @param donemap
	 * @param source
	 * @param copy
	 * @param pmm
	 * @throws Exception
	 */
	private <T> void copyChildListProperty(QDataContext dc, CopyInfo donemap, T source, T copy, PropertyMetaModel pmm) throws Exception {
		//-- If this list is not yet present (lazily loaded and not-loaded) exit- it cannot have changed
		if(isUnloadedChildList(source, pmm))
			return;
		Object schild = pmm.getAccessor().getValue(source);
		if(schild == null) {
			//-- We ignore NULL lists
			return;
		}
		List<Object> slist = (List<Object>) schild;

		Object dchild = pmm.getAccessor().getValue(copy);
		if(dchild == null) {
			//-- We need an actual instantiated target thingerydoo to add objects to.
			dchild = new ArrayList<Object>(slist.size());
			((IValueAccessor<Object>) pmm.getAccessor()).setValue(copy, dchild);
		}
		List<Object> dlist = (List<Object>) dchild;

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
				Object di = internalCopy(dc, donemap, si);
				dc.save(di);
				dlist.add(di);
			} else {
				Object di = dpkmap.remove(spk); // Does this same record exist @ destination?
				if(di != null) {
					copyProperties(dc, donemap, si, di, childmm);
				} else {
					//-- This did not exist @ destination. Map it to a destination object then add it there.
					di = internalCopy(dc, donemap, si);
					dc.save(di);
					dlist.add(di);
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
	private <T> void copyParentProperty(QDataContext dc, CopyInfo donemap, T source, T copy, PropertyMetaModel pmm) throws Exception {
		//-- Upward reference. If this is a lazy proxy that is NOT instantiated (clean) we do nothing, else we load and copy.
		if(isUnloadedParent(source, pmm))
			return;

		//-- If the parent pointer is null then just clear the value in the copy and be gone.
		Object sparent = pmm.getAccessor().getValue(source); // We are instantiated so get the property
		if(sparent == null) {
			//-- Source parent is null- set target parent to null too
			pmm.getAccessor().setValue(copy, null);
			return;
		}

		//-- Fsuck. Load the appropriate copy from the database;
		Object dparent = internalCopy(dc, donemap, sparent); // Make a deep copy of the source parent object instance
		((IValueAccessor<Object>) pmm.getAccessor()).setValue(copy, dparent);
	}


}
