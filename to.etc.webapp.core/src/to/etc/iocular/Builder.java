package to.etc.iocular;

import to.etc.iocular.def.ComponentBuilder;
import to.etc.iocular.def.ContainerDefinition;

/**
 * Thingy used to "build" a configuration for a definition.
 *
 * @author jal
 * Created on Mar 25, 2007
 */
public interface Builder {
	/**
	 * Binds an interface to a specific implementation.
	 * @param <T>
	 * @param intf
	 * @param impl
	 * @return
	 */
	public <T> void				bind(Class<T> intf, Class<T> impl);

	public ComponentBuilder		register();

	public ContainerDefinition	createDefinition();
}
