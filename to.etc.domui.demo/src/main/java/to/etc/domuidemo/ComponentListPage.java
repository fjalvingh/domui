package to.etc.domuidemo;

import to.etc.domuidemo.pages.DataTable1Page;
import to.etc.domuidemo.pages.MenuPage;
import to.etc.domuidemo.pages.TableMenuPage;
import to.etc.domuidemo.pages.basic.DemoTextArea;
import to.etc.domuidemo.pages.binding.tbl.DemoObservableListPage;
import to.etc.domuidemo.pages.graphs.GraphPage;
import to.etc.domuidemo.pages.overview.agenda.DemoWeekAgenda;
import to.etc.domuidemo.pages.overview.buttons.DemoDefaultButton;
import to.etc.domuidemo.pages.overview.buttons.DemoLinkButton;
import to.etc.domuidemo.pages.overview.delayed.DemoAsyncContainer;
import to.etc.domuidemo.pages.overview.delayed.DemoPollingDiv;
import to.etc.domuidemo.pages.overview.dnd.DemoDragDrop;
import to.etc.domuidemo.pages.overview.dnd.DemoTableInDrag;
import to.etc.domuidemo.pages.overview.graph.DemoColorPicker;
import to.etc.domuidemo.pages.overview.graph.DemoColorPicker2;
import to.etc.domuidemo.pages.overview.htmleditor.DemoCKEditor;
import to.etc.domuidemo.pages.overview.htmleditor.DemoDisplayHtml;
import to.etc.domuidemo.pages.overview.htmleditor.DemoHtmlEditor;
import to.etc.domuidemo.pages.overview.input.DemoBulkUpload;
import to.etc.domuidemo.pages.overview.input.DemoCheckbox;
import to.etc.domuidemo.pages.overview.input.DemoComboFixed;
import to.etc.domuidemo.pages.overview.input.DemoDateInput;
import to.etc.domuidemo.pages.overview.input.DemoFileUpload;
import to.etc.domuidemo.pages.overview.input.DemoRadioButton;
import to.etc.domuidemo.pages.overview.input.DemoSearchAsYouType1;
import to.etc.domuidemo.pages.overview.input.DemoSearchAsYouType2;
import to.etc.domuidemo.pages.overview.input.DemoText;
import to.etc.domuidemo.pages.overview.input.DemoTextStr;
import to.etc.domuidemo.pages.overview.layout.DemoAppTitle;
import to.etc.domuidemo.pages.overview.layout.DemoCaption;
import to.etc.domuidemo.pages.overview.layout.DemoCaptionedHeader;
import to.etc.domuidemo.pages.overview.layout.DemoCaptionedPanel;
import to.etc.domuidemo.pages.overview.layout.DemoMessageLine;
import to.etc.domuidemo.pages.overview.layout.DemoScrollableTabPanel;
import to.etc.domuidemo.pages.overview.layout.DemoSplitterPanel;
import to.etc.domuidemo.pages.overview.layout.DemoTabPanel;
import to.etc.domuidemo.pages.overview.lookup.DemoLookupForm;
import to.etc.domuidemo.pages.overview.lookup.DemoLookupForm2;
import to.etc.domuidemo.pages.overview.menu.DemoPopupMenu;
import to.etc.domuidemo.pages.overview.misc.DemoALink;
import to.etc.domuidemo.pages.overview.misc.DemoDisplayCheckbox;
import to.etc.domuidemo.pages.overview.misc.DemoDisplayValue;
import to.etc.domuidemo.pages.overview.misc.DemoMsgBox;
import to.etc.domuidemo.pages.overview.tree.DemoTree;
import to.etc.domuidemo.pages.overview.tree2.Tree2DemoPage;
import to.etc.domuidemo.pages.searchpanel.SearchPanelMenuPage;
import to.etc.domuidemo.pages.special.ace.AcePage;
import to.etc.formbuilder.pages.FormDesigner;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-7-17.
 */
public class ComponentListPage extends MenuPage {
	public ComponentListPage() {
		super("Component Overview");
	}

