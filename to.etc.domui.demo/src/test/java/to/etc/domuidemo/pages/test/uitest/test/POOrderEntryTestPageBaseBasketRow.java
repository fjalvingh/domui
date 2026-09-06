package to.etc.domuidemo.pages.test.uitest.test;

import to.etc.domui.webdriver.poproxies.CpDataTable;
import to.etc.domui.webdriver.poproxies.CpDataTableRowBase;
import to.etc.domui.webdriver.poproxies.CpDisplaySpan;
import to.etc.domui.webdriver.poproxies.CpLinkButton;

public class POOrderEntryTestPageBaseBasketRow extends CpDataTableRowBase {
    private CpDisplaySpan m_album;
    
    private CpLinkButton m_order;
    
    private CpDisplaySpan m_priceEach;
    
    
    public POOrderEntryTestPageBaseBasketRow(CpDataTable<POOrderEntryTestPageBaseBasketRow> dt, int rowIndex) {
        super(dt, rowIndex);
    }
    
    public CpDisplaySpan album() throws Exception {
        CpDisplaySpan album = m_album;
        if(null == album) {
            album = new CpDisplaySpan(this.wd(), () -> this.getCellComponentSelectorCss(0, "title"));
            m_album = album;
        }
        return album;
    }
    
    public CpLinkButton order() throws Exception {
        CpLinkButton order = m_order;
        if(null == order) {
            order = new CpLinkButton(this.wd(), () -> this.getCellComponentSelectorCss(2, "lbtn_Order"));
            m_order = order;
        }
        return order;
    }
    
    public CpDisplaySpan priceEach() throws Exception {
        CpDisplaySpan priceEach = m_priceEach;
        if(null == priceEach) {
            priceEach = new CpDisplaySpan(this.wd(), () -> this.getCellComponentSelectorCss(1, "price"));
            m_priceEach = priceEach;
        }
        return priceEach;
    }
    
}

