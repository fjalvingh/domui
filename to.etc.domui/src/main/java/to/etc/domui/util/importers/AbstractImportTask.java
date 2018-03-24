package to.etc.domui.util.importers;

import to.etc.domui.util.asyncdialog.AbstractAsyncDialogTask;
import to.etc.util.Progress;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
abstract public class AbstractImportTask extends AbstractAsyncDialogTask {
	@Nullable
	private File m_inputFile;

	public AbstractImportTask() {
	}

	@Override public void execute(Progress progress) throws Exception {
		try(IRowReader reader = openReader(Objects.requireNonNull(m_inputFile))) {
			initialize(reader);
			execute(reader, progress);
			finish(reader);
		}
	}

	protected void finish(IRowReader reader) throws Exception {

	}

	protected void initialize(IRowReader reader) throws Exception {

	}

	/**
	 * By default this just reads dataset 0.
	 */
	private void execute(IRowReader reader, Progress progress) throws Exception {
		reader.setHasHeaderRow(true);
		reader.setSetIndex(0);
		IImportRow headerRow = reader.getHeaderRow();
		onHeaderRow(headerRow);
		progress.setTotalWork((int) reader.getSetSizeIndicator());
		for(IImportRow row : reader) {
			if(! onRow(row))
				break;
			progress.setCompleted((int) reader.getProgressIndicator());
		}
	}

	protected abstract boolean onRow(IImportRow row) throws Exception;

	private void onHeaderRow(IImportRow headerRow) throws Exception {
	}

	@Nonnull
	abstract protected IRowReader openReader(File input) throws Exception;

	public void setInputFile(File inputFile) {
		m_inputFile = inputFile;
	}
}
