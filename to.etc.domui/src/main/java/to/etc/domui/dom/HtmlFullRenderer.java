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

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.misc.LiteralXhtml;
import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.dom.header.HeaderContributorEntry;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.NodeVisitorBase;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.PagePhase;
import to.etc.domui.dom.html.TextArea;
import to.etc.domui.dom.html.TextNode;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.themes.ITheme;
import to.etc.domui.util.javascript.JavascriptStmt;
import to.etc.domui.util.resources.IResourceRef;
import to.etc.template.JSTemplate;
import to.etc.template.JSTemplateCompiler;
import to.etc.util.DeveloperOptions;
import to.etc.util.StringTool;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Visits the node tree in such a way that a valid html document is generated.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 17, 2007
 */
public class HtmlFullRenderer extends NodeVisitorBase implements IContributorRenderer {
	//	private BrowserVersion m_browserVersion;

	/** The thingy responsible for rendering the tags, */
	private HtmlTagRenderer m_tagRenderer;

	@NonNull
	private IBrowserOutput m_o;

	private IRequestContext m_ctx;

	private Page m_page;

	private boolean m_xml;

	@NonNull
	private StringBuilder m_createJS = new StringBuilder();

	/** Javascript state change calls. */
	@NonNull
	private StringBuilder m_stateJS = new StringBuilder();

	/** Builder wrapping the above. */
	@NonNull
	private JavascriptStmt m_stateBuilder = new JavascriptStmt(m_stateJS);

	private DomApplication m_application;

	protected HtmlFullRenderer(@NonNull HtmlTagRenderer tagRenderer, @NonNull IBrowserOutput o) {
		//		m_browserVersion = tagRenderer.getBrowser();
		m_tagRenderer = tagRenderer;
		m_o = o;
		// 20090701 jal was ADDS which is WRONG - by definition a FULL render IS a full renderer... This caused SELECT tags to be rendered with domui_selected attributes instead of selected attributes.
		setRenderMode(HtmlRenderMode.FULL);
	}

	/**
	 * Main entrypoint: render the whole page.
	 */
	public void render(IRequestContext ctx, Page page) throws Exception {
		m_ctx = ctx;
		m_page = page;
		m_application = DomApplication.get();
		page.internalSetPhase(PagePhase.FULLRENDER);

		page.setDefaultFocusSource(null);							// Full page's do not use the default focus calculation from a start point.

		if(page.isRenderAsXHTML()) {
			setXml(true);
		}
		IResourceRef rt = page.getRenderTemplate();
		if(null == rt) {
			renderFullPage();
		} else {
			renderTemplatePage(rt);
		}
		m_page.internalSetPhase(PagePhase.NULL);
	}

	private void renderTemplatePage(IResourceRef rt) throws Exception {
		//-- Load the template.
		JSTemplate template;
		try(InputStream is = rt.getInputStream()) {
			JSTemplateCompiler compiler = new JSTemplateCompiler();
			template = compiler.compile(new InputStreamReader(is, "utf-8"), rt.toString());
		}

		Appendable a = new Appendable() {
			@Override
			public Appendable append(CharSequence csq) throws IOException {
				m_o.writeRaw(csq);
				return this;
			}

			@Override
			public Appendable append(CharSequence csq, int start, int end) throws IOException {
				m_o.writeRaw(csq.subSequence(start, end));
				return this;
			}

			@Override
			public Appendable append(char c) throws IOException {
				m_o.writeRaw(String.valueOf(c));
				return this;
			}
		};

		Map<String, Object> map = new HashMap<>();
		map.put("r", this);
		map.put("appUrl", m_ctx.getRelativePath(""));
		template.execute(a, map);
	}

	/**
	 * Called by the template. This should render the page body, but with the body element itself as a "div".
	 */
	public void renderBody() throws Exception {
		UrlPage body = m_page.getBody();
		body.internalSetTag("div");
		body.visit(this);
		renderAfterBody();
	}

	/**
	 * Render a DomUI page as a normal page.
	 */
	private void renderFullPage() throws Exception {
		//		page.build();  jal 20100618 moved to users of full renderer; building and rendering are now separate concerns
		renderHtmlDoctype();
		renderHeadContent();
		renderPageTitle();
		o().closetag("head");

		// Render rest ;-)
		m_page.getBody().visit(this);
		renderAfterBody();

		o().closetag("html");
	}

