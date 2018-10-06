package to.etc.domui.component2.navigation;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component2.navigation.BreadCrumb2.Item;
import to.etc.domui.databinding.list2.IListChangeListener;
import to.etc.domui.databinding.list2.ListChangeEvent;
import to.etc.domui.databinding.observables.IObservableList;
import to.etc.domui.databinding.observables.ObservableList;
import to.etc.domui.dom.html.ATag;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Li;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Ul;
import to.etc.domui.state.IShelvedEntry;
import to.etc.domui.state.ShelvedDomUIPage;
import to.etc.domui.state.UIContext;
import to.etc.domui.state.UIGoto;
import to.etc.domui.state.WindowSession;

import java.util.ArrayList;
import java.util.List;

/**
 * A breadcrumb class, modeled after <a href="http://cssmenumaker.com/blog/fancy-breadcrumb-navigation-tutorial-example/">this article</a>.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 6-10-18.
 */
@NonNullByDefault
public class BreadCrumb2 extends Div implements IListChangeListener<Item> {
	public interface Item {
		@Nullable
		NodeBase getIcon();

		String getName();

		@Nullable
		String getTitle();

		void clicked(Item item) throws Exception;
	}

	@Nullable
	private List<Item> m_value;

	public BreadCrumb2() {
	}

	static public BreadCrumb2 createPageCrumb() {
		List<Item> list = new ArrayList<>();
		WindowSession cm = UIContext.getRequestContext().getWindowSession();

		//-- Get the application's main page as the base;
		List<IShelvedEntry> stack = cm.getShelvedPageStack();
		int last = stack.size() - 1;
		for(int i = 0; i < stack.size(); i++) {
			IShelvedEntry p = stack.get(i);
			boolean noclick = i == last;

			Item it = new Item() {
				@Nullable
				@Override public NodeBase getIcon() {
					return null;
				}

				@Override public String getName() {
					return p.getName();
				}

				@Nullable
				@Override public String getTitle() {
					return p.getTitle();
				}

				@Override public void clicked(Item item) throws Exception {
					if(noclick)
						return;

					if(p instanceof ShelvedDomUIPage) {
						ShelvedDomUIPage pg = (ShelvedDomUIPage) p;
						UIGoto.moveSub(pg.getPage().getBody().getClass(), pg.getPage().getPageParameters());
					} else {
						UIGoto.redirect(p.getURL());
					}
				}
			};
			list.add(it);
		}
		BreadCrumb2 bc = new BreadCrumb2();
		bc.setValue(list);
		return bc;
	}

	@Override public void createContent() throws Exception {
		addCssClass("ui-brcr2");

		Ul cont = new Ul();
		add(cont);

		//-- Render the items in reverse order
		List<Item> value = getValue();
		if(null != value) {
			for(int i = value.size() - 1; i >= 0; i--) {
				Item item = value.get(i);
				renderItem(cont, item, i == value.size() - 1);
			}
		}
	}

	private void renderItem(Ul cont, Item item, boolean active) {
		Li li = new Li();
		cont.add(li);
		if(active)
			li.addCssClass("ui-brcr2-a");
		ATag a = new ATag();
		li.add(a);
		a.setClicked(v -> {
			item.clicked(item);
		});
		a.add(item.getName());
		a.setTitle(item.getTitle());
	}

	@Nullable
	public List<Item> getValue() {
		return m_value;
	}

	public void setValue(@Nullable List<Item> value) {
		List<Item> old = m_value;
		if(old instanceof IObservableList) {
			((ObservableList<Item>) old).removeChangeListener(this);
		}
		if(old != m_value) {					// Do not use structural equals because it will be expensive
			forceRebuild();
		}
		m_value = value;
		if(value instanceof IObservableList) {
			((IObservableList<Item>) value).addChangeListener(this);
		}
	}

	@Override public void handleChange(ListChangeEvent<Item> event) throws Exception {
		forceRebuild();
	}
}
