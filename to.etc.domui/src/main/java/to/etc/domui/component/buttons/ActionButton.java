package to.etc.domui.component.buttons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.component.layout.FloatingDiv;
import to.etc.domui.component.menu.IUIAction;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component2.popupmenus.PopupMenu2;
import to.etc.domui.dom.html.HR;
import to.etc.domui.dom.html.NodeBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Button with additional actions.
 * Additional actions are activated using side button, and show internal popup menu.
 */
@NonNullByDefault
public class ActionButton<T> extends DefaultButton {

	private final List<IUIAction<T>> m_actions = new ArrayList<>();

	private final T m_instance;

	public ActionButton(T instance, IUIAction<T> action) throws Exception {
		super(action);
		m_instance = instance;
	}

	public ActionButton<T> addAction(IUIAction<T> action) {
		m_actions.add(action);
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
		NodeBase actionButton = Icon.faEllipsisV.createNode();
		add(actionButton);
		actionButton.addCssClass("act-btn");
		actionButton.setClicked(c -> {
			PopupMenu2 p2 = new PopupMenu2(ActionButton.this);
			FloatingDiv floatingParent = ActionButton.this.getParent(FloatingDiv.class);
			if(null != floatingParent) {
				p2.setZIndex(floatingParent.getZIndex() + 100);
			}

			for(IUIAction<T> action: m_actions) {
				p2.text(action.getName(m_instance))
					.hint(action.getTitle(m_instance))
					.icon(action.getIcon(m_instance))
					.click(() -> action.execute(this, m_instance))
					.disableReason(action.getDisableReason(m_instance))
					.append();
			}
			p2.show(this);
		});
	}
}
