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

import java.util.*;

import to.etc.domui.component.misc.*;
import to.etc.domui.dom.header.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.util.*;

/**
 * Visits the node tree in such a way that a valid html document is generated.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 17, 2007
 */
public class HtmlFullRenderer extends NodeVisitorBase {
	//	private BrowserVersion m_browserVersion;

	/** The thingy responsible for rendering the tags, */
	private HtmlTagRenderer m_tagRenderer;

	private IBrowserOutput m_o;

	private IRequestContext m_ctx;

	private Page m_page;

	private boolean m_xml;

	private StringBuilder m_createJS = new StringBuilder();

	private StringBuilder m_stateJS = new StringBuilder();

	protected HtmlFullRenderer(HtmlTagRenderer tagRenderer, IBrowserOutput o) {
		//		m_browserVersion = tagRenderer.getBrowser();
		m_tagRenderer = tagRenderer;
		m_o = o;
		// 20090701 jal was ADDS which is WRONG - by definition a FULL render IS a full renderer... This caused SELECT tags to be rendered with domui_selected attributes instead of selected attributes.
		setRenderMode(HtmlRenderMode.FULL);
	}

	public HtmlTagRenderer getTagRenderer() {
		// 20090701 jal was ADDS which is WRONG - by definition a FULL render IS a full renderer... This caused SELECT tags to be rendered with domui_selected attributes instead of selected attributes.
		// 20091002 jal removed, make callers specify render mode...
		//		m_tagRenderer.setRenderMode(HtmlRenderMode.FULL); // All nodes from the full renderer are NEW by definition.
		return m_tagRenderer;
	}

	public HtmlRenderMode getMode() {
		return m_tagRenderer.getMode();
	}

	public void setRenderMode(HtmlRenderMode m) {
		m_tagRenderer.setRenderMode(m);
	}

	public boolean isXml() {
		return m_xml;
	}

	public void setXml(boolean xml) {
		m_xml = xml;
	}

	public IBrowserOutput o() {
		return m_o;
	}

	public IRequestContext ctx() {
		return m_ctx;
	}

	public Page page() {
		return m_page;
	}

	@Override
	public void visitNodeBase(NodeBase n) throws Exception {
		n.build();
		n.onBeforeFullRender(); // Do pre-node stuff,
		n.visit(getTagRenderer());
		if(n.getCreateJS() != null)
			m_createJS.append(n.getCreateJS());
		n.renderJavascriptState(m_stateJS); // Append Javascript state to state buffer
		if(!(n instanceof TextNode)) {
			if(m_xml) {
				if(!n.isRendersOwnClose()) {
					getTagRenderer().renderEndTag(n);
				}
			} else
				m_o.dec(); // 20080626 img et al does not dec()...
		}
		n.internalClearDelta();
		checkForFocus(n);
	}

	/**
	 * Overridden because this is a NodeBase node which MUST be terminated with a /div, always.
	 * @see to.etc.domui.dom.html.NodeVisitorBase#visitLiteralXhtml(to.etc.domui.component.misc.LiteralXhtml)
	 */
	@Override
	@Deprecated
	public void visitLiteralXhtml(LiteralXhtml n) throws Exception {
		visitNodeBase(n); // Handle most thingies we need to do,
		if(!m_xml) {
			//-- In HTML mode we MUST end this tag, and we need to inc() because the visitNodeBase() has decremented..
			m_o.inc();
			getTagRenderer().renderEndTag(n); // Force close the tag in HTML mode.
		}
	}

	/**
	 * Overridden to fix bug 627; this prevents embedding content in textarea and renders the value as
	 * an attribute.
	 *
	 * @see to.etc.domui.dom.html.NodeVisitorBase#visitTextArea(to.etc.domui.dom.html.TextArea)
	 */
	@Override
	public void visitTextArea(TextArea n) throws Exception {
		if(getMode() == HtmlRenderMode.FULL) { // In FULL mode render content inside textarea goddamnit
			visitNodeContainer(n);
			return;
		}

		visitNodeBase(n);
		o().setIndentEnabled(true); // jal 20091002 indent when rendering js attribute
		//		getTagRenderer().renderEndTag(n);
	}

