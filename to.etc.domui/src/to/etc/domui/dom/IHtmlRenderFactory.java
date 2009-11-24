package to.etc.domui.dom;

import to.etc.domui.server.*;

/**
 * Factory for creating tag- and full renderers per browser version.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 24, 2009
 */
public interface IHtmlRenderFactory {
	HtmlTagRenderer createTagRenderer(final BrowserVersion bv, final IBrowserOutput o);

	HtmlFullRenderer createFullRenderer(final BrowserVersion bv, final IBrowserOutput o);
}
