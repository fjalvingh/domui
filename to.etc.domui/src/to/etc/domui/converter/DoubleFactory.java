package to.etc.domui.converter;

import to.etc.domui.component.meta.*;

public class DoubleFactory implements IConverterFactory {
	public int accept(Class< ? > clz, PropertyMetaModel pmm) {
		if(!Double.class.isAssignableFrom(clz))
			return -1;
		return 10;
	}

	/**
	 * 
	 * @see to.etc.domui.converter.IConverterFactory#createConverter(java.lang.Class, to.etc.domui.component.meta.PropertyMetaModel)
	 */
	public <X, T extends IConverter<X>> T createConverter(Class<X> clz, PropertyMetaModel pmm) {
		return (T)ConverterRegistry.getConverter(DoubleConverter.class);
	}
}