	private void renderAfterBody() throws Exception {
		/*
		 * Render all attached Javascript in an onReady() function. This code will run
		 * as soon as the body load has completed.
		 */
		o().tag("script");
		o().attr("nonce", getPage().getNonce());
		o().endtag();
		o().text("$(document).ready(function() {");

		//-- If any component has a focus request issue that,
		NodeBase f = m_page.getFocusComponent();
		if(f != null) {
			o().text("WebUI.focus('" + f.getActualID() + "');");
			m_page.setFocusComponent(null);
		}
		if(getCreateJS().length() > 0) {
			o().writeRaw(getCreateJS().toString());
			//				o().text(m_createJS.toString());
		}
		StringBuilder sb = m_page.internalFlushAppendJS();
		if(null != sb)
			o().writeRaw(sb);
		sb = m_page.internalFlushJavascriptStateChanges();
		if(null != sb)
			o().writeRaw(sb);

		/*
		 * We need polling if we have any of the keep alive options on, or when there is an async request.
		 */
		int pollinterval = m_application.calculatePollInterval(m_page.getConversation().isPollCallbackRequired());
		if(pollinterval > 0) {
			o().writeRaw("WebUI.startPolling(" + pollinterval + ");");
		}
		int autorefresh = m_application.getAutoRefreshPollInterval();
		if(autorefresh > 0) {
			o().writeRaw("WebUI.setHideExpired();");
		}

		//-- Add the page name as a parameter to the body, so that WebDriver tests can see which page is loaded.
		o().writeRaw("WebUI.definePageName('" + m_page.getBody().getClass().getName() + "');");

		//		int kit = ctx().getApplication().getKeepAliveInterval();
		//		if(kit > 0) {
		//			o().writeRaw("WebUI.startPingServer(" + kit + ");");
		//		}

		o().text("});");
		o().closetag("script");
	}

	private void renderPageTitle() throws IOException {
		//-- Title is a required entity in head.
		String pageTitle = m_page.getBody().getTitle();
		if(null == pageTitle) {
			pageTitle = m_application.getDefaultPageTitle(m_page.getBody());
			if(null == pageTitle) {
				pageTitle = "DomUI Application";
			}
		}

		o().tag("title");
		o().endtag();
		o().text(pageTitle);
		o().closetag("title");
	}

