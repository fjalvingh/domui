package to.etc.domui.dom;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.component.misc.LiteralXhtml;
import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.dom.header.HeaderContributorEntry;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.NodeVisitorBase;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.TextArea;
import to.etc.domui.dom.html.TextNode;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.parts.TempFilePart;
import to.etc.domui.parts.TempFilePart.Disposition;
import to.etc.domui.server.BrowserVersion;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.server.parts.PartData;
import to.etc.domui.state.PageParameters;
import to.etc.domui.state.UIContext;
import to.etc.domui.themes.ITheme;
import to.etc.domui.themes.ThemeResourceFactory;
import to.etc.domui.trouble.ThingyNotFoundException;
import to.etc.domui.util.javascript.JavascriptStmt;
import to.etc.domui.util.resources.IResourceRef;
import to.etc.domui.util.resources.ResourceDependencyList;
import to.etc.util.ByteBufferInputStream;
import to.etc.util.DeveloperOptions;
import to.etc.util.FileTool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 9-3-18.
 */
public class HtmlFileRenderer extends NodeVisitorBase implements IContributorRenderer {
	static private final Logger LOG = LoggerFactory.getLogger(HtmlFileRenderer.class);

	private final NodeContainer m_rootNode;

	private final Page m_page;

	private boolean m_xml;

	/**
	 * The thingy responsible for rendering the tags,
	 */
	private HtmlTagRenderer m_tagRenderer;

	private String m_pageTitle = "DomUI report";

	@NonNull
	private IBrowserOutput m_o;

	private IRequestContext m_ctx;

	@NonNull
	private StringBuilder m_createJS = new StringBuilder();

	/**
	 * Javascript state change calls.
	 */
	@NonNull
	private StringBuilder m_stateJS = new StringBuilder();

	private final List<HeaderContributor> m_contributors = new ArrayList<>();

	/**
	 * Builder wrapping the above.
	 */
	@NonNull
	private JavascriptStmt m_stateBuilder = new JavascriptStmt(m_stateJS);

	public final static class Builder {
		private final NodeContainer m_root;

		private final List<HeaderContributor> m_contributors = new ArrayList<>();

		public Builder(NodeContainer rootNode) {
			m_root = rootNode;
		}

		public Builder add(HeaderContributor hc) {
			m_contributors.add(hc);
			return this;
		}

