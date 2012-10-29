package to.etc.domui.logic;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;
import to.etc.domui.util.db.*;
import to.etc.util.*;

/**
 * Helper class to copy models and to handle model changes related to models by comparing their
 * contents. The entire model is constructed by having a set of model "roots" that form the
 * basis for the data model we're manipulating.
 *
 * <p>For each model instance "reachable" from the model roots (by following properties on those
 * instances) we make a copy of said instance on request. The copy is directly reachable from an
 * original map. This allows us to quickly find, for every instance, a copy of said instance.</p>
 *
 * <p>Because a model is usually constructed out of Hibernate data classes, it is important to
 * limit the boundaries of the instances properly. We do this by limiting the model boundaries around
 * lazy properties that are not yet loaded.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 29, 2012
 */
public class LogiModel {
	@Nonnull
	private Set<Object> m_modelRoots = new HashSet<Object>();

	/** Maps original instance to copy of the instance, if present */
	@Nonnull
	private Map<Object, Object> m_originalToCopyMap = new HashMap<Object, Object>();

	@Nonnull
	private Map<Object, Object> m_copyToOriginalMap = new HashMap<Object, Object>();

	@Nullable
	private IModelCopier m_copyHandler;

	public LogiModel() {}

	@Nonnull
	public IModelCopier getCopyHandler() {
		if(null != m_copyHandler)
			return m_copyHandler;
		m_copyHandler = QCopy.getInstance();
		if(null != m_copyHandler)
			return m_copyHandler;
		throw new IllegalStateException("No copy handler is known");
	}

	public <T> void addRoot(@Nonnull T root) {
		m_modelRoots.add(root);
	}

	/**
	 * This walks all model roots, and updates the copy to fully and properly represent the model roots.
	 */
	public void updateCopy() throws Exception {
		//-- 1. Create new maps, but first copy the current originalToCopy map to find existing instances
		long ts = System.nanoTime();
		Map<Object, Object> oldOrigMap = m_originalToCopyMap;	// Keep the original map
		m_originalToCopyMap = new HashMap<Object, Object>();	// Create a clean one,
		m_copyToOriginalMap.clear();							// And clean out this one

		//-- Copy all roots.
		for(Object root : m_modelRoots) {
			createCopy(root, oldOrigMap);
		}
		ts = System.nanoTime() - ts;
		System.out.println("logi: copied " + m_originalToCopyMap.size() + " instances in " + StringTool.strNanoTime(ts));
	}

	@Nullable
	private <T> T createCopy(@Nullable T source, @Nonnull Map<Object, Object> oldOrigMap) throws Exception {
		if(null == source)
			return null;

		//-- Already mapped in this run?
		T copy = (T) m_originalToCopyMap.get(source);
		if(null != copy)
			return copy;									// Return copy instance.

		//-- We need to do work... Was there a copy in the "old" instance?
		Class<T> clz = (Class<T>) source.getClass();		// Java generics suck.
		copy = (T) oldOrigMap.get(source);
		if(null == copy) {
			//-- This is new here. We need an instance.
			copy = DomUtil.nullChecked(clz.newInstance());	// Create uninitialized instance.
		}

		//-- Instance is known- store it in the maps, now: this allows deeper references to find "back" the new thing, even though it is not yet properly initialized.
		m_originalToCopyMap.put(source, copy);				// Map source -> copy
		if(null != copy)
			m_copyToOriginalMap.put(copy, source);			// And versa vice ;)

		//-- Ok, time to handle all properties.
		ClassMetaModel cmm = MetaManager.findClassMeta(clz);
		List<PropertyMetaModel< ? >> pmml = cmm.getProperties();
		for(PropertyMetaModel< ? > pmm : pmml) {
			copyPropertyValue(source, copy, pmm, oldOrigMap);
		}

		// TODO Auto-generated method stub
		return null;
	}

	private <T, P> void copyPropertyValue(@Nonnull T source, @Nonnull T copy, @Nonnull PropertyMetaModel<P> pmm, @Nonnull Map<Object, Object> oldOrigMap) throws Exception {
		switch(pmm.getRelationType()){
			case NONE:
				P value = pmm.getValue(source);					// Get value in source
				pmm.setValue(copy, value);						// And set in copy
				return;

			case UP:
				P refval = null;
				if(!getCopyHandler().isUnloadedParent(source, pmm)) {
					//-- This parent instance is loaded- get it's copy and set it
					P oldval = pmm.getValue(source);			// Get the loaded source
					refval = createCopy(oldval, oldOrigMap);	// Create/get the copy of it
				}
				pmm.setValue(copy, refval);
				return;

			case DOWN:
				refval = null;
				if(!getCopyHandler().isUnloadedChildList(source, pmm)) {
					//-- We need to pass through all this and get copies
					P oldval = pmm.getValue(source);			// Get original collection
					if(oldval == null)
						refval = null;
					else
						refval = createChildCollection(source, oldval, pmm, oldOrigMap);
				}
				pmm.setValue(copy, refval);
				return;
		}
	}

	private <T, P> P createChildCollection(@Nonnull T source, @Nonnull P sourcevalue, @Nonnull PropertyMetaModel<P> pmm, @Nonnull Map<Object, Object> oldOrigMap) throws Exception {
		if(List.class.isAssignableFrom(sourcevalue.getClass())) {
			ArrayList<Object> al = new ArrayList<Object>();
			for(Object v : (List<Object>) sourcevalue) {
				Object nv = createCopy(v, oldOrigMap);
				al.add(nv);
			}
			return (P) al;
		} else
			throw new IllegalStateException("Child collection type: " + sourcevalue.getClass() + " not implemented, in instance " + source + " property " + pmm);
	}
}
