package to.etc.domuidemo.db;

import javax.annotation.*;
import javax.persistence.*;

import to.etc.domui.databinding.observables.*;

@Entity
@Table(name = "Album")
public class Album extends DbRecordBase<Long> implements IObservableEntity {
	private Long m_id;

	private String m_title;

	private Artist m_artist;

	@Nonnull
	private IObservableList<Track> m_trackList = new ObservableList<Track>();

	@Override
	@Id
	@SequenceGenerator(name = "sq", sequenceName = "album_sq")
	@Column(name = "AlbumId", precision = 20, nullable = false)
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		Long oldv = getId();
		m_id = id;
		firePropertyChange("id", oldv, id);
	}

	@Column(name = "Title", length = 160, nullable = false)
	public String getTitle() {
		return m_title;
	}

	public void setTitle(String name) {
		String oldv = getTitle();
		m_title = name;
		firePropertyChange("title", oldv, name);
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ArtistId")
	public Artist getArtist() {
		return m_artist;
	}

	public void setArtist(Artist primaryArtist) {
		Artist oldv = getArtist();
		m_artist = primaryArtist;
		firePropertyChange("artist", oldv, primaryArtist);
	}

	@Nonnull
	@OneToMany(mappedBy = "album", fetch = FetchType.LAZY)
	public IObservableList<Track> getTrackList() {
		return m_trackList;
	}

	public void setTrackList(@Nonnull IObservableList<Track> trackList) {
		m_trackList = trackList;
	}
}
