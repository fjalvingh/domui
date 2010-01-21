package to.etc.domui.dom;

import java.io.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.util.*;

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

	/**
	 * IE has trouble setting these attributes inline. So we generate delayed Javascript setters for them instead.
	 * @see to.etc.domui.dom.HtmlTagRenderer#renderDisabled(to.etc.domui.dom.html.NodeBase, boolean)
	 */
	@Override
	protected void renderDisabled(NodeBase n, boolean disabled) throws IOException {
		if(!isFullRender())
			addDelayedAttrs(n, "disabled", disabled ? "true" : "false");
		else
			super.renderDisabled(n, disabled);
	}

	/**
	 * IE has trouble setting these attributes inline. So we generate delayed Javascript setters for them instead.
	 * @see to.etc.domui.dom.HtmlTagRenderer#renderReadOnly(to.etc.domui.dom.html.NodeBase, boolean)
	 */
	@Override
	protected void renderReadOnly(NodeBase n, boolean readonly) throws IOException {
		if(!isFullRender())
			addDelayedAttrs(n, "readOnly", readonly ? "true" : "false");
		else
			super.renderReadOnly(n, readonly);
	}

	@Override
	protected void renderChecked(NodeBase n, boolean checked) throws IOException {
		if(!isFullRender())
			addDelayedAttrs(n, "checked", checked ? "true" : "false");
		else
			super.renderChecked(n, checked);
	}

	@Override
	protected void renderSelected(NodeBase n, boolean checked) throws IOException {
		if(!isFullRender())
			addDelayedAttrs(n, "selected", checked ? "true" : "false");
		else
			super.renderSelected(n, checked);
	}

	@Override
	public void visitTextArea(final TextArea n) throws Exception {
		basicNodeRender(n, o());
		if(n.getCols() > 0)
			o().attr("cols", n.getCols());
		if(n.getRows() > 0)
			o().attr("rows", n.getRows());

		renderDiRo(n, n.isDisabled(), n.isReadOnly());

		//-- Fix for bug 627: render textarea content in attribute to prevent zillion of IE fuckups.
		if(getMode() != HtmlRenderMode.FULL) {
			String txt = n.getRawValue();
			if(txt != null) {
				txt = StringTool.strToJavascriptString(txt, false);
				o().attr("domjs_value", txt);
			}
		}
		renderTagend(n, o());
		o().setIndentEnabled(false); // jal 20090923 again: do not indent content (bug 627)
		//		if(n.getRawValue() != null)
		//			o().text(n.getRawValue());
		//		o().closetag(n.getTag());
	}


}
