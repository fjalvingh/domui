package to.etc.domui.dom.css;

public enum Overflow {
	VISIBLE("visible"), HIDDEN("hidden"), SCROLL("scroll"), AUTO("auto");

	private String m_txt;

	Overflow(String txt) {
		m_txt = txt;
	}

	@Override
	public String toString() {
		return m_txt;
	}
}
