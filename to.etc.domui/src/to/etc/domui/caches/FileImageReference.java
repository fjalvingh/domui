package to.etc.domui.caches;

import java.io.*;

import to.etc.domui.util.images.*;

public class FileImageReference implements IImageReference {
	private File m_source;

	private String m_mime;

	public FileImageReference(File source, String mime) {
		m_source = source;
		m_mime = mime;
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public InputStream getInputStream() throws Exception {
		return new FileInputStream(m_source);
	}

	@Override
	public String getMimeType() throws Exception {
		return m_mime;
	}

	@Override
	public long getVersionLong() throws Exception {
		return 0;
	}
}
