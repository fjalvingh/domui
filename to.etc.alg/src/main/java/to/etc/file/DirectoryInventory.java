package to.etc.file;

import to.etc.function.FunctionEx;
import to.etc.util.FileTool;
import to.etc.util.WrappedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains a "directory contents" snapshot consisting of all files and directories in the target
 * directory and the hash values for all files. Inventories can be saved to a file and reloaded,
 * and two inventories can be compared to get a delta. All paths inside the inventory
 * are relative so inventories at different locations can be compared.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 16, 2013
 */
final public class DirectoryInventory implements Serializable {
	private static final long serialVersionUID = 42328462L;

	static public final class InvEntry implements Serializable {
		private static final long serialVersionUID = 423284312L;

		@Nonnull
		private final String m_name;

		private final long m_lastModified;

		private final int m_size;

		@Nonnull
		private final byte[] m_md5hash;

		@Nullable
		private final InvEntry[] m_children;

		public InvEntry(@Nonnull String name, int size, long lastModified, @Nonnull byte[] md5hash) {
			m_name = name;
			m_size = size;
			m_lastModified = lastModified;
			m_md5hash = md5hash;
			m_children = null;
		}

		public InvEntry(@Nonnull String name, int size, long lastModified, @Nonnull byte[] md5hash, @Nonnull InvEntry[] children) {
			m_name = name;
			m_size = size;
			m_lastModified = lastModified;
			m_md5hash = md5hash;
			m_children = children;
		}

		public boolean isDirectory() {
			return null != getChildren();
		}

		@Nonnull public String getName() {
			return m_name;
		}

		public long getLastModified() {
			return m_lastModified;
		}

		public int getSize() {
			return m_size;
		}

		@Nonnull public byte[] getMd5hash() {
			return m_md5hash;
		}

		@Nullable public InvEntry[] getChildren() {
			return m_children;
		}

		@Nullable
		public <R> R visit(@Nonnull FunctionEx<InvEntry, R> consumer) throws Exception {
			R val = consumer.apply(this);
			if(null != val)
				return val;
			InvEntry[] children = m_children;
			if(null != children) {
				for(InvEntry child : children) {
					val = child.visit(consumer);
					if(null != val)
						return val;
				}
			}
			return null;
		}
	}

	private int m_numFiles;

	private int m_numDirectories;

	private long m_totalBytes;

	@Nonnull
	private InvEntry m_root;

	private long m_creationTime;

	private DirectoryInventory(long currentTimeMillis) {
		m_creationTime = currentTimeMillis;
	}

	@Nonnull
	public static DirectoryInventory createEmpty() {
		DirectoryInventory de = new DirectoryInventory(System.currentTimeMillis());
		de.m_root = new InvEntry("", 0, 0, new byte[16], new InvEntry[0]);
		return de;
	}