	@Override public void createContent() throws Exception {
		addCaption("Layout components");
		addLink(DemoCaptionedHeader.class, "The CaptionedHeader");
		addLink(DemoCaption.class, "The Caption component");
		addLink(DemoCaptionedPanel.class, "The CaptionedPanel component");
		addLink(DemoAppTitle.class, "The AppPageTitle component");
		addLink(DemoTabPanel.class, "The TabPanel component");
		addLink(DemoScrollableTabPanel.class, "The ScrollableTabPanel panel, when there's many tabs to show.");
		addLink(DemoSplitterPanel.class, "The SplitterPanel, containing two panels with a movable separator between them");
		addLink(DemoMessageLine.class, "A message line");
		addLink(DemoMsgBox.class, "The Message Box");

		addCaption("Simple components");
		addLink(DemoDefaultButton.class, "The DefaultButton");
		addLink(DemoLinkButton.class, "The LinkButton");
		addLink(DemoALink.class, "The ALink and ATag components: several kinds of links");

		addCaption("Input components");
		addLink(DemoCheckbox.class, "The checkbox component");
		addLink(DemoRadioButton.class, "The RadioButton components");
		addLink(DemoText.class, "The Text<T> component");
		addLink(DemoTextStr.class, "The TextStr component (shortcut for Text<String>)");
		addLink(DemoDateInput.class, "The DateInput component for date and datetime input");
		addLink(DemoComboFixed.class, "The ComboFixed component");
		addLink(DemoFileUpload.class, "File upload component");
		addLink(DemoBulkUpload.class, "The bulk file upload component");
		addLink(DemoTextArea.class, "The TextArea component");
		addLink(DemoHtmlEditor.class, "The small and fast HTMLEditor component");
		addLink(DemoCKEditor.class, "The big HTML editor - CKEditor component");

		addCaption("Search as you type");
		addLink(DemoSearchAsYouType1.class, "Component to search-as-you-type in a list of possible values");
		addLink(DemoSearchAsYouType2.class, "Component to search-as-you-type in a list of possible values");

		addCaption("Display-only components");
		addLink(DemoDisplayValue.class, "The DisplayValue component");
		addLink(DemoDisplayHtml.class, "The DisplayHtml component");
		addLink(DemoDisplayCheckbox.class, "The DisplayCheckbox component");

		addCaption("Graphical components");
		addLink(DemoColorPicker.class, "The color picker in flat (opened) mode");
		addLink(DemoColorPicker2.class, "The color picker in button mode");
		addLink(GraphPage.class, "DOES NOT YET WORK- Pie chart using a dynamic image/JChart");

		addCaption("Tables");
		addLink(TableMenuPage.class, "Data tables, row renderers and ITableModels.");
		addLink(DataTable1Page.class, "Simple data table");

		addCaption("Trees");
		addLink(Tree2DemoPage.class, "The tree2 component");
		addLink(DemoTree.class, "The old tree component - file system tree, lazily loaded, and file type icons");

		addCaption("Drag and drop");
		addLink(DemoDragDrop.class, "Drag and drop - Petstore (DIV dropmode)");
		addLink(DemoTableInDrag.class, "Drag and drop - ordered row drop mode");

		addCaption("LookupForm and form builder");
		addLink(SearchPanelMenuPage.class, "SearchPanel: searching for things in the database");

		addLink(DemoLookupForm.class, "Using a lookupform to generalize search pages");
		addLink(DemoLookupForm2.class, "LookupForm with LookupInput for a many-to-one relation, and search-as-you-type");

		addLink(FormDesigner.class, "Form designer - work in progress");

		addCaption("Special components");
		addLink(AcePage.class, "The ACE code editor");
		addLink(DemoWeekAgenda.class, "The WeekAgenda");
		addLink(DemoAsyncContainer.class, "The AsyncContainer");
		addLink(DemoPollingDiv.class, "The PollingDiv component");
		addLink(DemoPopupMenu.class, "Popup menu");

		addCaption("Binding");
		addLink(DemoObservableListPage.class, "Database relation IObservableList binding");
	}
}
