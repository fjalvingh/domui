package to.etc.domui.server;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.annotations.UIPage;
import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.dom.html.SubPage;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.IPageParameters;
import to.etc.domui.state.PageParameters;
import to.etc.util.StringTool;

import java.beans.Introspector;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class maintains page -> url mappings, as defined by the
 * {@link to.etc.domui.annotations.UIPage} annotation.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-10-19.
 */
@NonNullByDefault
final public class PageUrlMapping {
	static private final Logger LOG = LoggerFactory.getLogger(PageUrlMapping.class);

	private final Map<String, String> m_urlToPage = new ConcurrentHashMap<>();

	private final DomApplication m_application;

	private final Level m_root = new Level();

	public enum PageSubtype {
		UrlPage,
		SubPage
	}

	public PageUrlMapping(DomApplication application) {
		m_application = application;
	}

	public void scan() {
		ScanResult r = m_application.getClasspathScanResult();
		for(ClassInfo classInfo : r.getClassesWithAnnotation(UIPage.class.getName())) {
			AnnotationInfo anninfo = classInfo.getAnnotationInfo(UIPage.class.getName());
			String pattern = (String) anninfo.getParameterValues().getValue("value");
			if(! pattern.startsWith("/")) {
				LOG.error("Page " + classInfo.getName() + ": @UIPage pattern must start with /");
			} else {
				pattern = pattern.substring(1);							// Remove leading slash
			}

			PageSubtype type;
			if(classInfo.extendsSuperclass(SubPage.class.getCanonicalName())) {
				type = PageSubtype.SubPage;
			} else {
				type = PageSubtype.UrlPage;
			}

			LOG.error("Page " + classInfo.getName() + " url " + pattern + ", type " + type);

			//-- Find all methods annotated with UIUrlParameter
			Map<String, String> pageParams = new HashMap<>();
			for(MethodInfo methodInfo : classInfo.getMethodInfo()) {
				AnnotationInfo mai = methodInfo.getAnnotationInfo(UIUrlParameter.class.getName());
				if(null != mai) {
					String typeStr = methodInfo.getTypeDescriptor().getResultType().toString();
					String pname = (String) mai.getParameterValues().getValue("name");
					if(pname == null || pname.isEmpty()) {
						pname = methodInfo.getName();
						if(pname.startsWith("is")) {
							pname = pname.substring(2);
						} else if(pname.startsWith("get")) {
							pname = pname.substring(3);
						}
						pname = Introspector.decapitalize(pname);
					}
					pageParams.put(pname, typeStr);
				}
			}
			appendPage(type, classInfo.getName(), pattern, pageParams);
		}
	}

	private void appendPage(PageSubtype type, String name, String pattern, Map<String, String> pageParams) {
		String[] segments = pattern.split("/");
		Level currentLevel = m_root;

		Map<Level, String> varMap = new HashMap<>();
		try {
			for(String segment : segments) {
				Level nextLevel = currentLevel.createMatcher(segment, pageParams, varMap);
				currentLevel = nextLevel;
			}

			//-- We're here -> set the action on this level. If there already is an action we have a duplicate.
			currentLevel.setTargetPage(type, name, varMap);
		} catch(PageUrlPatternException px) {
			LOG.error("ERROR: Page " + name + " pattern " + pattern + ": " + px.getMessage() + " (segment " + px.getSegment() + ")");
		}
	}

	/**
	 * Decode the page URL, and find the target to generate.
	 */
	@Nullable
	public Target findTarget(PageSubtype type, String inputPath, IPageParameters parameters) {
		String[] segments = inputPath.split("/");

		Level currentLevel = m_root;
		Map<Level, String> paramValues = new HashMap<>();
		for(String segment : segments) {
			if(!segment.isEmpty()) {
				Level nextLevel = currentLevel.findSegment(paramValues, segment);
				if(nextLevel == null) {
					//-- We do not have a next level -> no match found
					return null;
				}
				currentLevel = nextLevel;
			}
		}

		//-- Reached the end. Do we have a target?
		TypedPage page = currentLevel.findPage(type);
		if(null == page)
			return null;

		String targetPage = page.getTargetPage();

		//-- Create parameters from the URL
		Map<Level, String> varMap = page.getVarMap();
		PageParameters pp = parameters.getUnlockedCopy();
		paramValues.forEach((level, value) -> {
			if(varMap == null)
				throw new IllegalStateException("No page parameters found");
			String name = varMap.get(level);
			if(null == name)
				throw new IllegalStateException("No name stored for level " + level);
			pp.setParameterValues(name, new String[]{value});
		});

		return new Target(targetPage, pp);
	}

