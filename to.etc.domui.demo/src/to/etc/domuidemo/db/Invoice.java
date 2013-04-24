package to.etc.domuidemo.db;

import java.math.*;
import java.util.*;

import javax.persistence.*;

@Entity
@Table(name = "Invoice")
@SequenceGenerator(name = "sq", sequenceName = "invoice_sq")
//@MetaObject(defaultColumns = {@MetaDisplayProperty(name = "name")})
public class Invoice extends DbRecordBase<Long> {
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
		m_id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "CustomerId")
	public Customer getCustomer() {
		return m_customer;
	}

	public void setCustomer(Customer customer) {
		m_customer = customer;
	}

	@Column(name = "InvoiceDate", nullable = false)
	@Temporal(TemporalType.DATE)
	public Date getInvoiceDate() {
		return m_invoiceDate;
	}

	public void setInvoiceDate(Date invoiceDate) {
		m_invoiceDate = invoiceDate;
	}

	@Column(name = "BillingAddress", length = 70, nullable = true)
	public String getBillingAddress() {
		return m_billingAddress;
	}

	public void setBillingAddress(String billingAddress) {
		m_billingAddress = billingAddress;
	}

	@Column(name = "BillingCity", length = 40, nullable = true)
	public String getBillingCity() {
		return m_billingCity;
	}

	public void setBillingCity(String billingCity) {
		m_billingCity = billingCity;
	}

	@Column(name = "BillingState", length = 40, nullable = true)
	public String getBillingState() {
		return m_billingState;
	}

	public void setBillingState(String billingState) {
		m_billingState = billingState;
	}

	@Column(name = "BillingCountry", length = 40, nullable = true)
	public String getBillingCountry() {
		return m_billingCountry;
	}

	public void setBillingCountry(String billingCountry) {
		m_billingCountry = billingCountry;
	}

	@Column(name = "BillingPostalCode", length = 10, nullable = true)
	public String getBillingPostalCode() {
		return m_billingPostalCode;
	}

	public void setBillingPostalCode(String billingPostalCode) {
		m_billingPostalCode = billingPostalCode;
	}

	@Column(name = "Total", precision = 10, scale = 2, nullable = false)
	public BigDecimal getTotal() {
		return m_total;
	}

	public void setTotal(BigDecimal total) {
		m_total = total;
	}

	@OneToMany(mappedBy = "invoice")
	public List<InvoiceLine> getInvoiceLines() {
		return m_invoiceLines;
	}

	public void setInvoiceLines(List<InvoiceLine> invoiceLines) {
		m_invoiceLines = invoiceLines;
	}
}
