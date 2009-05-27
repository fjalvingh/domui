package to.etc.webapp.ajax.renderer.json;

import to.etc.webapp.ajax.renderer.*;

abstract public class JSONItemRenderer implements ItemRenderer {
	abstract public void render(JSONRenderer r, Object val) throws Exception;

	final public void render(final ObjectRenderer or, final Object val) throws Exception {
		render((JSONRenderer) or, val);
	}
}
