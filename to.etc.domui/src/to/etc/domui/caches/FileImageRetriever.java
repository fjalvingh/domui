package to.etc.domui.caches;

import java.io.*;
import java.util.*;

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
	static public String createFileURL(File what, String mime) {
		IRequestContext ctx = PageContext.getRequestContext();
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
		return CachedImagePart.getURL(KEY, key);
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
	public String getRetrieverKey() {
		return KEY;
	}

	@Override
	public IImageReference loadImage(String key) throws Exception {
		IRequestContext ctx = PageContext.getRequestContext();
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
