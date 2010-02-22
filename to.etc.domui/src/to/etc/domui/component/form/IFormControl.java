package to.etc.domui.component.form;

import to.etc.domui.dom.html.*;

/**
 * Generic control interface for controls generated from a factory: since
 * nothing is known about them they are IControl&lt;Objects&gt;. WHICH
 * IS PLAIN WRONG!!!
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 18, 2009
 */
public interface IFormControl<T> extends IControl<T> {
}
