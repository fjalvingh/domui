package to.etc.domui.util.exporters;

import javax.annotation.DefaultNonNull;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
@DefaultNonNull
public interface IExportWriter<R> extends AutoCloseable {
	String getMimeType();

	void startExport(List<? extends IExportColumn<?>> columnList) throws Exception;

	void exportRow(R data) throws Exception;

	int getRowLimit();

	@Override void close() throws Exception;
}
