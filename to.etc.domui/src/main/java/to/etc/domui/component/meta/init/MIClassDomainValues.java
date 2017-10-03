package to.etc.domui.component.meta.init;

import to.etc.domui.component.meta.impl.DefaultClassMetaModel;

import javax.annotation.Nonnull;

/**
 * If this is a Class&lt;enum> or the class Boolean define it's domain values.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-10-17.
 */
public class MIClassDomainValues implements IClassMetaProvider<DefaultClassMetaModel> {
	/**
	 * If this is an enum or the class Boolean define it's domain values.
	 */
	@Override public void provide(@Nonnull MetaInitContext context, @Nonnull DefaultClassMetaModel model) throws Exception {
		//-- If this is an enumerable thingerydoo...
		Class<?> actualClass = model.getActualClass();

		if(actualClass == Boolean.class) {
			model.setDomainValues(new Object[]{Boolean.FALSE, Boolean.TRUE});
		} else if(Enum.class.isAssignableFrom(actualClass)) {
			Class<Enum< ? >> ecl = (Class<Enum< ? >>) actualClass;
			model.setDomainValues(ecl.getEnumConstants());
		}
	}
}
