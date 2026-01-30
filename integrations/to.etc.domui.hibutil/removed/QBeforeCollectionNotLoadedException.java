package to.etc.domui.hibernate.beforeimages;

import org.eclipse.jdt.annotation.NonNull;


final public class QBeforeCollectionNotLoadedException extends RuntimeException {
	public QBeforeCollectionNotLoadedException(@NonNull String message) {
		super(message);
	}
}
