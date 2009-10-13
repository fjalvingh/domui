package to.etc.domui.component.input;

import to.etc.domui.dom.html.*;

/**
 * EXPERIMENTAL - DO NOT USE.
 * This defines a control binding event interface which gets called when the control's
 * movement calls are used.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 13, 2009
 */
public interface IBindingListener<T extends NodeBase> {
	void moveControlToModel(T control) throws Exception;

	void moveModelToControl(T control) throws Exception;
}
