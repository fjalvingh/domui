package to.etc.iocular;

/**
 * <p>An actual store of instantiated resources, part of a container tree. A container
 * has a <i>definition</i> determining the possible object wirings, and a <i>store</i>
 * containing the already instantiated objects.</p>
 *
 * <p>Instantiating a container is a very cheap operation. Objects defined for the container
 * are only retrieved/instantiated when they are needed. The container itself is configured
 * with a predefined ContainerDefinition; the definition is typically created at application
 * startup and *is* expensive to create.</p>
 *
 * @author jal
 * Created on Mar 25, 2007
 */
public interface Container {
	public void		start();

	public void		destroy();

	/**
	 * Return the 'unnamed' object with the specified class from this container. If the
	 * object is not known this will return null!! This should not normally be used; use
	 * the 'getObject' call with the same signature instead: it throws an exception when
	 * the associated object is not found.
	 *
	 * @param theClass
	 * @return
	 */
	public <T> T	findObject(Class<T> theClass);

	/**
	 * Return the 'unnamed' object with the specified class from this container. If the
	 * object is not known this will throw a IocNotFoundException.
	 *
	 * @param theClass
	 * @return
	 */
	public <T> T	getObject(Class<T> theClass) throws Exception;

	public <T> T	findObject(String name, Class<T> theClass);

	public <T> T	getObject(String name, Class<T> theClass) throws Exception;
}
