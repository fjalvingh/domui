package to.etc.domui.util.exporters;

import javax.annotation.DefaultNonNull;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
@DefaultNonNull
public interface IExportWriter extends AutoCloseable {
	void startExport(List<IExportColumn> columnList) throws Exception;

	void exportRow(List<?> data) throws Exception;

	@Override
	void close() throws Exception;
}
