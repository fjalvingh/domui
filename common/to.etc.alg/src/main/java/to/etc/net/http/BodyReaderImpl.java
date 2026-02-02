package to.etc.net.http;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-03-22.
 */
final class BodyReaderImpl<T> implements IBodyReader<T> {
	private final Class<T> m_typeClass;

	public BodyReaderImpl(Class<T> typeClass) {
		m_typeClass = typeClass;
	}

	@Override
	public Class<T> getTypeClass() {
		return m_typeClass;
	}
}
