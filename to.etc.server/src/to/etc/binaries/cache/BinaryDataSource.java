package to.etc.binaries.cache;

import java.io.*;

public interface BinaryDataSource {
	public InputStream getInputStream() throws Exception;

	public File getFile() throws Exception;

	public void discard();
}
