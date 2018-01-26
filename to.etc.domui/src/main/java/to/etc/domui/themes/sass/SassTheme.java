package to.etc.domui.themes.sass;

import to.etc.domui.parts.*;
import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.state.*;
import to.etc.domui.themes.*;
import to.etc.domui.util.js.*;
import to.etc.domui.util.resources.*;
import to.etc.util.*;

import javax.annotation.*;
import java.util.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 14-5-17.
 */
final public class SassTheme implements ITheme {
	@Nonnull
	final private DomApplication m_da;

	private final String m_themeName;

	@Nonnull
	final private String m_styleName;

	@Nonnull
	final private ResourceDependencies m_rd;

	@Nonnull
	final private IScriptScope m_propertyScope;

	@Nonnull
	final private List<String> m_searchPath;

	public SassTheme(@Nonnull DomApplication da, String themeName, @Nonnull String styleName, @Nonnull IScriptScope themeProperties, @Nonnull ResourceDependencies rd,
		@Nonnull List<String> searchpath) {
		m_da = da;
		m_themeName = themeName;
		m_styleName = styleName;
		m_propertyScope = themeProperties;
		m_rd = rd;
		m_searchPath = searchpath;
	}

	@Nonnull @Override public String getThemeName() {
		return m_themeName;
	}

	@Nonnull @Override public IScriptScope getPropertyScope() {
		throw new IllegalStateException("Cannot do this as I'm not javascript based.");
	}

	@Nonnull @Override public String translateResourceName(@Nonnull String name) {
		return name;
	}

	@Nonnull @Override public String getStyleSheetName() throws Exception {
		BrowserVersion version = UIContext.getRequestContext().getBrowserVersion();	// FIXME Fugly!!
		String css = ThemeResourceFactory.PREFIX + m_themeName + "/style.scss";
		ExtendedParameterInfoImpl pi = new ExtendedParameterInfoImpl(getThemeName(), version, css, "");
		PartData data = DomApplication.get().getPartService().getData(pi);
		String hash = StringTool.toHex(data.getHash());

		return css + "?$hash=" + hash;
	}

	@Nonnull
	@Override
	public ResourceDependencies getDependencies() {
		return m_rd;
	}

	/**
	 * Find the specified resource using the theme path.
	 */
	@Nonnull
	@Override
	public IResourceRef getThemeResource(@Nonnull String name, @Nonnull IResourceDependencyList rdl) throws Exception {
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
