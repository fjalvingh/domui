package my.domui.app.core.db;

import to.etc.domui.component.meta.MetaDisplayProperty;
import to.etc.domui.component.meta.MetaObject;
import to.etc.domui.component.meta.MetaSearchItem;
import to.etc.domui.component.meta.SearchPropertyType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "AU_USER")
@MetaObject(defaultColumns = { //
	@MetaDisplayProperty(name = DbUser.pFULLNAME), //
	@MetaDisplayProperty(name = DbUser.pEMAIL) //
},
	searchProperties = { //
		@MetaSearchItem(name = DbUser.pFULLNAME, order = 1, searchType = SearchPropertyType.BOTH), //
		@MetaSearchItem(name = DbUser.pEMAIL, order = 2, searchType = SearchPropertyType.BOTH), //
	})
public class DbUser extends AbstractDbEntity {
	private String m_email;

	private List<DbGroupMember> m_groupMemberList = new ArrayList<>();

	private String m_fullName;

	private Long m_groupid;

	private String m_lastName;

	private String m_ldapPassword;

	private String m_password;

	private String m_phoneNumber;


	public static final String pEMAIL = "email";

	public static final String pFULLNAME = "fullName";

	public static final String pID = "id";

	public static final String pPASSWORD = "password";

	public static final String pPHONENUMBER = "phoneNumber";

	@Column(name = "email", length = 512, nullable = false)
	public String getEmail() {
		return m_email;
	}

	public void setEmail(String value) {
		m_email = value;
	}

	@Column(name = "fullname", length = 128, nullable = false)
	public String getFullName() {
		return m_fullName;
	}

	public void setFullName(String value) {
		m_fullName = value;
	}

	@Column(name = "password", length = 512, nullable = false)
	public String getPassword() {
		return m_password;
	}

	public void setPassword(String value) {
		m_password = value;
	}

	@Column(name = "phonenumber", length = 512, nullable = true)
	public String getPhoneNumber() {
		return m_phoneNumber;
	}

	public void setPhoneNumber(String value) {
		m_phoneNumber = value;
	}

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	public List<DbGroupMember> getGroupMemberList() {
		return m_groupMemberList;
	}

	public void setGroupMemberList(List<DbGroupMember> groupMemberList) {
		m_groupMemberList = groupMemberList;
	}
}