	@Nullable
	public UrlAndParameters getUrlString(Class<? extends UrlPage> pageClass, @NonNull IPageParameters parameters) {
		UIPage ann = pageClass.getAnnotation(UIPage.class);
		if(null == ann)
			return null;

		String path = ann.value();
		if(path.isEmpty())
			return null;

		StringBuilder sb = new StringBuilder();
		PageParameters pp = parameters == null ? null : parameters.getUnlockedCopy();
		String[] segments = path.split("/");
		for(String segment : segments) {
			if(segment.startsWith("{") && segment.endsWith("}")) {
				String vn = segment.substring(1, segment.length() - 1);
				if(pp == null)
					throw new IllegalArgumentException("Missing value for page parameter {" + vn + "} for page " + pageClass.getName());
				String value = pp.getString(vn, null);
				if(null == value)
					throw new IllegalArgumentException("Missing value for page parameter {" + vn + "} for page " + pageClass.getName());

				segment = value;
				pp.removeParameter(vn);                                // No longer needed as a query parameter
			}
			if(sb.length() > 0)
				sb.append('/');
			sb.append(segment);
		}
		return new UrlAndParameters(sb.toString(), pp);
	}

	public static final class Target {
		private final String m_targetPage;

		private final PageParameters m_parameters;

		public Target(String targetPage, PageParameters parameters) {
			m_targetPage = targetPage;
			m_parameters = parameters;
		}

		public String getTargetPage() {
			return m_targetPage;
		}

		public PageParameters getParameters() {
			return m_parameters;
		}
	}

	public static final class UrlAndParameters {
		private final String m_url;

		@NonNull
		private final PageParameters m_pageParameters;

		public UrlAndParameters(String url, @NonNull PageParameters pageParameters) {
			m_url = url;
			m_pageParameters = pageParameters;
		}

		public String getUrl() {
			return m_url;
		}

		@NonNull
		public PageParameters getPageParameters() {
			return m_pageParameters;
		}
	}

	private static final class TypedPage {
		private final PageSubtype m_pageType;

		final private String m_targetPage;

		final private Map<Level, String> m_varMap;

		public TypedPage(PageSubtype pageType, String targetPage, Map<Level, String> varMap) {
			m_pageType = pageType;
			m_targetPage = targetPage;
			m_varMap = varMap;
		}

		public String getTargetPage() {
			return m_targetPage;
		}

		public Map<Level, String> getVarMap() {
			return m_varMap;
		}
	}

	/**
	 * Represents all matchers at a given level.
	 */
	private static final class Level {
		private Map<String, Level> m_byName = new ConcurrentHashMap<>();

		private final Map<PageSubtype, TypedPage> m_typedPageMap = new HashMap<>(3);

		//@Nullable
		//private String m_targetPage;
		//
		//private Map<Level, String> m_varMap = Collections.emptyMap();

		public Level createMatcher(String segment, Map<String, String> pageParams, Map<Level, String> paramMap) {
			if(segment.isEmpty())
				return this;

			if(segment.startsWith("{") && segment.endsWith("}")) {
				String vn = segment.substring(1, segment.length() - 1);
				String parameter = pageParams.get(vn);
				if(null == parameter) {
					throw new PageUrlPatternException(segment, "The page does not have a parameter '" + vn + "'");
				}
				paramMap.put(this, vn);

				return m_byName.computeIfAbsent("", a -> new Level());
			} else if(segment.contains("{") || segment.contains("}")) {
				throw new PageUrlPatternException(segment, "Segment contains { or } somewhere in the middle- that is not supported");
			} else {
				Level level = m_byName.computeIfAbsent(segment, a -> new Level());
				return level;
			}
		}

		//@Nullable
		//public synchronized String getTargetPage() {
		//	return m_targetPage;
		//}
		//
		//public Map<Level, String> getVarMap() {
		//	return m_varMap;
		//}

		public synchronized void setTargetPage(PageSubtype subType, String targetPage, Map<Level, String> varMap) {
			if(m_typedPageMap.get(subType) != null)
				throw new PageUrlPatternException("", "Duplicate URL pattern: page " + targetPage);
			m_typedPageMap.put(subType, new TypedPage(subType, targetPage, varMap));
		}

		@Nullable
		public synchronized TypedPage findPage(PageSubtype type) {
			return m_typedPageMap.get(type);
		}

		@Nullable
		public Level findSegment(Map<Level, String> paramValues, String segment) {
			//-- Literal match?
			Level level = m_byName.get(segment);
			if(null != level)
				return level;

			level = m_byName.get("");                        // Variable level present?
			if(null != level) {
				paramValues.put(this, StringTool.decodeURLEncoded(segment));                // Assign the value for this level
				return level;
			}
			return null;
		}
	}

}
