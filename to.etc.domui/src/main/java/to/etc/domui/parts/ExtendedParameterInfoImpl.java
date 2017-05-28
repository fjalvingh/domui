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

	public ExtendedParameterInfoImpl(BrowserVersion browser, String rurl, String queryString) {
		super(rurl, queryString);
		m_browser = browser;
	}

	public ExtendedParameterInfoImpl(BrowserVersion browser, String in) {
		super(in);
		m_browser = browser;
	}

	@Override public BrowserVersion getBrowserVersion() {
		return m_browser;
	}
}
