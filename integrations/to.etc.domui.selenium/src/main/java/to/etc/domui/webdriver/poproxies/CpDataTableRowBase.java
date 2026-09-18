package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNull;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import to.etc.domui.webdriver.core.WebDriverConnector;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 09-12-21.
 */
public class CpDataTableRowBase extends AbstractCpBase implements ICpWithElement {
	@NonNull
	private final CpDataTable<?> m_dataTable;

	private final int m_rowIndex;

	public CpDataTableRowBase(CpDataTable<?> dataTable, int rowIndex) {
		super(dataTable.wd());
		m_dataTable = dataTable;
		m_rowIndex = rowIndex;
	}

	public boolean isPresent() {
		return wd().isPresent(getSelector());
	}

	@Override
	@NonNull
	public WebElement getElement() {
		return wd().getElement(getSelector());
	}

	@NonNull
	public WebElement getCellElement(int column) {
		return wd().getElement(getCellSelector(column));
	}

	private String getSelectorCSS() {
		String tableSelector = m_dataTable.getSelectorSupplier().get();
		return tableSelector + " tbody > :nth-child(" + (m_rowIndex + 1) + ")";			// The idiots made it 1 based.
	}

	public By selector(String extra) {
		return By.cssSelector(getSelectorCSS() + " " + extra);
	}

	public By getSelector() {
		return By.cssSelector(getSelectorCSS());
	}

	public String getCellSelectorCss(int columnIndex) {
		return getSelectorCSS() + " > :nth-child(" + (columnIndex + 1) + ")";
	}

	/**
	 * The selector for a component inside one cell of this row. A component whose
	 * testid was set explicitly renders that id as it is, but an id that DomUI
	 * calculated itself is prefixed with the repeat id of the row it sits in
	 * (/r3/lbtn_Order) - and a page object only knows the part after that prefix,
	 * because that part is the same in every row. So: take the plain id when the
	 * cell has it, and otherwise match the end of a repeated one.
	 */
	public String getCellComponentSelectorCss(int columnIndex, String componentTestID) {
		String cellSelector = getCellSelectorCss(columnIndex);
		String exact = cellSelector + " " + WebDriverConnector.getTestIDSelector(componentTestID);
		if(wd().isPresent(By.cssSelector(exact)))
			return exact;
		return cellSelector + " *[testid$='/" + componentTestID + "']";
	}

	public By getCellSelector(int columnIndex) {
		return By.cssSelector(getCellSelectorCss(columnIndex));
	}

	public By getCellComponentSelector(int columnIndex, String testId) {
		return By.cssSelector(getCellComponentSelectorCss(columnIndex, testId));
	}
}
