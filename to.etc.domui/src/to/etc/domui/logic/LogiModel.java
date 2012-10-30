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

	/** This maps a copy instance to it's original source. */
	@Nonnull
	private Map<Object, Object> m_copyToOriginalMap = new HashMap<Object, Object>();

	/** This contains only the mappings for the root entries, as [source, copy]. Used to see what root entries have disappeared. */
	@Nonnull
	private Map<Object, Object> m_rootCopyMap = new HashMap<Object, Object>();

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
		m_rootCopyMap.clear();

		//-- Copy all roots.
		for(Object root : m_modelRoots) {
			Object copy = createCopy(root, oldOrigMap);
			m_rootCopyMap.put(root, copy);
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
		if(pmm.getReadOnly() == YesNoType.YES)
			return;

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


	/*--------------------------------------------------------------*/
	/*	CODING:	Compare to make a delta event.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Compare the copy with the current state of the original, and create the change event set.
	 */
	@Nonnull
	public LogiEventSet compareCopy() throws Exception {
		LogiEventSet les = new LogiEventSet();
		Set<Object> doneset = new HashSet<Object>();

		Map<Object, Object> rootsdone = new HashMap<Object, Object>(m_rootCopyMap);	// Copy the original roots and their copies.
		int rix = 0;
		for(Object root : m_modelRoots) {
			les.enterRoot(rix);
			Object copy = rootsdone.remove(root);					// Was there a previous root?
			if(copy == null) {
				les.addRootInstanceAdded(root);
			} else {
				compareInstances(les, doneset, root, copy);
			}
			les.exitRoot(rix);
		}

		//-- All that is left in rootsDone means root instances have been removed.
		for(Map.Entry<Object, Object> me : rootsdone.entrySet()) {
			les.addRootInstanceRemoved(me.getKey(), me.getValue());
		}
		return les;
	}

	/**
	 * Compare two instances, and add their changes. Both instances must exist and be of the same type.
	 * @param les
	 * @param doneset
	 * @param source
	 * @param copy
	 */
	private <T> void compareInstances(@Nonnull LogiEventSet les, @Nonnull Set<Object> doneset, @Nonnull T source, @Nonnull T copy) throws Exception {
		//-- Make very sure we pass every instance only once.
		if(doneset.contains(source) || doneset.contains(copy))
			return;
		doneset.add(source);
		doneset.add(copy);

		//-- Compare all direct properties 1st.
		Class<T> clz = (Class<T>) source.getClass();		// Java generics suck.
		ClassMetaModel cmm = MetaManager.findClassMeta(clz);
		List<PropertyMetaModel< ? >> pmml = cmm.getProperties();
		List<PropertyMetaModel< ? >> laterl = null;
		for(PropertyMetaModel< ? > pmm : pmml) {
			switch(pmm.getRelationType()){
				case NONE:
					//-- Compare these thingies, by value, not reference.
					compareValues(les, pmm, source, copy);
					break;

				case UP:
					if(!compareUpValues(les, pmm, source, copy)) {
						if(laterl == null)
							laterl = new ArrayList<PropertyMetaModel< ? >>();
						laterl.add(pmm);
					}
					break;

				case DOWN:
					if(laterl == null)
						laterl = new ArrayList<PropertyMetaModel< ? >>();
					laterl.add(pmm);
					break;
			}
		}

		if(null != laterl) {
			for(PropertyMetaModel< ? > pmm : laterl) {
				compareChildren(les, pmm, source, copy);
			}
		}
	}

	private <T, P> boolean compareChildren(@Nonnull LogiEventSet les, PropertyMetaModel<P> pmm, @Nonnull T source, @Nonnull T copy) throws Exception {
		if(getCopyHandler().isUnloadedChildList(source, pmm)) {
			//-- Source is (now) unloaded. Compare with copy: should be null.
			P copyval = pmm.getValue(copy);
			if(copyval == null)
				return false;

			//-- Child was set to empty list...
			throw new IllegalStateException("ni: child set to unloaded while copy is discrete??");
			//			return true;
		}

		//-- We have some collection here. Handle the supported cases (List only)
		P sourceval = pmm.getValue(source);
		P copyval = pmm.getValue(copy);
		if(sourceval == null) {
			if(copyval == null)
				return false;

			//-- A collection has been emptied. Send a property changed event for it
			les.propertyChange(pmm, source, copy, sourceval, copyval);

			//-- Send "clear" event for collection and delete events for all it's members.
			les.addCollectionClear(pmm, source, copy, sourceval, copyval);
			List< ? > clist = getChildValues(copyval);
			for(int i = clist.size(); --i >= 0;) {
				Object centry = clist.get(i);
				if(centry != null) {
					Object sentry = m_copyToOriginalMap.get(centry);
					if(sentry == null)
						throw new IllegalStateException("Cannot find original for copied entry: " + centry);

					les.addCollectionDelete(pmm, source, copy, i, sentry);
				}
			}
			return true;
		}

		if(copyval == null) {
			//-- We have a new list that was not present @ time of the copy. Send property change,
			les.propertyChange(pmm, source, copy, sourceval, copyval);

			//-- Send "add" event for all new members.
			List<Object> sorigl = (List<Object>) getChildValues(sourceval);				// The current list from source
			int i = 0;
			for(Object cv : sorigl) {
				les.addCollectionAdd(pmm, source, copy, i++, cv);
			}
			return true;
		}

		//-- We have two collections - we need to diff the instances... Get both "current collections"
		List< ? > sorigl = getChildValues(sourceval);				// The current list from source
		List<Object> scopyl = new ArrayList<Object>(sorigl.size());	// This will hold the "current copies" known for each source entry.
		for(Object t : sorigl) {
			Object tcopy = m_originalToCopyMap.get(t);				// Get (existing) copy; if no copy is found it means the entry changed anyway - store null for that
			scopyl.add(tcopy);
		}
		List<Object> copyl = (List<Object>) getChildValues(copyval);// The list of copied values

		return diffList(les, pmm, source, copy, scopyl, copyl);
	}

	private <T, P, I> boolean diffList(@Nonnull LogiEventSet les, PropertyMetaModel<P> pmm, @Nonnull T source, @Nonnull T copy, List<I> sourcel, List<I> copyl) throws Exception {
		//-- First slice off common start and end;
		int send = sourcel.size();
		int cend = copyl.size();
		int sbeg = 0;
		int cbeg = 0;

		//-- Slice common beginning
		while(sbeg < send && cbeg < cend) {
			I so = sourcel.get(sbeg);
			I co = copyl.get(cbeg);
			if(!MetaManager.areObjectsEqual(so, co)) {
				break;
			}
			sbeg++;
			cbeg++;
		}

		//-- Slice common end
		while(send > sbeg && cend > cbeg) {
			I so = sourcel.get(send - 1);
			I co = copyl.get(cend - 1);
			if(!MetaManager.areObjectsEqual(so, co)) {
				break;
			}
			cend--;
			send--;
		}
		if(sbeg >= send && cbeg >= cend) {
			//-- Equal arrays- no changes.
			return false;
		}

		//-- Ouf.. We need to do the hard bits. Find the lcs and then render the edit as the delta.
		int	m = (send - sbeg)+1;
		int n = (cend - cbeg) + 1;
		int[][] car = new int[m][];
		for(int i = 0; i < m; i++) {
			car[i] = new int[n];
			car[m][0] = 0;
		}
		for(int i = 0; i < n; i++) {
			car[0][i] = 0;
		}

		for(int i = 1; i < m; i++) {
			for(int j = 1; j < n; j++) {
				I so = sourcel.get(sbeg + i - 1);
				I co = copyl.get(cbeg + j - 1);
				if(MetaManager.areObjectsEqual(so, co)) {
					car[i][j] = car[i - 1][j - 1] + 1;				// Is length of previous subsequence + 1.
				} else {
					car[i][j] = Math.max(car[i][j - 1], car[i - 1][j]);	// Is length of the so-far longest subsequence
				}
			}
		}

		//-- Now: backtrack from the end to the start to render the delta. This creates the delta in the "reverse" order.
		int i = m;
		int j = n;
		List<LogiEventListDelta<T, P, I>> res = new ArrayList<LogiEventListDelta<T, P, I>>();

		List<String> tmp = new ArrayList<String>();
		while(j > 0 || i > 0) {
			if(i > 0 && j > 0 && MetaManager.areObjectsEqual(sourcel.get(sbeg + i - 1), copyl.get(cbeg + j - 1))) {
				i--;
				j--;

				//-- part of lcs - no delta
			} else if(j > 0 && (i == 0 || car[i][j - 1] >= car[i - 1][j])) {
				//-- Addition
				tmp.add("+ " + copyl.get(cbeg + j - 1));
				j--;
			} else if(i > 0 && (j == 0 || car[i][j - 1] < car[i - 1][j])) {
				//-- Deletion
				tmp.add("- " + sourcel.get(sbeg + i - 1));
				i--;
			}
		}
		Collections.reverse(tmp);
		for(String s : tmp)
			System.out.println(" " + s);

		return true;
	}


	@Nonnull
	private List< ? > getChildValues(@Nonnull Object source) {
		if(List.class.isAssignableFrom(source.getClass())) {
			List<Object> res = new ArrayList<Object>();
			for(Object v : (List<Object>) source)
				res.add(v);
			return res;
		} else
			throw new IllegalStateException(source + ": only List child sets are supported.");
	}


	private <T, P> boolean compareValues(@Nonnull LogiEventSet les, PropertyMetaModel<P> pmm, @Nonnull T source, @Nonnull T copy) throws Exception {
		P vals = pmm.getValue(source);
		P valc = pmm.getValue(copy);
		if(MetaManager.areObjectsEqual(vals, valc))
			return false;
		les.propertyChange(pmm, source, copy, vals, valc);
		return true;
	}

	private <T, P> boolean compareUpValues(@Nonnull LogiEventSet les, PropertyMetaModel<P> pmm, @Nonnull T source, @Nonnull T copy) throws Exception {
		if(getCopyHandler().isUnloadedParent(source, pmm)) {
			//-- Source is now unloaded. Check copy;
			P copyval = pmm.getValue(copy);
			if(copyval == null)
				return false;								// Unloaded parent and null in copy -> unchanged

			//-- This parent has changed.
			les.propertyChange(pmm, source, copy, null, copyval);
			return true;
		}

		//-- The model has a loaded parent... Get it, and get it's copy.
		P sourceval = pmm.getValue(source);
		P copyval = pmm.getValue(copy);
		if(null == sourceval) {
			//-- Current value is null... If copy is null too we're equal
			if(null == copyval)
				return false;								// Unchanged

			//-- This parent has changed.
			les.propertyChange(pmm, source, copy, null, copyval);
			return true;
		}

		//-- Source has a value. Is it mapped to a copy?
		P ncopyval = (P) m_originalToCopyMap.get(sourceval);
		if(ncopyval == null) {
			//-- There is no copy of this source -> new assignment to parent property.
			les.propertyChange(pmm, source, copy, sourceval, copyval);
			return true;
		}

		if(ncopyval != copyval) {
			//-- Different instances of copy -> changes
			les.propertyChange(pmm, source, copy, sourceval, copyval);
			return true;
		}

		//-- Same value.
		return false;
	}

	static private <I> boolean diffList(List<I> sourcel, List<I> copyl, Comparator<I> comparator) throws Exception {
		//-- First slice off common start and end;
		int send = sourcel.size();
		int cend = copyl.size();
		int sbeg = 0;
		int cbeg = 0;

		//-- Slice common beginning
		while(sbeg < send && cbeg < cend) {
			I so = sourcel.get(sbeg);
			I co = copyl.get(cbeg);
			if(0 != comparator.compare(so, co)) {
				break;
			}
			sbeg++;
			cbeg++;
		}

		//-- Slice common end
		while(send > sbeg && cend > cbeg) {
			I so = sourcel.get(send - 1);
			I co = copyl.get(cend - 1);
			if(0 != comparator.compare(so, co)) {
				break;
			}
			cend--;
			send--;
		}
		if(sbeg >= send && cbeg >= cend) {
			//-- Equal arrays- no changes.
			return false;
		}

		//-- Ouf.. We need to do the hard bits. Find the lcs and then render the edit as the delta.
		int m = (send - sbeg) + 1;
		int n = (cend - cbeg) + 1;
		int[][] car = new int[m][];
		for(int i = 0; i < m; i++) {
			car[i] = new int[n];
			car[i][0] = 0;
		}
		for(int i = 0; i < n; i++) {
			car[0][i] = 0;
		}

		for(int i = 1; i < m; i++) {
			for(int j = 1; j < n; j++) {
				I so = sourcel.get(sbeg + i - 1);
				I co = copyl.get(cbeg + j - 1);
				if(0 == comparator.compare(so, co)) {
					car[i][j] = car[i - 1][j - 1] + 1;				// Is length of previous subsequence + 1.
				} else {
					car[i][j] = Math.max(car[i][j - 1], car[i - 1][j]);	// Is length of the so-far longest subsequence
				}
			}
		}

		//-- Now: backtrack from the end to the start to render the delta. This creates the delta in the "reverse" order.

		List<String> tmp = new ArrayList<String>();
		for(int xxx = sourcel.size(); --xxx >= send;) {
			tmp.add("  " + sourcel.get(xxx) + " @" + xxx + " (e)");
		}

		int sindex = 0;
		int i = m - 1;
		int j = n - 1;
		while(j > 0 || i > 0) {
			if(i > 0 && j > 0 && 0 == comparator.compare(sourcel.get(sbeg + i - 1), copyl.get(cbeg + j - 1))) {
				tmp.add("  " + sourcel.get(sbeg + i - 1) + " @" + (sbeg + i - 1));

				i--;
				j--;

				//-- part of lcs - no delta
			} else if(j > 0 && (i == 0 || car[i][j - 1] >= car[i - 1][j])) {
				//-- Addition
				tmp.add("+ " + copyl.get(cbeg + j - 1) + " @" + (sbeg + i - 1));
				j--;
			} else if(i > 0 && (j == 0 || car[i][j - 1] < car[i - 1][j])) {
				//-- Deletion
				tmp.add("- " + sourcel.get(sbeg + i - 1) + " @" + (sbeg + i - 1));
				i--;
			}
		}

		//-- Add all unhandled @ start,
		for(i = sbeg; --i >= 0;) {
			tmp.add("  " + sourcel.get(i) + " @" + i);
		}

		Collections.reverse(tmp);
		for(String s : tmp)
			System.out.println(" " + s);

		return true;
	}


	public static void main(String[] args) throws Exception {
		List<String> a = Arrays.asList("A", "B", "B", "A", "D", "E", "A", "D");
		List<String> b = Arrays.asList("B", "A",      "A", "D", "E", "A", "D");
		Comparator<String> cs = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		};

		// abbadead: diff is -a @0 -b @1 +A @2
		// 01234567
		//  bbadead (-a @0)
		//   badead (-b @1)
		//

		diffList(a, b, cs);


	}


}
