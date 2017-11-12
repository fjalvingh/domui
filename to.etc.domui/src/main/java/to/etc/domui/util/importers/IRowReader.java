package to.etc.domui.util.importers;

import java.io.IOException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
public interface IRowReader extends Iterable<IImportRow>, AutoCloseable {
	IImportRow getHeaderRow();

	int getSetCount();

	void setSetIndex(int setIndex);

	long getSetSizeIndicator();

	@Override
	void close() throws IOException;

	void setHasHeaderRow(boolean hasHeaderRow);

	long getProgressIndicator();
}