		public void download(@NonNull String fileName) throws Exception {
			File tempFile = File.createTempFile("ht-", ".html");
			try(OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(tempFile), "utf-8")) {
				HtmlFileRenderer fr = HtmlFileRenderer.create(osw, m_root);
				fr.addHeaderContributors(m_contributors);
				fr.render(UIContext.getRequestContext());

				TempFilePart.createDownloadAction(m_root, tempFile, "application/octet-stream", Disposition.Attachment, fileName);
			}
		}
	}

	public void addHeaderContributors(List<HeaderContributor> contributors) {
		m_contributors.addAll(contributors);
	}

	public void addHeaderContributor(HeaderContributor contributor) {
		m_contributors.add(contributor);
	}

	static public HtmlFileRenderer create(@NonNull Writer output, @NonNull NodeContainer rootNode) throws Exception {
		FastXmlOutputWriter out = new FastXmlOutputWriter(output);
		HtmlTagRenderer rr = new StandardHtmlTagRenderer(BrowserVersion.INSTANCE, out, false);
		rr.setRenderInline(true);
		HtmlFileRenderer fr = new HtmlFileRenderer(rr, out, rootNode);
		return fr;
	}

	static public HtmlFileRenderer create(@NonNull Writer output, @NonNull Page sourcePage, @NonNull NodeContainer rootNode) throws Exception {
		NodeContainer renderRoot = rootNode;
		if(! rootNode.isAttached()) {
			UrlPage body;
			if(rootNode instanceof UrlPage) {
				body = (UrlPage) rootNode;
			} else {
				body = new UrlPage();
			}

			Page page = new Page(body);
			if(body != rootNode)
				body.add(rootNode);

			page.internalInitialize(sourcePage.getPageParameters(), sourcePage.getConversation());
			renderRoot = body;
		}

		FastXmlOutputWriter out = new FastXmlOutputWriter(output);
		HtmlTagRenderer rr = new StandardHtmlTagRenderer(BrowserVersion.INSTANCE, out, false);
		rr.setRenderInline(true);
		HtmlFileRenderer fr = new HtmlFileRenderer(rr, out, renderRoot);
		return fr;
	}


	static public void download(@NonNull NodeContainer resultFragment, @NonNull String fileName) throws Exception {
		File tempFile = File.createTempFile("ht-", ".html");
		try(OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(tempFile), "utf-8")) {
			HtmlFileRenderer fr = HtmlFileRenderer.create(osw, resultFragment);
			fr.render(UIContext.getRequestContext());

			TempFilePart.createDownloadAction(resultFragment, tempFile, "application/octet-stream", Disposition.Attachment, fileName);
		}
	}

	static public Builder on(NodeContainer rootNode) {
		return new Builder(rootNode);
	}

	protected HtmlFileRenderer(@NonNull HtmlTagRenderer tagRenderer, @NonNull IBrowserOutput output, NodeContainer rootNode) throws Exception {
		m_tagRenderer = tagRenderer;
		m_o = output;
		m_rootNode = rootNode;
		setRenderMode(HtmlRenderMode.FULL);

		if(! rootNode.isAttached()) {
			UrlPage body = new UrlPage();
			Page page = new Page(body);
			body.add(rootNode);
		}
		m_page = rootNode.getPage();
	}

	/**
	 * Main entrypoint: render the whole page.
	 */
	public void render(IRequestContext ctx) throws Exception {
		m_ctx = ctx;
		m_page.internalFullBuild();

		renderPageHeader();
		renderThemeCSS();
		renderHeadContributors();

		//-- Title is a required entity in head.
		o().tag("title");
		o().endtag();
		o().text(m_pageTitle);
		o().closetag("title");
		o().closetag("head");

		o().tag("body");
		o().endtag();

		m_rootNode.visit(this);

		o().tag("script");
		o().attr("language", "javascript");
		o().endtag();
		o().writeRaw(getCreateJS());
		o().closetag("script");

		o().closetag("body");

		/*
		 * Render all attached Javascript in an onReady() function. This code will run
		 * as soon as the body load has completed.
		 */
		//o().tag("script");
		//o().endtag();
		//o().text("$(document).ready(function() {");
		//
		//
		//o().text("});");
		//o().closetag("script");
		o().closetag("html");
	}

	public HtmlTagRenderer getTagRenderer() {
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

	@Override
	public void visitNodeBase(NodeBase n) throws Exception {
		// 20131206 jal attempt to check phases
		if(DeveloperOptions.isDeveloperWorkstation()) {
			if(!n.isBuilt()) {
				throw new IllegalStateException("Node " + n + " unbuilt in render?");
			}
		} else
			n.build();                                                // FIXME Should be removed once we prove change is stable

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
	}

	/**
	 * Overridden because this is a NodeBase node which MUST be terminated with a /div, always.
	 */
	@Override
	@Deprecated
	public void visitLiteralXhtml(LiteralXhtml n) throws Exception {
		visitNodeBase(n); 						// Handle most thingies we need to do,
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
		visitNodeContainer(n);
	}

	@Override
	public void visitNodeContainer(NodeContainer n) throws Exception {
		n.build(); 	                                       	// FIXME Should be removed once we prove change is stable

		n.onBeforeFullRender(); 							// Do pre-node stuff,
		n.renderJavascriptState(m_createJS);

		boolean indena = o().isIndentEnabled();             // jal 20090903 Save indenting request....
		n.visit(getTagRenderer());                          // Ask base renderer to render tag
		if(n.getCreateJS() != null)
			m_createJS.append(n.getCreateJS());
		n.internalRenderJavascriptState(m_stateBuilder);    // Append Javascript state to state buffer
		visitChildren(n);
		getTagRenderer().renderEndTag(n);
		o().setIndentEnabled(indena);                       // And restore indenting if tag handler caused it to be cleared.
		n.internalClearDelta();
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

	/**
	 * Render the page's doctype.
	 */
	protected void renderPageHeader() throws Exception {
		if(isXml()) {
			o().writeRaw(
				"<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" //
				+ "<head>\n" //
				+ "<meta http-equiv=\"Content-Type\" content=\"application/xhtml+xml; charset=UTF-8\"/>\n" //
			);
		} else {
			o().writeRaw("<!DOCTYPE html>\n");
			o().tag("html");
			o().endtag();
			o().tag("head");
			o().endtag();
			o().writeRaw("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
		}
	}

	static private String xmlStringize(final String is) {
		if(is == null)
			return "null";
		StringBuffer sb = new StringBuffer(is.length() + 20);
		xmlStringize(sb, is);
		return sb.toString();
	}

	static private void xmlStringize(final StringBuffer sb, final String is) {
		if(is == null) {
			sb.append("null");
			return;
		}
		for(int i = 0; i < is.length(); i++) {
			char c = is.charAt(i);
			switch(c){
				case '>':
					sb.append("&gt;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '&':
					sb.append("&amp;");
					break;
				//case '"':
				//	sb.append("&quot;");
				//	break;
				//case '\'':
				//	sb.append("&apos;");
				//	break;
				default:
					sb.append(c);
					break;
			}
		}
	}

	/**
	 * Render the proper themed stylesheet. This will be "style.theme.css" within the current
	 * "theme directory", which is defined by the "currentTheme" in DomApplication.
	 */
	protected void renderThemeCSS() throws Exception {
		ITheme theme = m_ctx.getCurrentTheme();
		String sheet = theme.getStyleSheetName();

		String themeName = DomApplication.get().getDefaultThemeName();
		BrowserVersion version = BrowserVersion.INSTANCE;
		String css = ThemeResourceFactory.PREFIX + themeName + "/style.scss";
		PageParameters pp = new PageParameters()
			.themeName(themeName)
			.browserVersion(version)
			.inputPath(css)
			.parameter("__nomap", "true")
			;

		PartData data = DomApplication.get().getPartService().getData(pp);

		o().writeRaw("<style type='text/css'>\n");
		try(ByteBufferInputStream bbis = new ByteBufferInputStream(data.getData())) {
			try(InputStreamReader isr = new InputStreamReader(bbis, "utf-8")) {
				String cssStr = FileTool.readStreamAsString(isr);
				cssStr = xmlStringize(cssStr);
				o().writeRaw(cssStr);
			}
		}
		o().writeRaw("\n</style>\n");
	}

	protected void renderResourceAsText(String resourceName) throws Exception {
		IResourceRef resource = DomApplication.get().getResource(resourceName, new ResourceDependencyList());
		if(! resource.exists()) {
			LOG.info(resourceName + ": image resource not found");
			return;
		}
		try(InputStream is = requireNonNull(resource.getInputStream())) {
			try(InputStreamReader isr = new InputStreamReader(is, "utf-8")) {
				String str = FileTool.readStreamAsString(isr);
				str = xmlStringize(str);
				o().writeRaw(str);
			}
		}
	}

	/**
	 * Get all contributor sources and create an ordered list (ordered by the indicated 'order') to render.
	 */
	public void renderHeadContributors() throws Exception {
		if(m_contributors.isEmpty()) {
			List<HeaderContributorEntry> full = new ArrayList<HeaderContributorEntry>(m_page.getApplication().getHeaderContributorList());

			Collections.sort(full, HeaderContributor.C_ENTRY);
			for(HeaderContributorEntry hce : full) {
				HeaderContributor contributor = hce.getContributor();
				if(contributor.isOfflineCapable())
					contributor.contribute(this);
			}
		} else {
			for(HeaderContributor contributor : m_contributors) {
				contributor.contribute(this);
			}
		}
	}

	@Override
	public void renderLoadCSS(String path, String... options) throws Exception {
		String rurl = m_page.getBody().getThemedResourceRURL(path);
		//path = ctx().getRelativePath(rurl);

		String themeName = DomApplication.get().getDefaultThemeName();
		BrowserVersion version = BrowserVersion.INSTANCE;

		PageParameters pp = new PageParameters()
			.themeName(themeName)
			.browserVersion(version)
			.inputPath(rurl)
			;
		try {
			PartData data = DomApplication.get().getPartService().getData(pp);
			try(ByteBufferInputStream bbis = new ByteBufferInputStream(data.getData())) {
				try(InputStreamReader isr = new InputStreamReader(bbis, "utf-8")) {
					String cssStr = FileTool.readStreamAsString(isr);
					cssStr = xmlStringize(cssStr);

					renderInlineStyle(cssStr);
				}
			}
			return;
		} catch(ThingyNotFoundException tnx) {
			//-- Not found means we can have a normal resource
		}

		File appFile = m_page.getApplication().getAppFile(rurl);
		if(appFile.exists() && appFile.isFile()) {
			String css = FileTool.readFileAsString(appFile, StandardCharsets.UTF_8);
			renderInlineStyle(css);
		}else {
			try(InputStream resourceAsStream = getClass().getResourceAsStream("/" + path)) {
				String css = FileTool.readStreamAsString(resourceAsStream, StandardCharsets.UTF_8);
				renderInlineStyle(css);
			}catch (Exception ex) {
				//-- Not found means not found as code resource neither
			}
		}
	}

	private void renderInlineStyle(String css) throws IOException {
		o().writeRaw("<style type='text/css'>\n");
		o().writeRaw(css);
		o().writeRaw("\n</style>\n");
	}

	@Override
	public void renderLoadJavascript(@NonNull String path, boolean async, boolean defer) throws Exception {
		if(path.startsWith("http")) {
			o().tag("script");
			o().attr("src", path);
			o().endtag();
			o().closetag("script");
			return;
		}

		String rurl = m_page.getBody().getThemedResourceRURL(path);
		path = ctx().getRelativePath(rurl);
		o().writeRaw("<script>\n");
		renderResourceAsText(rurl);
		o().writeRaw("\n</script>\n");
	}

	private void genVar(String name, String val) throws Exception {
		o().writeRaw("var " + name + "=" + val + ";\n");
	}

	/**
	 * Return all of the Javascript code to create/recreate this page.
	 */
	public StringBuilder getCreateJS() {
		if(m_stateJS.length() > 0) {                            // Stuff present in state buffer too?
			m_createJS.append(';');                                // Always add after all create stuff
			m_createJS.append(m_stateJS);
			m_stateJS.setLength(0);
		}
		return m_createJS;
	}

	@Override
	public Page getPage() {
		return m_page;
	}

	public void setPageTitle(String pageTitle) {
		m_pageTitle = pageTitle;
	}
}
