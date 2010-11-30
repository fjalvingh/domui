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
