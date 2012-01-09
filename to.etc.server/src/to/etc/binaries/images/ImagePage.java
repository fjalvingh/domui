package to.etc.binaries.images;

public class ImagePage {
	private int		m_pageNumber;

	/** The original width of this page, in pixels. */
	private int		m_width;

	/** The original height of this page, in pixels. */
	private int		m_height;

	private boolean	m_bitmap;

	private String	m_type;

	public ImagePage(int pageNumber, int width, int height, boolean bitmap) {
		m_pageNumber = pageNumber;
		m_width = width;
		m_height = height;
		m_bitmap = bitmap;
	}

	public boolean isBitmap() {
		return m_bitmap;
	}

	public int getHeight() {
		return m_height;
	}

	public int getPageNumber() {
		return m_pageNumber;
	}

	public int getWidth() {
		return m_width;
	}

	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		m_type = type;
	}
}
