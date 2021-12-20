package to.etc.domui.webdriver.poproxies;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 09-12-21.
 */
public class CpDataTableColumn {
	private final CpDataTable<?> m_dataTable;

	private final int m_columnIndex;

	public CpDataTableColumn(CpDataTable<?> dataTable, int columnIndex) {
		m_dataTable = dataTable;
		m_columnIndex = columnIndex;
	}
}
