package to.etc.domui.webdriver.poproxies;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 09-12-21.
 */
public class CpDataTableRowBase extends AbstractCpBase {
	private final int m_rowIndex;

	public CpDataTableRowBase(CpDataTable<?> dataTable, int rowIndex) {
		super(dataTable.wd());
		m_rowIndex = rowIndex;
	}

	public String getCellSelector(int columnIndex) {
		return "";
	}

	public String getCellComponentSelector(int columnIndex, String componentTestID) {
		return "";
	}


}
