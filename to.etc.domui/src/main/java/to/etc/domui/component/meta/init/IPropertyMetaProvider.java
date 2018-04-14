package to.etc.domui.component.meta.init;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.impl.DefaultClassMetaModel;

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

	default void beforeProperties(@NonNull MetaInitContext context, @NonNull C classModel) throws Exception {}

	void provide(@NonNull MetaInitContext context, @NonNull C classModel, @NonNull P propertyModel) throws Exception;

	default void afterPropertiesDone(@NonNull MetaInitContext context, @NonNull C classModel) throws Exception {}
}
