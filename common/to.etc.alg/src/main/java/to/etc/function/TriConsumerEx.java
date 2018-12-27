package to.etc.function;

import org.eclipse.jdt.annotation.NonNull;

@FunctionalInterface
public interface TriConsumerEx<T, U, V> {
	void accept(T t, U u, V v) throws Exception;

	default TriConsumerEx<T, U, V> andThen(@NonNull TriConsumerEx<? super T, ? super U, ? super V> after) throws Exception {
		return (T t, U u, V v) -> { accept(t, u, v); after.accept(t, u, v); };
	}
}
