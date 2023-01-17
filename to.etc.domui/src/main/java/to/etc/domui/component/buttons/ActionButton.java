package to.etc.domui.component.buttons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.component.layout.FloatingDiv;
import to.etc.domui.component.menu.IUIAction;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component2.popupmenus.PopupMenu2;
import to.etc.domui.dom.html.HR;
import to.etc.domui.dom.html.NodeBase;
import to.etc.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Button with additional actions.
 * Additional actions are activated using side button, and show internal popup menu.
 */
@NonNullByDefault
public class ActionButton extends DefaultButton {

	private final List<Pair<?, IUIAction<?>>> m_actions = new ArrayList<>();

	public <T> ActionButton(T instance, IUIAction<T> action) throws Exception {
		super(action);
	}

	public <T> ActionButton addAction(T instance, IUIAction<T> action) {
		m_actions.add(new Pair<>(instance, action));
		if(isBuilt()) {
			forceRebuild();
		}
		return this;
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		if(m_actions.isEmpty()) {
			return;
		}
		HR hr = new HR();
		hr.addCssClass("act-rule");
		add(hr);
		NodeBase actionButton = Icon.faChevronDown.createNode();
		add(actionButton);
		actionButton.addCssClass("act-btn");
		if(!isDisabled()) {
			actionButton.setTitle("");
			actionButton.setClicked(c -> {
				PopupMenu2 p2 = new PopupMenu2(ActionButton.this);
				FloatingDiv floatingParent = ActionButton.this.getParent(FloatingDiv.class);
				if(null != floatingParent) {
					p2.setZIndex(floatingParent.getZIndex() + 100);
				}

				for(Pair<?, IUIAction<?>> pair : m_actions) {
					Object instance = pair.get1();
					IUIAction<?> action = pair.get2();
					addMenuAction(p2, instance, action);
				}
				p2.show(this);
			});
		}
	}

	private <T> void addMenuAction(PopupMenu2 pm, Object instance, IUIAction<?> action) throws Exception {
		T inst = (T) instance;
		IUIAction<T> ta = (IUIAction<T>) action;
		pm.text(ta.getName(inst))
			.hint(ta.getTitle(inst))
			.icon(ta.getIcon(inst))
			.click(() -> ta.execute(this, inst))
			.disableReason(ta.getDisableReason(inst))
			.append();
	}
}
