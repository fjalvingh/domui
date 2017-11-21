package to.etc.domui.util.resources;

import to.etc.domui.server.reloader.Reloader;
import to.etc.util.FileTool;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Contains the inventory of a .jar file, with timestamps for all files in there.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
@DefaultNonNull final public class JarFileContainer implements IFileContainer {
	private final File m_file;

	private long m_tsModified;

	private final Map<String, JarredFileRef> m_fileMap = new ConcurrentHashMap<>();

	private final Map<String, byte[][]> m_cachedMap = new HashMap<>();

	private JarFileContainer(File file) {
		m_file = file;
	}

	public static JarFileContainer create(File f) {
		JarFileContainer c = new JarFileContainer(f);
		c.reload();
		return c;
	}

	/**
	 * Reload, keeping the original references but updating them if their timestamp or existence changed.
	 */
	private synchronized void reload() {
		if(!m_file.exists()) {
			m_tsModified = -1;
			m_fileMap.values().forEach(a -> a.update(-1, -1));
			return;
		}
		m_tsModified = m_file.lastModified();

		try(ZipInputStream zis = new ZipInputStream(new FileInputStream(m_file))) {
			ZipEntry ze;
			Set<String> oldNames = new HashSet<>(m_fileMap.keySet());
			while(null != (ze = zis.getNextEntry())) {
				JarredFileRef jr = m_fileMap.get(ze.getName());
				if(null == jr) {
					jr = new JarredFileRef(this, ze.getName(), ze.getTime(), ze.getSize());
					m_fileMap.put(ze.getName(), jr);
				} else {
					oldNames.remove(ze.getName());
					jr.update(ze.getTime(), ze.getSize());
				}

				//-- If the entry was cached: update its data.
				if(jr.getResourceData() != null) {
					jr.setResourceData(FileTool.loadByteBuffers(zis));
				}
			}

			//-- All oldNames are marked as DELETED
			oldNames.forEach(name -> m_fileMap.get(name).update(-1, -1));
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

	public synchronized void reloadIfChanged() {
		if(!m_file.exists()) {
			if(m_tsModified == -1)
				return;
		} else if(m_file.lastModified() == m_tsModified)
			return;
		if(Reloader.DEBUG)
			System.out.println("jarContainer: reloading " + m_file);
		reload();
	}

	/**
	 * Loads the resource from the jar. In addition to the specified resource this also
	 * load all other resources in the same directory because it's likely they are
	 * needed soon too.
	 */
	@Nonnull
	byte[][] loadResource(@Nonnull String name) throws IOException {
		int pos = name.lastIndexOf('/');
		if(pos == -1) {
			//-- Root resource - just load that.
			try(InputStream is = FileTool.getZipContent(m_file, name)) {
				if(null == is)
					throw new IOException("File '" + name + "' not found in jar " + m_file);
				return FileTool.loadByteBuffers(is);                    // Load as a set of byte buffers.
			}
		}

		byte[][] data = null;
		String path = name.substring(0, pos + 1);
		try(ZipInputStream zis = new ZipInputStream(new FileInputStream(m_file))) {
			for(; ; ) {
				ZipEntry ze = zis.getNextEntry();
				if(ze == null)
					break;
				String fn = ze.getName();
				if((fn.startsWith(path) && ! fn.endsWith(".class")) || fn.equals(name)) {
					//-- Load this thingy.
					byte[][] buffers = FileTool.loadByteBuffers(zis);
					if(fn.equals(name)) {
						data = buffers;
					} else {
						JarredFileRef ref = m_fileMap.get(fn);
						ref.setResourceData(buffers);
					}
				}
			}
		}
		if(null == data)
			throw new IOException("File '" + name + "' not found in jar " + m_file);
		return data;
	}
}
