package to.etc.domui.converter;

import to.etc.domui.component.meta.PropertyMetaModel;

import java.time.LocalDateTime;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 01-05-20.
 */
final public class LocalDateTimeConverterFactory implements IConverterFactory {
	@Override
	public int accept(Class< ? > clz, PropertyMetaModel< ? > pmm) {
		if(!LocalDateTime.class.isAssignableFrom(clz))
			return -1;
		return 10;
	}

	@Override
	public <X, T extends IConverter<X>> T createConverter(Class<X> clz, PropertyMetaModel<X> pmm) {
		//if(pmm == null)
			return (T) ConverterRegistry.getConverterInstance(LocalDateTimeConverter.class);

		//switch(pmm.getTemporal()){
		//	default:
		//		return (T) ConverterRegistry.getConverterInstance(DateTimeConverter.class);
		//	case DATE:
		//		return (T) ConverterRegistry.getConverterInstance(DateConverter.class);
		//	case TIME:
		//		return (T) ConverterRegistry.getConverterInstance(TimeOnlyConverter.class);
		//}
	}

}
