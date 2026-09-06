package to.etc.domuidemo.pages.test.uitest.test;

import javax.annotation.processing.Generated;
import to.etc.domui.webdriver.core.WebDriverConnector;
import to.etc.domui.webdriver.poproxies.AbstractCpPage;
import to.etc.domui.webdriver.poproxies.CpButton;
import to.etc.domui.webdriver.poproxies.CpComboFixed2;
import to.etc.domui.webdriver.poproxies.CpText2;
import to.etc.domuidemo.pages.test.uitest.OrderEntryTestPage;

@Generated("Generated on Sun Sep 06 12:13:35 CEST 2026")
public class POOrderEntryTestPageBase extends AbstractCpPage<OrderEntryTestPage> {
    private POOrderEntryTestPageBasket m_basket;
    
    private CpButton m_buttonClear;
    
    private CpText2 m_copies;
    
    private CpText2 m_customer;
    
    private CpComboFixed2 m_shipping;
    
    private CpButton m_sibReloadThePageFully;
    
    
    public POOrderEntryTestPageBase(WebDriverConnector connector) {
        super(connector, to.etc.domuidemo.pages.test.uitest.OrderEntryTestPage.class);
    }
    
    public POOrderEntryTestPageBasket basket() throws Exception {
        POOrderEntryTestPageBasket basket = m_basket;
        if(null == basket) {
            basket = new POOrderEntryTestPageBasket(this.wd(), () -> "*[testId='basket']");
            m_basket = basket;
        }
        return basket;
    }
    
    public CpButton buttonClear() throws Exception {
        CpButton buttonClear = m_buttonClear;
        if(null == buttonClear) {
            buttonClear = new CpButton(this.wd(), () -> "*[testId='button_Clear']");
            m_buttonClear = buttonClear;
        }
        return buttonClear;
    }
    
    public CpText2 copies() throws Exception {
        CpText2 copies = m_copies;
        if(null == copies) {
            copies = new CpText2(this.wd(), () -> "*[testId='copies']");
            m_copies = copies;
        }
        return copies;
    }
    
    public CpText2 customer() throws Exception {
        CpText2 customer = m_customer;
        if(null == customer) {
            customer = new CpText2(this.wd(), () -> "*[testId='customer']");
            m_customer = customer;
        }
        return customer;
    }
    
    public CpComboFixed2 shipping() throws Exception {
        CpComboFixed2 shipping = m_shipping;
        if(null == shipping) {
            shipping = new CpComboFixed2(this.wd(), () -> "*[testId='shipping']");
            m_shipping = shipping;
        }
        return shipping;
    }
    
    public CpButton sibReloadThePageFully() throws Exception {
        CpButton sibReloadThePageFully = m_sibReloadThePageFully;
        if(null == sibReloadThePageFully) {
            sibReloadThePageFully = new CpButton(this.wd(), () -> "*[testId='sib_Reload_the_page_fully']");
            m_sibReloadThePageFully = sibReloadThePageFully;
        }
        return sibReloadThePageFully;
    }
    
}

