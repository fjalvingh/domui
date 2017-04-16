/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.test.webapp.qsql;

import to.etc.webapp.qsql.*;
import to.etc.webapp.query.*;

/**
 * Ledger accounts from decade tables.
 *
 * @author <a href="mailto:dprica@execom.eu">Darko Prica</a>
 * Created on 24 Aug 2009
 */
@QJdbcTable(table = "v_dec_grootboekrekeningen")
public class LedgerAccount implements IIdentifyable<Long> {
	private Long m_id;

	private String m_code;

	private String m_description;

	public static final String pDESCRIPTION = "description";

	private String m_typeDescription;

	public LedgerAccount() {}

	public LedgerAccount(String code, String description, String typeDescription) {
		m_code = code;
		m_description = description;
		m_typeDescription = typeDescription;
	}

	@Override
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
		m_code = code;
	}

	@QJdbcColumn(name = "omschrijving", length = 128)
	public String getDescription() {
		return m_description;
	}

	public void setDescription(String description) {
		m_description = description;
	}

	@QJdbcColumn(name = "grbr_type_omschrijving")
	public String getTypeDescription() {
		return m_typeDescription;
	}

	public void setTypeDescription(String typeDescription) {
		m_typeDescription = typeDescription;
	}
}
