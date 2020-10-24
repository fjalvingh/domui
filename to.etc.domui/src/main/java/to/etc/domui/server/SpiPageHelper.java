package to.etc.domui.server;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.SpiPage;
import to.etc.domui.dom.html.SubPage;
import to.etc.domui.server.PageUrlMapping.Target;
import to.etc.domui.state.PageParameters;
import to.etc.domui.trouble.ThingyNotFoundException;
import to.etc.util.ClassUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
		Target target = m_application.getPageUrlMapping().findTarget(rurl, new PageParameters());
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
}
