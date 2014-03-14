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
package to.etc.domui.caches;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.parts.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.util.images.*;
import to.etc.util.*;

public class FileImageRetriever implements IImageRetriever {
	static private final long MIN_LIVE_TIME = 5 * 60 * 1000;

	static private final String KEY = "RqTf";

	static private class FileRef {
		public File m_file;

		public String m_mime;
		//		public String m_hash;

		public String m_key;

		public long m_validTill;

		public FileRef() {
		}
	}

	/**
	 * Creates a safe file reference. The key is stored in the session.
	 * @param what
	 * @return
	 */
	static public String createFileURL(File what, String mime, String... convs) {
		IRequestContext ctx = UIContext.getRequestContext();
		Map<String, FileRef> map = (Map<String, FileRef>) ctx.getSession().getAttribute(KEY);
		if(map == null) {
			map = new HashMap<String, FileRef>();
			ctx.getSession().setAttribute(KEY, map);
		}

		//-- Walk all files to remove expired ones and locate the needed one
		FileRef match = findAndClear(map, what);
		if(match != null)
			return CachedImagePart.getURL(KEY, match.m_key);

		//-- Create a new reference.
		String key = StringTool.generateGUID();
		FileRef fr = new FileRef();
		fr.m_key = key;
		fr.m_file = what;
		fr.m_mime = mime;
		long ts = System.currentTimeMillis();
		fr.m_validTill = ts + 2 * MIN_LIVE_TIME;
		map.put(key, fr);
		return CachedImagePart.getURL(KEY, key, convs);
	}

	static private FileRef findAndClear(Map<String, FileRef> map, File what) {
		long ts = System.currentTimeMillis();
		long ets = ts + MIN_LIVE_TIME;
		FileRef match = null;
		for(FileRef fr : new ArrayList<FileRef>(map.values())) {
			if(fr.m_file.equals(what)) {
				if(fr.m_validTill > ets)
					match = fr;
			}
			if(fr.m_validTill < ts) {
				map.remove(fr.m_key);
			}
		}
		return match;

	}

	@Override
	public long getCheckInterval() {
		return Integer.MAX_VALUE;
	}

	@Override
	public @Nonnull String getRetrieverKey() {
		return KEY;
	}

	@Override
	public IImageReference loadImage(@Nonnull String key) throws Exception {
		IRequestContext ctx = UIContext.getRequestContext();
		Map<String, FileRef> map = (Map<String, FileRef>) ctx.getSession().getAttribute(KEY);
		if(map == null)
			return null;
		findAndClear(map, null); // Remove obsoleted
		FileRef ref = map.get(key);
		if(ref == null)
			return null;
		return new FileImageReference(ref.m_file, ref.m_mime);
	}
}
