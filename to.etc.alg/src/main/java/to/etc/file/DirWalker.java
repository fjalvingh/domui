package to.etc.file;

import java.io.*;
import java.util.*;

import javax.annotation.*;

/**
 * Walks a directory tree and calls a handler  for each entry, recursively. Or returns a list of everything inside
 * a directory.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 6, 2014
 */
final public class DirWalker {
	private DirWalker() {}

	public interface IEntry<T> {
		@Nullable T onEntry(@Nonnull File file, @Nonnull String relativePath) throws Exception;
	}

	/**
	 * Walk a directory recursively and call a function for every entry found.
	 * @param srcdir
	 * @param handler
	 * @return
	 * @throws Exception
	 */
	@Nullable
	static public <T> T scan(@Nonnull File srcdir, @Nonnull IEntry<T> handler) throws Exception {
		StringBuilder sb = new StringBuilder();
		return scanInternal(srcdir, handler, sb);
	}

	@Nullable
	static private <T> T scanInternal(@Nonnull File srcdir, @Nonnull IEntry<T> handler, @Nonnull StringBuilder sb) throws Exception {
		File[] ar = srcdir.listFiles();
		if(ar == null)
			return null;
		int len = sb.length();
		for(File f : ar) {
			sb.setLength(len);
			if(len > 0)
				sb.append('/');
			sb.append(f.getName());
			String name = sb.toString();

			T res = handler.onEntry(f, name);
			if(null != res)
				return res;
			if(f.isDirectory()) {
				res = scanInternal(f, handler, sb);
			}
		}
		return null;
	}

	/**
	 * Get a list of all files, directories under a given directory.
	 * @param srcdir			The source dir
	 * @param files				T if you want files to be included in the list
	 * @param dirs				T if you want directories to be included in the list.
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	static public List<File> dir(@Nonnull File srcdir, boolean files, boolean dirs) throws Exception {
		if(!files && !dirs)
			throw new IllegalArgumentException("Looking for nothing is not useful");
		List<File> res = new ArrayList<File>();
		dir(res, srcdir, files, dirs);
		return res;
	}

	static private void dir(@Nonnull List<File> res, @Nonnull File srcdir, boolean files, boolean dirs) throws Exception {
		File[] ar = srcdir.listFiles();
		if(null == ar)
			return;
		for(File f : ar) {
			if(files && dirs)
				res.add(f);
			else if(files && f.isFile())
				res.add(f);
			else if(dirs && f.isDirectory())
				res.add(f);
			if(f.isDirectory()) {
				dir(res, f, files, dirs);
			}
		}
	}
}
