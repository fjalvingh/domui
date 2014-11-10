package to.etc.domui.component.meta;

import java.lang.reflect.*;
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

	private int m_copies;

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
		T copy = cloneInstance(m_source, sb);
		return copy;
	}

	private <I> I cloneInstance(I source, StringBuilder sb) throws InstantiationException, IllegalAccessException, Exception {
		if(++m_copies > 100)
			throw new IllegalStateException("Too many copies");
		ClassMetaModel cmm = MetaManager.findClassMeta(source.getClass());
		I copy = (I) cmm.getClass().newInstance();
		copyProperties(copy, source, sb, cmm);
		return copy;
	}

	private <I> void copyProperties(I copy, I source, StringBuilder sb, ClassMetaModel cmm) throws Exception {
		for(PropertyMetaModel< ? > pmm : cmm.getProperties()) {
			copyProperty(copy, m_source, pmm, sb);
		}
	}

	private <I, V> void copyProperty(I copy, I source, PropertyMetaModel<V> pmm, StringBuilder sb) throws Exception {
		if(pmm.getReadOnly() == YesNoType.YES)
			return;
		int len = sb.length();
		try {
			int len2 = len;
			if(len > 0) {
				sb.append('.');
				len2++;
			}

			sb.append("*");
			String all = sb.toString();

			sb.setLength(len2);
			sb.append(pmm.getName());
			String prop = sb.toString();

			Mode mode = m_modeMap.get(prop);
			if(mode == null) {
				mode = m_modeMap.get(all);
				if(mode == null)
					mode = m_defaultMode;
			}
			if(mode == Mode.IGNORE)
				return;

			//-- Get value, and copy and exit immediately on null.
			V srcvalue = pmm.getValue(source);
			if(srcvalue == null) {
				pmm.setValue(copy, null);
				return;
			}

			if(List.class.isAssignableFrom(pmm.getActualType())) {
				copyListProperty(copy, source, pmm, sb, mode);
			} else if(isUncopyable(pmm) || mode == Mode.SHALLOW) {
				pmm.setValue(copy, srcvalue);
			} else {
				V dstvalue = (V) m_old2newmap.get(srcvalue);
				if(null != dstvalue) {
					//-- Must create new instance
					dstvalue = cloneInstance(srcvalue, sb);
					m_old2newmap.put(srcvalue, dstvalue);
				}
				pmm.setValue(copy, dstvalue);
			}
		} finally {
			sb.setLength(len);
		}
	}

	private Class< ? >[] UNCOPYABLE = new Class< ? >[]{Date.class, String.class,};

	private boolean isUncopyable(PropertyMetaModel< ? > pmm) {
		Class<?> clz = pmm.getActualType();
		if(clz.isPrimitive())
			return true;
		if(pmm.getRelationType() == PropertyRelationType.UP)
			return false;
		try {
			Constructor< ? > cons = clz.getConstructor();
			if(!Modifier.isPublic(cons.getModifiers()))
				return true;
		} catch(Exception x) {
			return true;							// No default constructor -> uncopyable
		}

		for(Class< ? > uc : UNCOPYABLE) {
			if(uc.isAssignableFrom(clz))
				return true;
		}
		return false;
	}

	private <I, V> void copyListProperty(I copy, I source, PropertyMetaModel<V> pmm, StringBuilder sb, Mode mode) {

	}
}
