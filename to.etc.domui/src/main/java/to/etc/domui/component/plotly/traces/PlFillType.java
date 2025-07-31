package to.etc.domui.component.plotly.traces;

public enum PlFillType {
	none(null),
	toZeroY("tozeroy"),
	toNextY("tonexty"),
	;

	private String m_value;

	PlFillType(String value) {
		m_value = value;
	}

	public String getValue() {
		return m_value;
	}
}
