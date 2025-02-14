package to.etc.domui.dom.header;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.IContributorRenderer;
import to.etc.domui.state.UIContext;
import to.etc.domui.util.DomUtil;
import to.etc.util.FileTool;
import to.etc.util.StringTool;
import to.etc.webapp.nls.BundleRef;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-10-19.
 */
public class CookieInfoHeaderContributor extends HeaderContributor {
	final private BundleRef m_bundle;

	private String m_link;

	final private String m_script;

	public CookieInfoHeaderContributor(Class<?> anchor, String resourceName, String link) throws Exception {
		m_bundle = BundleRef.create(anchor, resourceName);
		m_link = link;
		m_script = FileTool.readResourceAsString(getClass(), "/misc/cookiewarningjs.js", "utf-8");
	}

	@Override
	public boolean isOfflineCapable() {
		return false;
	}

	@Override
	public void contribute(IContributorRenderer r) throws Exception {
		r.renderLoadJavascript("$js/jquery.cookieMessage.js", false, false);

		//-- If the link is not absolute then make it so
		String link = m_link;
		if(! DomUtil.isAbsoluteURL(link)) {
			link = UIContext.getRequestContext().getRelativePath(link);
		}

		String message = m_bundle.getString("message")
			.replace("${link}", link)
		;

		String script = m_script.replace("${msg}", StringTool.strToJavascriptString(message, false))
			.replace("${title}", StringTool.strToJavascriptString(m_bundle.getString("title"), false))
			.replace("${acclabel}", StringTool.strToJavascriptString(m_bundle.getString("acceptButton"), false))
			;
		r.o().tag("script");
		r.o().attr("language", "javascript");
		r.o().attr("nonce", r.getPage().getNonce());
		r.o().endtag();
		//r.o().writeRaw("<!--\n"); // Embed JS in comment IMPORTANT: the \n is required!!!
		r.o().writeRaw(script);
		//r.o().writeRaw("\n-->");
		r.o().writeRaw("\n");
		r.o().closetag("script");
	}

	@Override
	public int hashCode() {
		return 13;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		return (obj instanceof CookieHeaderContributor);
	}
}
