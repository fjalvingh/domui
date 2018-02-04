package to.etc.domui.component.searchpanel.lookupcontrols;

import to.etc.webapp.query.QCriteria;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 6-12-17.
 */
@DefaultNonNull
final public class ObjectLookupQueryBuilder<D> implements ILookupQueryBuilder<D> {
	private final String m_propertyName;

	public ObjectLookupQueryBuilder(String propertyName) {
		m_propertyName = requireNonNull(propertyName);
	}

	@Override public <T> LookupQueryBuilderResult appendCriteria(QCriteria<T> criteria, @Nullable D value) {
		//Object value = null;
		//try {
		//	value = txt.getValue();
		//} catch(Exception x) {
		//	return LookupQueryBuilderResult.INVALID;		// Has validation error -> exit.
		//}
		if(value == null || (value instanceof String && ((String) value).trim().length() == 0))
			return LookupQueryBuilderResult.EMPTY;			// Is okay but has no data

		// FIXME Handle minimal-size restrictions on input (search field metadata)


		//-- Put the value into the criteria..
		if(value instanceof String) {
			String str = (String) value;
			str = str.trim().replace("*", "%") + "%";			// FIXME Do not search with wildcard by default 8-(
			criteria.ilike(m_propertyName, str);
		} else {
			criteria.eq(m_propertyName, value);				// property == value
		}
		return LookupQueryBuilderResult.VALID;
	}
}
