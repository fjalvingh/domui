package to.etc.domui.component.meta;


public class MetaCopier<T> extends MetaObjectCopyBase<T> {
	private T m_copy;

	public MetaCopier(T copy, T source) {
		super(source, Mode.SHALLOW);
		m_copy = copy;
	}

	/**
	 * Specify which relation properties need a "deep" copy, where the relation itself is
	 * also copied. For list properties this implies that all elements of the list are
	 * copied too.
	 *
	 * @param properties
	 * @return
	 */
	public MetaCopier<T> deep(String... properties) {
		setProperties(Mode.DEEP, properties);
		return this;
	}

	public MetaCopier<T> shallow(String... properties) {
		setProperties(Mode.SHALLOW, properties);
		return this;
	}

	public MetaCopier<T> copy(String... properties) {
		setProperties(Mode.COPY, properties);
		return this;
	}

	/**
	 * Specify a set of properties that should be the only ones copied.
	 * @param properties
	 * @return
	 */
	public MetaCopier<T> only(String... properties) {
		setOnly(properties);
		return this;
	}

	@Override
	public MetaCopier<T> ignore(String... properties) {
		ignore(properties);
		return this;
	}

	public void build() throws Exception {
		ClassMetaModel cmm = MetaManager.findClassMeta(getSource().getClass());
		copyProperties(m_copy, getSource(), new StringBuilder(), cmm);
	}
}
