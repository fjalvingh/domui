package to.etc.domui.util.importers;

import org.apache.poi.ss.usermodel.Row;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
public class ExcelImportRow implements IImportRow {
	final private Row m_row;

	public ExcelImportRow(Row row) {
		m_row = row;
	}
}
