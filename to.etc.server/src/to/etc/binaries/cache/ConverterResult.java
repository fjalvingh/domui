package to.etc.binaries.cache;

public class ConverterResult {
	private int		m_width;

	private int		m_height;

	private String	m_mime;

	private String	m_type;

	public ConverterResult(String type, String mime, int width, int height) {
		m_type = type;
		m_mime = mime;
		m_width = width;
		m_height = height;
	}

	public int getWidth() {
		return m_width;
	}

	public int getHeight() {
		return m_height;
	}

	public String getMime() {
		return m_mime;
	}

	public String getType() {
		return m_type;
	}
}
