package to.etc.webapp.query;

import javax.annotation.*;

public interface IBeforeImageCache {
	@Nonnull <T> T createImage(@Nonnull Class<T> classType, @Nonnull T instance, boolean loaded) throws Exception;

	boolean wasNew();

	/**
	 * Returns any image, even if uninitialized.
	 * @param source
	 * @return
	 */
	@Nullable <T> T findBeforeImage(@Nonnull T source);

	/**
	 * Returns an image and ensures it's initialized.
	 */
	@Nullable <T> T getBeforeImage(@Nonnull T instance);

	<T> boolean isLoaded(@Nonnull T beforeImage);
}
