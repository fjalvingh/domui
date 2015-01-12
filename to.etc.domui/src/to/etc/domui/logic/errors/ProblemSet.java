package to.etc.domui.logic.errors;

import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;

import javax.annotation.*;
import java.util.*;

/**
 * EXPERIMENTAL - Do not use.
 *
 * Contains errors that (still) need to be reported in the UI.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 1, 2014
 */
final public class ProblemSet implements Iterable<ProblemInstance> {
	/**
	 * Maps [object, property] to a set of errors. If the property is not known it is mapped as null.
	 */
	@Nonnull
	final private Map<Object, Map<IValueAccessor< ? >, Set<ProblemInstance>>> m_map;

	ProblemSet(@Nonnull Map<Object, Map<PropertyMetaModel< ? >, Set<ProblemInstance>>> map) {
		m_map = new HashMap<>();

		for(Map.Entry<Object, Map<PropertyMetaModel< ? >, Set<ProblemInstance>>> m1 : map.entrySet()) {
			for(Map.Entry<PropertyMetaModel< ? >, Set<ProblemInstance>> m2 : m1.getValue().entrySet()) {
				for(ProblemInstance m : m2.getValue()) {
					//-- Register in new map
					Map<IValueAccessor< ? >, Set<ProblemInstance>> objectMap = m_map.get(m1.getKey());
					if(null == objectMap) {
						objectMap = new HashMap<>();
						m_map.put(m1.getKey(), objectMap);
					}

					Set<ProblemInstance> errSet = objectMap.get(m2.getKey());
					if(null == errSet) {
						errSet = new HashSet<>();
						objectMap.put(m2.getKey(), errSet);
					}

					errSet.add(m);
				}
			}
		}
	}

	@Nonnull
	public List<ProblemInstance> getErrorList() {
		List<ProblemInstance> res = new ArrayList<>();
		for(Map.Entry<Object, Map<IValueAccessor< ? >, Set<ProblemInstance>>> m1 : m_map.entrySet()) {
			for(Map.Entry<IValueAccessor< ? >, Set<ProblemInstance>> m2 : m1.getValue().entrySet()) {
				res.addAll(m2.getValue());
			}
		}
		return res;
	}

	@Nonnull
	@Override
	public Iterator<ProblemInstance> iterator() {
		return getErrorList().iterator();
	}

	/**
	 * Checks for errors for the specified instance/property. If found, removes them from the error set
	 * and returns them. Returns null if nothing is there.
	 * @param instance
	 * @param property
	 * @return
	 */
	public Set<ProblemInstance> remove(@Nonnull Object instance, @Nullable IValueAccessor< ? > property) {
		Map<IValueAccessor< ? >, Set<ProblemInstance>> objectMap = m_map.get(instance);
		if(null != objectMap) {
			Set<ProblemInstance> set = objectMap.remove(property);
			if(null != set)
				return set;

		}
		return Collections.EMPTY_SET;
	}
}
