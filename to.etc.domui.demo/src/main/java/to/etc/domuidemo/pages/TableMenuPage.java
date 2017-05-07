package to.etc.domuidemo.pages;

import to.etc.domuidemo.pages.binding.tbl.*;
import to.etc.domuidemo.pages.overview.*;
import to.etc.domuidemo.pages.overview.tbl.*;

public class TableMenuPage extends MenuPage {
	public TableMenuPage() {
		super("Data tables, row renderers and ITableModels");
	}

	@Override
	public void createContent() throws Exception {
		addCaption("Basic");
		addLink(DemoTableModel.class, "Using a DataTable and a simple ITableModel implementation");
		addLink(DatabaseSchemaExpl.class, "Explanation of the database schema for database examples");
		addLink(DemoDataTable.class, "The DataTable component and SimpleSearchModel: simplest use");
		addLink(DemoDataPager.class, "The DataPager component");
		addLink(DemoRowRenderer1.class, "Using the BasicRowRenderer, part 1");
		addLink(DemoRowRenderer2.class, "Using the BasicRowRenderer with an INodeContentRenderer");
		addLink(DemoDataTable2.class, "DataTable: making rows clickable");
		addLink(DemoTableSelect.class, "DataTable: adding select row(s) functionality with ISelectionModel");
		addLink(DemoSortableListTable.class, "DataTable: using a List<> with sorting");

		addCaption("Data tables using data binding");
		addLink(DemoTableBinding1.class, "DataTable display data binding: when a property value changes so does the column's displayed value");
		addLink(DemoTableBinding2.class, "Using a DataTable with editable rows");
		addLink(DemoObservableListPage.class, "Database relation IObservableList binding");
	}
}
