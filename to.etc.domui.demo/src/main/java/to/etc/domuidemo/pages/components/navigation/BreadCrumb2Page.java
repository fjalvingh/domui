package to.etc.domuidemo.pages.components.navigation;

import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component2.navigation.BreadCrumb2;
import to.etc.domui.component2.navigation.BreadCrumb2.IItem;
import to.etc.domui.component2.navigation.BreadCrumb2.Item;
import to.etc.domui.databinding.observables.ObservableList;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.PageParameters;
import to.etc.domui.state.UIGoto;

import java.util.ArrayList;
import java.util.List;

/**
 * BreadCrumb2: a path of steps, either the page stack or a path of your own.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class BreadCrumb2Page extends UrlPage {
	/** How many of these pages are stacked on top of each other; it is the page's name in the crumb. */
	private int m_level = 1;

	/** The state of the last example: the crumb watches this list and follows it. */
	private final ObservableList<IItem> m_path = new ObservableList<>();

	@Override
	public String getPageTitle() {
		return m_level == 1 ? "BreadCrumb2" : "BreadCrumb2, level " + m_level;
	}

	public int getLevel() {
		return m_level;
	}

	@UIUrlParameter(name = "level", mandatory = false)
	public void setLevel(int level) {
		m_level = level;
	}

	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "BreadCrumb2"));

		//-- 1. The page stack, which is what the crumb is used for most of the time.
		cp.add(new HTag(2, "The page stack"));
		cp.add(BreadCrumb2.createPageCrumb("Home", true));

		Div stackButtons = new Div("dm-tut");
		cp.add(stackButtons);
		stackButtons.add(new DefaultButton("Open this page on top of itself", a ->
			UIGoto.moveSub(BreadCrumb2Page.class, new PageParameters("level", m_level + 1))));

		cp.add(new Para().add("createPageCrumb() reads the shelved page stack and makes one step "
			+ "of every page on it, with the home page in front and an arrow to go back. Press the "
			+ "button: this page is opened on top of itself, and the crumb has grown a step. The "
			+ "last step is the page you are on, and clicking it does nothing; the arrow in front "
			+ "of home goes back to the page below."));

		//-- 2. A path that has nothing to do with pages.
		cp.add(new HTag(2, "A path of your own"));
		Div picked = new Div("dm-tut-q");
		picked.add("Click a step of the crumb above.");

		BreadCrumb2 own = new BreadCrumb2(path(picked, "Rock", "Led Zeppelin", "IV"));
		cp.add(own);
		cp.add(picked);

		Div pathButtons = new Div("dm-tut");
		cp.add(pathButtons);
		pathButtons.add(new DefaultButton("Show the jazz path", a -> own.setValue(path(picked, "Jazz", "Miles Davis", "Kind of Blue"))));
		pathButtons.add(new DefaultButton("Show the rock path", a -> own.setValue(path(picked, "Rock", "Led Zeppelin", "IV"))));

		cp.add(new Para().add("Every step is an IItem: an icon, a name, a tooltip and what to do "
			+ "when it is clicked. Item is the ready-made implementation of it, so a crumb over "
			+ "anything at all - a genre tree, a folder, the steps of a wizard - is a list of those. "
			+ "setValue() replaces the whole path, and the crumb redraws itself with the new one."));

		//-- 3. The same, over a list the crumb watches.
		cp.add(new HTag(2, "A crumb that follows its list"));
		if(m_path.isEmpty()) {
			m_path.add(new Item(Icon.faFolderOpen.createNode(), "Root", null, null));
		}
		cp.add(new BreadCrumb2(m_path));

		Div liveButtons = new Div("dm-tut");
		cp.add(liveButtons);
		liveButtons.add(new DefaultButton("A step deeper", a -> m_path.add(new Item(null, "Level " + m_path.size(), null, null))));
		liveButtons.add(new DefaultButton("A step back", a -> {
			if(m_path.size() > 1) {
				m_path.remove(m_path.size() - 1);
			}
		}));

		cp.add(new Para().add("The list here is an ObservableList, and the page never rebuilds: the "
			+ "buttons only add to it and remove from it. The crumb listens to the list and rebuilds "
			+ "itself, so the screen follows the path without anything telling it to."));
	}

	/**
	 * A path starting at the catalogue, with one step per name given.
	 */
	private static List<IItem> path(Div picked, String... names) {
		List<IItem> items = new ArrayList<>();
		items.add(new Item(Icon.faDatabase.createNode(), "Chinook", "The whole catalogue", it -> showPick(picked, it)));
		for(String name : names) {
			items.add(new Item(null, name, null, it -> showPick(picked, it)));
		}
		return items;
	}

	private static void showPick(Div where, IItem item) {
		where.removeAllChildren();
		where.add("You clicked the step \"" + item.getName() + "\".");
	}
}
