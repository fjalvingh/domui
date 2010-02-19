package to.etc.domui.dom.html;

import to.etc.domui.component.input.*;

/**
 * Generic representation of a control, having input and some state. This extends IFormControl but
 * with one more explicit rule: an IInputNode /always/ *is* a NodeBase, which does not have to hold
 * for IFormControl.
 *
 * FIXME This should probably be deprecated and replaced by IControl.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 25, 2009
 */
public interface IInputNode<T> extends IControl<T>, IBindable {
	/**
	 * Returns T if the control is currently in readonly mode. Controls that do not
	 * have a readonly ability (comboboxes) will use disabled as the readonly state.
	 * For those controls the readonly state mirrors the disabled state.
	 * @return
	 */
	boolean isReadOnly();

	/**
	 * Returns T if the control is currently in disabled. Controls that do not
	 * have a readonly ability (comboboxes) will use disabled as the readonly state.
	 * For those controls the readonly state mirrors the disabled state.
	 * @return
	 */
	boolean isDisabled();

	/**
	 * Returns T if this control is a mandatory input.
	 * @return
	 */
	boolean isMandatory();
}
