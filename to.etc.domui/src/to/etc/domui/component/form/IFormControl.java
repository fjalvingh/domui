package to.etc.domui.component.form;

import to.etc.domui.dom.html.*;

/**
 * Generic control interface for controls generated from a factory.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 18, 2009
 */
public interface IFormControl {
	/**
	 * Set a new value into the control.
	 * @param value
	 */
	void setValue(Object value);

	/**
	 * Return the "current" value from the control.
	 */
	Object getValue();

	/**
	 * Set a listener to be called when this control's value changes.
	 * @param listener
	 */
	void setOnValueChanged(IValueChanged<NodeBase, Object> listener);
}
