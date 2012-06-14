package to.etc.domuidemo.db;

import javax.persistence.*;

@Entity
@Table(name = "Album")
public class Album extends DbRecordBase<Long> {
	private Long m_id;

	private String m_title;

	private Artist m_artist;

	@Override
	@Id
	@SequenceGenerator(name = "sq", sequenceName = "album_sq")
	@Column(name = "AlbumId", precision = 20, nullable = false)
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		m_id = id;
	}

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
}
