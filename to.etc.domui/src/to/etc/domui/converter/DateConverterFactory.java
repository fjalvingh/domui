package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.component.meta.*;

/**
 * This converter factory accepts java.util.Date types and returns an appropriate
 * Date converter. By default (if insufficient metadata is available) it will return
 * a DateTime converter.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 29, 2009
 */
public class DateConverterFactory implements IConverterFactory {
	public int accept(Class< ? > clz, PropertyMetaModel pmm) {
		if(!Date.class.isAssignableFrom(clz))
			return -1;
		return 10;
	}

	/**
	 *
	 * @see to.etc.domui.converter.IConverterFactory#createConverter(java.lang.Class, to.etc.domui.component.meta.PropertyMetaModel)
	 */
	public IConverter createConverter(Class< ? > clz, PropertyMetaModel pmm) {
		if(pmm == null)
			return ConverterRegistry.getConverter(DateTimeConverter.class);

		switch(pmm.getTemporal()){
			default:
				return ConverterRegistry.getConverter(DateTimeConverter.class);
			case DATE:
				return ConverterRegistry.getConverter(DateConverter.class);
			case TIME:
				return ConverterRegistry.getConverter(TimeOnlyConverter.class);
		}
	}
}
