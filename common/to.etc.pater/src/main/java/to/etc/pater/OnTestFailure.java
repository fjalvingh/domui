package to.etc.pater;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A method annotated with this annotation (and which has one of the supported signatures) will
 * be called as soon as a test fails. Supported signatures:
 * <pre>
 * 	public void theMethod(@NonNull java.lang.reflect.Method method) throws Exception;
 * 	public void theMethod(@NonNull java.lang.reflect.Method method, @NonNull List<Throwable> errorList) throws Exception;
 * </pre>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 9, 2014
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnTestFailure {
}
