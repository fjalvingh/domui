package to.etc.domuidemo.pages.components.layout;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.ITabHandle;
import to.etc.domui.component.layout.ScrollableTabPanel;
import to.etc.domui.component.layout.TabPanel;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * TabPanel: several screens in the space of one, built with the tab builder -
 * and the scrolling variant for when there are too many to fit.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TabPanelPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("TabPanel");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "TabPanel"));

		Div shown = new Div("dm-tut-q");
		shown.add("Switch tabs, or close the last one.");

		//-- The panel is an error fence: a control error marks the tab it came from.
		TabPanel tp = new TabPanel(true);
		cp.add(tp);

		//-- A plain tab.
		Div details = new Div();
		details.add("The details of the album go here.");
		tp.tab().label("Details").content(details).build();

		//-- ...with an icon, and told when it is shown and hidden.
		Div tracks = new Div();
		tracks.add("The tracks of the album go here.");
		tp.tab().label("Tracks").image(Icon.faMusic).content(tracks)
			.onDisplay(handle -> {
				shown.removeAllChildren();
				shown.add("The Tracks tab was opened");
			})
			.onHide(handle -> {
				shown.removeAllChildren();
				shown.add("The Tracks tab was left");
			})
			.build();

		//-- A lazy tab: its content is built the first time it is opened.
		Div lazy = new Div();
		lazy.add("This content was only added to the page when the tab was first opened.");
		ITabHandle lazyHandle = tp.tab().label("Lazy").content(lazy).lazy().build();

		//-- A tab holding a mandatory field, to show what markErrorTabs does.
		Div form = new Div();
		Text2<String> name = new Text2<>(String.class);
		name.setMandatory(true);
		FormBuilder fb = new FormBuilder(form);
		fb.label("A mandatory field").control(name);
		tp.tab().label("With an error").content(form).build();

		//-- ...and one the user may close.
		Div closable = new Div();
		closable.add("This tab has a cross: press it and the tab is gone.");
		ITabHandle closableHandle = tp.tab().label("Closable").content(closable).closable()
			.onClose(handle -> {
				shown.removeAllChildren();
				shown.add("The closable tab was closed");
			})
			.build();

		Div buttons = new Div("dm-tut");
		cp.add(buttons);
		//-- A handle is what a tab is addressed by afterwards.
		buttons.add(new DefaultButton("Open the Lazy tab from code", a -> lazyHandle.select()));
		buttons.add(new DefaultButton("Rename the closable tab", a ->
			closableHandle.updateLabel("Renamed", Icon.faStar)));
		buttons.add(new DefaultButton("Read the mandatory field", a -> name.getValue()));
		cp.add(shown);

		cp.add(new Para().add("Press 'read the mandatory field' with the field empty: the "
			+ "error is reported on the control, and the tab it is on is marked - that is "
			+ "what new TabPanel(true) does. Without it an error on a tab nobody is looking "
			+ "at is invisible."));
		cp.add(new Para().add("A tab is built with the builder and gives back an ITabHandle, "
			+ "which is how it is selected, renamed, refilled or closed afterwards."));

		//-- Too many tabs to fit: the scrolling variant.
		cp.add(new HTag(2, "ScrollableTabPanel"));
		ScrollableTabPanel stp = new ScrollableTabPanel();
		cp.add(stp);
		for(int i = 1; i <= 20; i++) {
			Div d = new Div();
			d.add("The content of tab number " + i);
			stp.tab().label("Tab number " + i).content(d).build();
		}

		cp.add(new Para().add("The same panel, with the tabs kept on one line and arrows at "
			+ "the ends to scroll through them. Press the arrow on the right to reach the "
			+ "later tabs."));
	}
}
