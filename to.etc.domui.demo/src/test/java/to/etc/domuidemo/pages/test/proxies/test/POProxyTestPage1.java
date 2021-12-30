package to.etc.domuidemo.pages.test.proxies.test;

import to.etc.domui.webdriver.core.WebDriverConnector;
import to.etc.domui.webdriver.poproxies.CpNodeAsText;

public class POProxyTestPage1 extends POProxyTestPage1Base {
    private CpNodeAsText m_defaultButtonValue;
    private CpNodeAsText m_smallImgButtonValue;

    public POProxyTestPage1(WebDriverConnector connector) {
        super(connector);
        m_defaultButtonValue = new CpNodeAsText(connector, () -> WebDriverConnector.getTestIDSelector("defbtn_v"));
        m_smallImgButtonValue = new CpNodeAsText(connector, () -> WebDriverConnector.getTestIDSelector("sib_v"));
    }

    public CpNodeAsText getDefaultButtonValue() {
        return m_defaultButtonValue;
    }

    public CpNodeAsText getSmallImgButtonValue() {
        return m_smallImgButtonValue;
    }
}

