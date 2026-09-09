package to.etc.domui.themes.sass;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.server.BrowserVersion;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.parts.PartData;
import to.etc.domui.state.PageParameters;
import to.etc.domui.state.UIContext;
import to.etc.domui.themes.ITheme;
import to.etc.domui.themes.ThemeResourceFactory;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.domui.util.resources.IResourceRef;
import to.etc.domui.util.resources.ResourceDependencies;
import to.etc.util.StringTool;

import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 14-5-17.
 */
final public class SassTheme implements ITheme {
	@NonNull
	final private DomApplication m_da;

	private final String m_variantName;

	@NonNull
	final private ResourceDependencies m_rd;

	@NonNull
	final private List<String> m_searchPath;

	public SassTheme(@NonNull DomApplication da, String variantName, @NonNull ResourceDependencies rd,
		@NonNull List<String> searchpath) {
		m_da = da;
		m_variantName = variantName;
		m_rd = rd;
		m_searchPath = searchpath;
	}

	@NonNull @Override public String getVariantName() {
		return m_variantName;
	}

	@NonNull @Override public String translateResourceName(@NonNull String name) {
		return name;
	}

	@NonNull @Override public String getStyleSheetName() throws Exception {
		BrowserVersion version = UIContext.getRequestContext().getPageParameters().getBrowserVersion();	// FIXME Fugly!!
		String contextString = ThemeResourceFactory.PREFIX + m_variantName + "/";
		String css = contextString + "style.scss";
		PageParameters pp = new PageParameters()
			.themeVariant(getVariantName())
			.browserVersion(version)
			.inputPath(css)
			;
		pp.setUrlContextString(contextString);
		PartData data = DomApplication.get().getPartService().getData(pp);
		String hash = StringTool.toHex(data.getHash());

		return css + "?$hash=" + hash;
	}

	@NonNull
	@Override
	public ResourceDependencies getDependencies() {
		return m_rd;
	}

	/**
	 * Find the specified resource using the theme path.
	 */
	@NonNull
	@Override
	public IResourceRef getThemeResource(@NonNull String name, @NonNull IResourceDependencyList rdl) throws Exception {
		//-- "Normal" resource.
		for(String sitem : m_searchPath) {
			String real = sitem + "/" + name;
			IResourceRef rr = m_da.getResource(real, rdl);
			if(rr.exists())
				return rr;
		}
		return IResourceRef.NONEXISTENT;
	}
}
