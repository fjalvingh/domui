package to.etc.domui.dom.errors;

public enum MsgType {
	INFO(0), WARNING(1), ERROR(2), DIALOG(3);

	private int m_order;

	MsgType(int c) {
		m_order = c;
	}

	public int getOrder() {
		return m_order;
	}
}
