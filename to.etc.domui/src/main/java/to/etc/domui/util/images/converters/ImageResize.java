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

public class ImageResize implements IImageConversionSpecifier {
	private int m_width, m_height;

	private int m_filterSpec;

	private String m_targetMime;

	public ImageResize(int width, int height) {
		m_height = height;
		m_width = width;
	}

	public ImageResize(int width, int height, int filterspec) {
		m_height = height;
		m_width = width;
		m_filterSpec = filterspec;
	}

	public ImageResize(int width, int height, int filterspec, String targetMime) {
		m_height = height;
		m_width = width;
		m_filterSpec = filterspec;
		m_targetMime = targetMime;
	}

	public ImageResize(int width, int height, String targetMime) {
		m_height = height;
		m_width = width;
		m_targetMime = targetMime;
	}

	public int getWidth() {
		return m_width;
	}

	public int getHeight() {
		return m_height;
	}

	public int getFilterSpec() {
		return m_filterSpec;
	}

	@Override
	public String getConversionKey() {
		return "rsz-" + m_width + "x" + m_height + "$" + m_filterSpec;
	}

	public String getTargetMime() {
		return m_targetMime;
	}

	@Override
	public String toString() {
		return "ImageResize[" + getWidth() + "x" + getHeight() + ", mime=" + getTargetMime() + ", filterspec=" + getFilterSpec() + "]";
	}
}
