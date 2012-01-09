package to.etc.smtp;

import to.etc.util.*;

/**
 * An email recipient.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 16, 2006
 */
final public class Address {
	/** The @ email address. */
	private String	m_email;

	/** If applicable a name. */
	private String	m_name;

	public Address(String email) {
		if(email == null || !StringTool.isValidEmail(email))
			throw new IllegalStateException("The string '" + email + "' is not a valid email address.");
		m_email = email;
	}

	public Address(String email, String name) {
		this(email);
		if(name.trim().length() == 0)
			throw new IllegalStateException("Invalid name");
		m_name = name;
	}

	public String getEmail() {
		return m_email;
	}

	public String getName() {
		return m_name;
	}
}
