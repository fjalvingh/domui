package to.etc.iocular.web;

import to.etc.iocular.def.*;

/**
 * The configuration of the web containers in an Iocular webapp.
 *
 * @author jal
 * Created on Mar 25, 2007
 */
final public class WebConfiguration {
	private ContainerDefinition m_applicationDefinition;

	private ContainerDefinition m_sessionDefinition;

	private ContainerDefinition m_requestDefinition;

	public WebConfiguration(ContainerDefinition applicationDefinition, ContainerDefinition sessionDefinition, ContainerDefinition requestDefinition) {
		m_applicationDefinition = applicationDefinition;
		m_sessionDefinition = sessionDefinition;
		m_requestDefinition = requestDefinition;
	}

	public ContainerDefinition getApplicationDefinition() {
		return m_applicationDefinition;
	}

	public ContainerDefinition getSessionDefinition() {
		return m_sessionDefinition;
	}

	public ContainerDefinition getRequestDefinition() {
		return m_requestDefinition;
	}
}
