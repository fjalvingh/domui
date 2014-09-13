package to.etc.domui.logic;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;

/**
 * EXPERIMENTAL Collects errors related to the model we're editing. This contains
 * both errors present in the UI state (validation errors, conversion errors) and
 * errors that are the result of running business logic.
 * <p>This is part of the UI-agnostic model. The UI contains methods to detect errors
 * present inside the error model "bound" to the UI, and it can then present those
 * errors any way it likes: either by adding the errors as errors to the components
 * and/or adding those errors to "error panels" shown on top of the screen.</p>
 *
 * <p>Errors are always registered as belonging to part of the <i>model</i>, they
 * are not registered as belonging to some "control". An error is registered as
 * reported on a (business object, property) pair or on a (business object) instance.
 * When errors are to be shown the UI code (or XML renderer code, or csv import code
 * - whatever is using the logic) will get the error and find the mapping between
 * the business object's fields and whatever output is produced. For an UI this
 * means that bindings between a control and a business object (+property) determine
 * that an error is to be reported on that control.</p>
 *
 * <p>Hence, the only way a control can "know" that it must show errors from something
 * is through it's bindings!</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 10, 2014
 */
final public class LogiErrors {
	/** Group for errors coming from binding. */
	@Nonnull
	static public final String G_BINDING = "binding";

	/**
	 * Maps [object, property] to a set of errors. If the property is not known it is mapped as null.
	 */
	@Nonnull
	private Map<Object, Map<PropertyMetaModel< ? >, Set<UIMessage>>> m_map = new HashMap<>();

	public LogiErrors() {}

	public <T> void addMessage(@Nonnull T businessObject, @Nonnull UIMessage message) {
		addMessage(businessObject, (PropertyMetaModel< ? >) null, message);
	}

	public <T> void addMessage(@Nonnull T businessObject, @Nonnull String property, @Nonnull UIMessage message) {
		addMessage(businessObject, MetaManager.getPropertyMeta(businessObject.getClass(), property), message);
	}

	public <T, V> void addMessage(@Nonnull T businessObject, @Nullable PropertyMetaModel<V> property, @Nonnull UIMessage message) {
		Map<PropertyMetaModel< ? >, Set<UIMessage>> mapOnProp = m_map.get(businessObject);
		if(mapOnProp == null) {
			mapOnProp = new HashMap<PropertyMetaModel< ? >, Set<UIMessage>>();
			m_map.put(businessObject, mapOnProp);
		}
		Set<UIMessage> messages = mapOnProp.get(property);
		if(messages == null) {
			messages = new HashSet<UIMessage>();
			mapOnProp.put(property, messages);
		}
		messages.add(message);
	}

	public <T> void clearMessage(@Nonnull T businessObject, @Nonnull String property, @Nonnull UIMessage msg) {
		clearMessage(businessObject, MetaManager.getPropertyMeta(businessObject.getClass(), property), msg);
	}

	public <T, V> void clearMessage(@Nonnull T businessObject, @Nullable PropertyMetaModel<V> property, @Nonnull UIMessage msg) {
		Map<PropertyMetaModel< ? >, Set<UIMessage>> mapOnProp = m_map.get(businessObject);
		if(mapOnProp != null) {
			Set<UIMessage> messages = mapOnProp.get(property);
			if(messages != null) {
				messages.remove(msg);
			}
		}
	}

	@Nonnull
	public <T> Set<UIMessage> getErrorsOn(@Nonnull T businessObject) {
		return getErrorsOn(businessObject, (PropertyMetaModel< ? >) null);
	}

	@Nonnull
	public <T, V> Set<UIMessage> getErrorsOn(@Nonnull T businessObject, @Nullable PropertyMetaModel<V> property) {
		Map<PropertyMetaModel< ? >, Set<UIMessage>> mapOnProp = m_map.get(businessObject);
		if(mapOnProp != null) {
			Set<UIMessage> messagesList = mapOnProp.get(property);
			if(messagesList != null) {
				return messagesList; 						//consider making copy list
			}
		}
		return Collections.EMPTY_SET;
	}

	@Nonnull
	public <T> Set<UIMessage> getErrorsOn(@Nonnull T businessObject, @Nonnull String property) {
		return getErrorsOn(businessObject, MetaManager.getPropertyMeta(businessObject.getClass(), property));
	}

	public boolean hasBindingErrors() {
		for(Map<PropertyMetaModel< ? >, Set<UIMessage>> m1 : m_map.values()) {
			for(Set<UIMessage> set : m1.values()) {
				for(UIMessage m : set) {
					if(G_BINDING.equals(m.getGroup()))
						return true;
				}
			}
		}
		return false;
	}

	public boolean hasErrors() {
		for(Map<PropertyMetaModel< ? >, Set<UIMessage>> m1 : m_map.values()) {
			for(Set<UIMessage> set : m1.values()) {
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
	public ErrorSet getErrorSet() {
		return new ErrorSet(m_map);
	}

	/**
	 * EXPERIMENTAL Delete all errors of a given group. Usually called by business logic
	 * that will recalculate it's errors.
	 * @param name
	 */
	public void clearErrorGroup(@Nonnull String name) {
		for(Map.Entry<Object, Map<PropertyMetaModel< ? >, Set<UIMessage>>> m1 : m_map.entrySet()) {
			for(Map.Entry<PropertyMetaModel< ? >, Set<UIMessage>> m2 : m1.getValue().entrySet()) {
				for(Iterator<UIMessage> it = m2.getValue().iterator(); it.hasNext();) {
					UIMessage m = it.next();
					if(name.equals(m.getGroup())) {
						it.remove();
					}
				}
			}
		}
	}
}
