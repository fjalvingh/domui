package to.etc.domui.derbydata.db;

import to.etc.domui.component.meta.MetaDisplayProperty;
import to.etc.domui.component.meta.MetaObject;
import to.etc.domui.component.meta.MetaSearch;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "Employee")
@SequenceGenerator(name = "sq", sequenceName = "employee_sq", allocationSize = 1)
@MetaObject(defaultColumns = {@MetaDisplayProperty(name = "firstName"), @MetaDisplayProperty(name = "lastName")})
public class Employee extends DbRecordBase<Long> {
	private Long m_id;

	private String m_firstName;

	private String m_lastName;

	private String m_title;

	private Employee m_reportsTo;

	private Date m_birthDate;

	private Date m_dateHired;

	private String m_address;

	private String m_city;

	private String m_state;

	private String m_Country;

	private String m_postalCode;

	private String m_phone;

	private String m_fax;

	private String m_email;

	@Override
	@Id
	@SequenceGenerator(name = "sq", sequenceName = "employee_sq", allocationSize = 1)
	@Column(name = "EmployeeId", nullable = false, precision = 20)
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		m_id = id;
	}

	@Column(name = "FirstName", length = 20, nullable = false)
	public String getFirstName() {
		return m_firstName;
	}

	@MetaSearch(order = 2)
	public void setFirstName(String firstName) {
		m_firstName = firstName;
	}

	@MetaSearch(order = 1)
	@Column(name = "LastName", length = 20, nullable = false)
	public String getLastName() {
		return m_lastName;
	}

	public void setLastName(String lastName) {
		m_lastName = lastName;
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

	@Column(name = "Title", length = 30, nullable = true)
	public String getTitle() {
		return m_title;
	}

	public void setTitle(String title) {
		m_title = title;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "ReportsTo")
	public Employee getReportsTo() {
		return m_reportsTo;
	}

	public void setReportsTo(Employee reportsTo) {
		m_reportsTo = reportsTo;
	}

	@Column(name = "BirthDate", nullable = true)
	@Temporal(TemporalType.DATE)
	public Date getBirthDate() {
		return m_birthDate;
	}

	public void setBirthDate(Date birthDate) {
		m_birthDate = birthDate;
	}

	@Column(name = "HireDate", nullable = true)
	@Temporal(TemporalType.DATE)
	public Date getDateHired() {
		return m_dateHired;
	}

	public void setDateHired(Date dateHired) {
		m_dateHired = dateHired;
	}
}
