package to.etc.domui.themes;

import to.etc.domui.server.*;

/**
 * Factory which will create a theme instance from it's source files.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 11, 2011
 */
public interface IThemer {
	ITheme loadTheme(DomApplication da) throws Exception;
}
