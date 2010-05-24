package to.etc.domui.component.tbl;

import java.util.*;

/**
 * This uses a list as the base for the model. It handles all listener chores, and exposes
 * some extra methods for users to send model events.
 *
 * FIXME Needs to be generic
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public class DefaultTableModel<T> extends TableListModelBase<T> implements ITableModel<T> {
	private List<T> m_list;

	/*--------------------------------------------------------------*/
	/*	CODING:	Model event handling code.							*/
	/*--------------------------------------------------------------*/
	public DefaultTableModel() {
		this(new ArrayList<T>());
	}

	public DefaultTableModel(List<T> in) {
		m_list = new ArrayList<T>(in);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Model access										*/
	/*--------------------------------------------------------------*/

	@Override
	protected List<T> getList() throws Exception {
		return m_list;
	}

	@Override
	public void refresh() {}
}
