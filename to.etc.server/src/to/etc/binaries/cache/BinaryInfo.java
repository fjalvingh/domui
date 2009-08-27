package to.etc.binaries.cache;

import to.etc.binaries.images.*;

/**
 * Descriptor for a given Binary. This is a cached unmutable entity
 * describing one of the binaries, either a copy or the original.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 10, 2006
 */
public class BinaryInfo implements ImageInfo {
	private long	m_id;

	private long	m_original;

	private int		m_width;

	private int		m_height;

	private int		m_size;

	private String	m_mimetype;

	private String	m_type;

	public BinaryInfo(long id, long original, int width, int height, int size, String mimetype, String type) {
		m_id = id;
		m_original = original;
		m_width = width;
		m_height = height;
		m_size = size;
		m_mimetype = mimetype.toLowerCase().intern();
		m_type = type.toLowerCase().intern();
	}

	synchronized public long getId() {
		return m_id;
	}

	public long getOriginal() {
		return m_original;
	}

	public int getWidth() {
		return m_width;
	}

	public int getHeight() {
		return m_height;
	}

	synchronized public int getSize() {
		return m_size;
	}

	public String getMime() {
		return m_mimetype;
	}

	public String getType() {
		return m_type;
	}

	public boolean isOriginal() {
		return m_original == -1;
	}

	public int getPage() {
		return 0;
	}

	synchronized void update(long id, int size) {
		m_id = id;
		m_size = size;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + m_height;
		result = prime * result + (int) (m_id ^ (m_id >>> 32));
		result = prime * result + ((m_mimetype == null) ? 0 : m_mimetype.hashCode());
		result = prime * result + (int) (m_original ^ (m_original >>> 32));
		result = prime * result + m_size;
		result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
		result = prime * result + m_width;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		BinaryInfo other = (BinaryInfo) obj;
		if(m_height != other.m_height)
			return false;
		if(m_id != other.m_id)
			return false;
		if(m_mimetype == null) {
			if(other.m_mimetype != null)
				return false;
		} else if(!m_mimetype.equals(other.m_mimetype))
			return false;
		if(m_original != other.m_original)
			return false;
		if(m_size != other.m_size)
			return false;
		if(m_type == null) {
			if(other.m_type != null)
				return false;
		} else if(!m_type.equals(other.m_type))
			return false;
		if(m_width != other.m_width)
			return false;
		return true;
	}


}
