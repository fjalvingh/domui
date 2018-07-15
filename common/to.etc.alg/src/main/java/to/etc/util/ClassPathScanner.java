package to.etc.util;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This scans all parts of the classpath as passed, and calls user-supplied methods for the parts discovered. The idea
 * is that classpath-based resource discovery will be done only once for an application, by having everything register
 * their requirements on an instance of this class and then calling the scan method only once.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 11, 2013
 */
public class ClassPathScanner {
	static private ClassPathScanner	m_instance;

	private List<ClassLoader>		m_classLoaders	= new ArrayList<ClassLoader>();

	private List<URL>				m_urls			= new ArrayList<URL>();

	/**
	 * Notify event for a classpath .jar or directory.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Oct 11, 2013
	 */
	public interface IPathEntry {
		void foundPathEntry(@NonNull File classpathEntry) throws Exception;
	}

	/**
	 * Notify event for a classfile (a class or resource inside a jar or dir).
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Oct 11, 2013
	 */
	public interface IClassFileEntry {
		void foundFile(@NonNull File classPathDirectory, @NonNull File f, @NonNull String name) throws Exception;

		void foundJarEntry(@NonNull File classPathJarFile, @NonNull String name, @NonNull ZipInputStream zis) throws Exception;
	}

	public interface IClassEntry {
		void foundClass(@NonNull File source, @NonNull Class<?> theClass) throws Exception;
	}


	@NonNull
	private List<IPathEntry>	m_entryList	= new ArrayList<ClassPathScanner.IPathEntry>();

	@NonNull
	private List<IClassFileEntry>	m_classFileEntryList = new ArrayList<ClassPathScanner.IClassFileEntry>();

	@NonNull
	private List<IClassEntry>	m_classEntryList	= new ArrayList<ClassPathScanner.IClassEntry>();

	private int						m_nentries;

	private int						m_nclasses;

	@NonNull
	public static synchronized ClassPathScanner getInstance() {
		ClassPathScanner cs = m_instance;
		if(null == cs) {
			cs = m_instance = new ClassPathScanner();
			cs.addClassloader(cs.getClass().getClassLoader());
		}
		return cs;
	}

	public void addListener(@NonNull IPathEntry pe) {
		m_entryList.add(pe);
	}

	public void addListener(@NonNull IClassFileEntry pe) {
		m_classFileEntryList.add(pe);
	}

	public void addListener(@NonNull IClassEntry pe) {
		m_classEntryList.add(pe);
	}

	public void addClassloader(@NonNull ClassLoader classLoader) {
		ClassLoader pc = classLoader;
		for(;;) {
			if(m_classLoaders.contains(classLoader))
				return;
			m_classLoaders.add(classLoader);
			ClassLoader npc = classLoader.getParent();
			if(npc == null || npc == pc)
				return;
		}
	}

	public void addUrl(@NonNull URL url) {
		m_urls.add(url);
	}

	@NonNull
	private List<File> findEntriesFor(@NonNull ClassLoader cl) throws Exception {
		if(!(cl instanceof URLClassLoader))
			return Collections.EMPTY_LIST;
		URLClassLoader ucl = (URLClassLoader) cl;
		URL[] ar = ucl.getURLs();
		List<File> res = new ArrayList<File>();
		for(URL u : ar) {
			File f = new File(u.toURI());
			if(f.exists())
				res.add(f);
		}
		return res;
	}

	public void scan() throws Exception {
		//-- 1. Pass all classloaders 1st.
		long ts = System.nanoTime();
		Set<String> allset = new HashSet<String>();				// All path entries already done.
		for(ClassLoader cl : m_classLoaders) {
			List<File> flist = findEntriesFor(cl);
			for(File pathEntry : flist) {
				//				System.out.println("scan: " + pathEntry);
				String fpn = pathEntry.getAbsolutePath();
				if(allset.add(fpn)) {							// If not already done
					scanPathEntry(pathEntry, cl);
				}
			}
		}

		//-- 2. Pass all extra URLs
		for(URL u : m_urls) {
			File f = new File(u.toURI());
			if(f.exists()) {
				ClassLoader cl = null;
				if(m_classEntryList.size() > 0) {
					cl = new URLClassLoader(new URL[]{f.toURI().toURL()});
				}

				String fpn = f.getAbsolutePath();
				if(allset.add(fpn)) {							// If not already done
					scanPathEntry(f, cl);
				}
			}
		}
		ts = System.nanoTime() - ts;
		System.out.println("classpath: scanned " + m_nentries + " entries containing " + m_nclasses + " classes in " + StringTool.strNanoTime(ts));
	}

