package to.etc.domui.derbydata.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import to.etc.domui.component.meta.MetaDisplayProperty;
import to.etc.domui.component.meta.MetaObject;
import to.etc.domui.component.meta.MetaSearch;
import to.etc.domui.component.meta.SearchPropertyType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Artist", indexes = {@Index(name = "artist_name_idx", columnList = "name")})
@SequenceGenerator(name = "sq", sequenceName = "artist_sq", allocationSize = 1)
@MetaObject(defaultColumns = {@MetaDisplayProperty(name = "name")}, defaultSortColumn = "name")
public class Artist extends DbRecordBase<Long> {
	private Long m_id;

	private String m_name;

	private List<Album> m_albumList = new ArrayList<Album>();

	@Override
	@Id
	@SequenceGenerator(name = "sq", sequenceName = "artist_sq", allocationSize = 1)
	@Column(name = "ArtistId", nullable = false, precision = 20)
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		m_id = id;
	}

	/**
	 * IMPORTANT: Keep SearchPropertyType.SEARCH_FIELD or JUnit tests will fail.
	 */
	@MetaSearch(order = 1, searchType = SearchPropertyType.SEARCH_FIELD)
	@Column(length = 120, nullable = false, unique = true)
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	@OneToMany(mappedBy = "artist", fetch = FetchType.LAZY)
	public List<Album> getAlbumList() {
		return m_albumList;
	}

	public void setAlbumList(List<Album> albumList) {
		m_albumList = albumList;
	}

	@Override public String toString() {
		return getName();
	}
}
