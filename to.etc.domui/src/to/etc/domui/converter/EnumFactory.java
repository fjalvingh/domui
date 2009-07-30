package to.etc.domui.converter;

import to.etc.domui.component.meta.*;

public class EnumFactory implements IConverterFactory {
	public int accept(Class< ? > clz, PropertyMetaModel pmm) {
		if(clz.isEnum()) {
			return 5;
		}
		return -1;
	}

	/**
	 * 
	 * @see to.etc.domui.converter.IConverterFactory#createConverter(java.lang.Class, to.etc.domui.component.meta.PropertyMetaModel)
	 */
	public <X, T extends IConverter<X>> T createConverter(Class<X> clz, PropertyMetaModel pmm) {
		return (T) ConverterRegistry.getConverter(EnumConverter.class);
	}
}
