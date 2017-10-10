package to.etc.webapp.nls;

/**
 * Interface to be used on message bundle enums.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-10-17.
 */
public interface IBundleCode {
	String name();

	default BundleRef getBundle() {
		return BundleRef.create(getClass(), getClass().getSimpleName());
	}
}
