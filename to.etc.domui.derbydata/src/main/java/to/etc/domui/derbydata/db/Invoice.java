package to.etc.domui.derbydata.db;

import to.etc.domui.component.meta.MetaDisplayProperty;
import to.etc.domui.component.meta.MetaObject;
import to.etc.domui.component.meta.MetaSearchItem;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Invoice")
@SequenceGenerator(name = "sq", sequenceName = "invoice_sq", allocationSize = 1)
@MetaObject(defaultColumns = {								// 20180203 Must have metadata for SearchPanel/LookupForm tests.
	@MetaDisplayProperty(name = "customer.lastName", displayLength = 20)
	, @MetaDisplayProperty(name = "customer.firstName", displayLength = 10)
	, @MetaDisplayProperty(name = "invoiceDate")
	, @MetaDisplayProperty(name = "billingAddress", displayLength = 20)
	, @MetaDisplayProperty(name = "billingCity", displayLength = 10)
	, @MetaDisplayProperty(name = "total", displayLength = 10)
}
, searchProperties = {
	@MetaSearchItem(name = "invoiceDate")
	, @MetaSearchItem(name = "billingCity")
	, @MetaSearchItem(name = "customer")
}
)
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
	@SequenceGenerator(name = "sq", sequenceName = "invoice_sq", allocationSize = 1)
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


