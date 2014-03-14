package to.etc.domuidemo.db;

import java.util.*;

import javax.persistence.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.databinding.observables.*;

@Entity
@Table(name = "Employee")
@SequenceGenerator(name = "sq", sequenceName = "employee_sq")
@MetaObject(defaultColumns = {@MetaDisplayProperty(name = "firstName"), @MetaDisplayProperty(name = "lastName")})
public class Employee extends DbRecordBase<Long> implements IObservableEntity {
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
	@SequenceGenerator(name = "sq", sequenceName = "employee_sq")
	@Column(name = "EmployeeId", nullable = false, precision = 20)
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		Long oldv = getId();
		m_id = id;
		firePropertyChange("id", oldv, id);
	}

	@Column(name = "FirstName", length = 20, nullable = false)
	public String getFirstName() {
		return m_firstName;
	}

	@MetaSearch(order = 2)
	public void setFirstName(String firstName) {
		String oldv = getFirstName();
		m_firstName = firstName;
		firePropertyChange("firstName", oldv, firstName);
	}

	@MetaSearch(order = 1)
	@Column(name = "LastName", length = 20, nullable = false)
	public String getLastName() {
		return m_lastName;
	}

	public void setLastName(String lastName) {
		String oldv = getLastName();
		m_lastName = lastName;
		firePropertyChange("lastName", oldv, lastName);
	}

	@Column(name = "Address", length = 70, nullable = true)
	public String getAddress() {
		return m_address;
	}

	public void setAddress(String address) {
		String oldv = getAddress();
		m_address = address;
		firePropertyChange("address", oldv, address);
	}

	@Column(name = "City", length = 40, nullable = true)
	public String getCity() {
		return m_city;
	}

	public void setCity(String city) {
		String oldv = getCity();
		m_city = city;
		firePropertyChange("city", oldv, city);
	}

	@Column(name = "State", length = 40, nullable = true)
	public String getState() {
		return m_state;
	}

	public void setState(String state) {
		String oldv = getState();
		m_state = state;
		firePropertyChange("state", oldv, state);
	}

	@Column(name = "Country", length = 40, nullable = true)
	public String getCountry() {
		return m_Country;
	}

	public void setCountry(String country) {
		String oldv = getCountry();
		m_Country = country;
		firePropertyChange("country", oldv, country);
	}

	@Column(name = "PostalCode", length = 10, nullable = true)
	public String getPostalCode() {
		return m_postalCode;
	}

	public void setPostalCode(String postalCode) {
		String oldv = getPostalCode();
		m_postalCode = postalCode;
		firePropertyChange("postalCode", oldv, postalCode);
	}

	@Column(name = "Phone", length = 24, nullable = true)
	public String getPhone() {
		return m_phone;
	}

	public void setPhone(String phone) {
		String oldv = getPhone();
		m_phone = phone;
		firePropertyChange("phone", oldv, phone);
	}

	@Column(name = "Fax", length = 24, nullable = true)
	public String getFax() {
		return m_fax;
	}

	public void setFax(String fax) {
		String oldv = getFax();
		m_fax = fax;
		firePropertyChange("fax", oldv, fax);
	}

	@Column(name = "Email", length = 60, nullable = false)
	public String getEmail() {
		return m_email;
	}

	public void setEmail(String email) {
		String oldv = getEmail();
		m_email = email;
		firePropertyChange("email", oldv, email);
	}

	@Column(name = "Title", length = 30, nullable = true)
	public String getTitle() {
		return m_title;
	}

	public void setTitle(String title) {
		String oldv = getTitle();
		m_title = title;
		firePropertyChange("title", oldv, title);
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "ReportsTo")
	public Employee getReportsTo() {
		return m_reportsTo;
	}

	public void setReportsTo(Employee reportsTo) {
		Employee oldv = getReportsTo();
		m_reportsTo = reportsTo;
		firePropertyChange("reportsTo", oldv, reportsTo);
	}

	@Column(name = "BirthDate", nullable = true)
	@Temporal(TemporalType.DATE)
	public Date getBirthDate() {
		return m_birthDate;
	}

	public void setBirthDate(Date birthDate) {
		Date oldv = getBirthDate();
		m_birthDate = birthDate;
		firePropertyChange("birthDate", oldv, birthDate);
	}

	@Column(name = "HireDate", nullable = true)
	@Temporal(TemporalType.DATE)
	public Date getDateHired() {
		return m_dateHired;
	}

	public void setDateHired(Date dateHired) {
		Date oldv = getDateHired();
		m_dateHired = dateHired;
		firePropertyChange("dateHired", oldv, dateHired);
	}
}
