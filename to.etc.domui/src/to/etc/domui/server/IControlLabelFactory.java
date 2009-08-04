package to.etc.domui.server;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;

/**
 * A silly thing which creates the default visible labels for
 * form generators.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 8, 2008
 */
public interface IControlLabelFactory {
	public Label createControlLabel(NodeBase control, String text, boolean editable, boolean mandatory, PropertyMetaModel pmm);
}
