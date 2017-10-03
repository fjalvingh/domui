package to.etc.domui.component.meta.init;

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.impl.DefaultClassMetaModel;

import javax.annotation.Nonnull;

/**
 * Add extra data to a PropertyMetaModel - provided you accept the model and the class
 * model it belongs to.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-10-17.
 */
public interface IPropertyMetaProvider<C extends ClassMetaModel, P extends PropertyMetaModel<?>> {
	default Class<C> getClassModelClass() {
		return (Class<C>) DefaultClassMetaModel.class;
	}

	default void beforeProperties(@Nonnull MetaInitContext context, @Nonnull C classModel) throws Exception {}

	void provide(@Nonnull MetaInitContext context, @Nonnull C classModel, @Nonnull P propertyModel) throws Exception;

	default void afterPropertiesDone(@Nonnull MetaInitContext context, @Nonnull C classModel) throws Exception {}
}
