package to.etc.domui.injector;

import java.util.Map;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-2-17.
 */
public interface IPageInjectorCalculator {
	void calculatePageInjectors(Map<String, PropertyInjector> registrationMap, Class<?> pageClass);
}
