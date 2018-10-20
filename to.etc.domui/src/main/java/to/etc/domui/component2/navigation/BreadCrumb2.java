package to.etc.domui.component2.navigation;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import to.etc.domui.component.misc.FontIcon;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component2.navigation.BreadCrumb2.IItem;
import to.etc.domui.databinding.list2.IListChangeListener;
import to.etc.domui.databinding.list2.ListChangeEvent;
import to.etc.domui.databinding.observables.IObservableList;
import to.etc.domui.databinding.observables.ObservableList;
import to.etc.domui.dom.html.ATag;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Li;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.Ul;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.DomApplication;
import to.etc.domui.state.IShelvedEntry;
import to.etc.domui.state.ShelvedDomUIPage;
import to.etc.domui.state.UIContext;
import to.etc.domui.state.UIGoto;
import to.etc.domui.state.WindowSession;
import to.etc.function.ConsumerEx;

import java.util.ArrayList;
import java.util.List;

/**
 * A breadcrumb class, modeled after <a href="http://cssmenumaker.com/blog/fancy-breadcrumb-navigation-tutorial-example/">this article</a>.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 6-10-18.
 */
@NonNullByDefault
public class BreadCrumb2 extends Div implements IListChangeListener<IItem> {
	public interface IItem {
		@Nullable
		NodeBase getIcon();

		String getName();

		@Nullable
		String getTitle();

		void clicked(IItem item) throws Exception;
	}

	public static class Item implements IItem {
		@Nullable
		private final NodeBase m_icon;

		private final String m_name;

		@Nullable
		private final String m_title;

		@Nullable
		private ConsumerEx<IItem> m_clicked;

		public Item(@Nullable NodeBase icon, String name, @Nullable String title, @Nullable ConsumerEx<IItem> clicked) {
			m_icon = icon;
			m_name = name;
			m_title = title;
			m_clicked = clicked;
		}


		@Nullable
		@Override public NodeBase getIcon() {
			return m_icon;
		}

		@Override public String getName() {
			return m_name;
		}

		@Nullable
		@Override public String getTitle() {
			return m_title;
		}

		@Override public void clicked(IItem item) throws Exception {
			ConsumerEx<IItem> clicked = m_clicked;
			if(clicked != null) {
				clicked.accept(item);
			}
		}
	}


	@Nullable
	private List<IItem> m_value;

	public BreadCrumb2() {
	}

	public BreadCrumb2(List<IItem> items) {
		setValue(items);
	}

	static public BreadCrumb2 createPageCrumb(@Nullable String homeName) {
		return createPageCrumb(homeName, true);
	}
	static public BreadCrumb2 createPageCrumb(@Nullable String homeName, boolean withBack) {
		List<IItem> list = getPageStacktems(homeName);

		if(withBack) {
			List<IShelvedEntry> ps = UIContext.getCurrentPage().getConversation().getWindowSession().getShelvedPageStack();
			if(ps.size() > 1) {                                    // Nothing to go back to (only myself is on page) -> exit
				IShelvedEntry se = ps.get(ps.size() - 2);        // Get the page before me
				if(se instanceof ShelvedDomUIPage) {
					String name = ((ShelvedDomUIPage) se).getPage().getBody().getClass().getName();
					Class<? extends UrlPage> rootPage = DomApplication.get().getRootPage();
					if(rootPage == null || !name.equals(rootPage.getName())) {
						list.add(0, new Item(new FontIcon(Icon.faArrowCircleLeft), "", "Back to the previous screen", iItem -> UIGoto.back()));
					}
				}
			}
		}

		BreadCrumb2 bc = new BreadCrumb2(list);
		return bc;
	}

	@NotNull public static List<IItem> getPageStacktems(@Nullable String homeName) {
		List<IItem> list = new ArrayList<>();
		WindowSession cm = UIContext.getRequestContext().getWindowSession();

		//-- Always use the home page as the 1st link
		Class<? extends UrlPage> home = DomApplication.get().getRootPage();
		if(null != home) {
			list.add(new Item(new FontIcon(Icon.faHome), homeName == null ? "" : homeName, null, a -> UIGoto.moveNew(home)));
		}

		List<IShelvedEntry> stack = cm.getShelvedPageStack();
		int last = stack.size() - 1;
		for(int i = 0; i < stack.size(); i++) {
			IShelvedEntry p = stack.get(i);

			if(p instanceof ShelvedDomUIPage && null != home) {
				String name = ((ShelvedDomUIPage) p).getPage().getBody().getClass().getName();
				if(name.equals(home.getName()))
					continue;
			}

			boolean noclick = i == last;

			list.add(new Item(null, p.getName(), p.getTitle(), a -> {
				if(noclick)
					return;

				if(p instanceof ShelvedDomUIPage) {
					ShelvedDomUIPage pg = (ShelvedDomUIPage) p;
					UIGoto.moveSub(pg.getPage().getBody().getClass(), pg.getPage().getPageParameters());
				} else {
					UIGoto.redirect(p.getURL());
				}
			}));
		}
		return list;
	}

	@Override public void createContent() throws Exception {
		addCssClass("ui-brcr2");

		Ul cont = new Ul();
		add(cont);

		//-- Render the items in reverse order
		List<IItem> value = getValue();
		if(null != value) {
			for(int i = value.size() - 1; i >= 0; i--) {
				IItem item = value.get(i);
				renderItem(cont, item, i == value.size() - 1);
			}
		}
	}

	private void renderItem(Ul cont, IItem item, boolean active) {
		Li li = new Li();
		cont.add(li);
		if(active)
			li.addCssClass("ui-brcr2-a");
		ATag a = new ATag();
		li.add(a);
		a.setClicked(v -> {
			item.clicked(item);
		});
		NodeBase icon = item.getIcon();
		if(null != icon) {
			Span sp = new Span();
			a.add(sp);
			sp.addCssClass("ui-brcr2-i");
			sp.add(icon);
		}
		a.add(item.getName());
		a.setTitle(item.getTitle());
	}

	@Nullable
	public List<IItem> getValue() {
		return m_value;
	}

	public void setValue(@Nullable List<IItem> value) {
		List<IItem> old = m_value;
		if(old instanceof IObservableList) {
			((ObservableList<IItem>) old).removeChangeListener(this);
		}
		if(old != m_value) {					// Do not use structural equals because it will be expensive
			forceRebuild();
		}
		m_value = value;
		if(value instanceof IObservableList) {
			((IObservableList<IItem>) value).addChangeListener(this);
		}
	}

	@Override public void handleChange(ListChangeEvent<IItem> event) throws Exception {
		forceRebuild();
	}
}
