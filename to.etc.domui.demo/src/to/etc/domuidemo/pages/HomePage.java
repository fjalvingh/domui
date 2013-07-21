package to.etc.domuidemo.pages;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domuidemo.pages.basic.*;
import to.etc.domuidemo.pages.binding.*;
import to.etc.domuidemo.pages.formbuilder.*;
import to.etc.domuidemo.pages.graphs.*;
import to.etc.domuidemo.pages.overview.*;
import to.etc.domuidemo.pages.overview.agenda.*;
import to.etc.domuidemo.pages.overview.buttons.*;
import to.etc.domuidemo.pages.overview.delayed.*;
import to.etc.domuidemo.pages.overview.dnd.*;
import to.etc.domuidemo.pages.overview.graph.*;
import to.etc.domuidemo.pages.overview.htmleditor.*;
import to.etc.domuidemo.pages.overview.input.*;
import to.etc.domuidemo.pages.overview.layout.*;
import to.etc.domuidemo.pages.overview.lookup.*;
import to.etc.domuidemo.pages.overview.menu.*;
import to.etc.domuidemo.pages.overview.misc.*;
import to.etc.domuidemo.pages.overview.tbl.*;
import to.etc.domuidemo.pages.overview.tree.*;
import to.etc.domuidemo.sourceviewer.*;

public class HomePage extends UrlPage {
	public HomePage() {
		setPageTitle("Component Overview - DomUI");
	}

	@Override
	public void createContent() throws Exception {
		Div ip = new Div();
		add(ip);
		ip.setCssClass("ui-expl");
		Img i = new Img("THEME/big-info.png");
		i.setAlign(ImgAlign.LEFT);
		ip.add(i);
		String text = "Welcome to the DomUI demo application! This application has simple examples of many of the components. It also has some code "
			+ "from the tutorial. Use it to get "
			+ "an idea on what is possible with DomUI, and how easy it is! Click the links to go to a page, and when done use the \"breadcrumbs\" in the "
			+ "bar on top of the screen to return back to where you came from."
 + "<br><br>Please keep in mind: the examples here have been made as <b>simple as possible</b>. " //
			+ "Which means that the code is quite verbose sometimes. That is not how it usually is, of course, " //
			+ "it is done that way to make 'how it works' as clear as possible."
;
		ip.add(text);

		Div d = new Div();
		ip.add(d);
		//		d.setCssClass("d-expl");
		d.setText("At any time, you can press the Java icon ");
		d.add(new Img("img/java.png"));
		d.add(" to get a window showing the Java source code for the screen in question. In this window you can click the underlined class names to go to their sources too.");

		addCaption("Layout components");
		addLink(DemoCaptionedHeader.class, "The CaptionedHeader");
		addLink(DemoCaption.class, "The Caption component");
		addLink(DemoCaptionedPanel.class, "The CaptionedPanel component");
		addLink(DemoAppTitle.class, "The AppPageTitle component");
		addLink(DemoTabPanel.class, "The TabPanel component");
		addLink(DemoScrollableTabPanel.class, "The ScrollableTabPanel panel, when there's many tabs to show.");
		addLink(DemoSplitterPanel.class, "The SplitterPanel, containing two panels with a movable separator between them");

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
		addLink(DemoFCKEditor.class, "The big HTML editor - FCKEditor component");
//		addLink(.class, "");
		//		addLink(.class, "");
		//		addLink(.class, "");

		addCaption("Display-only components");
		addLink(DemoDisplayValue.class, "The DisplayValue component");
		addLink(DemoDisplayHtml.class, "The DisplayHtml component");
		addLink(DemoDisplayCheckbox.class, "The DisplayCheckbox component");

		addCaption("Graphical components");
		addLink(DemoColorPicker.class, "The color picker in flat (opened) mode");
		addLink(DemoColorPicker2.class, "The color picker in button mode");
		addLink(GraphPage.class, "DOES NOT YET WORK- Pie chart using a dynamic image/JChart");

		addCaption("Tables");
		addLink(DemoTableModel.class, "Using a DataTable and a simple ITableModel implementation");

		addLink(DatabaseSchemaExpl.class, "Explanation of the database schema for database examples");
		addLink(DemoDataTable.class, "The DataTable component and SimpleSearchModel: simplest use");
		addLink(DemoDataPager.class, "The DataPager component");
		addLink(DemoRowRenderer1.class, "Using the BasicRowRenderer, part 1");
		addLink(DemoRowRenderer2.class, "Using the BasicRowRenderer with an INodeContentRenderer");
		addLink(DemoDataTable2.class, "DataTable: making rows clickable");
		addLink(DemoTableSelect.class, "DataTable: adding select row(s) functionality with ISelectionModel");
		addLink(DemoSortableListTable.class, "DataTable: using a List<> with sorting");

		addCaption("Trees");
		addLink(DemoTree.class, "The tree component - file system tree, lazily loaded, and file type icons");

		addCaption("Drag and drop");
		addLink(DemoDragDrop.class, "Drag and drop - Petstore (DIV dropmode)");
		addLink(DemoTableInDrag.class, "Drag and drop - ordered row drop mode");

		addCaption("LookupForm and form builder");
		addLink(DemoLookupForm.class, "Using a lookupform to generalize search pages");
		addLink(DemoLookupForm2.class, "LookupForm with LookupInput for a many-to-one relation, and search-as-you-type");

		addLink(SimpleForm1.class, "The FormBuilder - very simple edit page");

		addCaption("Special components");
		addLink(DemoWeekAgenda.class, "The WeekAgenda");
		addLink(DemoAsyncContainer.class, "The AsyncContainer");
		addLink(DemoPollingDiv.class, "The PollingDiv component");
		addLink(DemoPopupMenu.class, "Popup menu");

		addCaption("Binding");
		addLink(BindingBasePage.class, "Basic data binding");


	}

	private void addCaption(String txt) {
		add(new VerticalSpacer(10));
		add(new CaptionedHeader(txt));
	}

	private void addLink(Class< ? extends UrlPage> clz, String text) {
		addLink(clz, text, false);
	}

	private void addLink(Class< ? extends UrlPage> clz, String text, boolean nw) {
		Div d = new Div();
		add(d);
		ALink link = new ALink(clz);
		d.add(link);
		link.setText(text);

		ALink link2 = new ALink(SourcePage.class, new PageParameters("name", clz.getName().replace('.', '/') + ".java"));
		d.add("\u00a0");
		d.add(link2);
		Img si = new Img("img/java.png");
		link2.add(si);
		link2.setTitle("View sourcefile");
		if(nw)
			d.add(new Img("img/aniNew.gif"));
	}
}
