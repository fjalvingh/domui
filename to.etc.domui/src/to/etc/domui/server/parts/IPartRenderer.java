package to.etc.domui.server.parts;

import to.etc.domui.server.*;

/**
 * This renders a given part, using whatever context parameters.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 4, 2008
 */
public interface IPartRenderer {
	public void render(RequestContextImpl ctx, String rest) throws Exception;
}
