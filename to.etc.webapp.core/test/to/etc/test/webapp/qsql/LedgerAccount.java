package to.etc.test.webapp.qsql;

import to.etc.webapp.qsql.*;

/**
 * Ledger accounts from decade tables.
 *
 * @author <a href="mailto:dprica@execom.eu">Darko Prica</a>
 * Created on 24 Aug 2009
 */
@QJdbcTable(table = "v_dec_grootboekrekeningen")
public class LedgerAccount {
	private Long m_id;

	private String m_code;

	private String m_description;

	private String m_typeDescription;

	public LedgerAccount(String code, String description, String typeDescription) {
		this.m_code = code;
		this.m_description = description;
		this.m_typeDescription = typeDescription;
	}

	@QJdbcId
	@QJdbcColumn(name = "ID", nullable = false)
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		m_id = id;
	}

	@QJdbcColumn(name = "grbr_code", length = 5)
	public String getCode() {
		return m_code;
	}

	public void setCode(String code) {
		this.m_code = code;
	}

	@QJdbcColumn(name = "omschrijving", length = 128)
	public String getDescription() {
		return m_description;
	}

	public void setDescription(String description) {
		this.m_description = description;
	}

	@QJdbcColumn(name = "grbr_type_omschrijving")
	public String getTypeDescription() {
		return m_typeDescription;
	}

	public void setTypeDescription(String typeDescription) {
		this.m_typeDescription = typeDescription;
	}
}
