package to.etc.domui.parts;

import to.etc.domui.server.*;

import javax.annotation.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 28-5-17.
 */
@DefaultNonNull
public class ExtendedParameterInfoImpl extends ParameterInfoImpl implements IExtendedParameterInfo {
	private final BrowserVersion m_browser;

	private final String m_themeName;

	public ExtendedParameterInfoImpl(String themeName, BrowserVersion browser, String rurl, String queryString) {
		super(rurl, queryString);
		m_browser = browser;
		m_themeName = themeName;
	}

	//public ExtendedParameterInfoImpl(BrowserVersion browser, String in) {
	//	super(in);
	//	m_browser = browser;
	//	m_themeName = null;
	//}

	@Nullable @Override public String getThemeName() {
		return m_themeName;
	}

	@Override public BrowserVersion getBrowserVersion() {
		return m_browser;
	}

	@Override public String toString() {
		return getInputPath() + " theme=" + getThemeName() + " " + getParameterNames();
	}
}
