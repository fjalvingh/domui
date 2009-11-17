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
	public <X, T extends IConverter<X>> T createConverter(Class<X> clz, PropertyMetaModel pmm) {
		if(pmm == null)
			return (T) ConverterRegistry.getConverterInstance(DateTimeConverter.class);

		switch(pmm.getTemporal()){
			default:
				return (T) ConverterRegistry.getConverterInstance(DateTimeConverter.class);
			case DATE:
				return (T) ConverterRegistry.getConverterInstance(DateConverter.class);
			case TIME:
				return (T) ConverterRegistry.getConverterInstance(TimeOnlyConverter.class);
		}
	}
}
