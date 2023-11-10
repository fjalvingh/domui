package to.etc.net.http;

import to.etc.util.InputStreamWrapper;
import to.etc.util.WrappedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-03-22.
 */
public class GenericHttpResponse<T> implements AutoCloseable {
	private final int m_statusCode;

	private final GenericHttpHeaders m_headers;

	private final T m_body;

	private final List<AutoCloseable> m_thingsToClose;

	public GenericHttpResponse(int statusCode, GenericHttpHeaders headers, T body, List<AutoCloseable> resourcesToClose) {
		m_statusCode = statusCode;
		m_headers = headers;
		m_body = body;
		m_thingsToClose = new ArrayList<>(resourcesToClose);

		//-- If the body is an inputstream we need to close things when it is closed.
		if(body instanceof InputStream) {
			wrapInputStream((InputStream) body);
		}
	}

	private void wrapInputStream(InputStream body) {
		InputStreamWrapper isw = new InputStreamWrapper(body) {
			@Override
			public void close() throws IOException {
				super.close();
				try {
					GenericHttpResponse.this.close();
				} catch(Exception x) {
					throw WrappedException.wrap(x);
				}
			}
		};
	}

	public GenericHttpResponse(int statusCode, GenericHttpHeaders headers, T body, AutoCloseable... resourcesToClose) {
		this(statusCode, headers, body, Arrays.asList(resourcesToClose));
	}

	public int statusCode() {
		return m_statusCode;
	}

	public T body() {
		return m_body;
	}

	public GenericHttpHeaders headers() {
		return m_headers;
	}

	@Override
	public void close() throws Exception {
		List<AutoCloseable> todo;
		synchronized(this) {
			todo = new ArrayList<>(m_thingsToClose);
			m_thingsToClose.clear();
		}

		for(AutoCloseable ac : todo) {
			try {
				ac.close();
			} catch(Exception x) {
				//-- Ignore
			}
		}
	}
}
