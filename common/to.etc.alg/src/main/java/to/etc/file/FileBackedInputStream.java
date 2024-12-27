package to.etc.file;

import to.etc.util.FileTool;
import to.etc.util.InputStreamWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * An input stream that comes from a file, and which deletes
 * that file when closed.
 */
public class FileBackedInputStream extends InputStreamWrapper {
	private final File m_file;

	public FileBackedInputStream(File file) throws FileNotFoundException {
		super(new FileInputStream(file));
		m_file = file;
	}

	@Override
	public void close() throws IOException {
		FileTool.delete(m_file);
		super.close();
	}
}
