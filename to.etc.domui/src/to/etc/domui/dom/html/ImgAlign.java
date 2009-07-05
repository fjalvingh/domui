package to.etc.domui.dom.html;

public enum ImgAlign {
	TOP("top"), BOTTOM("bottom"), MIDDLE("middle"), LEFT("left"), DEFAULT("default"), TEXTTOP("texttop"), ABSMIDDLE("absmiddle"), BASELINE("baseline"), ABSBOTTOM("absbottom"), CENTER("center"), RIGHT(
		"right");

	private String m_txt;

	ImgAlign(String t) {
		m_txt = t;
	}

	public String getCode() {
		return m_txt;
	}
}
