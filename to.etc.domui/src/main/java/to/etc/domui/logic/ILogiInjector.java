package to.etc.domui.logic;

import javax.annotation.*;

/**
 * If an injection framework is used, this should handle the two
 * injection methods: object creation and object member injection.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 28, 2014
 */
public interface ILogiInjector {
	@Nonnull <T> T getInstance(@Nonnull Class<T> typeClass);

	<T> void injectMembers(@Nonnull T instance);
}
