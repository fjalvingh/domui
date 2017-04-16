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

public class ImageManipulator {
	static private ImageHandler m_instance;

	synchronized public static ImageHandler getImageHandler() {
		if(m_instance == null) {
			m_instance = ImageMagicImageHandler.getInstance();
			if(m_instance == null)
				throw new RuntimeException("No ImageHandler current, and none of the defaults work.");
		}
		return m_instance;
	}

	static public ImageInfo identify(File input) throws Exception {
		return getImageHandler().identify(input);
	}
}
