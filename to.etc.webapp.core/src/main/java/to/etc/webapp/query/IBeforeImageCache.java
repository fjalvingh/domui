package to.etc.webapp.query;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public interface IBeforeImageCache {
	@NonNull <T> T createImage(@NonNull Class<T> classType, @NonNull T instance, boolean loaded) throws Exception;

	boolean wasNew();

	/**
	 * Returns any image, even if uninitialized.
	 * @param source
	 * @return
	 */
	@Nullable <T> T findBeforeImage(@NonNull T source);

	/**
	 * Returns an image and ensures it's initialized.
	 */
	@Nullable <T> T getBeforeImage(@NonNull T instance);

	<T> boolean isLoaded(@NonNull T beforeImage);
}
