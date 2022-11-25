package to.etc.domui.component.meta.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.util.PropertyInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-05-20.
 */
public class JavaPropertyAccessor<V> implements IPropertyModelAccessor<V> {
	private final PropertyInfo m_info;

	public JavaPropertyAccessor(PropertyInfo info) {
		m_info = info;
	}

	@Override
	public boolean isMutable() {
		return m_info.getSetter() != null;
	}

	@Nullable
	@Override
	public V getValue(@Nullable Object targetInstance) throws Exception {
		if(targetInstance == null)
			throw new IllegalStateException("The 'input' object is null (getter method=" + m_info.getGetter() + ")");
		m_info.getGetter().setAccessible(true);
		return (V) m_info.getGetter().invoke(targetInstance);
	}

	@Override
	public void setValue(@Nullable Object target, @Nullable V value) throws Exception {
		if(target == null)
			throw new IllegalStateException("The 'target' instance object is null");
		Method setter = m_info.getSetter();
		if(setter == null)
			throw new IllegalAccessException("The property " + this + " is read-only.");
		setter.setAccessible(true);
		setter.invoke(target, value);
	}

	/**
	 * This basic implementation returns annotations on the "getter" method of the property, if
	 * available.
	 */
	@Override
	@Nullable
	public <A> A getAnnotation(@NonNull Class<A> annclass) {
		if(Annotation.class.isAssignableFrom(annclass) && m_info != null && m_info.getGetter() != null) {
			Class< ? extends Annotation> aclz = (Class< ? extends Annotation>) annclass;

			return (A) m_info.getGetter().getAnnotation(aclz);
		}
		return null;
	}

	/**
	 * This basic implementation returns all annotations on the "getter" method of the property,
	 * if available. It returns the empty list if nothing is found.
	 */
	@Override
	@NonNull
	public List<Object> getAnnotations() {
		if(m_info != null && m_info.getGetter() != null) {
			@NonNull
			List<Object> res = Arrays.asList((Object[]) m_info.getGetter().getAnnotations());
			return res;
		}
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return m_info.toString();
	}

}
