package to.etc.domui.util.images.machines;

import java.io.*;

public class OriginalImagePage implements Serializable {
	private int m_pageNumber;

	/** The original width of this page, in pixels. */
	private int m_width;

	/** The original height of this page, in pixels. */
	private int m_height;

	private boolean m_bitmap;

	private String m_type;

	private String m_mimeType;

	public OriginalImagePage(int pageNumber, int width, int height, String mime, String type, boolean bitmap) {
		m_pageNumber = pageNumber;
		m_width = width;
		m_height = height;
		m_bitmap = bitmap;
		m_mimeType = mime;
		m_type = type;
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

	public String getMimeType() {
		return m_mimeType;
	}
}
