package to.etc.domui.component2.lookupinput;

import java.math.*;
import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;
import to.etc.webapp.*;
import to.etc.webapp.query.*;

public class DefaultStringQueryFactory<QT> implements IStringQueryFactory<QT> {
	@Override
	public QCriteria<QT> createQuery(String searchString) throws Exception {
		searchString = DomUtil.nullChecked(searchString.replace("*", "%"));

		//-- Has default meta?
		List<SearchPropertyMetaModel> spml = m_keywordLookupPropertyList == null ? getQueryMetaModel().getKeyWordSearchProperties() : getKeywordLookupPropertyList();
		QCriteria<QT> searchQuery = (QCriteria<QT>) getQueryMetaModel().createCriteria();

		QRestrictor<QT> r = searchQuery.or();
		int ncond = 0;
		if(spml.size() > 0) {
			for(SearchPropertyMetaModel spm : spml) {
				if(spm.getMinLength() <= searchString.length()) {

					//-- Abort on invalid metadata; never continue with invalid data.
					if(spm.getPropertyName() == null)
						throw new ProgrammerErrorException("The quick lookup properties for " + getQueryMetaModel() + " are invalid: the property name is null");

					List<PropertyMetaModel< ? >> pl = MetaManager.parsePropertyPath(getQueryMetaModel(), spm.getPropertyName()); // This will return an empty list on empty string input
					if(pl.size() == 0)
						throw new ProgrammerErrorException("Unknown/unresolvable lookup property " + spm.getPropertyName() + " on " + getQueryMetaModel());

					//It is required that lookup by id is also available, for now only Long type and BigDecimal interpretated as Long (fix for 1228) are supported
					//FIXME: see if it is possible to generalize things for all integer based types... (DomUtil.isIntegerType(pmm.getActualType()))
					if(pl.get(0).getActualType() == Long.class || pl.get(0).getActualType() == BigDecimal.class) {
						if(searchString.contains("%") && !pl.get(0).isTransient()) {
							r.add(new QPropertyComparison(QOperation.LIKE, spm.getPropertyName(), new QLiteral(searchString)));
						} else {
							try {
								Long val = Long.valueOf(searchString);
								if(val != null) {
									r.eq(spm.getPropertyName(), val.longValue());
									ncond++;
								}
							} catch(NumberFormatException ex) {
								//just ignore this since it means that it is not correct Long condition.
							}
						}
					} else if(pl.get(0).getActualType().isAssignableFrom(String.class)) {
						if(spm.isIgnoreCase()) {
							r.ilike(spm.getPropertyName(), searchString + "%");
						} else {
							r.like(spm.getPropertyName(), searchString + "%");
						}
						ncond++;
					}
				}
			}
		}
		if(ncond == 0) {
			return null;		//no search meta data is matching minimal length condition, search is cancelled
		}
	}

}
