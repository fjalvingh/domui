package to.etc.domui.derbydata.db;

import to.etc.domui.component.meta.MetaDisplayProperty;
import to.etc.domui.component.meta.MetaObject;
import to.etc.domui.component.meta.MetaSearch;
import to.etc.domui.component.meta.SearchPropertyType;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Album")
@MetaObject(defaultColumns = {@MetaDisplayProperty(name = "title")}, defaultSortColumn = "name")
public class Album extends DbRecordBase<Long> {
	private Long m_id;

	private String m_title;

	private Artist m_artist;

	@Nonnull
	private List<Track> m_trackList = new ArrayList<Track>();

	@Override
	@Id
	@SequenceGenerator(name = "sq", sequenceName = "album_sq", allocationSize = 1)
	@Column(name = "AlbumId", precision = 20, nullable = false)
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		m_id = id;
	}

	/**
	 * IMPORTANT: Keep SearchPropertyType.BOTH or JUnit tests will fail.
	 * @return
	 */
	@MetaSearch(order = 1, searchType = SearchPropertyType.BOTH)
	@Column(name = "Title", length = 160, nullable = false)
	public String getTitle() {
		return m_title;
	}

	public void setTitle(String name) {
		m_title = name;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ArtistId")
	public Artist getArtist() {
		return m_artist;
	}

	public void setArtist(Artist primaryArtist) {
		m_artist = primaryArtist;
	}

	@Nonnull
	@OneToMany(mappedBy = "album", fetch = FetchType.LAZY)
	public List<Track> getTrackList() {
		return m_trackList;
	}

	public void setTrackList(@Nonnull List<Track> trackList) {
		m_trackList = trackList;
	}

	@Override public String toString() {
		return getTitle();
	}
}
