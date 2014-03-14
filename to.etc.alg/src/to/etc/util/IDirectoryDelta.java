package to.etc.util;

import java.io.*;

import javax.annotation.*;

public interface IDirectoryDelta {

	void fileAdded(@Nonnull File b, @Nonnull File a, @Nonnull String relpath) throws Exception;

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
	boolean directoryAdded(@Nonnull File b, @Nonnull File a, @Nonnull String relpath) throws Exception;

	void fileDeleted(@Nonnull File b, @Nonnull File a, @Nonnull String relpath) throws Exception;

	/**
	 * Mark directory "b" as deleted (it exists as "a"). Return true
	 * if the underlying files in "a" need to be deltad as "deleted"
	 * too.
	 * @param b
	 * @param a
	 * @param relpath
	 * @return
	 */
	boolean directoryDeleted(@Nonnull File b, @Nonnull File a, @Nonnull String relpath) throws Exception;

	/**
	 * Called when a and b both contain the same-named file. You can add
	 * content or size comparisons here.
	 * @param b
	 * @param a
	 * @param relpath
	 */
	void compareFiles(@Nonnull File b, @Nonnull File a, @Nonnull String relpath) throws Exception;

}
