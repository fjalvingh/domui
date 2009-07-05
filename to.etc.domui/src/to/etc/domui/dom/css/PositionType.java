package to.etc.domui.dom.css;

public enum PositionType {
	STATIC("static"), RELATIVE("relative"), ABSOLUTE("absolute"), FIXED("fixed");

	private String m_txt;

	PositionType(String t) {
		m_txt = t;
	}

	public String getTxt() {
		return m_txt;
	}

}
