package to.etc.domui.component.tbl;

import java.util.*;

public abstract class SimpleKeyModel<T, P> extends TableModelBase<T> implements ITableModel<T> {

	private List<P> m_keyList;

	/*--------------------------------------------------------------*/
	/*	CODING:	Model event handling code.							*/
	/*--------------------------------------------------------------*/
	public SimpleKeyModel() {
		this(new HashSet<P>());
	}

	public SimpleKeyModel(Set<P> in) {
		m_keyList = new ArrayList<P>(in);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Model access										*/
	/*--------------------------------------------------------------*/

	public List<P> getKeyList() throws Exception {
		return m_keyList;
	}

	public abstract T getItemForKey(P key) throws Exception;

	@Override
	public T getItem(int ix) throws Exception {
		return getItemForKey(m_keyList.get(ix));
	}

	@Override
	public List<T> getItems(int start, int end) throws Exception {
		int size = getRows();
		if(start < 0)
			start = 0;
		if(end > size)
			end = size;
		if(start >= size || end <= 0 || start >= end)
			return Collections.EMPTY_LIST;
		List<T> res = new ArrayList<T>();
		for(int i = start; i < end; i++) {
			res.add(getItem(i));
		}
		return res;
	}

	@Override
	public int getRows() throws Exception {
		return m_keyList.size();
	}
}
