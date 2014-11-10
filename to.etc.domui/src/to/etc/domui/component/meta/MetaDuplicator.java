package to.etc.domui.component.meta;

import java.util.*;

import javax.annotation.*;

@DefaultNonNull
public class MetaDuplicator<T> {
	final private T m_source;

	static public enum Mode {
		DEEP, SHALLOW, COPY, IGNORE, ONLY
	}

	private Set<String> m_onlySet = new HashSet<>();

	private int m_ignored;

	final private Mode m_defaultMode;

	@Nullable
	private T m_copy;

	private Map<String, Mode> m_modeMap = new HashMap<>();

	@Nonnull
	private Map<Object, Object> m_old2newmap = new HashMap<>();

	public MetaDuplicator(T source) {
		m_source = source;
		m_defaultMode = Mode.SHALLOW;
	}

	/**
	 * Specify a (set of) properties that should be ignored when copying.
	 * @param properties
	 * @return
	 */
	public MetaDuplicator<T> ignore(String... properties) {
		if(m_onlySet.size() > 0)
			throw new IllegalArgumentException("Either use igore or only, not both!");
		for(String p : properties) {
			Mode prev = m_modeMap.put(p, Mode.IGNORE);
			if(null != prev)
				throw new IllegalArgumentException("Property " + p + " was set to " + prev + " earlier");
			m_ignored++;
		}
		return this;
	}

	/**
	 * Specify a set of properties that should be the only ones copied.
	 * @param properties
	 * @return
	 */
	public MetaDuplicator<T> only(String... properties) {
		if(m_ignored > 0)
			throw new IllegalArgumentException("Either use igore or only, not both!");
		for(String p : properties) {
			m_onlySet.add(p);
		}
		return this;
	}

	/**
	 * Specify which relation properties need a "deep" copy, where the relation itself is
	 * also copied. For list properties this implies that all elements of the list are
	 * copied too.
	 *
	 * @param properties
	 * @return
	 */
	public MetaDuplicator<T> deep(String... properties) {
		setProperties(Mode.DEEP, properties);
		return this;
	}

	public MetaDuplicator<T> shallow(String... properties) {
		setProperties(Mode.SHALLOW, properties);
		return this;
	}

	public MetaDuplicator<T> copy(String... properties) {
		setProperties(Mode.COPY, properties);
		return this;
	}

	private void setProperties(Mode mode, String... properties) {
		for(String p : properties) {
			Mode prev = m_modeMap.put(p, mode);
			if(null != prev)
				throw new IllegalArgumentException("Property " + p + " was set to " + prev + " earlier");
		}
	}

	public T build() throws Exception {
		StringBuilder sb = new StringBuilder();
		ClassMetaModel cmm = MetaManager.findClassMeta(m_source.getClass());

		T copy = (T) cmm.getClass().newInstance();
		for(PropertyMetaModel< ? > pmm : cmm.getProperties()) {
			copyProperty(copy, m_source, pmm, sb);
		}
		return copy;
	}

	private <V> void copyProperty(T copy, T source, PropertyMetaModel<V> pmm, StringBuilder sb) {
		if(pmm.getReadOnly() == YesNoType.YES)
			return;
		int len = sb.length();
		if(len > 0) {
			sb.append('.');
		}
		sb.append("*");
		String all = sb.toString();


		if(List.class.isAssignableFrom(pmm.getActualType())) {
			copyListProperty(copy, source, pmm, sb);
		} else if(isUncopyable(pmm)) {
			copyValue(copy, source, pmm, sb);
		} else {

		}

		sb.setLength(len);
	}

	private boolean isUncopyable(PropertyMetaModel< ? > pmm) {

	}


	private <V> void copyListProperty(T copy, T source, PropertyMetaModel<V> pmm, StringBuilder sb) {

	}
}
