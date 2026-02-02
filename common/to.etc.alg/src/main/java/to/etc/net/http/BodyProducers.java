package to.etc.net.http;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-03-22.
 */
final public class BodyProducers {
	private BodyProducers() {
	}

	static public IHttpBodyProducer noBody() {
		return new EmptyBodyProducer();
	}

	public static IHttpBodyProducer ofString(String body) {
		return new StringBodyProducer(body);
	}

	public static final class EmptyBodyProducer implements IHttpBodyProducer {
	}

	public static final class StringBodyProducer implements IHttpBodyProducer {
		private final String m_data;

		public StringBodyProducer(String data) {
			m_data = data;
		}

		public String getData() {
			return m_data;
		}
	}




}
