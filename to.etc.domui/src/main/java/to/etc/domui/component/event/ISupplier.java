package to.etc.domui.component.event;

/**
 * Represents a supplier of a resource.
 * In comparing to java 8 Supplier, this one throws Exception. :(
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #get()}.
 *
 * @param <T> the type of results supplied by this supplier
 *
 * Created by vmijic on 7.4.16..
 */
@FunctionalInterface
public interface ISupplier<T> {

	/**
	 * Gets a resource.
	 *
	 * @return a resource
	 */
	T get() throws Exception;
}
