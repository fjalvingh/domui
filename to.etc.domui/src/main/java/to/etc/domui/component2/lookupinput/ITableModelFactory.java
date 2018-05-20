package to.etc.domui.component2.lookupinput;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.tbl.IQueryHandler;
import to.etc.domui.component.tbl.ITableModel;
import to.etc.webapp.query.QCriteria;

public interface ITableModelFactory<QT, DT> {
	@NonNull
	ITableModel<DT> createTableModel(@NonNull IQueryHandler<QT> handler, @NonNull QCriteria<QT> query) throws Exception;
}
