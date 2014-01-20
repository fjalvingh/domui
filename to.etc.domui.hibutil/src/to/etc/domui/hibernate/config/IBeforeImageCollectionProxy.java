package to.etc.domui.hibernate.config;

import javax.annotation.*;

public interface IBeforeImageCollectionProxy<T> {
	public void initializeFromOriginal(@Nonnull T source);
}
