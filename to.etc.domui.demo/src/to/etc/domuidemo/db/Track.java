package to.etc.domuidemo.db;

import java.math.*;

import javax.persistence.*;

/**
 * A single track on a CD.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 20, 2010
 */
@Entity
@Table(name = "Track")
public class Track extends DbRecordBase<Long> {
	private Long m_id;

	private MediaType m_mediaType;

	private Genre m_genre;

	private Album m_album;

	/** The title of this track, overriding the song title */
	private String m_name;

	private String m_composer;

	private long m_milliseconds;

	private Integer m_bytes;

	private BigDecimal m_unitPrice;

	@Id
	@SequenceGenerator(name = "sq", sequenceName = "track_sq")
	@Column(name = "TrackId", precision = 20, nullable = false)
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		m_id = id;
	}

	@Column(name = "Name", length = 128, nullable = false)
	public String getName() {
		return m_name;
	}

	public void setName(String title) {
		m_name = title;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "GenreId")
	public Genre getGenre() {
		return m_genre;
	}

	public void setGenre(Genre song) {
		m_genre = song;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "AlbumId")
	public Album getAlbum() {
		return m_album;
	}

	public void setAlbum(Album album) {
		m_album = album;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "MediaTypeId")
	public MediaType getMediaType() {
		return m_mediaType;
	}

	public void setMediaType(MediaType mediaType) {
		m_mediaType = mediaType;
	}

	@Column(name = "Composer", length = 220, nullable = true)
	public String getComposer() {
		return m_composer;
	}

	public void setComposer(String composer) {
		m_composer = composer;
	}

	@Column(name = "Milliseconds", precision = 10, scale = 0, nullable = false)
	public long getMilliseconds() {
		return m_milliseconds;
	}

	public void setMilliseconds(long milliseconds) {
		m_milliseconds = milliseconds;
	}

	@Column(name = "bytes", precision = 10, scale = 0, nullable = true)
	public Integer getBytes() {
		return m_bytes;
	}

	public void setBytes(Integer bytes) {
		m_bytes = bytes;
	}

	@Column(name = "UnitPrice", precision = 10, scale = 2, nullable = false)
	public BigDecimal getUnitPrice() {
		return m_unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		m_unitPrice = unitPrice;
	}
}
