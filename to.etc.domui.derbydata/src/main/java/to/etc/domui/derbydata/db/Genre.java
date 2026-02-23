package to.etc.domui.derbydata.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "Genre")
public class Genre extends DbRecordBase<Long> {
	private Long m_id;

	private String m_name;

	@Id
	@SequenceGenerator(name = "sq", sequenceName = "genre_sq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq")
	@Column(name = "GenreId", precision = 20, nullable = false)
	@Override
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

	@Override public String toString() {
		return getName();
	}
}
