package to.etc.domui.converter;

import to.etc.domui.component.meta.*;

public class BooleanConverterFactory implements IConverterFactory {
	@Override
	public int accept(Class< ? > clz, PropertyMetaModel pmm) {
		return clz == Boolean.class || clz == boolean.class ? 5 : -1;
	}

	@Override
	public <X, T extends IConverter<X>> T createConverter(Class<X> clz, PropertyMetaModel pmm) {
		return (T) BooleanConverter.getInstance();
	}
}
