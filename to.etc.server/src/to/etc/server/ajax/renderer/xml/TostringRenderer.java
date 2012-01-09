package to.etc.server.ajax.renderer.xml;

public class TostringRenderer extends XmlItemRenderer {
	@Override
	public void render(XmlRenderer r, Object val) throws Exception {
		r.xw().cdata(val.toString());
	}
}
