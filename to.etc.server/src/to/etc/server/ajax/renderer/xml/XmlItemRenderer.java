package to.etc.server.ajax.renderer.xml;

import to.etc.server.ajax.renderer.*;

abstract public class XmlItemRenderer implements ItemRenderer {
	abstract public void render(XmlRenderer r, Object val) throws Exception;

	final public void render(ObjectRenderer or, Object val) throws Exception {
		render((XmlRenderer) or, val);
	}
}
