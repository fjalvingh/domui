package to.etc.domuidemo.db;

import java.util.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.databinding.observables.*;

@Entity
@Table(name = "Artist")
@SequenceGenerator(name = "sq", sequenceName = "artist_sq")
@MetaObject(defaultColumns = {@MetaDisplayProperty(name = "name")})
public class Artist extends DbRecordBase<Long> implements IObservableEntity {
	private Long m_id;

	private String m_name;

	private List<Album> m_albumList = new ArrayList<Album>();

	@Override
	@Id
	@SequenceGenerator(name = "sq", sequenceName = "artist_sq")
	@Column(name = "ArtistId", nullable = false, precision = 20)
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		Long oldv = getId();
		m_id = id;
		firePropertyChange("id", oldv, id);
	}

	@Column(length = 120, nullable = false, unique = true)
	@Index(name = "Name")
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		String oldv = getName();
		m_name = name;
		firePropertyChange("name", oldv, name);
	}

	@OneToMany(mappedBy = "artist")
	public List<Album> getAlbumList() {
		return m_albumList;
	}

	public void setAlbumList(List<Album> albumList) {
		m_albumList = albumList;
	}
}
