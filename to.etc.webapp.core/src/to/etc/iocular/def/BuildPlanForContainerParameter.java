package to.etc.iocular.def;

import java.io.*;
import java.util.*;

import to.etc.iocular.container.*;
import to.etc.util.*;

/**
 * A build plan definition to obtain a parameter-based object from a container. This version merely obtains
 * a simple object from the container.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 15, 2009
 */
public class BuildPlanForContainerParameter extends AbstractBuildPlan {
	private String	m_ident;

	public BuildPlanForContainerParameter(final Class< ? > actualType, final List<String> nameList) {
		if(nameList.size() == 0)
			m_ident = actualType.getName();
		else
			m_ident = nameList.toString();
	}

	@Override
	public void dump(final IndentWriter iw) throws IOException {
		iw.println("PARAMETER "+m_ident+": not built but must be present in Container");
	}

	/**
	 * Obtain this parameter from the container.
	 *
	 * @see to.etc.iocular.container.BuildPlan#getObject(to.etc.iocular.container.BasicContainer)
	 */
	@Override
	public Object getObject(final BasicContainer c) throws Exception {
		throw new IocContainerException(c, "The container parameter '"+m_ident+"' is not set");
	}

	@Override
	public boolean needsStaticInitialization() {
		return false;
	}

	@Override
	public void staticStart(final BasicContainer c) throws Exception {
	}
}