	/**
	 * Called from template.
	 */
	public void renderHeadContent() throws Exception {
		o().tag("script");
		o().attr("nonce", getPage().getNonce());
		o().endtag();
		if(!isXml())
			o().writeRaw("<!--\n");

		genVar("DomUIpageTag", Integer.toString(m_page.getPageTag()));
		String pb = m_page.getBody().getThemedResourceRURL("THEME/progressbar.gif");
		if(null == pb)
			throw new IllegalStateException("Required resource missing");
		genVar("DomUIProgressURL", StringTool.strToJavascriptString(m_ctx.getRelativePath(pb), true));
		genVar("DomUICID", StringTool.strToJavascriptString(m_page.getConversation().getFullId(), true));
		genVar("DomUIDevel", m_ctx.getApplication().inDevelopmentMode() ? "true" : "false");
		genVar("DomUIappURL", StringTool.strToJavascriptString(m_ctx.getRelativePath(""), true));

		if(!isXml())
			o().writeRaw("\n-->");
		o().writeRaw("\n</script>\n");

		// EXPERIMENTAL SVG/VML support
		if(m_page.isAllowVectorGraphics()) {
			if(m_ctx.getPageParameters().getBrowserVersion().isIE()) {
				o().writeRaw("<style>v\\: * { behavior:url(#default#VML); display:inline-block;} </style>\n"); // Puke....
				o().writeRaw("<xml:namespace ns=\"urn:schemas-microsoft-com:vml\" prefix=\"v\">\n");
			}
		}
		// END EXPERIMENTAL

		renderThemeCSS();
		renderHeadContributors();
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

	@Override
	public boolean isXml() {
		return m_xml;
	}

	public void setXml(boolean xml) {
		m_xml = xml;
	}

	@Override
	@NonNull
	public IBrowserOutput o() {
		return m_o;
	}

	@Override
	public IRequestContext ctx() {
		return m_ctx;
	}

	public Page page() {
		return m_page;
	}

	@Override
	public void visitNodeBase(NodeBase n) throws Exception {
		// 20131206 jal attempt to check phases
		if(DeveloperOptions.isDeveloperWorkstation()) {
			if(!n.isBuilt()) {
				throw new IllegalStateException("Node " + n + " unbuilt in render?");
			}
		} else
			n.build();												// FIXME Should be removed once we prove change is stable

		n.onBeforeFullRender(); // Do pre-node stuff,
		n.renderJavascriptState(m_createJS);
		n.visit(getTagRenderer());
		if(n.getCreateJS() != null)
			m_createJS.append(n.getCreateJS());
		n.internalRenderJavascriptState(m_stateBuilder);
		if(!(n instanceof TextNode)) {
			if(m_xml) {
				if(!n.isRendersOwnClose()) {
					getTagRenderer().renderEndTag(n);
				}
			} else
				m_o.dec();										// 20080626 img et al does not dec()...
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
		// 20131206 jal attempt to check phases
		if(DeveloperOptions.isDeveloperWorkstation()) {
			if(!n.isBuilt()) {
				throw new IllegalStateException("Node " + n + " unbuilt in render?");
			}
		} else
			n.build();												// FIXME Should be removed once we prove change is stable

		n.onBeforeFullRender(); // Do pre-node stuff,
		n.renderJavascriptState(m_createJS);

		boolean indena = o().isIndentEnabled();				// jal 20090903 Save indenting request....
		n.visit(getTagRenderer());							// Ask base renderer to render tag
		if(n.getCreateJS() != null)
			m_createJS.append(n.getCreateJS());
		n.internalRenderJavascriptState(m_stateBuilder);	// Append Javascript state to state buffer
		visitChildren(n);
		getTagRenderer().renderEndTag(n);
		o().setIndentEnabled(indena);						// And restore indenting if tag handler caused it to be cleared.
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

	/**
	 * Render the page's doctype.
	 */
	protected void renderHtmlDoctype() throws Exception {
		if(isXml()) {
			o().writeRaw("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/xhtml1-transitional.dtd\">\n" //
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" //
				+ "<head>\n" //
				+ "<meta http-equiv=\"Content-Type\" content=\"application/xhtml+xml; charset=UTF-8\"/>\n" //
			);
		} else {
			o().writeRaw(
				"<!DOCTYPE html>\n"
			+ 	"<html>\n"					//
			+ "<head>\n"					//
			+ 	"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"	//
			);
		}
	}

	/**
	 * Render the proper themed stylesheet. This will be "style.theme.css" within the current
	 * "theme directory", which is defined by the "currentTheme" in DomApplication.
	 */
	protected void renderThemeCSS() throws Exception {
		ITheme theme = m_ctx.getCurrentTheme();
		String sheet = theme.getStyleSheetName();

		//-- Render style fragments part.
		o().writeRaw("<link rel=\"stylesheet\" type=\"text/css\" nonce=\"" + m_page.getNonce() + "\" href=\"");
		o().writeRaw(ctx().getRelativePath(sheet));
		if(isXml())
			o().writeRaw("\"/>");
		else
			o().writeRaw("\">\n");
		//else
		//	o().writeRaw("\"></link>\n");					No longer needed
	}

	/**
	 * Get all contributor sources and create an ordered list (ordered by the indicated 'order') to render.
	 */
	public void renderHeadContributors() throws Exception {
		List<HeaderContributorEntry> full = new ArrayList<HeaderContributorEntry>(page().getApplication().getHeaderContributorList());
		page().internalAddContributors(full);
		Collections.sort(full, HeaderContributor.C_ENTRY);
		for(HeaderContributorEntry hce : full)
			hce.getContributor().contribute(this);
		page().internalContributorsRendered(); // Mark as rendered.
	}

	@Override
	public void renderLoadCSS(String path, String... options) throws Exception {
		String rurl = m_page.getBody().getThemedResourceRURL(path);
		path = ctx().getRelativePath(rurl);

		//-- render an app-relative url
		o().tag("link");
		o().attr("rel", "stylesheet");
		o().attr("type", "text/css");
		o().rawAttr("href", path);
		o().attr("nonce", m_page.getNonce());

		for(int i = 0; i < options.length; i += 2) {
			o().rawAttr(options[i], options[i + 1]);
		}

		if(isXml())
			o().endAndCloseXmltag();
		else
			o().endtag();
		o().dec();					// do not close
		//o().closetag("link");
	}

	@Override
	public void renderLoadJavascript(@NonNull String path, boolean async, boolean defer) throws Exception {
		if(!path.startsWith("http")) {
			String rurl = m_page.getBody().getThemedResourceRURL(path);
			path = ctx().getRelativePath(rurl);
		}

		//-- render an app-relative url
		o().tag("script");
		o().attr("nonce", getPage().getNonce());
		o().attr("src", path);
		if(async)
			o().writeRaw(" async='async'");
		if(defer)
			o().writeRaw(" defer='defer'");
		o().endtag();
		o().closetag("script");
	}

	private void genVar(String name, String val) throws Exception {
		o().writeRaw("var " + name + "=" + val + ";\n");
	}

	/**
	 * Return all of the Javascript code to create/recreate this page.
	 * @return
	 */
	public StringBuilder getCreateJS() {
		if(m_stateJS.length() > 0) { 							// Stuff present in state buffer too?
			m_createJS.append(';');								// Always add after all create stuff
			m_createJS.append(m_stateJS);
			m_stateJS.setLength(0);
		}
		return m_createJS;
	}

	@NonNull
	@Override
	public Page getPage() {
		return m_page;
	}
}
