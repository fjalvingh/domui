package to.etc.util;

import java.lang.reflect.*;

import javax.annotation.*;

/**
 * Generic way to access/represent some kind of property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 15, 2010
 */
public interface IPropertyAccessor {
	@Nonnull
	String getName();

	@Nonnull
	Class< ? > getActualType();

	@Nullable
	Type getActualGenericType();

	@Nullable
	Object getValue(@Nullable Object instance) throws Exception;

	void setValue(@Nullable Object instance, @Nullable Object value) throws Exception;
}
