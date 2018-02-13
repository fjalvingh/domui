package my.domui.app.core.db;

import org.hibernate.annotations.GenericGenerator;
import to.etc.webapp.query.IIdentifyable;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * This is an example of a base class that can be used for entities,
 * allowing all of them to exhibit certain basic properties and behavior.
 * This example also handles primary key generation using an UUID.
 */
@MappedSuperclass
abstract public class AbstractDbEntity implements IIdentifyable<String> {
	private String m_id;

	//private Date createdOn;
	//
	//private Date modifiedOn;
	//
	//private DbUser modifiedBy;
	//
	//private DbUser createdBy;
	static public final String pCREATEDON = "createdOn";

	static public final String pCREATEDBY = "createdBy";

	static public final String pMODIFIEDON = "modifiedOn";

	static public final String pMODIFIEDBY = "modifiedBy";

	@GeneratedValue(generator = "uuid3")
	@GenericGenerator(name = "uuid3", strategy = "my.domui.app.core.db.UUIDGenerator3")
	@Id
	@Column(name = "id", length = 32, nullable = false)
	@Nullable @Override public String getId() {
		return m_id;
	}

	public void setId(String id) {
		m_id = id;
	}

	//@Temporal(TemporalType.TIMESTAMP)
	//@Column(name = "created_on", length = 29)
	//public Date getCreatedOn() {
	//	return this.createdOn;
	//}
	//
	//public void setCreatedOn(Date createdOn) {
	//	this.createdOn = createdOn;
	//}
	//
	//@Temporal(TemporalType.TIMESTAMP)
	//@Column(name = "modified_on", length = 29)
	//public Date getModifiedOn() {
	//	return this.modifiedOn;
	//}
	//
	//public void setModifiedOn(Date modifiedOn) {
	//	this.modifiedOn = modifiedOn;
	//}
	//
	//@ManyToOne(fetch = FetchType.LAZY)
	//@JoinColumn(name = "modified_by")
	//public DbUser getModifiedBy() {
	//	return this.modifiedBy;
	//}
	//
	//public void setModifiedBy(DbUser modifiedBy) {
	//	this.modifiedBy = modifiedBy;
	//}
	//
	//
	//@ManyToOne(fetch = FetchType.LAZY)
	//@JoinColumn(name = "created_by")
	//public DbUser getCreatedBy() {
	//	return this.createdBy;
	//}
	//
	//public void setCreatedBy(DbUser createdBy) {
	//	this.createdBy = createdBy;
	//}
}
