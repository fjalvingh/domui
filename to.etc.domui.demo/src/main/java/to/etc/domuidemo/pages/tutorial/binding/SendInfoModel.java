package to.etc.domuidemo.pages.tutorial.binding;

import to.etc.annotations.GenerateProperties;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.derbydata.db.Customer;

/**
 * The model behind BindPropertyPage: it holds the two choices, and it decides
 * whether the button may be pressed. No component appears in here at all.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
@GenerateProperties
public class SendInfoModel {
	private Artist m_artist;

	private Customer m_customer;

	public Artist getArtist() {
		return m_artist;
	}

	public void setArtist(Artist artist) {
		m_artist = artist;
	}

	public Customer getCustomer() {
		return m_customer;
	}

	public void setCustomer(Customer customer) {
		m_customer = customer;
	}

	/**
	 * There is nothing to send until both choices have been made.
	 */
	public boolean isSendDisabled() {
		return m_artist == null || m_customer == null;
	}
}
