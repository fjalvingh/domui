package to.etc.domuidemo.pages.binding.xxxmodel;

import to.etc.domui.derbydata.db.InvoiceLine;

import java.math.BigDecimal;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-3-18.
 */
public class InvoiceLineModel {
	final private InvoiceLine m_line;

	public InvoiceLineModel(InvoiceEditModel invoiceEditModel, InvoiceLine line) {
		m_line = line;
	}

	public Long getId() {
		return m_line.getId();
	}

	public BigDecimal getUnitPrice() {
		return m_line.getUnitPrice();
	}

	public int getQuantity() {
		return m_line.getQuantity();
	}

	public InvoiceLine getData() {
		return m_line;
	}
}
