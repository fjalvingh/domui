package to.etc.domui.component.tbl;

import java.util.*;

/**
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
