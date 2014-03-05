package to.etc.domuidemo.db;

import javax.persistence.*;
import to.etc.domui.databinding.observables.*;

@Entity
@Table(name = "Genre")
public class Genre extends DbRecordBase<Long> implements IObservableEntity {
	private Long m_id;

	private String m_name;

	@Id
	@SequenceGenerator(name = "sq", sequenceName = "genre_sq")
	@Column(name = "GenreId", precision = 20, nullable = false)
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		Long oldv = getId();
		m_id = id;
		firePropertyChange("id", oldv, id);
	}

	@Column(name = "Name", length = 120, nullable = false)
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		String oldv = getName();
		m_name = name;
		firePropertyChange("name", oldv, name);
	}
}
