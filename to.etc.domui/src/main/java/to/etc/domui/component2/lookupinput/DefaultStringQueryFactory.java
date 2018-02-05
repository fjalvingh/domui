package to.etc.domui.component2.lookupinput;

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.util.DomUtil;
import to.etc.util.RuntimeConversions;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QLiteral;
import to.etc.webapp.query.QOperation;
import to.etc.webapp.query.QPropertyComparison;
import to.etc.webapp.query.QRestrictor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;

/**
 * This is the default query generator which calculates QCriteria for a quick search
 * done in {@link LookupInputBase2}. It uses the search properties to create a query
 * by taking the entered string, and creating an "or" of "like" operations for each
 * property we search on.
 * <p>For instance a search string 'Utr' could result in something like:</p>
 * <pre>
 *     streetname like 'UTR%' or cityname like 'UTR%'
 * </pre>
 *
 * @param <QT>
 */
public class DefaultStringQueryFactory<QT> implements IStringQueryFactory<QT> {
	/**
	 * The metamodel to use to handle the query data in this class. For Javabean data classes this is automatically
	 * obtained using MetaManager; for meta-based data models this gets passed as a constructor argument.
	 */
	@Nonnull
	final private ClassMetaModel m_queryMetaModel;

	/** Contains manually added quicksearch properties. Is null if none are added. */
	@Nullable
	private List<SearchPropertyMetaModel> m_keywordLookupPropertyList;

	public DefaultStringQueryFactory(@Nonnull ClassMetaModel queryMetaModel) {
		m_queryMetaModel = queryMetaModel;
	}

	@Override
	public QCriteria<QT> createQuery(String searchString) throws Exception {
		searchString = DomUtil.nullChecked(searchString.replace("*", "%"));
		if(searchString.startsWith("$$") && searchString.length() > 2) {
			String idString = searchString.substring(2);
			PropertyMetaModel<?> primaryKey = getQueryMetaModel().getPrimaryKey();
			if(null != primaryKey) {
				Class<?> pkType = primaryKey.getActualType();
				Object pk = RuntimeConversions.convertTo(idString, pkType);
				if(null != pk) {
					QCriteria<QT> searchQuery = (QCriteria<QT>) getQueryMetaModel().createCriteria();
					searchQuery.eq(primaryKey.getName(), pk);
					return searchQuery;
				}
			}
		}

		//-- Has default meta?
		List<SearchPropertyMetaModel> keywordLookupPropertyList = m_keywordLookupPropertyList;
		List<SearchPropertyMetaModel> spml = keywordLookupPropertyList == null ? getQueryMetaModel().getKeyWordSearchProperties() : keywordLookupPropertyList;
		QCriteria<QT> searchQuery = (QCriteria<QT>) getQueryMetaModel().createCriteria();

		QRestrictor<QT> r = searchQuery.or();
		int ncond = 0;
		if(spml.size() > 0) {
			for(SearchPropertyMetaModel spm : spml) {
				if(spm.getMinLength() <= searchString.length()) {

					//-- Abort on invalid metadata; never continue with invalid data.
					PropertyMetaModel<?> pmm = spm.getProperty();
					if(pmm == null)
						throw new ProgrammerErrorException("The quick lookup properties for " + getQueryMetaModel() + " are invalid: the property name is null");

					if(pmm.getActualType() == Long.class || pmm.getActualType() == BigDecimal.class) {
						if(searchString.contains("%") && !pmm.isTransient()) {
							r.add(new QPropertyComparison(QOperation.LIKE, pmm.getName(), new QLiteral(searchString)));
						} else {
							try {
								Object value = RuntimeConversions.convertTo(searchString, pmm.getActualType());
								if(null != value) {
									r.eq(pmm.getName(), value);
									ncond++;
								}
							} catch(Exception ex) {
								//just ignore this since it means that it is not correct Long condition.
							}
						}
					} else if(pmm.getActualType().isAssignableFrom(String.class)) {
						if(spm.isIgnoreCase()) {
							r.ilike(pmm.getName(), searchString + "%");
						} else {
							r.like(pmm.getName(), searchString + "%");
						}
						ncond++;
					}
				}
			}
		}
		if(ncond == 0) {
			return null;		//no search meta data is matching minimal length condition, search is cancelled
		}
		return searchQuery;
	}

	@Nonnull
	public ClassMetaModel getQueryMetaModel() {
		return m_queryMetaModel;
	}
}
