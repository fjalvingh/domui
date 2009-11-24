package to.etc.domui.dom;

import to.etc.domui.server.*;

/**
 * This is the HTML tag renderer for standard-compliant browsers.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 24, 2009
 */
public class StandardHtmlTagRenderer extends HtmlTagRenderer {
	public StandardHtmlTagRenderer(BrowserVersion bv, IBrowserOutput o) {
		super(bv, o);
	}
}
