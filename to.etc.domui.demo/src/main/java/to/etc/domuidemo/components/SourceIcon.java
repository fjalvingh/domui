package to.etc.domuidemo.components;

import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.Div;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-8-17.
 */
final public class SourceIcon extends Div {
	@Override public void createContent() throws Exception {
		setDisplay(DisplayType.INLINE_BLOCK);
		setCssClass("dm-src-icon");
	}
}
