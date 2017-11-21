package to.etc.domuidemo.sourceviewer;

import to.etc.domui.server.DomApplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceLocator {
	static private final SourceLocator m_instance = new SourceLocator();

	static private final SourceFile NOT_FOUND = new SourceFile(null, null, null);

	private Map<String, SourceFile> m_sourceMap = new HashMap<String, SourceFile>();

	static public SourceLocator getInstance() {
		return m_instance;
	}

	/**
	 * Try to locate the class/whatever resource by scanning the classpath in all the myriad ways. The name
	 * must be a path based name, meaning it must have slashes, not dots, as the directory separator, and
	 * it must contain the proper extension for the file to show. So a valid name would be
	 * <pre>
	 * to/etc/domui/server/DomApplication.java
	 * </pre>
	 *
	 * @param name
	 * @return
	 */
	public synchronized SourceFile findSource(String name) {
		SourceFile sf = m_sourceMap.get(name);
		if(sf == NOT_FOUND)
			return null;
		else if(sf != null)
			return sf;

		//-- Need to look it up. Is the source available on the classpath directly?
		InputStream is = null;
		try {
			is = getClass().getResourceAsStream("/" + name); // Try to open in classpath verbatim. This returns OK if source is packed inside jars
			if(is != null) {
				//-- YESS. Create a source that locates from the classpath.
				sf = new SourceFile(name, null, name) {
					@Override
					public InputStream getContent() throws IOException {
						return SourceLocator.class.getResourceAsStream("/" + getName()); // Try to open in classpath verbatim. This returns OK if source is packed inside jars
					}
				};
				m_sourceMap.put(name, sf);
				return sf;
			}

			//-- Not found in regular way..
			List<File> res = new ArrayList<File>();
			locateInWebpath(res, name);
			if(res.size() == 0) {
				m_sourceMap.put(name, NOT_FOUND);
				return null;
			} else if(res.size() == 1) {
				sf = new SourceFile(name, res.get(0), name);
				m_sourceMap.put(name, sf);
				return sf;
			} else {
				//-- Found multiple- treat as NOTFOUND for now.
				System.err.println("sourceViewer: got " + res.size() + " files named " + name + ": " + res);
				sf = new SourceFile(name, res.get(0), name);
				m_sourceMap.put(name, sf);
				return sf;
			}
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * This will usually locate source files when used from within an Eclipse workspace. It walks
	 * the webapp's path upwards trying to locate the file, and it takes some extra care to locate
	 * the file within any Eclipse projects found.
	 * @param name
	 * @return
	 */
	private void locateInWebpath(List<File> res, String name) {
		File webroot = DomApplication.get().getWebAppFileRoot();
		while(webroot != null) {
			scanAppPath(res, webroot, name);
			if(res.size() > 0)
				return;

			webroot = webroot.getParentFile();
			//			if(webroot == null)
			//				return null;
		}
	}

	private void scanAppPath(List<File> res, File root, String name) {
		//-- First try the file here,
		File tgt = new File(root, name);
		if(tgt.exists() && tgt.isFile()) {
			res.add(tgt);
			return;
		}

		//-- If this is an Eclipse workspace scan all projects below it.
		tgt = new File(root, ".metadata");
		if(tgt.exists() && tgt.isDirectory()) {
			scanEclipseWorkspace(res, root, name, 2);
		}
	}

	private void scanEclipseWorkspace(List<File> res, File root, String name, int depthleft) {
		if(depthleft < 0)
			return;
		File[] far = root.listFiles();
		for(File cur : far) {
			if(cur.isDirectory()) {
				//-- File can be found in here?
				File tgt = new File(cur, name);
				if(tgt.exists() && tgt.isFile()) {
					res.add(tgt);
				}

				//-- Scan below in dirs there too
				scanEclipseWorkspace(res, cur, name, depthleft - 1);
			}
		}
	}
}
