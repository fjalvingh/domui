package to.etc.domui.component.searchpanel.lookupcontrols;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.webapp.query.QCriteria;

import static java.util.Objects.requireNonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 6-12-17.
 */
@NonNullByDefault
final public class ObjectLookupQueryBuilder<D> implements ILookupQueryBuilder<D> {
	static private volatile boolean m_lookupWildcardByDefault = true;

	private final String m_propertyName;

	public ObjectLookupQueryBuilder(String propertyName) {
		m_propertyName = requireNonNull(propertyName);
	}

	@Override public <T> LookupQueryBuilderResult appendCriteria(QCriteria<T> criteria, @Nullable D value) {
		if(value == null || (value instanceof String && ((String) value).trim().length() == 0))
			return LookupQueryBuilderResult.EMPTY;			// Is okay but has no data

		// FIXME Handle minimal-size restrictions on input (search field metadata)
		//-- Put the value into the criteria..
		if(value instanceof String) {
			String str = (String) value;

			if(m_lookupWildcardByDefault) {
				if(str.endsWith(".")) {
					str = str.substring(0, str.length()-1).trim();
				} else {
					str = str.trim().replace("*", "%") + "%";            // FIXME Do not search with wildcard by default 8-(
				}
			}
			criteria.ilike(m_propertyName, str);
		} else {
			criteria.eq(m_propertyName, value);				// property == value
		}
		return LookupQueryBuilderResult.VALID;
	}

	public static void setLookupWildcardByDefault(boolean lookupWildcardByDefault) {
		m_lookupWildcardByDefault = lookupWildcardByDefault;
	}

	public static String addWildcards(String searchTerm) {
		if(!searchTerm.endsWith(".")) {
			searchTerm = searchTerm.trim().replace("*", "%") + "%";
		}
		return searchTerm;
	}

}
