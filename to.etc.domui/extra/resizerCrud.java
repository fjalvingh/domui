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
import java.io.*;
import java.util.*;

import to.etc.domui.util.images.*;
import to.etc.domui.util.images.cache.*;
import to.etc.domui.util.images.converters.*;
import to.etc.util.*;

	/**
	 * Main workhorse for converting images.
	 *
	 * @param irt
	 * @param cacheKey
	 * @param conversions
	 * @throws Exception
	 */
	synchronized void initializeConvertedInstance(final IImageRetriever irt, final Object cacheKey, final List<IImageConversionSpecifier> conversions) throws Exception {
		if(m_initialized)
			return;

		//-- Create a cache key & try to load off secondary cache quickly.
		StringBuilder sb = new StringBuilder(128);
		sb.append(m_imageRoot.getFilenameBase()); // Base of the image as cached file
		sb.append("-");
		sb.append(m_permutation);
		sb.append(".cf");
		try {
			if(loadCachedFile(sb.toString())) { // Try to load it using the file.
				m_initialized = true; // Mark as completed
				return;
			}
		} catch(Exception x) {
			//-- Load from file cache failed-> yecc. We must recreate the object.
		}

		//-- 2. We need to (re)create the object from it's source. So retrieve the original. DOUBLE LOCK ON different ImageInstance's, and RECURSIVE LOCK [ImageInstance->ImageCache]!!!
		ImageInstance original = getRoot().getCache().getOriginal(irt, cacheKey);

		//-- 2. Create the object using the permutator factories.
		File tmp = null;
		ImageConverterHelper ich = new ImageConverterHelper();
		try {
			//-- FIXME Write the original as a file (should be a cached file later on)
			tmp = File.createTempFile("imgorg", ".tmp");
			FileTool.save(tmp, original.getBuffers()); // Save original as a tempfile,
			ImageSpec sis = new ImageSpec(tmp, original.getImageData());
			ich.executeConversionChain(sis, conversions); // Execute the conversion chain

			sis = ich.getTarget(); // Result after completion.
			m_buffers = FileTool.loadByteBuffers(sis.getSource()); // Read result into buffer chain,
			m_size = (int) sis.getSource().length();
			m_imageData = sis.getData();
			m_initialized = true;
		} finally {
			try {
				if(tmp != null)
					tmp.delete();
			} catch(Exception x) {}
			ich.destroy();
		}
	}

	/**
	 * Tries to load the whole shebang from the file system.
	 * @param f
	 * @throws Exception
	 */
	private boolean loadCachedFile(final String key) throws Exception {
		File cacheFile = new File(m_imageRoot.getCache().getCacheDir(), key + ".cf");
		if(!cacheFile.exists())
			return false;

		//-- bla bla bla

		throw new IllegalStateException("Not implemented yet"); // FIXME Implement.
		//
		//		cacheFile.setLastModified(System.currentTimeMillis());	// Touch the file to indicate it's been used
		//		return true;
	}

