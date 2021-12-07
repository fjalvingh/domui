package to.etc.domui.component.plotly.traces;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
public enum PlSunburstTextInfo {
	Label, Text, Value, CurrentPath("current path"), PercentRoot("percent root"), PercentEntry("percent entry"), PercentParent("percent parent");

	private String m_code;

	PlSunburstTextInfo(String s) {
		m_code = s;
	}

	PlSunburstTextInfo() {
		m_code = name().toLowerCase();
	}

	public String getCode() {
		return m_code;
	}
}
