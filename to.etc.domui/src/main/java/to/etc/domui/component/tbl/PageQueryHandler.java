package to.etc.domui.component.tbl;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.webapp.query.*;

/**
 * This query handler uses the page's data context factory to issue a query.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 10, 2014
 */
final public class PageQueryHandler<T> implements IQueryHandler<T> {
	@Nonnull
	private NodeBase m_source;

	public PageQueryHandler(@Nonnull NodeBase source) {
		m_source = source;
	}

	@Override
	public List<T> query(QCriteria<T> q) throws Exception {
		return m_source.getSharedContext().query(q);
	}
}
