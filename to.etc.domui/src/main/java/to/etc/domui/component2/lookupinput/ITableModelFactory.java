package to.etc.domui.component2.lookupinput;

import javax.annotation.*;

import to.etc.domui.component.tbl.*;
import to.etc.webapp.query.*;

public interface ITableModelFactory<QT, DT> {
	@Nonnull
	public ITableModel<DT> createTableModel(@Nonnull IQueryHandler<QT> handler, @Nonnull QCriteria<QT> query) throws Exception;
}
