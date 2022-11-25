package to.etc.function;

import org.eclipse.jdt.annotation.NonNull;

@FunctionalInterface
public interface QuadConsumerEx<T, U, V, W> {
	void accept(T t, U u, V v, W w) throws Exception;

	default QuadConsumerEx<T, U, V, W> andThen(@NonNull QuadConsumerEx<? super T, ? super U, ? super V, ? super W> after) throws Exception {
		return (T t, U u, V v, W w) -> { accept(t, u, v, w); after.accept(t, u, v, w); };
	}
}
