package to.etc.domui.component.tbl;

import java.util.*;

/**
 * DO NOT USE IF YOUR DATA CAN CHANGE AND YOU ARE NOT UPDATING THOSE CHANGES HERE!!
 * This model uses a list to populate a table. It is meant to be used <i>only</i> if
 * the resulting model is maintained by yourself.
 *
 * Model for list-based data. The actual list instance will be maintained on updates.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 6, 2009
 */
public class SimpleListModel<T> extends TableListModelBase<T> {
	private List<T> m_list;

	public SimpleListModel(List<T> list) {
		m_list = list;
	}

	@Override
	protected List<T> getList() throws Exception {
		return m_list;
	}
}
