package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.List;
import java.util.function.Supplier;

@NonNullByDefault
public class CpDataTable<T extends CpDataTableRowBase> extends AbstractCpComponent {

	public CpDataTable(WebDriverConnector connector, Supplier<String> selectorProvider) {
		super(connector, selectorProvider);
	}

	//public int getNumberOfRows() {
	//	return getRows().size();
	//}

	/**
	 * The DataTable is empty if it has no table inside.
	 */
	public boolean isEmpty() {
		var table = wd().findElement(selector("table"));
		return table == null;
	}

	@Nullable
	public WebElement getTBody() {
		return wd().findElement(selector("tbody"));
	}

	private WebElement getTable() {
		var table = wd().findElement(selector("table"));
		if(table == null) {
			throw new IllegalStateException("cant find table: is results empty?");
		}
		return table;
	}

	/**
	 * Return the number of rows that is currently visible in the data table. This
	 * is NOT the total #of rows available if the component has paging set.
	 */
	public int getVisibleRowCount() {
		WebElement body = getTBody();
		if(null == body)
			return 0;
		List<WebElement> children = wd().getChildren(body);
		return children.size();
	}


	//public List<RowPO> getRows() {
	//	return getTable().findElements(By.cssSelector("tbody tr")).stream().map(x->new RowPO(x, wd())).collect(Collectors.toUnmodifiableList());
	//}
	//
	//public RowPO getRow(int i) {
	//	return getRows().get(i);
	//}

	//public class RowPO {
	//	private WebElement m_webElement;
	//
	//	private WebDriverConnector m_wd;
	//
	//	public RowPO(WebElement row, WebDriverConnector wd) {
	//		m_webElement = row;
	//		m_wd = wd;
	//	}
	//
	//	public ColumnPO getColumn(int column) {
	//		var columns =  getColumns();
	//		return columns.get(column);
	//	}
	//
	//	public List<ColumnPO> getColumns() {
	//		return m_webElement.findElements(
	//			By.cssSelector("td")
	//		).stream().map(x->new ColumnPO(x,m_wd)).collect(Collectors.toUnmodifiableList());
	//	}
	//}

	public class ColumnPO {
		private WebElement m_webElement;

		private WebDriverConnector m_wd;

		public ColumnPO(WebElement x, WebDriverConnector wd) {
			m_webElement = x;
			m_wd = wd;
		}

		public void click() {
			m_webElement.click();
		}

		public void check() {
			if("input".equalsIgnoreCase(m_webElement.getTagName())) {
				m_webElement.click();
				return;
			}
			var input = m_webElement.findElement(By.tagName("input"));
			if(input == null) {
				throw new IllegalStateException("Column is not a checkbox");
			}
			input.click();
		}

		public String getText() {
			return m_webElement.getText();
		}
	}
}
