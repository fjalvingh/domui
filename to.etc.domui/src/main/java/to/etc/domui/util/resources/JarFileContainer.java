package to.etc.domui.util.resources;

import to.etc.util.ByteBufferInputStream;
import to.etc.util.FileTool;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Contains the content of a .jar file, with timestamps for all files in there.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
@DefaultNonNull
final public class JarFileContainer implements IFileContainer {
	private final File m_file;

	private long m_tsModified;

	private final Map<String, JarredFileRef> m_fileMap = new HashMap<>();

	private final Map<String, byte[][]> m_cachedMap = new HashMap<>();

	private JarFileContainer(File file) {
		m_file = file;
	}

	public static JarFileContainer create(File f) {
		JarFileContainer c = new JarFileContainer(f);
		c.reload();
		return c;
	}

	private synchronized void reload() {
		m_fileMap.clear();

		if(! m_file.exists()) {
			m_tsModified = -1;
			return;
		}
		m_tsModified = m_file.lastModified();

		try(ZipInputStream zis = new ZipInputStream(new FileInputStream(m_file))) {
			ZipEntry ze;
			while(null != (ze = zis.getNextEntry())) {
				JarredFileRef jf = new JarredFileRef(ze.getName(), ze.getTime(), ze.getSize());
				m_fileMap.put(ze.getName(), jf);
			}
		} catch(Exception xz) {
			System.out.println("domui: failed to scan " + m_file + ": " + xz);
		}
	}

	@Override public List<String> getInventory() {
		reloadIfChanged();
		return new ArrayList<>(m_fileMap.keySet());
	}

	@Override
	@Nullable
	public JarredFileRef findFile(String name) {
		reloadIfChanged();
		return m_fileMap.get(name);
	}

	private void reloadIfChanged() {
		if(! m_file.exists()) {
			if(m_tsModified == -1)
				return;
		} else if(m_file.lastModified() == m_tsModified)
			return;
		reload();
	}

	/**
	 * In debug mode, this tries to read the specified resource from the .jar file and
	 * caches it. This does an explicit test for the jar being changed and clears the
	 * cache if it has.
	 *
	 * @param relname
	 * @return
	 */
	private synchronized byte[][] getCachedResource(String relname) throws IOException {
		reloadIfChanged();

		//-- Load the entry
		byte[][] bufs = m_cachedMap.get(relname);
		if(bufs == null) {
			bufs = loadFromJar(relname);
			if(bufs == null)
				throw new IOException("Jar file entry " + relname + " not found in jar " + m_file);
			m_cachedMap.put(relname, bufs);
		}
		return bufs;
	}

	/**
	 * Load the specified resource from the .jar file, as a set of byte buffers.
	 * @param name
	 * @return
	 * @throws IOException
	 */
	private byte[][] loadFromJar(String name) throws IOException {
		try (InputStream is = FileTool.getZipContent(m_file, name)) {
			if(null == is)
				throw new IOException("File '" + name + "' not found in jar " + m_file);
			return FileTool.loadByteBuffers(is); // Load as a set of byte buffers.
		}
	}

	public InputStream getResource(String relname) throws IOException {
		return new ByteBufferInputStream(getCachedResource(relname));
	}

}
