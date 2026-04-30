package to.etc.domui.component.meta.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-05-20.
 */
final public class RecordComponentAccessor<V> implements IPropertyModelAccessor<V> {
	private final RecordComponent m_info;

	public RecordComponentAccessor(RecordComponent info) {
		m_info = info;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Nullable
	@Override
	public V getValue(@Nullable Object targetInstance) throws Exception {
		if(targetInstance == null)
			throw new IllegalStateException("The 'input' object is null (getter method=" + m_info.getName() + ")");
		return (V) m_info.getAccessor().invoke(targetInstance);
	}

	@Override
	public void setValue(@Nullable Object target, @Nullable V value) throws Exception {
		throw new IllegalAccessException("The property " + this + " is read-only.");
	}

	/**
	 * This basic implementation returns annotations on the "getter" method of the property, if
	 * available.
	 */
	@Override
	@Nullable
	public <A extends Annotation> A getAnnotation(@NonNull Class<A> annclass) {
		return m_info.getAnnotation(annclass);
	}

	/**
	 * This basic implementation returns all annotations on the "getter" method of the property,
	 * if available. It returns the empty list if nothing is found.
	 */
	@Override
	@NonNull
	public List<Object> getAnnotations() {
		return Arrays.asList(m_info.getAnnotations());
	}

	@Override
	public String toString() {
		return m_info.toString();
	}

}
