package to.etc.webapp.ajax.renderer.xml;

import to.etc.webapp.ajax.renderer.*;

abstract public class XmlItemRenderer implements ItemRenderer {
	abstract public void render(XmlRenderer r, Object val) throws Exception;

	final public void render(final ObjectRenderer or, final Object val) throws Exception {
		render((XmlRenderer) or, val);
	}
}