	private void scanPathEntry(@NonNull File pathEntry, @Nullable ClassLoader loader) throws Exception {
		for(IPathEntry pe : m_entryList)
			pe.foundPathEntry(pathEntry);

		if(m_classEntryList.size() > 0 || m_classFileEntryList.size() > 0) {
			StringBuilder sb = new StringBuilder();

			try {
				m_nentries++;
				if(pathEntry.isDirectory())
					scanDir(sb, pathEntry, pathEntry, loader);
				else
					scanJar(pathEntry, loader);
			} catch(Exception x) {
				System.err.println("classpathScanner: While scanning " + pathEntry + ": " + x);
				throw x;
				//				x.printStackTrace();
			}
		}
	}

	private void scanDir(@NonNull StringBuilder sb, @NonNull final File directory, @NonNull final File classPathDirectory, @Nullable ClassLoader loader) throws Exception {
		int len = sb.length();
		for(File f : directory.listFiles()) {
			sb.setLength(len);
			if(len > 0)
				sb.append('/');
			sb.append(f.getName());
			if(f.isDirectory())
				scanDir(sb, f, classPathDirectory, loader);
			else
				scanFileEntry(loader, classPathDirectory, f, sb.toString());
		}
	}

	private void scanFileEntry(@Nullable ClassLoader loader, @NonNull File classPathDirectory, @NonNull File f, @NonNull String name) throws Exception {
		for(IClassFileEntry fe: m_classFileEntryList) {
			fe.foundFile(classPathDirectory, f, name);
		}
		m_nclasses++;
		if(null != loader)
			scanClass(loader, classPathDirectory, name);
	}


	private void scanJarEntry(@Nullable ClassLoader loader, @NonNull File jar, @NonNull String name, @NonNull ZipInputStream zis) throws Exception {
		for(IClassFileEntry fe : m_classFileEntryList) {
			fe.foundJarEntry(jar, name, zis);
		}
		m_nclasses++;
		if(null != loader)
			scanClass(loader, jar, name);
	}

	private void scanClass(@NonNull ClassLoader loader, @NonNull File classPathDirectory, @NonNull String name) throws Exception {
		if(!name.endsWith(".class"))
			return;
		name = name.substring(0, name.length() - 6);			// Remove .class
		name = name.replace('/', '.');							// Make it a class name containing dots.

		Class< ? > clz;
		try {
			clz = loader.loadClass(name);
		} catch(Exception x) {
			return;
		} catch(Error x) {
			return;
		}
		for(IClassEntry ce : m_classEntryList) {
			ce.foundClass(classPathDirectory, clz);
		}
	}

	private void scanJar(@NonNull final File jar, @Nullable ClassLoader loader) throws Exception {
		//		long ts = System.nanoTime();
		FileInputStream fis = new FileInputStream(jar);
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(fis);
			ZipEntry ze;
			while(null != (ze = zis.getNextEntry())) {
				//				System.out.println("ZIPENTRY: "+ze.getName());
				if(!ze.isDirectory()) {
					scanJarEntry(loader, jar, ze.getName(), zis);
				}
			}
		} finally {
			try {
				if(zis != null)
					zis.close();
			} catch(Exception x) {}
			try {
				fis.close();
			} catch(Exception x) {}
			//			ts = System.nanoTime() - ts;
			//			System.out.println("scanned "+jar+" in "+StringTool.strNanoTime(ts));
		}
	}
}
