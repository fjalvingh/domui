package to.etc.domui.util.db;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.webapp.query.IIdentifyable;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QField;
import to.etc.webapp.query.QQueryUtils;

import static java.util.Objects.requireNonNull;

/**
 * A utility class containing various methods for checking and validating properties
 * and data within a specified context. This class is intended to be extended with
 * additional utility functions over time.
 *
 * Created on Nov 28, 2023
 */
final public class QCheckUtil {
	/**
	 * Checks if a given value of a specified property is unique within a data context.
	 *
	 * @param <T>                 The type of the entity being checked.
	 * @param <P>                 The type of the identifier for the entity.
	 * @param <V>                 The type of the value of the property being checked.
	 * @param dc                  The data context in which the uniqueness is checked.
	 * @param clz                 The class of the entity.
	 * @param uniqueCheckProperty The property for which uniqueness is to be checked.
	 * @param value               The entity containing the value to be checked.
	 * @param idProperty          The property representing the identifier of the entity.
	 * @return {@code true} if the value is unique for the specified property; {@code false} otherwise.
	 * @throws Exception If any error occurs during the execution of the method.
	 */
	public static <T extends IIdentifyable<P>, P, V> boolean isUnique(@NonNull QDataContext dc, Class<T> clz, @NonNull QField<T, V> uniqueCheckProperty, T value, @NonNull QField<T, P> idProperty)
		throws Exception {

		PropertyMetaModel<V> pmm = MetaManager.getPropertyMeta(clz, uniqueCheckProperty);
		V propertyValue = pmm.getValue(value);
		QCriteria<T> q = QCriteria.create(clz).eq(uniqueCheckProperty, requireNonNull(propertyValue));
		P id = value.getId();
		if(null != id) {
			q.ne(idProperty, id);
		}
		return 0 == QQueryUtils.queryCount(dc, q);
	}

}
