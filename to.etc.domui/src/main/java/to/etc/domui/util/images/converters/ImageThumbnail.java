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

/**
 * A specialization indicating that the target conversion is a thumbnail instead of
 * a normal smaller image.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 8, 2008
 */
public class ImageThumbnail extends ImageResize {
	public ImageThumbnail(int height, int width, int filterspec, String targetMime) {
		super(height, width, filterspec, targetMime);
	}

	public ImageThumbnail(int height, int width, String targetMime) {
		super(height, width, targetMime);
	}

	public ImageThumbnail(int height, int width, int filterspec) {
		super(height, width, filterspec);
	}

	public ImageThumbnail(int height, int width) {
		super(height, width);
	}

	@Override
	public String toString() {
		return "ImageThumbnail[" + getWidth() + "x" + getHeight() + ", mime=" + getTargetMime() + ", filterspec=" + getFilterSpec() + "]";
	}
}
