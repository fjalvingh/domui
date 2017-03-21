package to.etc.function;

import javax.annotation.Nonnull;

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
	void accept(@Nonnull T t) throws Exception;

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
	default ConsumerEx<T> andThen(@Nonnull ConsumerEx<? super T> after) throws Exception {
		return (T t) -> { accept(t); after.accept(t); };
	}
}
