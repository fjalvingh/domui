package to.etc.iocular;

import to.etc.iocular.def.ContainerDefinition;

/**
 * @author  <a href="mailto:jal@etc.to">Frits Jalvingh</a>  Created on May 4, 2007
 */
public interface Configurator {
	public ContainerDefinition		getContainerDefinition() throws Exception;
}
