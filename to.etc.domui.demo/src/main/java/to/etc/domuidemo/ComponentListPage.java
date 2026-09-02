package to.etc.domuidemo;

import to.etc.domui.dom.html.Div;
import to.etc.domuidemo.pages.DataTable1Page;
import to.etc.domuidemo.pages.MenuPage;
import to.etc.domuidemo.pages.TableMenuPage;
import to.etc.domuidemo.pages.binding.editabletable.EditableTablePage;
import to.etc.domuidemo.pages.components.lookup.LookupInput2LookPage;
import to.etc.domuidemo.pages.components.lookup.LookupInput2Page;
import to.etc.domuidemo.pages.components.lookup.LookupInput2QueryPage;
import to.etc.domuidemo.pages.components.lookup.SearchAsYouTypePage;
import to.etc.domuidemo.pages.components.lookup.SearchPanelControlPage;
import to.etc.domuidemo.pages.components.lookup.SearchPanelFormPage;
import to.etc.domuidemo.pages.components.lookup.SearchPanelItemsPage;
import to.etc.domuidemo.pages.components.lookup.SearchPanelPage;
import to.etc.domuidemo.pages.components.buttons.ActionButtonPage;
import to.etc.domuidemo.pages.components.buttons.ButtonBar2Page;
import to.etc.domuidemo.pages.components.buttons.ButtonKindsPage;
import to.etc.domuidemo.pages.components.buttons.DefaultButtonPage;
import to.etc.domuidemo.pages.components.buttons.ToggleButtonPage;
import to.etc.domuidemo.pages.components.display.DisplayBooleanPage;
import to.etc.domuidemo.pages.components.display.DisplayHtmlPage;
import to.etc.domuidemo.pages.components.display.DisplaySpanPage;
import to.etc.domuidemo.pages.components.display.RulerPage;
import to.etc.domuidemo.pages.components.tables.ColumnDefPage;
import to.etc.domuidemo.pages.components.tables.DataTablePage;
import to.etc.domuidemo.pages.components.tables.TableEditPage;
import to.etc.domuidemo.pages.components.tables.OtherTablesPage;
import to.etc.domuidemo.pages.components.tables.TableModelPage;
import to.etc.domuidemo.pages.components.tables.TableSelectionPage;
import to.etc.domuidemo.pages.components.tables.Tree3Page;
import to.etc.domuidemo.pages.components.choice.CheckboxPage;
import to.etc.domuidemo.pages.components.choice.ComboFixed2Page;
import to.etc.domuidemo.pages.components.choice.ComboLookup2Page;
import to.etc.domuidemo.pages.components.choice.EnumSetInputPage;
import to.etc.domuidemo.pages.components.choice.RadioGroupPage;
import to.etc.domuidemo.pages.components.input.ColorPickerButtonPage;
import to.etc.domuidemo.pages.components.input.ColorPickerInputPage;
import to.etc.domuidemo.pages.components.input.ColorPickerPage;
import to.etc.domuidemo.pages.components.input.DateInput2Page;
import to.etc.domuidemo.pages.components.input.Text2LookPage;
import to.etc.domuidemo.pages.components.input.Text2Page;
import to.etc.domuidemo.pages.components.input.Text2ValidatePage;
import to.etc.domuidemo.pages.components.input.TextAreaPage;
import to.etc.domuidemo.pages.binding.tbl.DemoObservableListPage;
import to.etc.domuidemo.pages.graphs.GraphPage;
import to.etc.domuidemo.pages.overview.agenda.DemoWeekAgenda;
import to.etc.domuidemo.pages.overview.delayed.DemoAsyncContainer;
import to.etc.domuidemo.pages.overview.delayed.DemoPollingDiv;
import to.etc.domuidemo.pages.overview.dnd.DemoDragDrop;
import to.etc.domuidemo.pages.overview.dnd.DemoTableInDrag;
import to.etc.domuidemo.pages.overview.htmleditor.DemoCKEditor;
import to.etc.domuidemo.pages.overview.htmleditor.DemoCKEditorResizing;
import to.etc.domuidemo.pages.overview.htmleditor.DemoHtmlEditor;
import to.etc.domuidemo.pages.overview.input.DemoBulkUpload;
import to.etc.domuidemo.pages.overview.input.DemoFileUpload;
import to.etc.domuidemo.pages.overview.layout.DemoAppTitle;
import to.etc.domuidemo.pages.overview.layout.DemoCaption;
import to.etc.domuidemo.pages.overview.layout.DemoCaptionedHeader;
import to.etc.domuidemo.pages.overview.layout.DemoCaptionedPanel;
import to.etc.domuidemo.pages.overview.layout.DemoMessageLine;
import to.etc.domuidemo.pages.overview.layout.DemoSplitterPanel;
import to.etc.domuidemo.pages.overview.layout.DemoTabPanel;
import to.etc.domuidemo.pages.overview.menu.DemoPopupMenu;
import to.etc.domuidemo.pages.overview.misc.DemoALink;
import to.etc.domuidemo.pages.overview.misc.DemoMsgBox;
import to.etc.domuidemo.pages.overview.misc.SvgIconPage;
import to.etc.domuidemo.pages.plotly.Plotly1;
import to.etc.domuidemo.pages.plotly.PlotlyGaugePage;
import to.etc.domuidemo.pages.plotly.PlotlyPie1;
import to.etc.domuidemo.pages.plotly.PlotlyStackedBar;
import to.etc.domuidemo.pages.plotly.PlotlySunburst;
import to.etc.domuidemo.pages.special.ace.AcePage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-7-17.
 */
