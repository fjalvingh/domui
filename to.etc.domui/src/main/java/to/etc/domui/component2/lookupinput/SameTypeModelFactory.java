package to.etc.domui.component2.lookupinput;

import to.etc.domui.component.tbl.*;
import to.etc.webapp.query.*;

/**
 * This is the "identical" type model factory, where QT and OT are actually the same type.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 10, 2014
 */
public class SameTypeModelFactory<T> implements ITableModelFactory<T, T> {
	@Override
	public ITableModel<T> createTableModel(IQueryHandler<T> handler, QCriteria<T> query) throws Exception {
		return new SimpleSearchModel<T>(handler, query);
	}
}
