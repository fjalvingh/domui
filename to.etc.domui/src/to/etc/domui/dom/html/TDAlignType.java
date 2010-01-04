package to.etc.domui.dom.html;

public enum TDAlignType {
	NONE("none"), LEFT("left"), RIGHT("right"), CENTER("center"),

	/**
	 * Deprecated: allowed, but as usual does not render properly in IE.
	 */
	@Deprecated
	JUSTIFY("justify");

	private String m_code;

	TDAlignType(String code) {
		m_code = code;
	}

	public String getCode() {
		return m_code;
	}
}
