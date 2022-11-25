package to.etc.domuidemo.pages.test.proxies.test;

import to.etc.domui.webdriver.core.WebDriverConnector;
import to.etc.domui.webdriver.poproxies.AbstractCpPage;
import to.etc.domui.webdriver.poproxies.CpButton;
import to.etc.domui.webdriver.poproxies.CpCheckbox;
import to.etc.domui.webdriver.poproxies.CpCheckboxButton;
import to.etc.domui.webdriver.poproxies.CpComboFixed;
import to.etc.domui.webdriver.poproxies.CpComboFixed2;
import to.etc.domui.webdriver.poproxies.CpRadioGroup;
import to.etc.domui.webdriver.poproxies.CpText;
import to.etc.domui.webdriver.poproxies.CpText2;
import to.etc.domuidemo.pages.test.proxies.ProxyTestPage1;
import to.etc.domuidemo.pages.test.proxies.ProxyTestPage1.MyValues;

import javax.annotation.processing.Generated;

@Generated("Generated on Thu Feb 03 14:26:45 CET 2022")
public class POProxyTestPage1Base extends AbstractCpPage<ProxyTestPage1> {
    private CpCheckboxButton m_cbb;

    private CpComboFixed m_cf;

    private CpComboFixed2 m_cf2;

    private CpCheckbox m_checkbox;

    private CpButton m_defbtn;

    private CpRadioGroup<MyValues> m_ragrou;

    private CpButton m_sib;

    private CpButton m_sibReloadThePageFully;

    private CpText m_text;

    private CpText2 m_text2;


    public POProxyTestPage1Base(WebDriverConnector connector) {
        super(connector, to.etc.domuidemo.pages.test.proxies.ProxyTestPage1.class);
    }

    public CpCheckboxButton cbb() throws Exception {
        CpCheckboxButton cbb = m_cbb;
        if(null == cbb) {
            cbb = new CpCheckboxButton(this.wd(), () -> "*[testId='cbb']");
            m_cbb = cbb;
        }
        return cbb;
    }

    public CpComboFixed cf() throws Exception {
        CpComboFixed cf = m_cf;
        if(null == cf) {
            cf = new CpComboFixed(this.wd(), () -> "*[testId='cf']");
            m_cf = cf;
        }
        return cf;
    }

    public CpComboFixed2 cf2() throws Exception {
        CpComboFixed2 cf2 = m_cf2;
        if(null == cf2) {
            cf2 = new CpComboFixed2(this.wd(), () -> "*[testId='cf2']");
            m_cf2 = cf2;
        }
        return cf2;
    }

    public CpCheckbox checkbox() throws Exception {
        CpCheckbox checkbox = m_checkbox;
        if(null == checkbox) {
            checkbox = new CpCheckbox(this.wd(), () -> "*[testId='checkbox']");
            m_checkbox = checkbox;
        }
        return checkbox;
    }

    public CpButton defbtn() throws Exception {
        CpButton defbtn = m_defbtn;
        if(null == defbtn) {
            defbtn = new CpButton(this.wd(), () -> "*[testId='defbtn']");
            m_defbtn = defbtn;
        }
        return defbtn;
    }

    public CpRadioGroup<MyValues> ragrou() throws Exception {
        CpRadioGroup<MyValues> ragrou = m_ragrou;
        if(null == ragrou) {
            ragrou = new CpRadioGroup<MyValues>(this.wd(), () -> "*[testId='ragrou']", MyValues.Is,"*[testId='rb-Is']",MyValues.King,"*[testId='rb-King']",MyValues.Kings,"*[testId='rb-Kings']",MyValues.My,"*[testId='rb-My']",MyValues.Name,"*[testId='rb-Name']",
                MyValues.Of,"*[testId='rb-Of']",MyValues.Ozymandias,"*[testId='rb-Ozymandias']");
            m_ragrou = ragrou;
        }
        return ragrou;
    }

    public CpButton sib() throws Exception {
        CpButton sib = m_sib;
        if(null == sib) {
            sib = new CpButton(this.wd(), () -> "*[testId='sib']");
            m_sib = sib;
        }
        return sib;
    }

    public CpButton sibReloadThePageFully() throws Exception {
        CpButton sibReloadThePageFully = m_sibReloadThePageFully;
        if(null == sibReloadThePageFully) {
            sibReloadThePageFully = new CpButton(this.wd(), () -> "*[testId='sib_Reload_the_page_fully']");
            m_sibReloadThePageFully = sibReloadThePageFully;
        }
        return sibReloadThePageFully;
    }

    public CpText text() throws Exception {
        CpText text = m_text;
        if(null == text) {
            text = new CpText(this.wd(), () -> "*[testId='text']");
            m_text = text;
        }
        return text;
    }

    public CpText2 text2() throws Exception {
        CpText2 text2 = m_text2;
        if(null == text2) {
            text2 = new CpText2(this.wd(), () -> "*[testId='text2']");
            m_text2 = text2;
        }
        return text2;
    }

}

