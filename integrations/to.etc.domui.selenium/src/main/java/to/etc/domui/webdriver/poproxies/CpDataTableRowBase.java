package to.etc.domui.webdriver.poproxies;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 09-12-21.
 */
public class CpDataTableRowBase extends AbstractCpBase {
	public CpDataTableRowBase(CpDataTable<?> dataTable) {
		super(dataTable.wd());
	}
}
