package to.etc.server.ajax.renderer.json;

import to.etc.server.ajax.renderer.*;

abstract public class JSONItemRenderer implements ItemRenderer {
	abstract public void render(JSONRenderer r, Object val) throws Exception;

	final public void render(ObjectRenderer or, Object val) throws Exception {
		render((JSONRenderer) or, val);
	}
}
