package to.etc.domuidemo.pages.test.uitest.test;

import java.util.function.Supplier;
import to.etc.domui.webdriver.core.WebDriverConnector;
import to.etc.domui.webdriver.poproxies.CpDataTable;
import to.etc.domui.webdriver.poproxies.CpDataTableColumn;

public class POOrderEntryTestPageBasket extends CpDataTable<POOrderEntryTestPageBaseBasketRow> {
    private CpDataTableColumn m_album;
    
    private CpDataTableColumn m_order;
    
    private CpDataTableColumn m_priceEach;
    
    
    public POOrderEntryTestPageBasket(WebDriverConnector connector, Supplier<String> selectorProvider) {
        super(connector, selectorProvider);
    }
    
    public CpDataTableColumn album() throws Exception {
        CpDataTableColumn album = m_album;
        if(null == album) {
            album = new CpDataTableColumn(this, 0);
            m_album = album;
        }
        return album;
    }
    
    public CpDataTableColumn order() throws Exception {
        CpDataTableColumn order = m_order;
        if(null == order) {
            order = new CpDataTableColumn(this, 2);
            m_order = order;
        }
        return order;
    }
    
    public CpDataTableColumn priceEach() throws Exception {
        CpDataTableColumn priceEach = m_priceEach;
        if(null == priceEach) {
            priceEach = new CpDataTableColumn(this, 1);
            m_priceEach = priceEach;
        }
        return priceEach;
    }
    
    public POOrderEntryTestPageBaseBasketRow row(int rowIndex) throws Exception {
        return new POOrderEntryTestPageBaseBasketRow(this, rowIndex);
    }
    
}

