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
	public LogiErrors() {}

	public <T> void message(@Nonnull T businessObject, @Nonnull UIMessage message) {}

	public <T> void message(@Nonnull T businessObject, @Nonnull String property, @Nonnull UIMessage message) {
		message(businessObject, MetaManager.getPropertyMeta(businessObject.getClass(), property), message);
	}

	public <T, V> void message(@Nonnull T businessObject, @Nonnull PropertyMetaModel<V> property, @Nonnull UIMessage message) {
	}

	@Nonnull
	public <T> List<UIMessage> getErrorsOn(@Nonnull T businessObject) {
		return Collections.EMPTY_LIST;
	}

	@Nonnull
	public <T, V> List<UIMessage> getErrorsOn(@Nonnull T businessObject, @Nonnull PropertyMetaModel<V> pmm) {
		return Collections.EMPTY_LIST;
	}

	@Nonnull
	public <T> List<UIMessage> getErrorsOn(@Nonnull T businessObject, @Nonnull String property) {
		return getErrorsOn(businessObject, MetaManager.getPropertyMeta(businessObject.getClass(), property));
	}
}
