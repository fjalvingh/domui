package to.etc.domuidemo.sourceviewer;

import java.io.*;

public class SourceFile {
	private String		m_name;
	private File		m_sourceFile;
	private String		m_key;

	public SourceFile(String name, File sourceFile, String key) {
		m_name = name;
		m_sourceFile = sourceFile;
		m_key = key;
	}

	public String getName() {
		return m_name;
	}

	public String getKey() {
		return m_key;
	}

	public File getSourceFile() {
		return m_sourceFile;
	}

	public InputStream getContent() throws IOException {
		return new FileInputStream(m_sourceFile);
	}
}
