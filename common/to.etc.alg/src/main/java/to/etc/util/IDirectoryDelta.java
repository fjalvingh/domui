package to.etc.util;

import org.eclipse.jdt.annotation.NonNull;

import java.io.File;

public interface IDirectoryDelta {

	void fileAdded(@NonNull File b, @NonNull File a, @NonNull String relpath) throws Exception;

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
	boolean directoryAdded(@NonNull File b, @NonNull File a, @NonNull String relpath) throws Exception;

	void fileDeleted(@NonNull File b, @NonNull File a, @NonNull String relpath) throws Exception;

	/**
	 * Mark directory "b" as deleted (it exists as "a"). Return true
	 * if the underlying files in "a" need to be deltad as "deleted"
	 * too.
	 * @param b
	 * @param a
	 * @param relpath
	 * @return
	 */
	boolean directoryDeleted(@NonNull File b, @NonNull File a, @NonNull String relpath) throws Exception;

	/**
	 * Called when a and b both contain the same-named file. You can add
	 * content or size comparisons here.
	 * @param b
	 * @param a
	 * @param relpath
	 */
	void compareFiles(@NonNull File b, @NonNull File a, @NonNull String relpath) throws Exception;

}
