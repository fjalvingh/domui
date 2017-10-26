package to.etc.domui.util.exporters;

import javax.annotation.DefaultNonNull;
import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
@DefaultNonNull
public interface IExportWriter<R> {
	void startExport(File target, List<IExportColumn<?>> columnList) throws Exception;

	void exportRow(R data) throws Exception;

	int getRowLimit();

	void close() throws Exception;

	void finish() throws Exception;
}
