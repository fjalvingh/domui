package to.etc.domui.converter;

import to.etc.domui.component.meta.*;

/**
 * Generic factory to accept double numeric values. This has a low priority so monetary values
 * can override this.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 17, 2009
 */
final public class DoubleFactory implements IConverterFactory {
	public int accept(final Class< ? > clz, final PropertyMetaModel pmm) {
		if(!Double.class.isAssignableFrom(clz) && double.class != clz)
			return -1;
		return 5;
	}

	/**
	 *
	 * @see to.etc.domui.converter.IConverterFactory#createConverter(java.lang.Class, to.etc.domui.component.meta.PropertyMetaModel)
	 */
	public <X, T extends IConverter<X>> T createConverter(final Class<X> clz, final PropertyMetaModel pmm) {
		return (T) ConverterRegistry.getConverterInstance(DoubleConverter.class);
	}
}
