/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
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
	public MsGarbageHtmlTagRenderer(BrowserVersion bv, IBrowserOutput o, boolean uiTestMode) {
		super(bv, o, uiTestMode);
	}

	/**
	 * IE has trouble setting these attributes inline. So we generate delayed Javascript setters for them instead.
	 * @see to.etc.domui.dom.HtmlTagRenderer#renderDisabled(to.etc.domui.dom.html.NodeBase, boolean)
	 */
	@Override
	protected void renderDisabled(NodeBase n, boolean disabled) throws IOException {
		if(!isFullRender() && getBrowser().getMajorVersion() < 11)
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
		if(!isFullRender() && getBrowser().getMajorVersion() < 11)
			addDelayedAttrs(n, "readOnly", readonly ? "true" : "false");
		else
			super.renderReadOnly(n, readonly);
	}

	@Override
	protected void renderChecked(NodeBase n, boolean checked) throws IOException {
		if(!isFullRender() && getBrowser().getMajorVersion() < 11)
			addDelayedAttrs(n, "checked", checked ? "true" : "false");
		else
			super.renderChecked(n, checked);
	}

	@Override
	protected void renderSelected(NodeBase n, boolean checked) throws IOException {
		if(!isFullRender() && getBrowser().getMajorVersion() < 11)
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
		if(n.getMaxLength() > 0) {
			o().attr("maxlength", n.getMaxLength());				// Not valid for html < 5, handled by Javascript
		}
		renderTagend(n, o());
		o().setIndentEnabled(false); // jal 20090923 again: do not indent content (bug 627)
		//		if(n.getRawValue() != null)
		//			o().text(n.getRawValue());
		//		o().closetag(n.getTag());
	}


}
