package to.etc.domui.hibernate.beforeimages;

import org.eclipse.jdt.annotation.NonNull;

public interface IBeforeImageCollectionProxy<T> {
	void initializeFromOriginal(@NonNull T source);
}
