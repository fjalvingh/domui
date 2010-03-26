package to.etc.test.webapp.qsql;

import to.etc.webapp.qsql.*;

/**
 * The primary key for a DecadePaymentOrder. Must implement equals properly.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 25, 2010
 */
@QJdbcCompound
public class DecadePaymentOrderPK {
	private String m_administrationID;

	public static final String ADMINISTRATION_ID = "administrationID";

	private Long m_docnr;

	public static final String DOCNR = "docnr";

	@QJdbcColumn(name = "docnr", nullable = false)
	public Long getDocnr() {
		return m_docnr;
	}

	public void setDocnr(Long docnr) {
		m_docnr = docnr;
	}

	@QJdbcColumn(name = "admn_id", length = 5, nullable = true)
	public String getAdministrationID() {
		return m_administrationID;
	}

	public void setAdministrationID(String administrationID) {
		m_administrationID = administrationID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_administrationID == null) ? 0 : m_administrationID.hashCode());
		result = prime * result + ((m_docnr == null) ? 0 : m_docnr.hashCode());
		return result;
	}


	@Override
	public String toString() {
		return "DecadePaymentOrderPK [m_administrationID=" + m_administrationID + ", m_docnr=" + m_docnr + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		DecadePaymentOrderPK other = (DecadePaymentOrderPK) obj;
		if(m_administrationID == null) {
			if(other.m_administrationID != null)
				return false;
		} else if(!m_administrationID.equals(other.m_administrationID))
			return false;
		if(m_docnr == null) {
			if(other.m_docnr != null)
				return false;
		} else if(!m_docnr.equals(other.m_docnr))
			return false;
		return true;
	}

}
