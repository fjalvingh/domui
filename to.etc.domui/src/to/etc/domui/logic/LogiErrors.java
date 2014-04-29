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
	@Nonnull
	private Map<Object, Map<PropertyMetaModel< ? >, Set<UIMessage>>> m_map = new HashMap<>();

	@Nonnull
	private Map<Object, Set<UIMessage>> m_mapGlobals = new HashMap<>();

	public LogiErrors() {}

	public <T> void addMessage(@Nonnull T businessObject, @Nonnull UIMessage message) {
		Set<UIMessage> messages = m_mapGlobals.get(businessObject);
		if(messages == null) {
			messages = new HashSet<UIMessage>();
			m_mapGlobals.put(businessObject, messages);
		}
		messages.add(message);
	}

	public <T> void addMessage(@Nonnull T businessObject, @Nonnull String property, @Nonnull UIMessage message) {
		addMessage(businessObject, MetaManager.getPropertyMeta(businessObject.getClass(), property), message);
	}

	public <T, V> void addMessage(@Nonnull T businessObject, @Nonnull PropertyMetaModel<V> property, @Nonnull UIMessage message) {
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

	public <T, V> void clearMessage(@Nonnull T businessObject, @Nonnull PropertyMetaModel<V> property, @Nonnull UIMessage msg) {
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
		Set<UIMessage> messagesList = m_mapGlobals.get(businessObject);
		if(messagesList != null) {
			return Collections.unmodifiableSet(messagesList);
		}
		return Collections.EMPTY_SET;
	}

	@Nonnull
	public <T, V> Set<UIMessage> getErrorsOn(@Nonnull T businessObject, @Nonnull PropertyMetaModel<V> property) {
		Map<PropertyMetaModel< ? >, Set<UIMessage>> mapOnProp = m_map.get(businessObject);
		if(mapOnProp != null) {
			Set<UIMessage> messagesList = mapOnProp.get(property);
			if(messagesList != null) {
				return messagesList; //consider making copy list
			}
		}
		return Collections.EMPTY_SET;
	}

	@Nonnull
	public <T> Set<UIMessage> getErrorsOn(@Nonnull T businessObject, @Nonnull String property) {
		return getErrorsOn(businessObject, MetaManager.getPropertyMeta(businessObject.getClass(), property));
	}
}
