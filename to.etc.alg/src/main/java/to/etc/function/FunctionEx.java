package to.etc.function;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 12-1-17.
 */
@FunctionalInterface
public interface FunctionEx<T, R> {

	/**
	 * Applies this function to the given argument.
	 *
	 * @param t the function argument
	 * @return the function result
	 */
	R apply(T t) throws Exception;

	/**
	 * Returns a composed function that first applies the {@code before}
	 * function to its input, and then applies this function to the result.
	 * If evaluation of either function throws an exception, it is relayed to
	 * the caller of the composed function.
	 *
	 * @param <V> the type of input to the {@code before} function, and to the
	 *           composed function
	 * @param before the function to apply before this function is applied
	 * @return a composed function that first applies the {@code before}
	 * function and then applies this function
	 * @throws NullPointerException if before is null
	 *
	 * @see #andThen(FunctionEx)
	 */
	default <V> FunctionEx<V, R> compose(@Nonnull FunctionEx<? super V, ? extends T> before) throws Exception {
		return (V v) -> apply(before.apply(v));
	}

	default <V> FunctionEx<T, V> andThen(FunctionEx<? super R, ? extends V> after) {
		Objects.requireNonNull(after);
		return (T t) -> after.apply(apply(t));
	}

	/**
	 * Returns a function that always returns its input argument.
	 *
	 * @param <T> the type of the input and output objects to the function
	 * @return a function that always returns its input argument
	 */
	static <T> FunctionEx<T, T> identity() {
		return t -> t;
	}
}
