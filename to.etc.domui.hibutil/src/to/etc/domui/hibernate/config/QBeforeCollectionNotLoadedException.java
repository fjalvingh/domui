package to.etc.domui.hibernate.config;

import javax.annotation.*;


final public class QBeforeCollectionNotLoadedException extends RuntimeException {
	public QBeforeCollectionNotLoadedException(@Nonnull String message) {
		super(message);
	}
}
