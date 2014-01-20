package to.etc.domui.hibernate.beforeimages;

import javax.annotation.*;

public interface IBeforeImageCollectionProxy<T> {
	public void initializeFromOriginal(@Nonnull T source);
}
