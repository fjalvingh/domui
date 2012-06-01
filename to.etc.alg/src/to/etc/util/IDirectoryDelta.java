package to.etc.util;

import java.io.*;

public interface IDirectoryDelta {

	void fileAdded(File b, File a, String relpath) throws Exception;

	/**
	 * The directory "b" was added, and did not exist in "a". Return
	 * true if you want all files in "b" and below to be reported as
	 * delta too.
	 *
	 * @param b
	 * @param a
	 * @param relpath
	 * @return
	 */
	boolean directoryAdded(File b, File a, String relpath) throws Exception;

	void fileDeleted(File b, File a, String relpath) throws Exception;

	/**
	 * Mark directory "b" as deleted (it exists as "a"). Return true
	 * if the underlying files in "a" need to be deltad as "deleted"
	 * too.
	 * @param b
	 * @param a
	 * @param relpath
	 * @return
	 */
	boolean directoryDeleted(File b, File a, String relpath) throws Exception;

	/**
	 * Called when a and b both contain the same-named file. You can add
	 * content or size comparisons here.
	 * @param b
	 * @param a
	 * @param relpath
	 */
	void compareFiles(File b, File a, String relpath) throws Exception;

}
