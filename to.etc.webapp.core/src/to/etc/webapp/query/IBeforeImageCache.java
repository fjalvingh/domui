package to.etc.webapp.query;

import javax.annotation.*;

public interface IBeforeImageCache {
	@Nonnull
	public <T> T createImage(@Nonnull Class<T> classType, @Nonnull T instance, boolean loaded) throws Exception;

	public boolean wasNew();

	/**
	 * Returns any image, even if uninitialized.
	 * @param source
	 * @return
	 */
	@Nullable
	public <T> T findBeforeImage(@Nonnull T source);

	/**
	 * Returns an image and ensures it's initialized.
	 */
	@Nullable
	public <T> T getBeforeImage(@Nonnull T instance);

	public <T> boolean isLoaded(@Nonnull T beforeImage);
}
