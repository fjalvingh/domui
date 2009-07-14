package to.etc.webapp.query;

import java.util.*;

/**
 * Some kind of selection item which is formed by using multiple sub
 * items (like numeric operations). This is currently unused as the main
 * persistence provider (Hibernate) does not implement this in any way.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 14, 2009
 */
public class QMultiSelection extends QSelectionItem {
	private QSelectionItem[]	m_itemList;

	protected QMultiSelection(QSelectionFunction function, QSelectionItem[] itemList) {
		super(function);
		m_itemList = itemList;
	}

	public QMultiSelection(QSelectionFunction function, Collection<QSelectionItem> list) {
		super(function);
		m_itemList = list.toArray(new QSelectionItem[list.size()]);
	}
	public QSelectionItem[] getItemList() {
		return m_itemList;
	}
}
