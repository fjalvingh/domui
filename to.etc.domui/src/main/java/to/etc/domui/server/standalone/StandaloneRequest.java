package to.etc.domui.server.standalone;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.BrowserVersion;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.server.IRequestResponse;
import to.etc.domui.server.IServerSession;
import to.etc.domui.state.AppSession;
import to.etc.domui.state.PageParameters;
import to.etc.domui.state.UIContext;
import to.etc.domui.state.WindowSession;
import to.etc.domui.themes.DefaultThemeVariant;
import to.etc.domui.themes.ITheme;
import to.etc.domui.themes.IThemeVariant;
import to.etc.domui.util.js.IScriptScope;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.domui.util.resources.IResourceRef;
import to.etc.domui.util.resources.ResourceDependencies;
import to.etc.util.WrappedException;
import to.etc.webapp.query.IQDataContextSource;
import to.etc.webapp.query.QContextManager;
import to.etc.webapp.query.QDataContext;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Util that allows use of IRequestContext that is not initiated via standard server requests UI responses.
 */
public class StandaloneRequest implements IRequestContext, AutoCloseable {
	private DomApplication m_app;

	private final String m_input;

	private AppSession m_session;

	private StringWriter m_sw;

	private final Map<String, String[]> m_parameterMap = new HashMap<String, String[]>();

	private WindowSession m_conversationManager;

	private PageParameters m_pageParameters = new PageParameters();

	private final @Nullable QDataContext m_dc;

	private StandaloneRequest() {
		this(null);
	}

	private StandaloneRequest(@Nullable QDataContext dc) {
		m_input = "test/page.html";
		m_dc = dc;
	}

	public static StandaloneRequest create(@Nullable QDataContext dc) {
		try {
			UrlPage urlPage = new UrlPage() {
				@Override
				public IQDataContextSource getSharedContextFactory(@NonNull String key) {
					if(!QContextManager.DEFAULT.equals(key)) {
						throw new IllegalStateException("Must use key " + QContextManager.DEFAULT);
					}
					if(null == dc) {
						throw new IllegalStateException("dc is not set for " + StandaloneRequest.class.getSimpleName());
					}
					return new IQDataContextSource() {

						@NonNull
						@Override
						public QDataContext getDataContext() {
							return dc;
						}
					};
				}
			};
			Page page = new Page(urlPage);
			UIContext.internalSet(page);
			StandaloneRequest ctx = new StandaloneRequest(dc);
			UIContext.internalSet(ctx);
			return ctx;
		}catch(Exception ex) {
			throw WrappedException.wrap(ex);
		}
	}

	@Override
	public @NonNull DomApplication getApplication() {
		if(m_app == null)
			m_app = new DomApplication() {
				@Override
				public Class< ? extends UrlPage> getRootPage() {
					return null;
				}
			};
		return m_app;
	}

	@NonNull
	@Override
	public PageParameters getPageParameters() {
		return m_pageParameters;
	}

	@Override
	public @NonNull WindowSession getWindowSession() {
		if(m_conversationManager == null)
			m_conversationManager = new WindowSession(getSession());
		return m_conversationManager;
	}

	@Override
	public @NonNull String getExtension() {
		int pos = m_input.lastIndexOf('.');
		if(pos == -1)
			return "";
		return m_input.substring(pos + 1);
	}

	@Override
	public @NonNull String getInputPath() {
		return m_input;
	}

	@Override public String getPageName() {
		int pos = m_input.lastIndexOf('.');
		return m_input.substring(0, pos);
	}

	@Override
	@NonNull
	public Writer getOutputWriter(@NonNull String contentType, @Nullable String encoding) throws IOException {
		if(m_sw == null)
			m_sw = new StringWriter();
		return m_sw;
	}

	@Override
	public @NonNull String getRelativePath(final @NonNull String rel) {
		return "webapp/" + rel;
	}

	@Override
	@NonNull
	public AppSession getSession() {
		if(m_session == null)
			m_session = new AppSession(getApplication());
		return m_session;
	}

	@Override
	public String getUserAgent() {
		return "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9) Gecko/2008060309 Firefox/3.0";
	}

	public String getParameter(final @NonNull String name) {
		String[] v = getParameters(name);
		if(v == null || v.length != 1)
			return null;
		return v[0];
	}

	@NonNull
	public String[] getParameterNames() {
		return m_parameterMap.keySet().toArray(new String[m_parameterMap.size()]);
	}

	@NonNull
	public String[] getParameters(final @NonNull String name) {
		String[] strings = m_parameterMap.get(name);
		return strings == null ? new String[0] : strings;
	}

	public BrowserVersion getBrowserVersion() {
		return null;
	}

	@Override
	@NonNull
	public IRequestResponse getRequestResponse() {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	@Nullable
	public IServerSession getServerSession(boolean create) {
		throw new IllegalStateException("Not implemented");
	}

	@NonNull @Override public ITheme getCurrentTheme() {
		return new ITheme() {
			@NonNull @Override public String getThemeName() {
				return "aa/bb";
			}

			@NonNull @Override public ResourceDependencies getDependencies() {
				return new ResourceDependencies(Collections.emptyList());
			}

			@NonNull @Override public IResourceRef getThemeResource(@NonNull String name, @NonNull IResourceDependencyList rdl) throws Exception {
				throw new IllegalStateException("Not implemented");
			}

			@NonNull @Override public IScriptScope getPropertyScope() {
				throw new IllegalStateException("Not implemented");
			}

			@NonNull @Override public String translateResourceName(@NonNull String name) {
				return name;
			}

			@NonNull @Override public String getStyleSheetName() throws Exception {
				return "style.css";
			}
		};
	}

	@NonNull @Override public IThemeVariant getThemeVariant() {
		return DefaultThemeVariant.INSTANCE;
	}

	@Override public void setThemeVariant(@NonNull IThemeVariant variant) {
	}

	@Override public void setPersistedParameter(@NonNull String name, @NonNull String value) {

	}

	@Nullable
	public String getThemeName() {
		return getCurrentTheme().getThemeName();
	}

	@Override public void setThemeName(String userThemeName) {

	}

	@Override
	public boolean isCrawler() {
		return false;
	}

	@Override
	public void close() throws IOException {
		QDataContext dc = m_dc;
		if(null != dc) {
			dc.close();
		}
	}
}
