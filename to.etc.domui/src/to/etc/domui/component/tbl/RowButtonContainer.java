package to.etc.domui.component.tbl;

import to.etc.domui.component.layout.*;
import to.etc.domui.dom.html.*;

public class RowButtonContainer extends ButtonMakerBase {
	private NodeContainer m_into;

	private int m_count;

	public RowButtonContainer() {}
	public RowButtonContainer(NodeContainer into) {
		m_into = into;
	}

	public void setContainer(NodeContainer nc) {
		m_into = nc;
		m_count = 0;
	}

	@Override
	protected void addButton(NodeBase b) {
		if(m_count++ > 1)
			m_into.add(" ");
		m_into.add(b);
	}

	public void add(NodeBase other) {
		m_into.add(other);
	}
}
