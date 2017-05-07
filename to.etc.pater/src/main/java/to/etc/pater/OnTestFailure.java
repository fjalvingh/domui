package to.etc.pater;

import java.lang.annotation.*;

/**
 * A method annotated with this annotation (and which has one of the supported signatures) will
 * be called as soon as a test fails. Supported signatures:
 * <pre>
 * 	public void theMethod(@Nonnull java.lang.reflect.Method method) throws Exception;
 * 	public void theMethod(@Nonnull java.lang.reflect.Method method, @Nonnull List<Throwable> errorList) throws Exception;
 * </pre>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 9, 2014
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnTestFailure {
}
