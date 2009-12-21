package to.etc.domui.component.ntbl;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.html.*;

/**
 * Event handler for row-based editors.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 21, 2009
 */
public interface IRowEditorEvent<T, E extends NodeContainer> {
	/**
	 * Called after a row has been edited in an editable table component, when editing is (somehow) marked
	 * as complete. When called the editor's contents has been moved to the model by using the bindings. This method
	 * can be used to check the data for validity or to check for duplicates, for instance by using {@link MetaManager#hasDuplicates(java.util.List, Object, String)}.
	 *
	 * @param tablecomponent
	 * @param editor
	 * @param instance
	 * @return false to refuse the change.
	 * @throws Exception
	 */
	boolean onRowChanged(TableModelTableBase<T> tablecomponent, E editor, T instance) throws Exception;
}
