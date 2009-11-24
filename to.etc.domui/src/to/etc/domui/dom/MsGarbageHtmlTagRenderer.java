package to.etc.domui.dom;

import to.etc.domui.server.*;

/**
 * This is the HTML tag renderer for Microsoft Internet Exploder < 8.x, which tries
 * to work around all of the gazillion bugs and blunders in these pieces of crapware.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 24, 2009
 */
public class MsGarbageHtmlTagRenderer extends HtmlTagRenderer {
	public MsGarbageHtmlTagRenderer(BrowserVersion bv, IBrowserOutput o) {
		super(bv, o);
	}


}
