package to.etc.domui.themes.sass;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.parts.ExtendedParameterInfoImpl;
import to.etc.domui.server.BrowserVersion;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.parts.PartData;
import to.etc.domui.state.UIContext;
import to.etc.domui.themes.ITheme;
import to.etc.domui.themes.ThemeResourceFactory;
import to.etc.domui.util.js.IScriptScope;
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

	private final String m_themeName;

	@NonNull
	final private String m_styleName;

	@NonNull
	final private ResourceDependencies m_rd;

	@NonNull
	final private IScriptScope m_propertyScope;

	@NonNull
	final private List<String> m_searchPath;

	public SassTheme(@NonNull DomApplication da, String themeName, @NonNull String styleName, @NonNull IScriptScope themeProperties, @NonNull ResourceDependencies rd,
		@NonNull List<String> searchpath) {
		m_da = da;
		m_themeName = themeName;
		m_styleName = styleName;
		m_propertyScope = themeProperties;
		m_rd = rd;
		m_searchPath = searchpath;
	}

	@NonNull @Override public String getThemeName() {
		return m_themeName;
	}

	@NonNull @Override public IScriptScope getPropertyScope() {
		throw new IllegalStateException("Cannot do this as I'm not javascript based.");
	}

	@NonNull @Override public String translateResourceName(@NonNull String name) {
		return name;
	}

	@NonNull @Override public String getStyleSheetName() throws Exception {
		BrowserVersion version = UIContext.getRequestContext().getBrowserVersion();	// FIXME Fugly!!
		String css = ThemeResourceFactory.PREFIX + m_themeName + "/style.scss";
		ExtendedParameterInfoImpl pi = new ExtendedParameterInfoImpl(getThemeName(), version, css, "");
		PartData data = DomApplication.get().getPartService().getData(pi);
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
