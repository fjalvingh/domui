package to.etc.domuidemo;

import to.etc.domui.dom.html.Div;
import to.etc.domuidemo.pages.DataTable1Page;
import to.etc.domuidemo.pages.MenuPage;
import to.etc.domuidemo.pages.TableMenuPage;
import to.etc.domuidemo.pages.basic.DemoTextArea;
import to.etc.domuidemo.pages.binding.editabletable.EditableTablePage;
import to.etc.domuidemo.pages.binding.tbl.DemoObservableListPage;
import to.etc.domuidemo.pages.graphs.GraphPage;
import to.etc.domuidemo.pages.overview.agenda.DemoWeekAgenda;
import to.etc.domuidemo.pages.overview.buttons.DemoDefaultButton;
import to.etc.domuidemo.pages.overview.buttons.DemoLinkButton;
import to.etc.domuidemo.pages.overview.buttons.RadioButtonPage;
import to.etc.domuidemo.pages.overview.delayed.DemoAsyncContainer;
import to.etc.domuidemo.pages.overview.delayed.DemoPollingDiv;
import to.etc.domuidemo.pages.overview.dnd.DemoDragDrop;
import to.etc.domuidemo.pages.overview.dnd.DemoTableInDrag;
import to.etc.domuidemo.pages.overview.graph.DemoColorPicker;
import to.etc.domuidemo.pages.overview.graph.DemoColorPicker2;
import to.etc.domuidemo.pages.overview.htmleditor.DemoCKEditor;
import to.etc.domuidemo.pages.overview.htmleditor.DemoCKEditorResizing;
import to.etc.domuidemo.pages.overview.htmleditor.DemoDisplayHtml;
import to.etc.domuidemo.pages.overview.htmleditor.DemoHtmlEditor;
import to.etc.domuidemo.pages.overview.input.DemoBulkUpload;
import to.etc.domuidemo.pages.overview.input.DemoCheckbox;
import to.etc.domuidemo.pages.overview.input.DemoComboFixed;
import to.etc.domuidemo.pages.overview.input.DemoDateInput;
import to.etc.domuidemo.pages.overview.input.DemoFileUpload;
import to.etc.domuidemo.pages.overview.input.DemoSearchAsYouType1;
import to.etc.domuidemo.pages.overview.input.DemoSearchAsYouType2;
import to.etc.domuidemo.pages.overview.input.DemoText;
import to.etc.domuidemo.pages.overview.layout.DemoAppTitle;
import to.etc.domuidemo.pages.overview.layout.DemoCaption;
import to.etc.domuidemo.pages.overview.layout.DemoCaptionedHeader;
import to.etc.domuidemo.pages.overview.layout.DemoCaptionedPanel;
import to.etc.domuidemo.pages.overview.layout.DemoMessageLine;
import to.etc.domuidemo.pages.overview.layout.DemoSplitterPanel;
import to.etc.domuidemo.pages.overview.layout.DemoTabPanel;
import to.etc.domuidemo.pages.overview.lookup.DemoLookupForm;
import to.etc.domuidemo.pages.overview.lookup.DemoLookupForm2;
import to.etc.domuidemo.pages.overview.menu.DemoPopupMenu;
import to.etc.domuidemo.pages.overview.misc.DemoALink;
import to.etc.domuidemo.pages.overview.misc.DemoDisplayCheckbox;
import to.etc.domuidemo.pages.overview.misc.DemoDisplayValue;
import to.etc.domuidemo.pages.overview.misc.DemoMsgBox;
import to.etc.domuidemo.pages.overview.misc.SvgIconPage;
import to.etc.domuidemo.pages.overview.tree.DemoTree;
import to.etc.domuidemo.pages.overview.tree2.Tree2DemoPage;
import to.etc.domuidemo.pages.plotly.Plotly1;
import to.etc.domuidemo.pages.searchpanel.SearchPanelMenuPage;
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

		f = new ListFragment("Buttons");
		main.add(f);
		f.addLink(DemoDefaultButton.class, "The DefaultButton");
		f.addLink(DemoLinkButton.class, "The LinkButton");
		f.addLink(RadioButtonPage.class, "The Radio button and ButtonGroup");
		f.addLink(DemoCheckbox.class, "The checkbox component");

		f = new ListFragment("Input Components");
		main.add(f);
		f.addLink(DemoText.class, "The Text2<T> component");
		//f.addLink(DemoTextStr.class, "The TextStr component (shortcut for Text<String>)");
		f.addLink(DemoDateInput.class, "The DateInput2 component for date and datetime input");
		f.addLink(DemoComboFixed.class, "The ComboFixed component");
		f.addLink(DemoFileUpload.class, "File upload component");
		f.addLink(DemoBulkUpload.class, "The bulk file upload component");
		f.addLink(DemoTextArea.class, "The TextArea component");
		f.addLink(DemoHtmlEditor.class, "The small and fast HTMLEditor component");
		f.addLink(DemoCKEditor.class, "CKEditor HTML component, fixed size");
		f.addLink(DemoCKEditorResizing.class, "CKEditor HTML component, auto resizing");
		f.addLink(AcePage.class, "The ACE code editor");

		f = new ListFragment("Search as you type");
		main.add(f);
		f.addLink(DemoSearchAsYouType1.class, "Component to search-as-you-type in a list of possible values");
		f.addLink(DemoSearchAsYouType2.class, "Component to search-as-you-type in a list of possible values");

		f = new ListFragment("Display-only components");
		main.add(f);
		f.addLink(DemoDisplayValue.class, "The DisplayValue component");
		f.addLink(DemoDisplayHtml.class, "The DisplayHtml component");
		f.addLink(DemoDisplayCheckbox.class, "The DisplayCheckbox component");

		f = new ListFragment("Graphical components");
		main.add(f);
		f.addLink(DemoColorPicker.class, "The color picker in flat (opened) mode");
		f.addLink(DemoColorPicker2.class, "The color picker in button mode");
		f.addLink(GraphPage.class, "DOES NOT YET WORK- Pie chart using a dynamic image/JChart");

		f = new ListFragment("Tables");
		main.add(f);
		f.addLink(TableMenuPage.class, "Data tables, row renderers and ITableModels.");
		f.addLink(DataTable1Page.class, "Simple data table");

		f = new ListFragment("Trees");
		main.add(f);
		f.addLink(Tree2DemoPage.class, "The tree2 component");
		f.addLink(DemoTree.class, "The old tree component - file system tree, lazily loaded, and file type icons");

		f = new ListFragment("Drag and drop");
		main.add(f);
		f.addLink(DemoDragDrop.class, "Drag and drop - Petstore (DIV dropmode)");
		f.addLink(DemoTableInDrag.class, "Drag and drop - ordered row drop mode");

		f = new ListFragment("Form builders and form components");
		main.add(f);
		f.addLink(SearchPanelMenuPage.class, "SearchPanel: searching for things in the database");
		f.addLink(DemoLookupForm.class, "Using a lookupform to generalize search pages");
		f.addLink(DemoLookupForm2.class, "LookupForm with LookupInput for a many-to-one relation, and search-as-you-type");

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
	}
}
