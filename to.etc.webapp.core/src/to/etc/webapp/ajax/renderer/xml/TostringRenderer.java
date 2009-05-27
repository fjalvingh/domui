package to.etc.webapp.ajax.renderer.xml;

public class TostringRenderer extends XmlItemRenderer {
	@Override
	public void render(XmlRenderer r, Object val) throws Exception {
		r.xw().cdata(val.toString());
	}
}
