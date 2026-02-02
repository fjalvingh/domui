package to.etc.net.http;

import java.io.InputStream;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-03-22.
 */
final public class BodyReaders {
	static public IBodyReader<String> ofString() {
		return new BodyReaderImpl<>(String.class);
	}

	static public IBodyReader<InputStream> ofInputStream() {
		return new BodyReaderImpl<>(InputStream.class);
	}

}
