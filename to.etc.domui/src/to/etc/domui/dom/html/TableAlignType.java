package to.etc.domui.dom.html;

public enum TableAlignType {
	NONE("none"), LEFT("left"), CENTER("center"), RIGHT("right");

	private String m_txt;

	TableAlignType(String t) {
		m_txt = t;
	}

	public String getCode() {
		return m_txt;
	}
}
