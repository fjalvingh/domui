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
package to.etc.domui.util.images.converters;

import java.io.*;
import java.util.*;

import to.etc.domui.util.images.machines.*;

public class ImageSpec {
	private File m_source;

	private ImageInfo m_data;

	public ImageSpec() {}

	public ImageSpec(File source, ImageInfo id) {
		m_source = source;
		m_data = id;
	}

	public ImageSpec(File source, String mime, int w, int h) {
		m_source = source;
		List<OriginalImagePage> l = new ArrayList<OriginalImagePage>(1);
		l.add(new OriginalImagePage(0, w, h, mime, null, false));
		m_data = new ImageInfo(mime, null, true, l);
	}

	public String getMime() {
		return m_data.getMime();
	}

	public File getSource() {
		return m_source;
	}

	public void setSource(File source) {
		m_source = source;
	}

	public ImageInfo getInfo() {
		return m_data;
	}
}
