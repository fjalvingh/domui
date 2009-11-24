package to.etc.domui.dom;


/**
 * This is the HTML full structure renderer for Microsoft Internet Exploder < 8.x, which tries
 * to work around all of the gazillion bugs and blunders in these pieces of crapware.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 24, 2009
 */
public class MsGarbageHtmlFullRenderer extends HtmlFullRenderer {
	public MsGarbageHtmlFullRenderer(HtmlTagRenderer tagRenderer, IBrowserOutput o) {
		super(tagRenderer, o);
	}


}
