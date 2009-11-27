package to.etc.domui.component.tbl;

import to.etc.domui.dom.html.*;

/**
 * Temp thingy to create the header for a table. This is PENDING DELETE- DO NOT USE.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 2, 2008
 */
public class HeaderContainer {
	private DataTable m_table;

	private TR m_tr;

	public HeaderContainer(DataTable table) {
		m_table = table;
	}

	void setParent(TR p) {
		m_tr = p;
	}

	public DataTable getTable() {
		return m_table;
	}

	/**
	 * Adds a column to the table.
	 * @param columnContent
	 */
	public TH add(NodeBase columnContent) {
		TH td = new TH();
		m_tr.add(td);
		if(columnContent != null)
			td.add(columnContent);
		return td;
	}

	public TH add(String txt) {
		if(txt != null) {
			txt = txt.trim();
			if(txt.length() > 0) {
				return add(new TextNode(txt));
			}
		}

		//-- Just add an empty (for now) header and return it.
		return add((NodeBase) null);
	}
}
