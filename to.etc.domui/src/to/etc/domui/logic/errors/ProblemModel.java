package to.etc.domui.logic.errors;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;

public class ProblemModel {
	/**
	 * Maps [object, property] to a set of errors. If the property is not known it is mapped as null.
	 */
	@Nonnull
	private Map<Object, Map<PropertyMetaModel< ? >, Set<ProblemInstance>>> m_map = new HashMap<>();

	public ProblemModel() {}

	void addProblem(@Nonnull ProblemInstance message) {
		Map<PropertyMetaModel< ? >, Set<ProblemInstance>> mapOnProp = m_map.get(message.getInstance());
		if(mapOnProp == null) {
			mapOnProp = new HashMap<PropertyMetaModel< ? >, Set<ProblemInstance>>();
			m_map.put(message.getInstance(), mapOnProp);
		}
		Set<ProblemInstance> messages = mapOnProp.get(message.getProperty());
		if(messages == null) {
			messages = new HashSet<ProblemInstance>();
			mapOnProp.put(message.getProperty(), messages);
		}
		messages.add(message);
	}

	<T, P> void clear(@Nonnull Problem problem, @Nonnull T instance, @Nullable PropertyMetaModel<P> pmm) {
		Map<PropertyMetaModel< ? >, Set<ProblemInstance>> mapOnProp = m_map.get(instance);
		if(mapOnProp != null) {
			Set<ProblemInstance> messages = mapOnProp.get(pmm);
			if(messages != null) {
				for(ProblemInstance pi : messages) {
					if(pi.getProblem().equals(problem)) {
						messages.remove(pi);
						return;
					}
				}
			}
		}
	}

	@Nonnull
	public <T> Set<ProblemInstance> getErrorsOn(@Nonnull T businessObject) {
		return getErrorsOn(businessObject, (PropertyMetaModel< ? >) null);
	}

	@Nonnull
	public <T, V> Set<ProblemInstance> getErrorsOn(@Nonnull T businessObject, @Nullable PropertyMetaModel<V> property) {
		Map<PropertyMetaModel< ? >, Set<ProblemInstance>> mapOnProp = m_map.get(businessObject);
		if(mapOnProp != null) {
			Set<ProblemInstance> messagesList = mapOnProp.get(property);
			if(messagesList != null) {
				return messagesList; 						//consider making copy list
			}
		}
		return Collections.EMPTY_SET;
	}

	@Nonnull
	public <T> Set<ProblemInstance> getErrorsOn(@Nonnull T businessObject, @Nonnull String property) {
		return getErrorsOn(businessObject, MetaManager.getPropertyMeta(businessObject.getClass(), property));
	}

	public boolean hasErrors() {
		for(Map<PropertyMetaModel< ? >, Set<ProblemInstance>> m1 : m_map.values()) {
			for(Set<ProblemInstance> set : m1.values()) {
				if(set.size() > 0)
					return true;
			}
		}
		return false;
	}

//	@Nonnull
//	public List<LogicError> getErrorList() {
//		List<LogicError> res = new ArrayList<>();
//		for(Map.Entry<Object, Map<PropertyMetaModel< ? >, Set<UIMessage>>> m1 : m_map.entrySet()) {
//			for(Map.Entry<PropertyMetaModel<?>, Set<UIMessage>> m2: m1.getValue().entrySet()) {
//				for(UIMessage m : m2.getValue()) {
//					res.add(new LogicError(m, m1.getKey(), m2.getKey()));
//				}
//			}
//		}
//		return res;
//	}

	@Nonnull
	public ProblemSet getErrorSet() {
		return new ProblemSet(m_map);
	}

}
