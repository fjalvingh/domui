package to.etc.domui.logic;

import org.eclipse.jdt.annotation.NonNull;

/**
 * If an injection framework is used, this should handle the two
 * injection methods: object creation and object member injection.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 28, 2014
 */
public interface ILogiInjector {
	@NonNull <T> T getInstance(@NonNull Class<T> typeClass);

	<T> void injectMembers(@NonNull T instance);
}
