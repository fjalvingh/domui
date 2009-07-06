package to.etc.domui.dom;

import to.etc.domui.dom.html.*;

/**
 * Indicates that the node itself renders it's attribute delta.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 9, 2009
 */
public interface IHtmlDeltaAttributeRenderer {
	void renderAttributeChanges(HtmlRenderer privity, OptimalDeltaRenderer odt, IBrowserOutput o) throws Exception;
}
