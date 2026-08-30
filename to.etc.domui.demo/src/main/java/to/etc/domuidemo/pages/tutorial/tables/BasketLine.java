package to.etc.domuidemo.pages.tutorial.tables;

import to.etc.annotations.GenerateProperties;

import java.math.BigDecimal;

/**
 * A line in the basket of the "showing rows" tutorial: a plain object that has
 * never seen a database.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
@GenerateProperties
public class BasketLine {
	private String m_title;

	private int m_copies;

	private BigDecimal m_price;

	public BasketLine(String title, int copies, BigDecimal price) {
		m_title = title;
		m_copies = copies;
		m_price = price;
	}

	public String getTitle() {
		return m_title;
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public int getCopies() {
		return m_copies;
	}

	public void setCopies(int copies) {
		m_copies = copies;
	}

	public BigDecimal getPrice() {
		return m_price;
	}

	public void setPrice(BigDecimal price) {
		m_price = price;
	}
}
