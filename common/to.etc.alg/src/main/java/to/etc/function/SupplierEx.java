package to.etc.function;

/**
 * Version of Supplier which allows for exceptions, replacing the version
 * written by Oracle's morons 8-(.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 12-1-17.
 */
public interface SupplierEx<T> {
	/**
	 * Gets a result. Deliberately left without nullity specification.
	 *
	 * @return a result
	 */
	T get() throws Exception;
}
