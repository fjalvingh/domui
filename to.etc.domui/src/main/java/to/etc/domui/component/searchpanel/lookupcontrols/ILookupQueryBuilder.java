package to.etc.domui.component.searchpanel.lookupcontrols;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.webapp.query.QCriteria;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-12-17.
 */
@FunctionalInterface
public interface ILookupQueryBuilder<Q, D> {
	@NonNull
	LookupQueryBuilderResult appendCriteria(@NonNull QCriteria<Q> criteria, @Nullable D lookupValue);
}
