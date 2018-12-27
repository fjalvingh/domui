package to.etc.function;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-12-18.
 */
@FunctionalInterface
public interface TriFunctionEx<A,B,C,R> {
	R apply(A a, B b, C c) throws Exception;

	default <V> TriFunctionEx<A, B, C, V> andThen(Function<? super R, ? extends V> after) {
		Objects.requireNonNull(after);
		return (A a, B b, C c) -> after.apply(apply(a, b, c));
	}
}
