package to.etc.domuidemo.pages.binding.xxxmodel;

import to.etc.domui.databinding.observables.ObservableList;
import to.etc.domui.derbydata.db.Invoice;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-3-18.
 */
public class InvoiceEditModel {
	private final Invoice m_invoice;

	private final ObservableList<InvoiceLineModel> m_lines = new ObservableList<>();

	public InvoiceEditModel(Invoice invoice) {
		m_invoice = invoice;
		m_lines.addAll(
			m_invoice.getInvoiceLines().stream()
				.map(a -> new InvoiceLineModel(this, a))
				.collect(Collectors.toList())
		);
	}

	public BigDecimal getTotalPrice() {
		BigDecimal sum = BigDecimal.ZERO;
		for(InvoiceLineModel line : m_lines) {
			sum = sum.add(line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
		}
		return sum;
	}

	public Invoice getData() {
		return m_invoice;
	}

	public ObservableList<InvoiceLineModel> getLines() {
		return m_lines;
	}
}
