package to.etc.domui.util.importers;

import to.etc.domui.component.delayed.IProgress;
import to.etc.domui.dom.html.NodeContainer;

import java.io.File;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
abstract public class AbstractImportTask<R> implements IImportTask<R> {
	@Override public R execute(File input, IProgress progress) throws Exception {
		try(IRowReader reader = openReader(input)) {
			execute(reader, progress);
		}
		return null;
	}

	/**
	 * By default this just reads dataset 0.
	 */
	private void execute(IRowReader reader, IProgress progress) throws Exception {
		reader.setHasHeaderRow(true);
		reader.setSetIndex(0);
		IImportRow headerRow = reader.getHeaderRow();
		onHeaderRow(headerRow);
		progress.setTotalWork((int) reader.getSetSizeIndicator());
		for(IImportRow row : reader) {
			onRow(row);
			progress.setCompleted((int) reader.getProgressIndicator());
		}
	}

	protected abstract void onRow(IImportRow row) throws Exception;

	private void onHeaderRow(IImportRow headerRow) throws Exception {
	}

	abstract protected IRowReader openReader(File input) throws Exception;

	@Override public void onComplete(NodeContainer node, R result) {

	}
}
