package to.etc.function;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Version of Consumer which allows for exceptions, replacing the version
 * written by Oracle's morons 8-(.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 12-1-17.
 */
@FunctionalInterface
public interface ConsumerEx<T> {
	/**
	 * Performs this operation on the given argument.
	 *
	 * @param t the input argument
	 */
	void accept(@NonNull T t) throws Exception;

	/**
	 * Returns a composed {@code Consumer} that performs, in sequence, this
	 * operation followed by the {@code after} operation. If performing either
	 * operation throws an exception, it is relayed to the caller of the
	 * composed operation.  If performing this operation throws an exception,
	 * the {@code after} operation will not be performed.
	 *
	 * @param after the operation to perform after this operation
	 * @return a composed {@code Consumer} that performs in sequence this
	 * operation followed by the {@code after} operation
	 * @throws NullPointerException if {@code after} is null
	 */
	default ConsumerEx<T> andThen(@NonNull ConsumerEx<? super T> after) throws Exception {
		return (T t) -> { accept(t); after.accept(t); };
	}
}
