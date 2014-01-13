package to.etc.webapp.query;

import javax.annotation.*;

public interface IBeforeImageCache {
	@Nonnull
	public <T> T createImage(@Nonnull T instance, boolean loaded) throws Exception;

	public boolean wasNew();

	@Nullable
	public <T> T findBeforeImage(@Nonnull T source);

	public <T> boolean isLoaded(@Nonnull T beforeImage);

}
