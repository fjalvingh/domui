package to.etc.domui.caches.images;

import java.io.*;

public class SerializedImageDetails implements Serializable {
	/** The mime type of the object */
	private String m_mimeType;

	/** When paged, this contains the #of pages. Everything <= 1 is unpaged. */
	private int m_pageCount;

	private Serializable m_factoryInformation;

	private int m_pixelWidth, m_pixelHeight;

	private long m_versionLong;

	public SerializedImageDetails() {}
	public String getMimeType() {
		return m_mimeType;
	}

	public void setMimeType(String mimeType) {
		m_mimeType = mimeType;
	}

	public int getPageCount() {
		return m_pageCount;
	}

	public void setPageCount(int pageCount) {
		m_pageCount = pageCount;
	}

	public Serializable getFactoryInformation() {
		return m_factoryInformation;
	}

	public void setFactoryInformation(Serializable factoryInformation) {
		m_factoryInformation = factoryInformation;
	}

	public int getPixelWidth() {
		return m_pixelWidth;
	}

	public void setPixelWidth(int pixelWidth) {
		m_pixelWidth = pixelWidth;
	}

	public int getPixelHeight() {
		return m_pixelHeight;
	}

	public void setPixelHeight(int pixelHeight) {
		m_pixelHeight = pixelHeight;
	}

	public long getVersionLong() {
		return m_versionLong;
	}

	public void setVersionLong(long versionLong) {
		m_versionLong = versionLong;
	}
}
