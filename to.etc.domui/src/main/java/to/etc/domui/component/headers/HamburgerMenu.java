package to.etc.domui.component.headers;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.event.INotify;
import to.etc.domui.component.menu.IUIAction;
import to.etc.domui.component.misc.CloseOnClickPanel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.Span;
import to.etc.domui.parts.GrayscalerPart;

import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 9/21/15.
 */
@NonNullByDefault
public class HamburgerMenu extends CloseOnClickPanel {
	final private List<IUIAction<?>> m_actionList;

	@Nullable
	private INotify<IUIAction<?>> m_onSelection;

	public HamburgerMenu(List<IUIAction<?>> actionList) {
		m_actionList = actionList;
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-hmbrg-menu");

		boolean hasicon = false;
		for(IUIAction<?> action : m_actionList) {
			if(action.getIcon(null) != null) {
				hasicon = true;
			}
		}

		for(IUIAction<?> action : m_actionList) {
			renderAction(action, hasicon);
		}
		super.createContent();
	}

	private void renderAction(IUIAction<?> action, boolean hasicon) throws Exception {
		Div sel = new Div();
		add(sel);
		sel.setCssClass("ui-hmbrg-item" + (hasicon ? " ui-hmbrg-icon" : ""));
		String icon = action.getIcon(null);
		String disable = action.getDisableReason(null);
		if(null != icon) {
			icon = getThemedResourceRURL(icon);
			sel.setBackgroundImage(disable == null ? icon : GrayscalerPart.getURL(icon));
		}
		Span sp = new Span("ui-hmbrg-txt", action.getName(null));
		sel.add(sp);
		if(null != disable) {
			sel.addCssClass("ui-hmbrg-disabled");
			sel.setTitle(disable);
		} else {
			sel.setClicked(new IClicked<Div>() {
				@Override
				public void clicked(@NonNull Div clickednode) throws Exception {
					handleSelection(action);
				}
			});
		}
	}

	private void handleSelection(IUIAction<?> action) throws Exception {
		close();

		INotify<IUIAction<?>> onSelection = m_onSelection;
		if(null != onSelection)
			onSelection.onNotify(action);
	}

	/**
	 * Sets a notification to be called when a menu selection is made.
	 * @return
	 */
	@Nullable
	public INotify<IUIAction<?>> getOnSelection() {
		return m_onSelection;
	}

	public void setOnSelection(@Nullable INotify<IUIAction<?>> onSelection) {
		m_onSelection = onSelection;
	}
}
