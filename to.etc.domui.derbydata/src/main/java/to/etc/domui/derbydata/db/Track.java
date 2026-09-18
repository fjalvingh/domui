package to.etc.domui.derbydata.db;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import to.etc.domui.component.meta.MetaDisplayProperty;
import to.etc.domui.component.meta.MetaObject;
import to.etc.domui.component.meta.MetaProperty;
import to.etc.domui.component.meta.MetaSearchItem;
import to.etc.domui.component.meta.NumericPresentation;
import to.etc.domui.converter.MsDurationConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * A single track on a CD.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 20, 2010
 */
@MetaObject(defaultColumns = {
	@MetaDisplayProperty(name = "name", displayLength = 30)
	, @MetaDisplayProperty(name = "milliseconds", displayLength = 12)
	, @MetaDisplayProperty(name = "unitPrice", displayLength = 8)
	, @MetaDisplayProperty(name = "album.title", displayLength = 20)
	, @MetaDisplayProperty(name = "album.artist.name", displayLength = 40)
}, defaultSortColumn = "name", searchProperties = {
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq")
	@Column(name = "TrackId", precision = 20, nullable = false)
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		m_id = id;
	}

	@Column(name = "Name", length = 200, nullable = false)
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

	@MetaProperty(converterClass = MsDurationConverter.class)
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

	@MetaProperty(numericPresentation = NumericPresentation.MONEY_FULL)
	@Column(name = "UnitPrice", precision = 10, scale = 2, nullable = false)
	public BigDecimal getUnitPrice() {
		return m_unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		m_unitPrice = unitPrice;
	}
}