public class ComponentListPage extends MenuPage {
	public ComponentListPage() {
		super("Component Overview");
	}

	@Override public void createContent() throws Exception {
		Div main = new Div("dm-comp-page");
		add(main);

		ListFragment f = new ListFragment("Layout Components");
		main.add(f);

		f.addLink(DemoCaptionedHeader.class, "The CaptionedHeader");
		f.addLink(DemoCaption.class, "The Caption component");
		f.addLink(DemoCaptionedPanel.class, "The CaptionedPanel component");
		f.addLink(DemoAppTitle.class, "The AppPageTitle component");
		f.addLink(DemoTabPanel.class, "The TabPanel component");
		//f.addLink(DemoScrollableTabPanel.class, "The ScrollableTabPanel panel");		FIXME Broken
		f.addLink(DemoSplitterPanel.class, "The SplitterPanel");
		f.addLink(DemoMessageLine.class, "A message line");
		f.addLink(DemoMsgBox.class, "The Message Box");

		f = new ListFragment("Simple components");
		main.add(f);
		f.addLink(DemoALink.class, "The ALink and ATag components (links)");
		f.addLink(SvgIconPage.class, "Renders components with SVG icons");

		f = new ListFragment("Buttons and actions");
		main.add(f);
		f.addLink(DefaultButtonPage.class, "DefaultButton: the ordinary button");
		f.addLink(ButtonKindsPage.class, "The kinds of button");
		f.addLink(ToggleButtonPage.class, "CheckboxButton and SwitchButton");
		f.addLink(ActionButtonPage.class, "Buttons made from an action");
		f.addLink(ButtonBar2Page.class, "ButtonBar2: the bar the buttons sit on");

		f = new ListFragment("Text and value input");
		main.add(f);
		f.addLink(Text2Page.class, "Text2: a typed input box");
		f.addLink(Text2LookPage.class, "Text2: size, marker and buttons");
		f.addLink(Text2ValidatePage.class, "Text2: what getValue() checks");
		f.addLink(TextAreaPage.class, "TextArea: more than one line");
		f.addLink(DateInput2Page.class, "DateInput2: entering a date");
		f.addLink(ColorPickerPage.class, "ColorPicker: the open picker");
		f.addLink(ColorPickerButtonPage.class, "ColorPickerButton: a swatch to press");
		f.addLink(ColorPickerInputPage.class, "ColorPickerInput: the code and a swatch");

		f = new ListFragment("Choice input");
		main.add(f);
		f.addLink(CheckboxPage.class, "Checkbox: yes or no");
		f.addLink(RadioGroupPage.class, "RadioGroup: one out of a few");
		f.addLink(ComboFixed2Page.class, "ComboFixed2: a fixed list of choices");
		f.addLink(ComboLookup2Page.class, "ComboLookup2: choosing a record");
		f.addLink(EnumSetInputPage.class, "EnumSetInput: choosing several");

		f = new ListFragment("Lookup and search");
		main.add(f);
		f.addLink(LookupInput2Page.class, "LookupInput2: finding a record");
		f.addLink(LookupInput2LookPage.class, "LookupInput2: what it shows");
		f.addLink(LookupInput2QueryPage.class, "LookupInput2: limiting what can be found");
		f.addLink(SearchAsYouTypePage.class, "SearchAsYouType: typing the value");
		f.addLink(SearchPanelPage.class, "SearchPanel: a search screen from metadata");
		f.addLink(SearchPanelItemsPage.class, "SearchPanel: fields of your own");
		f.addLink(SearchPanelControlPage.class, "SearchPanel: controls of your own");
		f.addLink(SearchPanelFormPage.class, "SearchPanel: the form and its buttons");

		f = new ListFragment("Input Components");
		main.add(f);
		f.addLink(DemoFileUpload.class, "File upload component");
		f.addLink(DemoBulkUpload.class, "The bulk file upload component");
		f.addLink(DemoHtmlEditor.class, "The small and fast HTMLEditor component");
		f.addLink(DemoCKEditor.class, "CKEditor HTML component, fixed size");
		f.addLink(DemoCKEditorResizing.class, "CKEditor HTML component, auto resizing");
		f.addLink(AcePage.class, "The ACE code editor");

		f = new ListFragment("Display-only components");
		main.add(f);
		f.addLink(DisplaySpanPage.class, "DisplaySpan: showing a value");
		f.addLink(DisplayBooleanPage.class, "Showing a yes or no");
		f.addLink(DisplayHtmlPage.class, "DisplayHtml: showing html");
		f.addLink(RulerPage.class, "A ruler and a piece of code");

		f = new ListFragment("Graphical components");
		main.add(f);
		f.addLink(GraphPage.class, "DOES NOT YET WORK- Pie chart using a dynamic image/JChart");

		f = new ListFragment("Tables, lists and trees");
		main.add(f);
		f.addLink(DataTablePage.class, "DataTable: rows on the screen");
		f.addLink(ColumnDefPage.class, "Columns: what a column can be told");
		f.addLink(TableModelPage.class, "Table models");
		f.addLink(TableSelectionPage.class, "Selecting rows");
		f.addLink(TableEditPage.class, "Editing in a table");
		f.addLink(OtherTablesPage.class, "A grid and a shuttle");
		f.addLink(Tree3Page.class, "Tree3: showing a tree");

		f = new ListFragment("Tables: more examples");
		main.add(f);
		f.addLink(TableMenuPage.class, "Data tables, row renderers and ITableModels.");
		f.addLink(DataTable1Page.class, "Simple data table");

		f = new ListFragment("Drag and drop");
		main.add(f);
		f.addLink(DemoDragDrop.class, "Drag and drop - Petstore (DIV dropmode)");
		f.addLink(DemoTableInDrag.class, "Drag and drop - ordered row drop mode");

		f = new ListFragment("Form builders and form components");
		main.add(f);

		//f.addLink(FormDesigner.class, "Form designer - work in progress");

		f = new ListFragment("Special components");
		main.add(f);
		f.addLink(DemoWeekAgenda.class, "The WeekAgenda");
		f.addLink(DemoAsyncContainer.class, "The AsyncContainer");
		f.addLink(DemoPollingDiv.class, "The PollingDiv component");
		f.addLink(DemoPopupMenu.class, "Popup menu");

		f = new ListFragment("Data binding");
		main.add(f);
		f.addLink(DemoObservableListPage.class, "Database relation IObservableList binding");
		f.addLink(EditableTablePage.class, "Editable table using data binding and a model");
		//f.addLink(InvoiceListPage.class, "Editable table using data binding and a model");

		f = new ListFragment("Plotly");
		main.add(f);
		f.addLink(Plotly1.class, "Plotly time series demo");
		f.addLink(PlotlyStackedBar.class, "Plotly stacked bar demo");
		f.addLink(PlotlyPie1.class, "Pie chart");
		f.addLink(PlotlySunburst.class, "Sunburst");
		f.addLink(PlotlyGaugePage.class, "Gauges");
	}
}
