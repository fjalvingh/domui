package to.etc.domui.util.importers;

import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
public interface IRowReader extends Iterable<IImportRow>, AutoCloseable {
	@Nullable
	IImportRow getHeaderRow() throws IOException;

	int getSetCount();

	List<IDatasetInfo> getSets() throws Exception;

	void setSetIndex(int setIndex);

	long getSetSizeIndicator();

	@Override
	void close() throws IOException;

	void setHasHeaderRow(boolean hasHeaderRow);

	long getProgressIndicator();
}
