package to.etc.domui.converter;

import to.etc.domui.component.meta.*;

/**
 * A factory for creating IConverters to convert values.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 30, 2008
 */
public interface IConverterFactory {
	/**
	 * This must decide whether to accept the class and property model pair. This must return a score which is
	 * used to decide the best converter to use; the higher the score the better the chance of this converter
	 * being used.
	 * If this converter does not accept the class it <b>must</b> return -1. If the class is accepted <i>but</i>
	 * the PropertyMetaModel passed was unacceptable this <b>must</b> return 0.
	 *
	 * @param clz
	 * @param pmm
	 * @return
	 */
	public int accept(Class<?> clz, PropertyMetaModel pmm);

	/**
	 * Return the converter which properly converts the specified class and meta model.
	 * @param clz
	 * @param pmm
	 * @return
	 */
	public <X, T extends IConverter<X>> T createConverter(Class<X> clz, PropertyMetaModel pmm);
}
