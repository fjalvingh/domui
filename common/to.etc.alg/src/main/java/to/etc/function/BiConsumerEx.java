package to.etc.function;

import org.eclipse.jdt.annotation.NonNull;

@FunctionalInterface
public interface BiConsumerEx<T, U> {
	void accept(T t, U u) throws Exception;

	default BiConsumerEx<T, U> andThen(@NonNull BiConsumerEx<? super T, ? super U> after) throws Exception {
		return (T t, U u) -> { accept(t, u); after.accept(t, u); };
	}
}
