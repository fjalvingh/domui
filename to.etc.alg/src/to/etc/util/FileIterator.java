package to.etc.util;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import to.etc.util.FileIterator.*;

/**
 * This iterator allows you to walk through all files/directories in a directory
 * tree using just a loop instead of the usual set of recursive methods. The basic
 * way to use it is:
 * <pre>
 *     File path = new File("/path/to/dir");
 *     for(Entry entry: new FileIterator(path)) {
 *         //-- Use information from entry like File, type, and whether this is a "before" or "after" directory call.
 *     }
 * </pre>
 * See {@link #main(String[])} for a runnable example of it's use.
 *
 * <p>The iterator traverses the entire directory tree starting (but excluding) with the path passed. It walks all
 * files and all directories in the order that they are encountered. Files are reported once (meaning each file
 * will return a single Entry), but directories will be reported twice: once <b>before</b> the files belonging
 * inside the directory are traversed and once immediately after. This means a directory is always seen twice,
 * and while traversing you can decide when to do something with that directory. For example when deleting a
 * structure with this iterator you would delete files immediately but directories when the entry type is DirectoryAfter.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4/12/16.
 */
@DefaultNonNull
public class FileIterator implements Iterator<Entry>, Iterable<Entry> {
	public enum Type {
		DirectoryBefore,
		DirectoryAfter,
		File
	}

	static public final class Entry {
		private final File m_entry;

		private final String m_relativePath;

		private final Type m_type;

		public Entry(Type type, File entry, String relativePath) {
			m_type = type;
			m_entry = entry;
			m_relativePath = relativePath;
		}

		public Type getType() {
			return m_type;
		}

		public File getEntry() {
			return m_entry;
		}

		public String getRelativePath() {
			return m_relativePath;
		}
	}

	final private File m_root;

	private StringBuilder m_pathSb = new StringBuilder();

	private static class Level {
		final private File[] m_files;

		final private String m_relativePath;

		private int m_index;

		public Level(File[] files, String relativePath) {
			m_files = files;
			m_relativePath = relativePath;
		}

		private boolean eof() {
			return m_index >= m_files.length;
		}

		public int getIndex() {
			return m_index;
		}

		public void next() {
			m_index++;
		}

		public File current() {
			if(eof())
				throw new IllegalStateException();
			return m_files[m_index];
		}

		public String getRelativePath() {
			return m_relativePath;
		}
	}

	private final List<Level> m_levelStack = new ArrayList<>();

	private boolean m_lastPopped;

	public FileIterator(File root) {
		m_root = root;
		File[] files = root.listFiles();
		if(files != null && files.length != 0)
			m_levelStack.add(new Level(files, ""));
	}

	private Level getCurrentLevel() {
		return m_levelStack.get(m_levelStack.size()-1);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("I do not like it that you remove files, yet.");
	}
	@Override
	public boolean hasNext() {
		return m_levelStack.size() > 0;
	}

	@Override
	public Entry next() {
		if(m_levelStack.size() == 0)
			throw new IllegalStateException("No (more) entries");

		Level l = getCurrentLevel();
		File file = l.current();
		Type type;

		if(file.isDirectory()) {
			type = m_lastPopped ? Type.DirectoryAfter : Type.DirectoryBefore;
		} else {
			type = Type.File;
		}

		Entry entry = new Entry(type, file, makePath(l.getRelativePath(), file.getName()));

		moveToNext();
		return entry;
	}

	private void moveToNext() {
		Level l = getCurrentLevel();
		if(l.eof())
			return;
		File file = l.current();
		if(! m_lastPopped && file.isDirectory()) {
			File[] files = file.listFiles();
			if(files.length > 0) {
				Level nl = new Level(files, makePath(l.getRelativePath(), file.getName()));
				m_levelStack.add(nl);
			} else {
				m_lastPopped = true;
			}
			return;
		}
		m_lastPopped = false;
		l.next();
		if(! l.eof())
			return;

		//-- We have reached EOF. Pop a level.
		m_levelStack.remove(m_levelStack.size()-1);
		m_lastPopped = true;
	}

	private String makePath(String a, String b) {
		m_pathSb.setLength(0);
		m_pathSb.append(a);
		if(a.length() != 0)
			m_pathSb.append(File.separatorChar);
		m_pathSb.append(b);
		return m_pathSb.toString();
	}

	@Override
	public Iterator<Entry> iterator() {
		return this;
	}

	/**
	 * Runnable example of usage.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		File f = new File("/tmp");

		int count = 0;
		for(Entry e: new FileIterator(f)) {
			System.out.println(e.getType() + " " + e.getRelativePath());
			if(e.getType() == Type.File)
				count++;
		}
		System.out.println("Got " + count + " files");
	}

}
