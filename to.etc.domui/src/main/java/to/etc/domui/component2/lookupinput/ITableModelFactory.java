package to.etc.domui.component2.lookupinput;

import to.etc.domui.component.tbl.IQueryHandler;
import to.etc.domui.component.tbl.ITableModel;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;

public interface ITableModelFactory<QT, DT> {
	@Nonnull
	ITableModel<DT> createTableModel(@Nonnull IQueryHandler<QT> handler, @Nonnull QCriteria<QT> query) throws Exception;
}