	@Override
	public void visitNodeContainer(NodeContainer n) throws Exception {
		n.build();
		n.onBeforeFullRender(); // Do pre-node stuff,

		boolean indena = o().isIndentEnabled(); // jal 20090903 Save indenting request....
		n.visit(getTagRenderer()); // Ask base renderer to render tag
		if(n.getCreateJS() != null)
			m_createJS.append(n.getCreateJS());
		n.renderJavascriptState(m_stateJS); // Append Javascript state to state buffer
		visitChildren(n);
		getTagRenderer().renderEndTag(n);
		o().setIndentEnabled(indena); // And restore indenting if tag handler caused it to be cleared.
		n.internalClearDelta();
		checkForFocus(n);
	}

	@Override
	public void visitChildren(NodeContainer c) throws Exception {
		if(c instanceof IRenderNBSPIfEmpty) {
			if(c.getChildCount() == 0) {
				//-- jal 20091223 If the TD is fully-empty add a nbsp to prevent IE from misrendering the cell.
				//-- vmijic 20100528 In case of null DisplayValue value render &nbsp; so that height of display value can be correct.
				o().text("\u00a0"); // Render a nbsp. DO NOT USE THE ENTITY - IT DOES NOT EXIST IN XML.
				return;
			}
		}
		super.visitChildren(c);
	}

	/*
	 * Handle default input focus: if no focus is set AND this is an input control -> set focus.
	 */
	private void checkForFocus(NodeBase n) {
		if(m_tagRenderer.getMode() != HtmlRenderMode.FULL)
			return;
		if(n.getPage().getFocusComponent() != null)
			return;
		if(n.isFocusable())
			n.getPage().setFocusComponent(n);
	}

