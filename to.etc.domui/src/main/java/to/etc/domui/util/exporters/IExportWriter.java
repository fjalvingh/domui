package to.etc.domui.util.exporters;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
@NonNullByDefault
public interface IExportWriter<R> extends AutoCloseable {
	String getMimeType();

	void startExport(List<? extends IExportColumn<?>> columnList) throws Exception;

	void exportRow(R data) throws Exception;

	int getRowLimit();

	@Override void close() throws Exception;
}
