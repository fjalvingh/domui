package to.etc.domuidemo.db;

import java.math.*;
import java.util.*;

import javax.persistence.*;
import to.etc.domui.databinding.observables.*;

@Entity
@Table(name = "Invoice")
@SequenceGenerator(name = "sq", sequenceName = "invoice_sq")
//@MetaObject(defaultColumns = {@MetaDisplayProperty(name = "name")})
public class Invoice extends DbRecordBase<Long> implements IObservableEntity {
	private Long m_id;

	private Customer m_customer;

	private Date m_invoiceDate;

	private String m_billingAddress;

	private String m_billingCity;

	private String m_billingState;

	private String m_billingCountry;

	private String m_billingPostalCode;

	private BigDecimal m_total;

	private List<InvoiceLine> m_invoiceLines;


	@Override
	@Id
	@SequenceGenerator(name = "sq", sequenceName = "invoice_sq")
	@Column(name = "InvoiceId", nullable = false, precision = 20)
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		Long oldv = getId();
		m_id = id;
		firePropertyChange("id", oldv, id);
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "CustomerId")
	public Customer getCustomer() {
		return m_customer;
	}

	public void setCustomer(Customer customer) {
		Customer oldv = getCustomer();
		m_customer = customer;
		firePropertyChange("customer", oldv, customer);
	}

	@Column(name = "InvoiceDate", nullable = false)
	@Temporal(TemporalType.DATE)
	public Date getInvoiceDate() {
		return m_invoiceDate;
	}

	public void setInvoiceDate(Date invoiceDate) {
		Date oldv = getInvoiceDate();
		m_invoiceDate = invoiceDate;
		firePropertyChange("invoiceDate", oldv, invoiceDate);
	}

	@Column(name = "BillingAddress", length = 70, nullable = true)
	public String getBillingAddress() {
		return m_billingAddress;
	}

	public void setBillingAddress(String billingAddress) {
		String oldv = getBillingAddress();
		m_billingAddress = billingAddress;
		firePropertyChange("billingAddress", oldv, billingAddress);
	}

	@Column(name = "BillingCity", length = 40, nullable = true)
	public String getBillingCity() {
		return m_billingCity;
	}

	public void setBillingCity(String billingCity) {
		String oldv = getBillingCity();
		m_billingCity = billingCity;
		firePropertyChange("billingCity", oldv, billingCity);
	}

	@Column(name = "BillingState", length = 40, nullable = true)
	public String getBillingState() {
		return m_billingState;
	}

	public void setBillingState(String billingState) {
		String oldv = getBillingState();
		m_billingState = billingState;
		firePropertyChange("billingState", oldv, billingState);
	}

	@Column(name = "BillingCountry", length = 40, nullable = true)
	public String getBillingCountry() {
		return m_billingCountry;
	}

	public void setBillingCountry(String billingCountry) {
		String oldv = getBillingCountry();
		m_billingCountry = billingCountry;
		firePropertyChange("billingCountry", oldv, billingCountry);
	}

	@Column(name = "BillingPostalCode", length = 10, nullable = true)
	public String getBillingPostalCode() {
		return m_billingPostalCode;
	}

	public void setBillingPostalCode(String billingPostalCode) {
		String oldv = getBillingPostalCode();
		m_billingPostalCode = billingPostalCode;
		firePropertyChange("billingPostalCode", oldv, billingPostalCode);
	}

	@Column(name = "Total", precision = 10, scale = 2, nullable = false)
	public BigDecimal getTotal() {
		return m_total;
	}

	public void setTotal(BigDecimal total) {
		BigDecimal oldv = getTotal();
		m_total = total;
		firePropertyChange("total", oldv, total);
	}

	@OneToMany(mappedBy = "invoice")
	public List<InvoiceLine> getInvoiceLines() {
		return m_invoiceLines;
	}

	public void setInvoiceLines(List<InvoiceLine> invoiceLines) {
		m_invoiceLines = invoiceLines;
	}
}
