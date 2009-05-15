package to.etc.iocular.def;

import java.io.*;

import to.etc.iocular.container.*;
import to.etc.util.*;

/**
 * A build plan definition to obtain a parameter-based object from a container. This version merely obtains
 * a simple object from the container.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 15, 2009
 */
public class ContainerParameterBuildPlan implements BuildPlan {
	public void dump(final IndentWriter iw) throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * Obtain this parameter from the container.
	 *
	 * @see to.etc.iocular.container.BuildPlan#getObject(to.etc.iocular.container.BasicContainer)
	 */
	public Object getObject(final BasicContainer c) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean needsStaticInitialization() {
		return false;
	}

	public void staticStart(final BasicContainer c) throws Exception {
	}
}
