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
import java.util.*;

import javax.annotation.*;

/**
 * The decoded data for an <i>original</i> image.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 10, 2008
 */
final public class ImageInfo implements Serializable {
	/** The mime type of the original image, if known */
	private String m_mime;

	/** The type name of the file, if known (identified by the 'file' command, if available) */
	final private String m_typeName;

	/** If false this image format cannot be converted, and can only be downloaded. This is usually an indication that identify failed. */
	final private boolean m_convertable;

	/** The decoded list of per-page information, if available. */
	@Nonnull
	final private List<OriginalImagePage> m_pageList;

	public ImageInfo(String mime, String typeName, boolean convertible, @Nonnull List<OriginalImagePage> pageList) {
		m_mime = mime;
		m_pageList = Collections.unmodifiableList(pageList);
		m_convertable = convertible;
		m_typeName = typeName;
	}

	public String getMime() {
		return m_mime;
	}

	public void setMime(String mime) {
		m_mime = mime;
	}

	public String getTypeName() {
		return m_typeName;
	}

	public boolean isConvertable() {
		return m_convertable;
	}

	@Nonnull
	public List<OriginalImagePage> getPageList() {
		return m_pageList;
	}

	public int getPageCount() {
		return m_pageList == null ? -1 : m_pageList.size();
	}

	@Nonnull
	public OriginalImagePage getPage(int ix) {
		if(m_pageList == null)
			throw new IllegalStateException("The page list is null?");
		OriginalImagePage ip = m_pageList.get(ix);
		if(null == ip)
			throw new IllegalStateException("Page " + ix + " is null in page list??");
		return ip;
	}
}
