package to.etc.domui.derbydata.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "InvoiceLine")
@SequenceGenerator(name = "sq", sequenceName = "invoiceline_sq", allocationSize = 1)
//@MetaObject(defaultColumns = {@MetaDisplayProperty(name = "name")})
public class InvoiceLine extends DbRecordBase<Long> {
	private Long m_id;

	private Invoice m_invoice;

	private Track m_track;

	private BigDecimal m_unitPrice;

	private int m_quantity;

	@Override
	@Id
	@SequenceGenerator(name = "sq", sequenceName = "invoiceline_sq", allocationSize = 1)
	@Column(name = "InvoiceLineId", nullable = false, precision = 20)
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		Long oldv = getId();
		m_id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "InvoiceId")
	public Invoice getInvoice() {
		return m_invoice;
	}

	public void setInvoice(Invoice invoice) {
		Invoice oldv = getInvoice();
		m_invoice = invoice;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "TrackId")
	public Track getTrack() {
		return m_track;
	}

	public void setTrack(Track track) {
		Track oldv = getTrack();
		m_track = track;
	}

	@Column(name = "UnitPrice", precision = 10, scale = 2, nullable = false)
	public BigDecimal getUnitPrice() {
		return m_unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		BigDecimal oldv = getUnitPrice();
		m_unitPrice = unitPrice;
	}

	@Column(name = "Quantity", precision = 10, scale = 0, nullable = false)
	public int getQuantity() {
		return m_quantity;
	}

	public void setQuantity(int quantity) {
		Integer oldv = Integer.valueOf(getQuantity());
		m_quantity = quantity;
	}
}
