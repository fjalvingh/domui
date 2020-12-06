package to.etc.domui.server;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.annotations.UIPage;
import to.etc.domui.dom.html.SpiContainer;
import to.etc.domui.dom.html.SpiPage;
import to.etc.domui.dom.html.SubPage;
import to.etc.domui.server.PageUrlMapping.PageSubtype;
import to.etc.domui.server.PageUrlMapping.Target;
import to.etc.domui.state.IPageParameters;
import to.etc.domui.state.PageParameters;
import to.etc.domui.trouble.ThingyNotFoundException;
import to.etc.util.ClassUtil;
import to.etc.util.StringTool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static to.etc.domui.util.DomUtil.nullChecked;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-10-20.
 */
public class SpiPageHelper {
	private DomApplication m_application;

	public SpiPageHelper(DomApplication application) {
		m_application = application;
	}

	SubPage createSubPage(SpiPage spiPage, String rurl) throws Exception {
		Target target = m_application.getPageUrlMapping().findTarget(PageSubtype.SubPage, rurl, new PageParameters());
		if(null == target) {
			throw new ThingyNotFoundException("Spi fragment with identifier=" + rurl + " is not known");
		}

		String targetPageName = target.getTargetPage();
		SubPage subPage = createSpiPage(targetPageName);
		if(null == subPage) {
			throw new ThingyNotFoundException("Spi fragment with identifier=" + rurl + " is not known (no proper class)");
		}
		System.out.println(">>>> target " + subPage);
		m_application.getInjector().injectPageValues(subPage, nullChecked(target.getParameters()));
		return subPage;
	}

	@Nullable
	SubPage createSpiPage(String pageName) throws Exception {
		Class<?> clz = ClassUtil.loadClass(getClass().getClassLoader(), pageName);
		if(null == clz) {
			System.err.println("Unknown Spi fragment class " + pageName);
			return null;
		}
		if(! SubPage.class.isAssignableFrom(clz)) {
			System.err.println("Spi fragment class is not a SubPage: " + pageName);
			return null;
		}
		return createSpiPage((Class<? extends SubPage>) clz);
	}

	public SubPage createSpiPage(Class<? extends SubPage> spiClass) throws Exception {
		try {
			Constructor<?> constructor = spiClass.getConstructor();
			return (SubPage) constructor.newInstance();
		} catch(InvocationTargetException itx) {
			Throwable c = itx.getCause();
			if(c instanceof Exception)
				throw (Exception) c;
			else if(c instanceof Error)
				throw (Error) c;
			else
				throw itx;
		}
	}

	public String getContainerHashes(SpiPage page) {
		StringBuilder sb = new StringBuilder();
		List<SpiContainer> containerList = page.getContainers();
		for(SpiContainer container : containerList) {
			String hash = calculateContainerHash(container);
			if(null != hash) {
				if(sb.length() > 0)
					sb.append(';');
				if(containerList.size() > 1) {
					sb.append(container.getContainerName().name()).append(":");
				}
				sb.append(hash);
			}
		}
		return sb.toString();
	}

	@Nullable
	private String calculateContainerHash(SpiContainer container) {
		Class<? extends SubPage> currentPage = container.getCurrentPage();
		if(currentPage == null)
			return null;
		UIPage annotation = currentPage.getAnnotation(UIPage.class);
		if(null == annotation)
			throw new IllegalStateException("Missing " + UIPage.class.getName() + " annotation on SPI fragment class " + currentPage.getName());
		String value = annotation.value();

		String[] segments = value.split("/");
		StringBuilder sb = new StringBuilder();
		for(String segment : segments) {
			if(sb.length() > 0)
				sb.append('/');
			if(segment.startsWith("{") && segment.endsWith("}")) {
				String name = segment.substring(1, segment.length() - 1);
				IPageParameters pp = container.getCurrentParameters();
				String val = pp == null ? null : pp.getString(name, null);
				if(val == null) {
					val = "";
					//throw new IllegalStateException("SpiFragment " + currentPage.getName() + " missing actual parameter value for parameter " + name);
				}
				sb.append(StringTool.encodeURLEncoded(val));
			} else {
				sb.append(segment);
			}
		}
		return sb.toString();
	}

}
