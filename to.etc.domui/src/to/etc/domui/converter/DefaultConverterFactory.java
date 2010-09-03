package to.etc.domui.converter;

import to.etc.domui.component.meta.*;

/**
 * This is the default converter factory, which returns the default converter instance all of the time.
 *
 * FIXME Needed still?
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 30, 2008
 */
public final class DefaultConverterFactory implements IConverterFactory {
	static private IConverter<Object> DEFAULT_CONVERTER = new DefaultConverter();

	/**
	 * Returns 1 all of the time: accepts everything.
	 *
	 * @see to.etc.domui.converter.IConverterFactory#accept(java.lang.Class, to.etc.domui.component.meta.PropertyMetaModel)
	 */
	@Override
	public int accept(Class< ? > clz, PropertyMetaModel pmm) {
		return 1;
	}

	@Override
	public <X, T extends IConverter<X>> T createConverter(Class<X> clz, PropertyMetaModel pmm) {
		return (T) DEFAULT_CONVERTER;
	}
}
