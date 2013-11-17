package to.etc.file;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.annotation.*;

import to.etc.util.*;

/**
 * Contains a "directory contents" snapshot consisting of all files and directories in the target
 * directory and the hash values for all files. Inventories can be saved to a file and reloaded,
 * and two inventories can be compared to get a delta. All paths inside the inventory
 * are relative so inventories at different locations can be compared.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 16, 2013
 */
public class DirectoryInventory implements Serializable {
	private static final long serialVersionUID = 42328462L;

	static private final class InvEntry implements Serializable {
		@Nonnull
		final public String m_name;

		final public long m_lastModified;

		final public int m_size;

		@Nonnull
		final public byte[] m_md5hash;

		@Nullable
		final public InvEntry[] m_children;

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
			return null != m_children;
		}
	}

	private int m_numFiles;

	private int m_numDirectories;

	private long m_totalBytes;

	@Nonnull
	private InvEntry m_root;

	private long m_creationTime;

	public DirectoryInventory(long currentTimeMillis) {
		m_creationTime = currentTimeMillis;
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
		StringBuilder sb = new StringBuilder(128);
		DirectoryInventory de = new DirectoryInventory(System.currentTimeMillis());
		de.m_root = de.scanDirectory(src, sb);
		ts = System.nanoTime() - ts;
		System.out.println(".. initial inventory of " + src + " took " + StringTool.strNanoTime(ts) + " for " + de.m_numFiles + " files in " + de.m_numDirectories + " dirs");
		return de;
	}

	private static final Comparator<InvEntry> C_ORDER = new Comparator<InvEntry>() {
		@Override
		public int compare(InvEntry a, InvEntry b) {
			int res = a.m_name.compareTo(b.m_name);
			if(res != 0)
				return res;
			return compareArrays(a.m_md5hash, b.m_md5hash);
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
	private InvEntry scanDirectory(@Nonnull File src, @Nonnull StringBuilder sb) throws Exception {
		String dirname = sb.toString();
		int len = sb.length();
		int elen = len;
		if(len > 0) {
			sb.append('/');
			elen++;
		}
		File[] far = src.listFiles();
		if(null == far)
			throw new IllegalStateException("No results from " + src);
		List<InvEntry> list = new ArrayList<InvEntry>(far.length);
		for(File f : far) {
			//-- Construct the relative path into sb
			sb.setLength(elen);
			sb.append(f.getName());

			if(f.isDirectory()) {
				InvEntry ie = scanDirectory(f, sb);
				list.add(ie);
				m_numDirectories++;
			} else {
				//-- File: add signature
				String rn = sb.toString();
				byte[] hash = FileTool.hashFile(f);
				InvEntry ie = new InvEntry(rn, (int) f.length(), f.lastModified(), hash);
				list.add(ie);
				m_numFiles++;
			}
		}
		InvEntry[] ar = list.toArray(new InvEntry[list.size()]);
		Arrays.sort(ar, C_ORDER);
		MessageDigest dig = MessageDigest.getInstance("md5");
		for(int i = 0; i < ar.length; i++) {
			InvEntry ie = ar[i];
			dig.update(ie.m_name.getBytes("UTF-8"));
			dig.update(ie.m_md5hash);
		}
		byte[] dirhash = dig.digest();
		return new InvEntry(dirname, 0, 0, dirhash, ar);
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
	static public interface IDeltaListener {
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
		if(Arrays.equals(src.m_md5hash, dst.m_md5hash))
			return;

		int len = sb.length();
		if(len > 0) {
			sb.append('/');
			len++;
		}

		Map<String, InvEntry> dstmap = new HashMap<String, InvEntry>();
		for(InvEntry de : dst.m_children)
			dstmap.put(de.m_name, de);
		for(InvEntry se : src.m_children) {
			sb.setLength(len);
			sb.append(se.m_name);								// Make a full path name
			String path = sb.toString();
			InvEntry de = dstmap.remove(se.m_name);				// If there is a dest with that name remove it,
			if(null != de) {
				compareEntries(sb, se, de, listener);
			} else {
				//-- Source is there, dest is not -> delete of "src"
				if(!se.isDirectory()) {
					listener.fileDeleted(path, se.m_lastModified, se.m_md5hash);
				} else {
					//-- depth-1st traversal to send delete event for everything.
					handleTreeDelete(listener, sb, se.m_children);
				}
			}
		}

		//-- All left in the map exists in dst but not in src so has been added
		for(InvEntry ie : dstmap.values()) {
			sb.setLength(len);
			sb.append(ie.m_name);								// Make a full path name
			String path = sb.toString();

			if(ie.isDirectory()) {
				handleTreeAdd(listener, sb, ie.m_children);
			} else {
				listener.fileAdded(path, ie.m_lastModified, ie.m_md5hash);
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
			sb.append(ie.m_name);
			if(ie.isDirectory()) {
				handleTreeAdd(listener, sb, ie.m_children);				// Recurse into dir
			} else {
				listener.fileAdded(sb.toString(), ie.m_lastModified, ie.m_md5hash);
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
			sb.append(ie.m_name);
			if(ie.isDirectory()) {
				handleTreeDelete(listener, sb, ie.m_children);	// Recurse into dir
			} else {
				listener.fileDeleted(sb.toString(), ie.m_lastModified, ie.m_md5hash);
			}
		}
		listener.directoryDeleted(name);
	}

	/**
	 * Compare same-named entries.
	 * @param sb
	 * @param de
	 * @param de2
	 * @param listener
	 */
	private void compareEntries(@Nonnull StringBuilder sb, @Nonnull InvEntry src, @Nonnull InvEntry dst, @Nonnull IDeltaListener listener) throws Exception {
		if(src.isDirectory() && dst.isDirectory()) {
			handleCompare(sb, listener, src, dst);
		} else if(src.isDirectory()) {
			//-- Directory changed to file...
			handleTreeDelete(listener, sb, src.m_children);		// send delete event for all in src tree
			listener.fileAdded(sb.toString(), dst.m_lastModified, dst.m_md5hash);
		} else if(dst.isDirectory()) {
			//-- File replaced by directory
			listener.fileDeleted(sb.toString(), src.m_lastModified, src.m_md5hash);
			handleTreeAdd(listener, sb, dst.m_children);
		} else {
			//-- Both are files.
			if(!Arrays.equals(src.m_md5hash, dst.m_md5hash)) {
				listener.fileModified(sb.toString(), src.m_lastModified, dst.m_lastModified, src.m_md5hash, dst.m_md5hash);
			}
		}
	}


	public static void main(String[] args) throws Exception {
		DirectoryInventory a = DirectoryInventory.create(new File("/home/jal/bzr/puzzler-split/domui/to.etc.domui/src"));
		DirectoryInventory b = DirectoryInventory.create(new File("/home/jal/bzr/puzzler-split/domui/to.etc.domui/src"));
		List<DeltaRecord> res = a.compareTo(b);
		System.out.println("We have " + res.size() + " changes");

		DirectoryInventory c = DirectoryInventory.create(new File("/home/jal/bzr/domui-form/to.etc.domui/src"));
		res = a.compareTo(c);
		System.out.println("We have " + res.size() + " changes");

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
