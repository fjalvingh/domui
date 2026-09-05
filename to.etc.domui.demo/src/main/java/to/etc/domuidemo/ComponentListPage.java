package to.etc.domuidemo;

import to.etc.domui.dom.html.Div;
import to.etc.domuidemo.pages.MenuPage;
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
import to.etc.domuidemo.pages.components.dialog.DialogPage;
import to.etc.domuidemo.pages.components.dialog.ErrorDisplayPage;
import to.etc.domuidemo.pages.components.dialog.FlarePage;
import to.etc.domuidemo.pages.components.dialog.InputDialogPage;
import to.etc.domuidemo.pages.components.dialog.MsgBox2Page;
import to.etc.domuidemo.pages.components.dialog.NoticePage;
import to.etc.domuidemo.pages.components.dialog.WindowPage;
import to.etc.domuidemo.pages.components.navigation.ALinkPage;
import to.etc.domuidemo.pages.components.navigation.BreadCrumb2Page;
import to.etc.domuidemo.pages.components.navigation.HamburgerMenuPage;
import to.etc.domuidemo.pages.components.navigation.PageTitleBarPage;
import to.etc.domuidemo.pages.components.navigation.PopupMenu2Page;
import to.etc.domuidemo.pages.components.images.FileUploadPage;
import to.etc.domuidemo.pages.components.images.IconsPage;
import to.etc.domuidemo.pages.components.images.ImageUploadPage;
import to.etc.domuidemo.pages.components.images.ImgPage;
import to.etc.domuidemo.pages.components.editors.AceEditorPage;
import to.etc.domuidemo.pages.components.editors.CKEditorPage;
import to.etc.domuidemo.pages.components.editors.HtmlEditorPage;
import to.etc.domuidemo.pages.components.async.AsyncContainerPage;
import to.etc.domuidemo.pages.components.async.AsyncDivPage;
import to.etc.domuidemo.pages.components.async.PollingDivPage;
import to.etc.domuidemo.pages.components.charts.BarChartPage;
import to.etc.domuidemo.pages.components.charts.GaugeChartPage;
import to.etc.domuidemo.pages.components.charts.PieChartPage;
import to.etc.domuidemo.pages.components.charts.SunburstChartPage;
import to.etc.domuidemo.pages.components.charts.TimeSeriesChartPage;
import to.etc.domuidemo.pages.components.display.DisplayBooleanPage;
import to.etc.domuidemo.pages.components.display.DisplayHtmlPage;
import to.etc.domuidemo.pages.components.display.DisplaySpanPage;
import to.etc.domuidemo.pages.components.display.RulerPage;
import to.etc.domuidemo.pages.components.layout.ChildFragmentPage;
import to.etc.domuidemo.pages.components.layout.HeadersPage;
import to.etc.domuidemo.pages.components.layout.PanelsPage;
import to.etc.domuidemo.pages.components.layout.SplitterPanelPage;
import to.etc.domuidemo.pages.components.layout.TabPanelPage;
import to.etc.domuidemo.pages.components.tables.ColumnDefPage;
import to.etc.domuidemo.pages.components.tables.DataTablePage;
import to.etc.domuidemo.pages.components.tables.TableBindingPage;
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
import to.etc.domuidemo.pages.tutorial.messages.MsgExceptionPage;
import to.etc.domuidemo.pages.binding.tbl.DemoObservableListPage;
import to.etc.domuidemo.pages.components.agenda.MonthPanelPage;
import to.etc.domuidemo.pages.components.agenda.WeekAgendaPage;
import to.etc.domuidemo.pages.components.dragdrop.DragDropDivPage;
import to.etc.domuidemo.pages.components.dragdrop.DragDropRowPage;

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

		ListFragment f = new ListFragment("Layout and page structure");
		main.add(f);
		f.addLink(PanelsPage.class, "Panels");
		f.addLink(HeadersPage.class, "Headers");
		f.addLink(TabPanelPage.class, "TabPanel");
		f.addLink(SplitterPanelPage.class, "SplitterPanel");
		f.addLink(ChildFragmentPage.class, "ChildFragment: the children of a record");

		f = new ListFragment("Windows, dialogs and messages");
		main.add(f);
		f.addLink(WindowPage.class, "Window: a floating window");
		f.addLink(DialogPage.class, "Dialog: a window with save and cancel");
		f.addLink(InputDialogPage.class, "InputDialog: asking for one value");
		f.addLink(MsgBox2Page.class, "MsgBox2: the message box");
		f.addLink(MsgExceptionPage.class, "ExceptionDialog: showing an exception");
		f.addLink(ErrorDisplayPage.class, "ErrorPanel and ErrorMessageDiv: showing messages");
		f.addLink(FlarePage.class, "MessageFlare: a message that vanishes");
		f.addLink(NoticePage.class, "MessageLine, InfoPanel and Explanation");

		f = new ListFragment("Navigation and menus");
		main.add(f);
		f.addLink(BreadCrumb2Page.class, "BreadCrumb2: the path that led here");
		f.addLink(PageTitleBarPage.class, "AppPageTitleBar: the bar at the top of a page");
		f.addLink(PopupMenu2Page.class, "PopupMenu2: a menu at a component");
		f.addLink(HamburgerMenuPage.class, "HamburgerMenu: the menu of an ExpandHeader");
		f.addLink(ALinkPage.class, "ALink: a link to another page");

		f = new ListFragment("Images, icons and file upload");
		main.add(f);
		f.addLink(IconsPage.class, "Icons: font, svg and image");
		f.addLink(ImgPage.class, "Img: a picture on the page");
		f.addLink(ImageUploadPage.class, "DisplayImage and ImageSelectControl");
		f.addLink(FileUploadPage.class, "FileUpload2 and FileUploadMultiple");

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

		f = new ListFragment("Rich content editors");
		main.add(f);
		f.addLink(HtmlEditorPage.class, "HtmlEditor: the small wysiwyg editor");
		f.addLink(CKEditorPage.class, "CKEditor: the full wysiwyg editor");
		f.addLink(AceEditorPage.class, "AceEditor: the code editor");

		f = new ListFragment("Display-only components");
		main.add(f);
		f.addLink(DisplaySpanPage.class, "DisplaySpan: showing a value");
		f.addLink(DisplayBooleanPage.class, "Showing a yes or no");
		f.addLink(DisplayHtmlPage.class, "DisplayHtml: showing html");
		f.addLink(RulerPage.class, "A ruler and a piece of code");

		f = new ListFragment("Tables, lists and trees");
		main.add(f);
		f.addLink(DataTablePage.class, "DataTable: rows on the screen");
		f.addLink(ColumnDefPage.class, "Columns: what a column can be told");
		f.addLink(TableModelPage.class, "Table models");
		f.addLink(TableSelectionPage.class, "Selecting rows");
		f.addLink(TableEditPage.class, "Editing in a table");
		f.addLink(TableBindingPage.class, "Data binding in a table");
		f.addLink(OtherTablesPage.class, "A grid and a shuttle");
		f.addLink(Tree3Page.class, "Tree3: showing a tree");

		f = new ListFragment("Drag and drop");
		main.add(f);
		f.addLink(DragDropDivPage.class, "Dragging nodes between two zones");
		f.addLink(DragDropRowPage.class, "Dropping into a table, at a position");

		f = new ListFragment("Asynchronous and long-running work");
		main.add(f);
		f.addLink(AsyncContainerPage.class, "AsyncContainer: work on a thread of its own");
		f.addLink(AsyncDivPage.class, "AsyncDiv: the same, with the result built for you");
		f.addLink(PollingDivPage.class, "PollingDiv: a piece of screen that refreshes itself");

		f = new ListFragment("Agenda and calendar");
		main.add(f);
		f.addLink(WeekAgendaPage.class, "WeekAgendaComponent: a week of appointments");
		f.addLink(MonthPanelPage.class, "MonthPanel: a month to pick a day from");

		f = new ListFragment("Data binding");
		main.add(f);
		f.addLink(DemoObservableListPage.class, "Database relation IObservableList binding");
		f.addLink(EditableTablePage.class, "Editable table using data binding and a model");
		//f.addLink(InvoiceListPage.class, "Editable table using data binding and a model");

		f = new ListFragment("Charts");
		main.add(f);
		f.addLink(TimeSeriesChartPage.class, "Time series");
		f.addLink(BarChartPage.class, "Bar charts");
		f.addLink(PieChartPage.class, "Pie and donut charts");
		f.addLink(SunburstChartPage.class, "Sunburst charts");
		f.addLink(GaugeChartPage.class, "Gauges");
	}
}
