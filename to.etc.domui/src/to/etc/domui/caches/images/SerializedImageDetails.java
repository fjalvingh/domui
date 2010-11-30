/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
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
