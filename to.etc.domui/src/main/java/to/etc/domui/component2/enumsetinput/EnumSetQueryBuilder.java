package to.etc.domui.component2.enumsetinput;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.searchpanel.lookupcontrols.ILookupQueryBuilder;
import to.etc.domui.component.searchpanel.lookupcontrols.LookupQueryBuilderResult;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QRestrictorImpl;

import java.util.Set;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-2-18.
 */
public class EnumSetQueryBuilder<V> implements ILookupQueryBuilder<Set<V>> {
	private final String m_propertyName;

	public EnumSetQueryBuilder(String propertyName) {
		m_propertyName = propertyName;
	}

	@NonNull @Override public <T> LookupQueryBuilderResult appendCriteria(@NonNull QCriteria<T> criteria, @Nullable Set<V> lookupValue) {
		if(lookupValue == null || lookupValue.isEmpty())
			return LookupQueryBuilderResult.EMPTY;
		QRestrictorImpl<T> or = criteria.or();
		lookupValue.forEach(value -> or.eq(m_propertyName, value));
		return LookupQueryBuilderResult.VALID;
	}
}
