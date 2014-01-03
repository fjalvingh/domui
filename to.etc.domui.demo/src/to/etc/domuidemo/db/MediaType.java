package to.etc.domuidemo.db;

import javax.persistence.*;

@Entity
@Table(name = "MediaType")
public class MediaType extends DbRecordBase<Long> {
	private Long m_id;

	private String m_name;

	@Id
	@SequenceGenerator(name = "sq", sequenceName = "track_sq")
	@Column(name = "MediaTypeId", precision = 20, nullable = false)
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		m_id = id;
	}

	@Column(name = "Name", length = 120, nullable = false)
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}
}
