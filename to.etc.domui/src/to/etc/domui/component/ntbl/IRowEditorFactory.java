package to.etc.domui.component.ntbl;

import to.etc.domui.dom.html.*;

/**
 * Thingy to create a row editor to use within the {@link ExpandingEditTable}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 8, 2009
 */
public interface IRowEditorFactory<T, E extends NodeContainer & IEditor> {
	E createRowEditor(T instance) throws Exception;
}
