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
}
