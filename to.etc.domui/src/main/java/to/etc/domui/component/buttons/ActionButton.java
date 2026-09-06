package to.etc.domui.component.buttons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.component.layout.FloatingDiv;
import to.etc.domui.component.menu.IUIAction;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component2.popupmenus.PopupMenu2;
import to.etc.domui.component2.popupmenus.PopupMenu2.Mode;
import to.etc.domui.dom.html.HR;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.NodeBase;
import to.etc.webapp.nls.IBundleCode;

import java.util.ArrayList;
import java.util.List;
import to.etc.function.IExecute;

/**
 * Button with additional actions.
 * Additional actions are activated using side button, and show internal popup menu.
 */
@NonNullByDefault
public class ActionButton extends DefaultButton {

	private PopupMenu2.Mode m_mode = PopupMenu2.Mode.BELOW;

	private final List<IUIAction> m_actions = new ArrayList<>();

	public <T> ActionButton(IUIAction action) throws Exception {
		super(action);
	}

	public ActionButton(IBundleCode code, IIconRef icon, final IClicked<DefaultButton> clicked) {
		super(code, icon, clicked);
	}

	public ActionButton(IBundleCode code, IIconRef icon, final IExecute clicked) {
		super(code, icon, clicked);
	}

	public ActionButton addAction(IUIAction action) {
		m_actions.add(action);
		if(isBuilt()) {
			forceRebuild();
		}
		return this;
	}

	public ActionButton above() {
		m_mode = PopupMenu2.Mode.ABOVE;
		return this;
	}

	public ActionButton below() {
		m_mode = Mode.BELOW;
		return this;
	}

	public ActionButton removeActions() {
		m_actions.clear();
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
				if(m_mode == Mode.ABOVE) {
					p2.above();
				}
				FloatingDiv floatingParent = ActionButton.this.findParent(FloatingDiv.class);
				if(null != floatingParent) {
					p2.setZIndex(floatingParent.getZIndex() + 100);
				}

				for(IUIAction action : m_actions) {
					addMenuAction(p2, action);
				}
				p2.show(this);
			});
		}
	}

	private void addMenuAction(PopupMenu2 pm, IUIAction ta) throws Exception {
		pm.text(ta.getName())
			.hint(ta.getTitle())
			.icon(ta.getIcon())
			.click(() -> ta.execute(this))
			.disableReason(ta.getDisableReason())
			.append();
	}
}
