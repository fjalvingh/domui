package to.etc.domui.derbydata.db;

import to.etc.domui.component.meta.MetaDisplayProperty;
import to.etc.domui.component.meta.MetaObject;
import to.etc.domui.component.meta.MetaSearchItem;
import to.etc.domui.component.meta.SearchPropertyType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "Customer")
@SequenceGenerator(name = "sq", sequenceName = "customer_sq", allocationSize = 1)
@MetaObject(
	defaultColumns = {@MetaDisplayProperty(name = "firstName"), @MetaDisplayProperty(name="lastName")}
,	searchProperties = {
		@MetaSearchItem(name="firstName", searchType=SearchPropertyType.BOTH)
	,	@MetaSearchItem(name="lastName", searchType=SearchPropertyType.BOTH)
	,	@MetaSearchItem(name="email")
	,	@MetaSearchItem(name="city")
	}	
)
public class Customer extends DbRecordBase<Long> {
	private Long m_id;

	private String m_firstName;

	private String m_lastName;

	private String m_company;

	private String m_address;

	private String m_city;

	private String m_state;

	private String m_Country;

	private String m_postalCode;

	private String m_phone;

	private String m_fax;

	private String m_email;

	private Employee m_supportRepresentative;

	@Override
	@Id
	@SequenceGenerator(name = "sq", sequenceName = "customer_sq", allocationSize = 1)
	@Column(name = "CustomerId", nullable = false, precision = 20)
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		m_id = id;
	}

	@Column(name = "FirstName", length = 40, nullable = false)
	public String getFirstName() {
		return m_firstName;
	}

	public void setFirstName(String firstName) {
		m_firstName = firstName;
	}

	@Column(name = "LastName", length = 20, nullable = false)
	public String getLastName() {
		return m_lastName;
	}

	public void setLastName(String lastName) {
		m_lastName = lastName;
	}

	@Column(name = "Company", length = 80, nullable = true)
	public String getCompany() {
		return m_company;
	}

	public void setCompany(String company) {
		m_company = company;
	}

	@Column(name = "Address", length = 70, nullable = true)
	public String getAddress() {
		return m_address;
	}

	public void setAddress(String address) {
		m_address = address;
	}

	@Column(name = "City", length = 40, nullable = true)
	public String getCity() {
		return m_city;
	}

	public void setCity(String city) {
		m_city = city;
	}

	@Column(name = "State", length = 40, nullable = true)
	public String getState() {
		return m_state;
	}

	public void setState(String state) {
		m_state = state;
	}

	@Column(name = "Country", length = 40, nullable = true)
	public String getCountry() {
		return m_Country;
	}

	public void setCountry(String country) {
		m_Country = country;
	}

	@Column(name = "PostalCode", length = 10, nullable = true)
	public String getPostalCode() {
		return m_postalCode;
	}

	public void setPostalCode(String postalCode) {
		m_postalCode = postalCode;
	}

	@Column(name = "Phone", length = 24, nullable = true)
	public String getPhone() {
		return m_phone;
	}

	public void setPhone(String phone) {
		m_phone = phone;
	}

	@Column(name = "Fax", length = 24, nullable = true)
	public String getFax() {
		return m_fax;
	}

	public void setFax(String fax) {
		m_fax = fax;
	}

	@Column(name = "Email", length = 60, nullable = false)
	public String getEmail() {
		return m_email;
	}

	public void setEmail(String email) {
		m_email = email;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "SupportRepId")
	public Employee getSupportRepresentative() {
		return m_supportRepresentative;
	}

	public void setSupportRepresentative(Employee supportRepresentative) {
		m_supportRepresentative = supportRepresentative;
	}
}
