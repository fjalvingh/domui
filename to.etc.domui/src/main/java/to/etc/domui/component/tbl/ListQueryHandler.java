package to.etc.domui.component.tbl;

import to.etc.domui.component.meta.MetaManager;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This handler allows basic QCriteria filtering on any list of objects. Not all QCriteria
 * options are available; see {@link to.etc.domui.util.db.CriteriaMatchingVisitor} for details.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
public class ListQueryHandler<T> implements IQueryHandler<T> {
	private final List<T> m_list;

	public ListQueryHandler(List<T> list) {
		m_list = list;
	}

	@Nonnull @Override public List<T> query(@Nonnull QCriteria<T> q) throws Exception {
		List<T> filter = MetaManager.query(m_list, q);
		return filter;
	}
}
