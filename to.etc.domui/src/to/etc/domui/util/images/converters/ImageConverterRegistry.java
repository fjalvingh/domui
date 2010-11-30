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

import javax.annotation.*;

import to.etc.domui.util.images.machines.*;

final public class ImageConverterRegistry {
	static private List<IImageConverter> m_list = new ArrayList<IImageConverter>();

	static private List<IImageIdentifier> m_identList = new ArrayList<IImageIdentifier>();

	private ImageConverterRegistry() {}

	static synchronized public void registerFactory(IImageConverter c) {
		m_list = new ArrayList<IImageConverter>(m_list); // Copy original
		m_list.add(c); // Append new one
	}

	static synchronized List<IImageConverter> getConverterList() {
		return m_list;
	}

	static synchronized public void registerIdentifier(IImageIdentifier c) {
		m_identList = new ArrayList<IImageIdentifier>(m_identList); // Copy original
		m_identList.add(c); // Append new one
	}

	public static synchronized List<IImageIdentifier> getIdentList() {
		return m_identList;
	}

	/**
	 * Select the best converter to use to handle the (first) conversions in the list.
	 * @param mime
	 * @param convs
	 * @return
	 */
	@Nonnull
	static public IImageConverter getBestConverter(String mime, List<IImageConversionSpecifier> convs) throws Exception {
		IImageConverter best = null;
		int bestscore = -1;
		for(IImageConverter ic : getConverterList()) {
			int score = ic.accepts(mime, convs);
			if(score > bestscore) {
				bestscore = score;
				best = ic;
			}
		}

		if(best == null)
			throw new IllegalStateException("No image converter known to convert a " + mime + " using " + convs.get(0));
		return best;
	}

	static public ImageInfo identify(String mime, File src) {
		for(IImageIdentifier ii : getIdentList()) {
			ImageInfo id = ii.identifyImage(src, mime);
			if(id != null)
				return id;
		}
		return null;
	}

	static {
		BitmapConverter bc = new BitmapConverter();
		registerFactory(bc);
		registerIdentifier(bc);
	}
}
