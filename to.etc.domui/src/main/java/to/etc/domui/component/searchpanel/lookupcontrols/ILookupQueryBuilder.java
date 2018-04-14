package to.etc.domui.component.searchpanel.lookupcontrols;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.webapp.query.QCriteria;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-12-17.
 */
public interface ILookupQueryBuilder<D> {
	@NonNull
	<T> LookupQueryBuilderResult appendCriteria(@NonNull QCriteria<T> criteria, @Nullable D lookupValue);
}
