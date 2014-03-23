package to.etc.domuidemo.db;

import java.math.*;

import javax.persistence.*;
import to.etc.domui.databinding.observables.*;

@Entity
@Table(name = "InvoiceLine")
@SequenceGenerator(name = "sq", sequenceName = "invoiceline_sq")
//@MetaObject(defaultColumns = {@MetaDisplayProperty(name = "name")})
public class InvoiceLine extends DbRecordBase<Long> implements IObservableEntity {
	private Long m_id;

	private Invoice m_invoice;

	private Track m_track;

	private BigDecimal m_unitPrice;

	private int m_quantity;

	@Override
	@Id
	@SequenceGenerator(name = "sq", sequenceName = "invoiceline_sq")
	@Column(name = "InvoiceLineId", nullable = false, precision = 20)
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		Long oldv = getId();
		m_id = id;
		firePropertyChange("id", oldv, id);
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "InvoiceId")
	public Invoice getInvoice() {
		return m_invoice;
	}

	public void setInvoice(Invoice invoice) {
		Invoice oldv = getInvoice();
		m_invoice = invoice;
		firePropertyChange("invoice", oldv, invoice);
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "TrackId")
	public Track getTrack() {
		return m_track;
	}

	public void setTrack(Track track) {
		Track oldv = getTrack();
		m_track = track;
		firePropertyChange("track", oldv, track);
	}

	@Column(name = "UnitPrice", precision = 10, scale = 2, nullable = false)
	public BigDecimal getUnitPrice() {
		return m_unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		BigDecimal oldv = getUnitPrice();
		m_unitPrice = unitPrice;
		firePropertyChange("unitPrice", oldv, unitPrice);
	}

	@Column(name = "Quantity", precision = 10, scale = 0, nullable = false)
	public int getQuantity() {
		return m_quantity;
	}

	public void setQuantity(int quantity) {
		Integer oldv = Integer.valueOf(getQuantity());
		m_quantity = quantity;
		firePropertyChange("quantity", oldv, Integer.valueOf(quantity));
	}
}
