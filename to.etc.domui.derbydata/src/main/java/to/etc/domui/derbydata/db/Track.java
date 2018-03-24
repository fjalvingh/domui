package to.etc.domui.derbydata.db;

import to.etc.domui.component.meta.MetaDisplayProperty;
import to.etc.domui.component.meta.MetaObject;
import to.etc.domui.component.meta.MetaSearchItem;
import to.etc.domui.converter.MsDurationConverter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * A single track on a CD.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 20, 2010
 */
@MetaObject(defaultColumns = {
	@MetaDisplayProperty(name="name", displayLength = 30)
	, @MetaDisplayProperty(name = "milliseconds", converterClass = MsDurationConverter.class)
	, @MetaDisplayProperty(name = "album.title", displayLength = 20)
	, @MetaDisplayProperty(name = "album.artist.name", displayLength = 40)
}, searchProperties = {
	@MetaSearchItem(name = "name")
	, @MetaSearchItem(name = "album")
	, @MetaSearchItem(name = "album.artist")
}
)
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

	@Override
	@Id
	@SequenceGenerator(name = "sq", sequenceName = "track_sq", allocationSize = 1)
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
