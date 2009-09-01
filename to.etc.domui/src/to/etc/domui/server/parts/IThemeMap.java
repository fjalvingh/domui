package to.etc.domui.server.parts;

import to.etc.domui.util.resources.*;

/**
 * If a theme is calculated (as in the case for ViewPoint) this is an interface to
 * get values from wherever the calculated things come from.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 31, 2009
 */
public interface IThemeMap extends IModifyableResource {
	String getValue(String key) throws Exception;
}