	protected void renderPageHeader() throws Exception {
		if(isXml()) {
			o().writeRaw("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/xhtml1-transitional.dtd\">\n" //
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" //
				+ "<head>\n" //
				+ "<meta http-equiv=\"Content-Type\" content=\"application/xhtml+xml; charset=UTF-8\"/>\n" //
			);
		} else {
			o().writeRaw(
				"<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n" + "<html>\n" + "<head>\n"
					+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
		}
	}

	public void renderThemeCSS() throws Exception {
		String sheet = m_ctx.getApplication().getTheme(null).getStylesheet();

		//-- Render style fragments part.
		o().writeRaw("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
		o().writeRaw(ctx().getRelativePath(sheet));
		if(isXml())
			o().writeRaw("\"/>");
		else
			o().writeRaw("\"></link>\n");
	}

	/**
	 * Get all contributor sources and create an ordered list (ordered by the indicated 'order') to render.
	 * @throws Exception
	 */
	public void renderHeadContributors() throws Exception {
		List<HeaderContributorEntry> full = new ArrayList<HeaderContributorEntry>(page().getApplication().getHeaderContributorList());
		page().internalAddContributors(full);
		Collections.sort(full, HeaderContributor.C_ENTRY);
		for(HeaderContributorEntry hce : full)
			hce.getContributor().contribute(this);
		page().internalContributorsRendered(); // Mark as rendered.
	}

	public void renderLoadCSS(String path) throws Exception {
		//-- render an app-relative url
		o().tag("link");
		o().attr("rel", "stylesheet");
		o().attr("type", "text/css");
		o().attr("href", ctx().getRelativePath(path));
		o().endtag();
		o().closetag("link");
	}

	public void renderLoadJavascript(String path) throws Exception {
		//-- render an app-relative url
		o().tag("script");
		o().attr("language", "javascript");
		o().attr("src", ctx().getRelativePath(path));
		o().endtag();
		o().closetag("script");
	}

	private void genVar(String name, String val) throws Exception {
		o().writeRaw("var " + name + "=" + val + ";\n");
	}

	public void render(IRequestContext ctx, Page page) throws Exception {
		m_ctx = ctx;
		m_page = page;

		page.calculateDefaultFocus(null);							// Full page's do not use the default focus calculation from a start point.

		if(page.isRenderAsXHTML()) {
			setXml(true);
		}
//		page.build();  jal 20100618 moved to users of full renderer; building and rendering are now separate concerns

		renderPageHeader();
		//		o().writeRaw(
		//			"<script language=\"javascript\"><!--\n"
		//		+	"var DomUIpageTag="+page.getPageTag()+";\n"
		//		+	"var DomUIThemeURL="+StringTool.strToJavascriptString(ctx.getRelativePath( ctx.getRelativeThemePath("") ), true)+";\n"
		//		+	"--></script>\n"
		//		);
		o().writeRaw("<script language=\"javascript\">");
		if(!isXml())
			o().writeRaw("<!--\n");

		genVar("DomUIpageTag", Integer.toString(page.getPageTag()));
		String pb = DomApplication.get().getThemedResourceRURL("ICON/progressbar.gif");
		if(null == pb)
			throw new IllegalStateException("Required resource missing");
		DomApplication application = DomApplication.get();
		genVar("DomUIProgressURL", StringTool.strToJavascriptString(ctx.getRelativePath(pb), true));
		genVar("DomUICID", StringTool.strToJavascriptString(page.getConversation().getFullId(), true));
		genVar("DomUIDevel", ctx.getApplication().inDevelopmentMode() ? "true" : "false");
		genVar("DomUIappURL", StringTool.strToJavascriptString(ctx.getRelativePath(""), true));

		if(!isXml())
			o().writeRaw("\n-->");
		o().writeRaw("\n</script>\n");

		// EXPERIMENTAL SVG/VML support
		if(m_page.isAllowVectorGraphics()) {
			if(ctx.getBrowserVersion().isIE()) {
				o().writeRaw("<style>v\\: * { behavior:url(#default#VML); display:inline-block;} </style>\n"); // Puke....
				o().writeRaw("<xml:namespace ns=\"urn:schemas-microsoft-com:vml\" prefix=\"v\">\n");
			}
		}
		// END EXPERIMENTAL

		renderThemeCSS();
		renderHeadContributors();
		if(page.getBody().getTitle() != null) {
			o().tag("title");
			o().endtag();
			o().text(page.getBody().getTitle());
			o().closetag("title");
		}
		o().closetag("head");

		// Render rest ;-)
		page.getBody().visit(this);

		/*
		 * Render all attached Javascript in an onReady() function. This code will run
		 * as soon as the body load has completed.
		 */
		StringBuilder sq = page.internalGetAppendedJS();
		o().tag("script");
		o().attr("language", "javascript");
		o().endtag();
		o().text("$(document).ready(function() {");

		//-- If any component has a focus request issue that,
		NodeBase f = page.getFocusComponent();
		if(f != null) {
			o().text("WebUI.focus('" + f.getActualID() + "');");
			page.setFocusComponent(null);
		}
		if(getCreateJS().length() > 0) {
			o().writeRaw(getCreateJS().toString());
			//				o().text(m_createJS.toString());
		}
		if(sq != null) {
			o().writeRaw(sq.toString());
			//				o().text(sq.toString());
		}

		/*
		 * We need polling if we have any of the keep alive options on, or when there is an async request.
		 */
		int pollinterval = application.calculatePollInterval(page.getConversation().isPollCallbackRequired());
		if(pollinterval > 0) {
			o().writeRaw("WebUI.startPolling(" + pollinterval + ");");
		}
		int autorefresh = application.getAutoRefreshInterval();
		if(autorefresh > 0) {
			o().writeRaw("WebUI.setHideExpired();");
		}


		//		int kit = ctx().getApplication().getKeepAliveInterval();
		//		if(kit > 0) {
		//			o().writeRaw("WebUI.startPingServer(" + kit + ");");
		//		}

		o().text("});");
		o().closetag("script");
		o().closetag("html");
	}

	/**
	 * Return all of the Javascript code to create/recreate this page.
	 * @return
	 */
	public StringBuilder getCreateJS() {
		if(m_stateJS.length() > 0) { // Stuff present in state buffer too?
			m_createJS.append(';'); // Always add after all create stuff
			m_createJS.append(m_stateJS);
			m_stateJS.setLength(0);
		}
		return m_createJS;
	}
}
