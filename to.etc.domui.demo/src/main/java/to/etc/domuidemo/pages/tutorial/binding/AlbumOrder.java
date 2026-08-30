package to.etc.domuidemo.pages.tutorial.binding;

import to.etc.annotations.GenerateProperties;
import to.etc.domui.derbydata.db.Genre;

import java.math.BigDecimal;
import java.util.Date;

/**
 * The model the "data binding" tutorial pages edit: an order for a number of
 * albums. It is a plain class - @GenerateProperties is all it takes to get
 * AlbumOrder_ with a typed property per field.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
@GenerateProperties
public class AlbumOrder {
	private String m_customerName;

	private Genre m_genre;

	private Date m_deliveryDate;

	private Integer m_copies;

	private BigDecimal m_price;

	private OrderState m_state = OrderState.New;

	public String getCustomerName() {
		return m_customerName;
	}

	public void setCustomerName(String customerName) {
		m_customerName = customerName;
	}

	public Genre getGenre() {
		return m_genre;
	}

	public void setGenre(Genre genre) {
		m_genre = genre;
	}

	public Date getDeliveryDate() {
		return m_deliveryDate;
	}

	public void setDeliveryDate(Date deliveryDate) {
		m_deliveryDate = deliveryDate;
	}

	public Integer getCopies() {
		return m_copies;
	}

	public void setCopies(Integer copies) {
		m_copies = copies;
	}

	public BigDecimal getPrice() {
		return m_price;
	}

	public void setPrice(BigDecimal price) {
		m_price = price;
	}

	public OrderState getState() {
		return m_state;
	}

	public void setState(OrderState state) {
		m_state = state;
	}
}