	/**
	 * Create an inventory for the specified directory by scanning and reading all files.
	 * @param src
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	static public DirectoryInventory create(@Nonnull File src) throws Exception {
		if(!src.exists())
			throw new IOException(src + ": directory does not exist");
		if(!src.isDirectory())
			throw new IOException(src + ": is not a directory");
		long ts = System.nanoTime();
		DirectoryInventory de = new DirectoryInventory(System.currentTimeMillis());
		de.m_root = de.scanDirectory(src);
		ts = System.nanoTime() - ts;
//		System.out.println(".. inventory of " + src + " took " + StringTool.strNanoTime(ts) + " for " + de.m_numFiles + " files in " + de.m_numDirectories + " dirs");
		return de;
	}

	private static final Comparator<InvEntry> C_ORDER = new Comparator<InvEntry>() {
		@Override
		public int compare(InvEntry a, InvEntry b) {
			int res = a.getName().compareTo(b.getName());
			if(res != 0)
				return res;
			return compareArrays(a.getMd5hash(), b.getMd5hash());
		}
	};

	static public int compareArrays(@Nonnull byte[] aa, @Nonnull byte[] ba) {
		int ct = Math.min(aa.length, ba.length);
		while(--ct >= 0) {
			int r = aa[ct] - ba[ct];
			if(r != 0)
				return r;
		}
		return aa.length - ba.length;
	}

	@Nonnull
	private InvEntry scanDirectory(@Nonnull File src) throws Exception {
		File[] far = src.listFiles();
		if(null == far)
			throw new IllegalStateException("No results from " + src);
		List<InvEntry> list = new ArrayList<InvEntry>(far.length);
		for(File f : far) {
			//-- Construct the relative path into sb
			if(f.isDirectory()) {
				InvEntry ie = scanDirectory(f);
				list.add(ie);
				m_numDirectories++;
			} else {
				//-- File: add signature
				byte[] hash = FileTool.hashFile(f);
				InvEntry ie = new InvEntry(f.getName(), (int) f.length(), f.lastModified(), hash);
				list.add(ie);
				m_numFiles++;
			}
		}
		InvEntry[] ar = list.toArray(new InvEntry[list.size()]);
		Arrays.sort(ar, C_ORDER);
		MessageDigest dig = MessageDigest.getInstance("md5");
		for(int i = 0; i < ar.length; i++) {
			InvEntry ie = ar[i];
			dig.update(ie.getName().getBytes("UTF-8"));
			dig.update(ie.getMd5hash());
		}
		byte[] dirhash = dig.digest();
		return new InvEntry(src.getName(), 0, 0, dirhash, ar);
	}


	/**
	 * Load the inventory from a file.
	 * @param src
	 * @return
	 */
	@Nonnull
	static public DirectoryInventory load(@Nonnull File src) throws Exception {
		if(!src.exists())
			throw new FileNotFoundException(src + ": not found");
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(src));
			return (DirectoryInventory) ois.readObject();
		} finally {
			FileTool.closeAll(ois);
		}
	}

	/**
	 * Saves the entire inventory.
	 * @param src
	 */
	public void save(@Nonnull File src) {
		boolean ok = false;
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(src));
			oos.writeObject(this);
			oos.close();
			oos = null;
			ok = true;
		} catch(Exception x) {
			x.printStackTrace();
		} finally {
			try {
				if(oos != null)
					oos.close();
			} catch(Exception x) {}
			try {
				if(!ok)
					src.delete();
			} catch(Exception x) {}
		}
	}

	/**
	 * This listener receives all changes found by comparing two DirectoryInventory objects.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Nov 16, 2013
	 */
	public interface IDeltaListener {
		void fileDeleted(@Nonnull String path, long lastModified, @Nonnull byte[] md5hash) throws Exception;

		void directoryDeleted(@Nonnull String path) throws Exception;

		void directoryAdded(@Nonnull String path) throws Exception;

		void fileAdded(@Nonnull String path, long lastModified, @Nonnull byte[] md5hash) throws Exception;

		void fileModified(@Nonnull String path, long srcLastModified, long dstLastModified, @Nonnull byte[] srchash, @Nonnull byte[] dsthash) throws Exception;
	}

	public enum DeltaType {
		fileDeleted, fileAdded, fileModified, directoryAdded, directoryDeleted,
	}

	/**
	 * Records a single change between two inventories.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Nov 17, 2013
	 */
	static public class DeltaRecord {
		@Nonnull
		final private DeltaType m_type;

		@Nonnull
		final private String m_path;

		public DeltaRecord(@Nonnull DeltaType type, @Nonnull String path) {
			m_type = type;
			m_path = path;
		}

		@Nonnull
		public DeltaType getType() {
			return m_type;
		}

		@Nonnull
		public String getPath() {
			return m_path;
		}
	}

	/**
	 * Compare this and another inventory, and generate events that tell how THIS would need to change to become OTHER.
	 * @param other
	 */
	public void compareTo(@Nonnull DirectoryInventory other, @Nonnull IDeltaListener listener) throws Exception {
		StringBuilder sb = new StringBuilder();					// For building paths.
		handleCompare(sb, listener, m_root, other.m_root);
	}

	/**
	 * Compare to inventories and return a list of changes.
	 * @param other
	 * @return
	 */
	@Nonnull
	public List<DeltaRecord> compareTo(@Nonnull DirectoryInventory other) {
		try {
			final List<DeltaRecord> result = new ArrayList<>();

			compareTo(other, new IDeltaListener() {
				@Override
				public void fileModified(String path, long srcLastModified, long dstLastModified, byte[] srchash, byte[] dsthash) throws Exception {
					result.add(new DeltaRecord(DeltaType.fileModified, path));
				}

				@Override
				public void fileDeleted(String path, long lastModified, byte[] md5hash) throws Exception {
					result.add(new DeltaRecord(DeltaType.fileDeleted, path));
				}

				@Override
				public void fileAdded(String path, long lastModified, byte[] md5hash) throws Exception {
					result.add(new DeltaRecord(DeltaType.fileAdded, path));
				}

				@Override
				public void directoryDeleted(String path) throws Exception {
					result.add(new DeltaRecord(DeltaType.directoryDeleted, path));
				}

				@Override
				public void directoryAdded(String path) throws Exception {
					result.add(new DeltaRecord(DeltaType.directoryAdded, path));
				}
			});
			return result;
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	/**
	 * Main event-generating delta method, comparing two directory entries.
	 * @param sb
	 * @param listener
	 * @param src
	 * @param dst
	 * @throws Exception
	 */
	private void handleCompare(@Nonnull StringBuilder sb, @Nonnull IDeltaListener listener, @Nonnull InvEntry src, @Nonnull InvEntry dst) throws Exception {
		//-- If both directory hash entries are equal this dir has no changes
		if(Arrays.equals(src.getMd5hash(), dst.getMd5hash()))
			return;

		int len = sb.length();
		if(len > 0) {
			sb.append('/');
			len++;
		}

		Map<String, InvEntry> dstmap = new HashMap<String, InvEntry>();
		for(InvEntry de : dst.getChildren())
			dstmap.put(de.getName(), de);
		for(InvEntry se : src.getChildren()) {
			sb.setLength(len);
			sb.append(se.getName());								// Make a full path name
			String path = sb.toString();
			InvEntry de = dstmap.remove(se.getName());				// If there is a dest with that name remove it,
			if(null != de) {
				compareEntries(sb, se, de, listener);
			} else {
				//-- Source is there, dest is not -> delete of "src"
				if(!se.isDirectory()) {
					listener.fileDeleted(path, se.getLastModified(), se.getMd5hash());
				} else {
					//-- depth-1st traversal to send delete event for everything.
					handleTreeDelete(listener, sb, se.getChildren());
				}
			}
		}

		//-- All left in the map exists in dst but not in src so has been added
		for(InvEntry ie : dstmap.values()) {
			sb.setLength(len);
			sb.append(ie.getName());								// Make a full path name
			String path = sb.toString();

			if(ie.isDirectory()) {
				handleTreeAdd(listener, sb, ie.getChildren());
			} else {
				listener.fileAdded(path, ie.getLastModified(), ie.getMd5hash());
			}
		}
	}

	private void handleTreeAdd(IDeltaListener listener, StringBuilder sb, InvEntry[] children) throws Exception {
		listener.directoryAdded(sb.toString());
		int len = sb.length();
		if(len > 0) {
			sb.append('/');
			len++;
		}
		for(InvEntry ie : children) {
			sb.setLength(len);
			sb.append(ie.getName());
			if(ie.isDirectory()) {
				handleTreeAdd(listener, sb, ie.getChildren());				// Recurse into dir
			} else {
				listener.fileAdded(sb.toString(), ie.getLastModified(), ie.getMd5hash());
			}
		}
	}

	/**
	 * Send delete indicators for everything passed.
	 * @param listener
	 * @param sb
	 * @param children
	 */
	private void handleTreeDelete(IDeltaListener listener, StringBuilder sb, InvEntry[] children) throws Exception {
		String name = sb.toString();
		int len = sb.length();
		if(len > 0) {
			sb.append('/');
			len++;
		}
		for(InvEntry ie : children) {
			sb.setLength(len);
			sb.append(ie.getName());
			if(ie.isDirectory()) {
				handleTreeDelete(listener, sb, ie.getChildren());	// Recurse into dir
			} else {
				listener.fileDeleted(sb.toString(), ie.getLastModified(), ie.getMd5hash());
			}
		}
		listener.directoryDeleted(name);
	}

	/**
	 * Compare same-named entries.
	 * @param sb
	 * @param listener
	 */
	private void compareEntries(@Nonnull StringBuilder sb, @Nonnull InvEntry src, @Nonnull InvEntry dst, @Nonnull IDeltaListener listener) throws Exception {
		if(src.isDirectory() && dst.isDirectory()) {
			handleCompare(sb, listener, src, dst);
		} else if(src.isDirectory()) {
			//-- Directory changed to file...
			handleTreeDelete(listener, sb, src.getChildren());		// send delete event for all in src tree
			listener.fileAdded(sb.toString(), dst.getLastModified(), dst.getMd5hash());
		} else if(dst.isDirectory()) {
			//-- File replaced by directory
			listener.fileDeleted(sb.toString(), src.getLastModified(), src.getMd5hash());
			handleTreeAdd(listener, sb, dst.getChildren());
		} else {
			//-- Both are files.
			if(!Arrays.equals(src.getMd5hash(), dst.getMd5hash())) {
				listener.fileModified(sb.toString(), src.getLastModified(), dst.getLastModified(), src.getMd5hash(), dst.getMd5hash());
			}
		}
	}

	/**
	 * Visits all entries.
	 * @param consumer
	 * @throws Exception
	 */
	@Nullable
	public <R> R visit(@Nonnull FunctionEx<InvEntry, R> consumer) throws Exception {
		return m_root.visit(consumer);
	}

	public static void main(@Nonnull String[] args) throws Exception {
		DirectoryInventory a = DirectoryInventory.create(new File("/home/jal/bzr/puzzler-split/domui/to.etc.domui/src"));
		DirectoryInventory b = DirectoryInventory.create(new File("/home/jal/bzr/puzzler-split/domui/to.etc.domui/src"));
		List<DeltaRecord> res = a.compareTo(b);
		System.out.println("We have " + res.size() + " changes");

		DirectoryInventory c = DirectoryInventory.create(new File("/home/jal/bzr/domui-form/to.etc.domui/src"));
		res = a.compareTo(c);
		System.out.println("We have " + res.size() + " changes");
		for(DeltaRecord dr : res) {
			System.out.println(dr.getType() + ": " + dr.getPath());
		}
	}

	public int getNumFiles() {
		return m_numFiles;
	}

	public int getNumDirectories() {
		return m_numDirectories;
	}

	public long getCreationTime() {
		return m_creationTime;
	}
}
