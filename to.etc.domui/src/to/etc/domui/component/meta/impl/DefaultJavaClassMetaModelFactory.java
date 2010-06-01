package to.etc.domui.component.meta.impl;

import javax.annotation.*;

import to.etc.domui.component.meta.*;

/**
 * A default, base implementation of a MetaModel layer. This tries to discover metadata by using
 * base property information plus Hibernate/JPA annotation data.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 16, 2008
 */
public class DefaultJavaClassMetaModelFactory implements IClassMetaModelFactory {
	@Override
	public int accepts(@Nonnull Object theThingy) {
		if(!(theThingy instanceof Class< ? >)) // Only accept Class<?> thingies.
			return -1;
		return 1;
	}

	/**
	 *
	 * @see to.etc.domui.component.meta.IClassMetaModelFactory#createModel(java.lang.Object)
	 */
	@Override
	@Nonnull
	public ClassMetaModel createModel(@Nonnull Object theThingy) {
		Class< ? > clz = (Class< ? >) theThingy;
		DefaultClassMetaModel dmm = new DefaultClassMetaModel(clz);
		dmm.initialize(); // FIXME Delegate initialization here?
		return dmm;
	}
}
