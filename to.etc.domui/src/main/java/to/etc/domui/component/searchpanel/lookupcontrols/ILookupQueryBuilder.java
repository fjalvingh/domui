package to.etc.domui.component.searchpanel.lookupcontrols;

import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-12-17.
 */
public interface ILookupQueryBuilder<D> {
	@Nonnull
	<T> LookupQueryBuilderResult appendCriteria(@Nonnull QCriteria<T> criteria, @Nullable D lookupValue);
}
