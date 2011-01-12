package to.etc.domui.themes;

import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.util.resources.*;

/**
 * This handles all URLs that start with "currentTheme/", and locates the appropriate resources
 * that belong there.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 12, 2011
 */
public class CurrentThemePart implements IBufferedPartFactory, IUrlPart {
	/**
	 * Accept all RURLs that start with "currentTheme/".
	 * @see to.etc.domui.server.parts.IUrlPart#accepts(java.lang.String)
	 */
	@Override
	public boolean accepts(String rurl) {
		return rurl.startsWith("currentTheme/");
	}

	@Override
	public Object decodeKey(String rurl, IExtendedParameterInfo param) throws Exception {
		return rurl;
	}

	@Override
	public void generate(PartResponse pr, DomApplication da, Object key, ResourceDependencyList rdl) throws Exception {

	}

}
