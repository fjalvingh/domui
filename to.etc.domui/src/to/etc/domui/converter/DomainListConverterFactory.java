package to.etc.domui.converter;

import to.etc.domui.component.meta.*;

/**
 * This accepts all properties that have a list of values as their domain model.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 17, 2009
 */
final public class DomainListConverterFactory implements IConverterFactory {
	@Override
	public int accept(final Class< ? > clz, final PropertyMetaModel pmm) {
		if(pmm == null)
			return -1;
		return pmm.getDomainValues() != null && pmm.getDomainValues().length > 0 ? 10 : -1;
	}

	@Override
	public <X, T extends IConverter<X>> T createConverter(final Class<X> clz, final PropertyMetaModel pmm) {
		return (T) new DomainListConverter(pmm);
	}
}
