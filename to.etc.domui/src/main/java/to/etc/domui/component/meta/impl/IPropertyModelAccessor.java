package to.etc.domui.component.meta.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-05-20.
 */
public interface IPropertyModelAccessor<V> {
	boolean isMutable();

	void setValue(@Nullable Object targetClass, @Nullable V value) throws Exception;

	@Nullable
	V getValue(@Nullable Object targetInstance) throws Exception;

	@Nullable
	<A> A getAnnotation(@NonNull Class<A> annclass);

	@NonNull
	List<Object> getAnnotations();
}
