package to.etc.binaries.cache;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import to.etc.server.cache.*;
import to.etc.util.*;

/**
 * A reference to some binary that you want to play with. When you're
 * done with the binary you need to call release() on this structure
 * to allow the cache to release maintained data. Each reference to
 * a Binary has it's own BinaryRef which is usable only as long as
 * it has not been Released.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 13, 2006
 */
public class BinaryRef {
	private ResourceRef	m_rr;

	/** When T this ref is no longer valid because it has been released */
	private boolean		m_released;

	BinaryRef(ResourceRef r) {
		m_rr = r;
	}

	public void release() {
		if(m_released)
			return;
	}

	@Override
	protected void finalize() throws Throwable {
		release();
	}

	public File getFile() {
		CachedBinary c = (CachedBinary) m_rr.getObject();
		return c.getFile();
	}

	public byte[][] getData() {
		CachedBinary c = (CachedBinary) m_rr.getObject();
		return c.getData();
	}

	public BinaryInfo getInfo() {
		CachedBinary c = (CachedBinary) m_rr.getObject();
		return c.getBi();
	}

	public InputStream getInputStream() {
		if(getData() != null)
			return new ByteBufferInputStream(getData());
		try {
			return new FileInputStream(getFile());
		} catch(FileNotFoundException x) {
			throw new IllegalStateException(x); // James Gosling is a bloody moron with his stupid checked exception crap.
		}
	}

	public void generate(HttpServletResponse res) throws IOException {
		res.setContentType(getInfo().getMime());
		res.setContentLength(getInfo().getSize());

		ServletOutputStream sos = res.getOutputStream();
		byte[][] data = getData();
		if(data != null) {
			for(byte[] b : data)
				sos.write(b);
			return;
		}
		InputStream is = new FileInputStream(getFile());
		try {
			int size = getInfo().getSize();
			if(size > 8192)
				size = 8192;
			byte[] buf = new byte[size];
			while(0 < (size = is.read(buf))) {
				sos.write(buf, 0, size);
			}
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}
}
