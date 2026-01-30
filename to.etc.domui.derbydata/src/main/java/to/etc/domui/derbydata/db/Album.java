package to.etc.domui.derbydata.db;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.meta.MetaDisplayProperty;
import to.etc.domui.component.meta.MetaObject;
import to.etc.domui.component.meta.MetaSearch;
import to.etc.domui.component.meta.SearchPropertyType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Album")
@MetaObject(defaultColumns = {@MetaDisplayProperty(name = "title")}, defaultSortColumn = "name")
public class Album extends DbRecordBase<Long> {
	private Long m_id;

	private String m_title;

	private Artist m_artist;

	@NonNull
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

	@NonNull
	@OneToMany(mappedBy = "album", fetch = FetchType.LAZY)
	public List<Track> getTrackList() {
		return m_trackList;
	}

	public void setTrackList(@NonNull List<Track> trackList) {
		m_trackList = trackList;
	}

	@Override public String toString() {
		return getTitle();
	}
}
