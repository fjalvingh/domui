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
	void		start();

	void		destroy();

	/**
	 * Return the 'unnamed' object with the specified class from this container. If the
	 * object is not known this will return null!! This should not normally be used; use
	 * the 'getObject' call with the same signature instead: it throws an exception when
	 * the associated object is not found.
	 *
	 * @param theClass
	 * @return
	 */
	<T> T	findObject(Class<T> theClass);

	/**
	 * Return the 'unnamed' object with the specified class from this container. If the
	 * object is not known this will throw a IocNotFoundException.
	 *
	 * @param theClass
	 * @return
	 */
	<T> T	getObject(Class<T> theClass) throws Exception;

	<T> T	findObject(String name, Class<T> theClass);

	<T> T	getObject(String name, Class<T> theClass) throws Exception;
	
	/**
	 * Set a container parameter object. The parameter to set is inferred from the object type.
	 * @param instance
	 */
	void	setParameter(final Object instance);

	/**
	 * Set the parameter as identified by it's target class to the specified instance. This instance CAN
	 * be null, in which case null will be set into contructors and/or setters dependent on this parameter.
	 * @param clz
	 * @param instance
	 */
	void	setParameter(final Class<?> theClass, final Object instance);

	/**
	 * Sets the parameter with the specified name to the instance passed. This instance CAN
	 * be null, in which case null will be set into contructors and/or setters dependent on
	 * this parameter.
	 * @param name
	 * @param instance
	 */
	void	setParameter(final String name, final Object instance);
}
