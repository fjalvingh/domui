package to.etc.domuidemo.db;

import javax.persistence.*;

@Entity
@Table(name = "Genre")
public class Genre {
	private Long m_id;

	private String m_name;

	@Id
	@SequenceGenerator(name = "sq", sequenceName = "genre_sq")
	@Column(name = "GenreId", precision = 20, nullable = false)
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
