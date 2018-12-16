package to.etc.domui.injector;

import to.etc.domui.annotations.UIUrlContext;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IUrlContextDecoder;
import to.etc.domui.state.IPageParameters;
import to.etc.domui.util.Msgs;
import to.etc.util.PropertyInfo;
import to.etc.webapp.ProgrammerErrorException;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Properties annotated with @UIUrlContext will be annotated with the appropriate context
 * thing. This uses the {@link IUrlContextDecoder} set on DomApplication to calculate the
 * context values as needed by the application, and uses those to
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-12-18.
 */
final public class UrlContextPropertyInjector implements IPagePropertyFactory {
	@Override public PropertyInjector calculateInjector(PropertyInfo propertyInfo) {
		UIUrlContext ann = propertyInfo.getGetter().getAnnotation(UIUrlContext.class);
		if(null == ann)
			return null;

		IUrlContextDecoder decoder = DomApplication.get().getUrlContextDecoder();
		if(decoder == null)
			throw new ProgrammerErrorException("Property " + propertyInfo + " annotated with @UIUrlContext, but no context decoder registered with DomApplication.setUrlContextDecoder()");
		Method setter = propertyInfo.getSetter();
		if(null == setter)
			throw new ProgrammerErrorException("Property " + propertyInfo + " annotated with @UIUrlContext but it has no setter");
		Class<?> actualType = propertyInfo.getActualType();
		return new PropertyInjector(setter) {
			@Override public void inject(UrlPage page, IPageParameters pp, Map<String, Object> attributeMap) throws Exception {
				Map<String, Object> map = (Map<String, Object>) attributeMap.computeIfAbsent(UrlContextPropertyInjector.class.getName(), a -> {
					String urlContextString = pp.getUrlContextString();
					if(null == urlContextString) {
						if(ann.optional())
							return null;
						throw new UrlContextUnknownException(Msgs.pageWithoutUrlContext, setter.toString());
					}
					return decoder.getContextValues(urlContextString);
				});

				if(null != map) {
					for(Object value : map.values()) {
						if(value != null) {
							if(actualType.isAssignableFrom(value.getClass())) {
								setValue(page, value);
								return;
							}
						}
					}
				}
				if(! ann.optional())
					throw new UrlContextUnknownException(Msgs.noUrlContextValueFor, setter.toString());
			}
		};
	}
}
