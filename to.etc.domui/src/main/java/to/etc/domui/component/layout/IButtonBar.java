package to.etc.domui.component.layout;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.menu.IUIAction;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.NodeBase;
import to.etc.webapp.nls.IBundleCode;

@NonNullByDefault
public interface IButtonBar {
//	void addButton(NodeBase b, int order);
//
	/**
	 * Add a normal button.
	 */
	DefaultButton addButton(String txt, IIconRef icon, IClicked<DefaultButton> click, int order);

	default DefaultButton addButton(IBundleCode txt, IIconRef icon, IClicked<DefaultButton> click, int order) {
		return addButton(txt.getString(), icon, click, order);
	}

	DefaultButton addButton(String txt, @Nullable IIconRef icon, IClicked<DefaultButton> click);

	default DefaultButton addButton(IBundleCode txt, IIconRef icon, IClicked<DefaultButton> click) {
		return addButton(txt.getString(), icon, click);
	}

	DefaultButton addButton(IUIAction action) throws Exception;

	DefaultButton addButton(IUIAction action, int order) throws Exception;

	DefaultButton addButton(String txt, IClicked<DefaultButton> click);

	default DefaultButton addButton(IBundleCode txt, IClicked<DefaultButton> click) {
		return addButton(txt.getString(), click);
	}

	DefaultButton addButton(String txt, IClicked<DefaultButton> click, int order);

	default DefaultButton addButton(IBundleCode txt, IClicked<DefaultButton> click, int order) {
		return addButton(txt.getString(), click, order);
	}

	void addButton(NodeBase item, int order);

	DefaultButton addBackButton(String txt, IIconRef icon);

	default DefaultButton addBackButton(IBundleCode txt, IIconRef icon) {
		return addBackButton(txt.getString(), icon);
	}

	DefaultButton addBackButton(String txt, IIconRef icon, int order);

	default DefaultButton addBackButton(IBundleCode txt, IIconRef icon, int order) {
		return addBackButton(txt.getString(), icon, order);
	}

	DefaultButton addBackButton();

	DefaultButton addBackButton(int order);

	DefaultButton addCloseButton(String txt, IIconRef icon);

	default DefaultButton addCloseButton(IBundleCode txt, IIconRef icon) {
		return addCloseButton(txt.getString(), icon);
	}

	DefaultButton addCloseButton(String txt, IIconRef icon, int order);

	default DefaultButton addCloseButton(IBundleCode txt, IIconRef icon, int order) {
		return addCloseButton(txt.getString(), icon, order);
	}

	DefaultButton addCloseButton();

	DefaultButton addCloseButton(int order);

	@Nullable
	DefaultButton addBackButtonConditional();

	@Nullable
	DefaultButton addBackButtonConditional(int order);

	LinkButton addLinkButton(String txt, IIconRef img, IClicked<LinkButton> click, int order);

	default LinkButton addLinkButton(IBundleCode txt, IIconRef img, IClicked<LinkButton> click, int order) {
		return addLinkButton(txt.getString(), img, click, order);
	}

	LinkButton addLinkButton(String txt, IIconRef img, IClicked<LinkButton> click);

	default LinkButton addLinkButton(IBundleCode txt, IIconRef img, IClicked<LinkButton> click) {
		return addLinkButton(txt.getString(), img, click);
	}

	DefaultButton addAction(IUIAction action) throws Exception;

	DefaultButton addAction(IUIAction action, int order) throws Exception;

	DefaultButton addConfirmedButton(String txt, String msg, IClicked<DefaultButton> click);

	default DefaultButton addConfirmedButton(IBundleCode txt, String msg, IClicked<DefaultButton> click) {
		return addConfirmedButton(txt.getString(), msg, click);
	}

	DefaultButton addConfirmedButton(String txt, IIconRef icon, String msg, final IClicked<DefaultButton> click);

	default DefaultButton addConfirmedButton(IBundleCode txt, IIconRef icon, String msg, final IClicked<DefaultButton> click) {
		return addConfirmedButton(txt.getString(), icon, msg, click);
	}

	DefaultButton addConfirmedButton(String txt, String msg, IClicked<DefaultButton> click, int order);

	default DefaultButton addConfirmedButton(IBundleCode txt, String msg, IClicked<DefaultButton> click, int order) {
		return addConfirmedButton(txt.getString(), msg, click, order);
	}

	DefaultButton addConfirmedButton(String txt, IIconRef icon, String msg, IClicked<DefaultButton> click, int order);

	default DefaultButton addConfirmedButton(IBundleCode txt, IIconRef icon, String msg, IClicked<DefaultButton> click, int order) {
		return addConfirmedButton(txt.getString(), icon, msg, click, order);
	}
}
