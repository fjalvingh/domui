package to.etc.domui.server.parts;

import to.etc.domui.util.*;
import to.etc.domui.util.resources.*;

/**
 * UNSTABLE INTERFACE Temp thing to allow editing style.css for ViewPoint.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 31, 2009
 */
public interface IResourceModifier {
	boolean accepts(String rurl);

	void generate(PartResponse pr, String rurl, IResourceRef ires, ResourceDependencyList rdl) throws Exception;
}
