package to.etc.domui.injector;

import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.IPageParameters;
import to.etc.function.BiFunctionEx;
import to.etc.util.ClassUtil;
import to.etc.util.PropertyInfo;
import to.etc.webapp.ProgrammerErrorException;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-7-18.
 */
public class PagePropertyInjector implements IPagePropertyFactory {
	private final Class<?> m_acceptClass;

	private final BiFunctionEx<UrlPage, String, Object> m_calculator;

	public PagePropertyInjector(Class<?> acceptClass, BiFunctionEx<UrlPage, String, Object> calculator) {
		m_acceptClass = acceptClass;
		m_calculator = calculator;
	}

	protected boolean accepts(PropertyInfo pi) {
		return m_acceptClass.isAssignableFrom(pi.getActualType());
	}

	@Override public PropertyInjector calculateInjector(PropertyInfo propertyInfo) {
		if(! accepts(propertyInfo))
			return null;

		Method getter = propertyInfo.getGetter();
		UIUrlParameter upp = null;
		if(null != getter) {
			upp = ClassUtil.findAnnotationIncludingSuperClasses(getter, UIUrlParameter.class);
		}
		Method setter = propertyInfo.getSetter();
		if(null != setter && upp == null) {
			upp = ClassUtil.findAnnotationIncludingSuperClasses(setter, UIUrlParameter.class);
		}
		if(null == upp)
			return null;

		if(null == setter)
			throw new ProgrammerErrorException(UIUrlParameter.class.getSimpleName() + " annotation cannot be used on a setterless property (is the setter private?)");

		String name = upp.name().isEmpty() ? propertyInfo.getName() : upp.name();
		return new CalculatedInjector(m_calculator, setter, name, upp.mandatory());
	}

	private static class CalculatedInjector extends PropertyInjector {
		private final BiFunctionEx<UrlPage, String, Object> m_calculator;

		private final String m_name;

		private final boolean m_mandatory;

		public CalculatedInjector(BiFunctionEx<UrlPage, String, Object> calculator, Method setter, String name, boolean mandatory) {
			super(setter);
			m_calculator = calculator;
			m_name = name;
			m_mandatory = mandatory;
		}

		@Override public void inject(UrlPage page, IPageParameters pp) throws Exception {
			String value = pp.getString(m_name, null);
			if(null == value) {
				if(m_mandatory)
					throw new IllegalArgumentException("The page " + page.getClass() + " REQUIRES the URL parameter " + m_name);
				//setValue(page, null);
				return;
			}
			Object instance = m_calculator.apply(page, value);
			setValue(page, instance);
		}
	}
}
