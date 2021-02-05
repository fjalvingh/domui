package to.etc.domui.component.layout;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.menu.IUIAction;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.NodeBase;
import to.etc.webapp.nls.IBundleCode;

public interface IButtonBar {
//	void addButton(@NonNull NodeBase b, int order);
//
	/**
	 * Add a normal button.
	 * @param txt
	 * @param icon
	 * @param click
	 * @return
	 */
	@NonNull
	DefaultButton addButton(String txt, IIconRef icon, IClicked<DefaultButton> click, int order);

	@NonNull
	default DefaultButton addButton(IBundleCode txt, IIconRef icon, IClicked<DefaultButton> click, int order) {
		return addButton(txt.getString(), icon, click, order);
	}

	@NonNull
	DefaultButton addButton(String txt, IIconRef icon, IClicked<DefaultButton> click);

	@NonNull
	default DefaultButton addButton(IBundleCode txt, IIconRef icon, IClicked<DefaultButton> click) {
		return addButton(txt.getString(), icon, click);
	}

	@NonNull
	DefaultButton addButton(@NonNull IUIAction<Void> action) throws Exception;

	@NonNull
	DefaultButton addButton(@NonNull IUIAction<Void> action, int order) throws Exception;

	@NonNull
	DefaultButton addButton(String txt, IClicked<DefaultButton> click);

	@NonNull
	default DefaultButton addButton(IBundleCode txt, IClicked<DefaultButton> click) {
		return addButton(txt.getString(), click);
	}

	@NonNull
	DefaultButton addButton(String txt, IClicked<DefaultButton> click, int order);

	@NonNull
	default DefaultButton addButton(IBundleCode txt, IClicked<DefaultButton> click, int order) {
		return addButton(txt.getString(), click, order);
	}

	void addButton(@NonNull NodeBase item, int order);

	@NonNull
	DefaultButton addBackButton(String txt, IIconRef icon);

	@NonNull
	default DefaultButton addBackButton(IBundleCode txt, IIconRef icon) {
		return addBackButton(txt.getString(), icon);
	}

	@NonNull
	DefaultButton addBackButton(String txt, IIconRef icon, int order);

	@NonNull
	default DefaultButton addBackButton(IBundleCode txt, IIconRef icon, int order) {
		return addBackButton(txt.getString(), icon, order);
	}

	@NonNull
	DefaultButton addBackButton();

	@NonNull
	DefaultButton addBackButton(int order);

	@NonNull
	DefaultButton addCloseButton(@NonNull String txt, @NonNull IIconRef icon);

	@NonNull
	default DefaultButton addCloseButton(@NonNull IBundleCode txt, @NonNull IIconRef icon) {
		return addCloseButton(txt.getString(), icon);
	}

	@NonNull
	DefaultButton addCloseButton(@NonNull String txt, @NonNull IIconRef icon, int order);

	@NonNull
	default DefaultButton addCloseButton(@NonNull IBundleCode txt, @NonNull IIconRef icon, int order) {
		return addCloseButton(txt.getString(), icon, order);
	}

	@NonNull
	DefaultButton addCloseButton();

	@NonNull
	DefaultButton addCloseButton(int order);

	@Nullable
	DefaultButton addBackButtonConditional();

	@Nullable
	DefaultButton addBackButtonConditional(int order);

	@NonNull
	LinkButton addLinkButton(String txt, IIconRef img, IClicked<LinkButton> click, int order);

	@NonNull
	default LinkButton addLinkButton(IBundleCode txt, IIconRef img, IClicked<LinkButton> click, int order) {
		return addLinkButton(txt.getString(), img, click, order);
	}

	@NonNull
	LinkButton addLinkButton(String txt, IIconRef img, IClicked<LinkButton> click);

	@NonNull
	default LinkButton addLinkButton(IBundleCode txt, IIconRef img, IClicked<LinkButton> click) {
		return addLinkButton(txt.getString(), img, click);
	}

	@NonNull
	<T> DefaultButton addAction(T instance, IUIAction<T> action) throws Exception;

	@NonNull
	<T> DefaultButton addAction(T instance, IUIAction<T> action, int order) throws Exception;

	@NonNull DefaultButton addConfirmedButton(String txt, String msg, IClicked<DefaultButton> click);

	@NonNull default DefaultButton addConfirmedButton(IBundleCode txt, String msg, IClicked<DefaultButton> click) {
		return addConfirmedButton(txt.getString(), msg, click);
	}

	@NonNull DefaultButton addConfirmedButton(String txt, IIconRef icon, String msg, final IClicked<DefaultButton> click);

	@NonNull default DefaultButton addConfirmedButton(IBundleCode txt, IIconRef icon, String msg, final IClicked<DefaultButton> click) {
		return addConfirmedButton(txt.getString(), icon, msg, click);
	}

	@NonNull DefaultButton addConfirmedButton(String txt, String msg, IClicked<DefaultButton> click, int order);

	@NonNull default DefaultButton addConfirmedButton(IBundleCode txt, String msg, IClicked<DefaultButton> click, int order) {
		return addConfirmedButton(txt.getString(), msg, click, order);
	}

	@NonNull
	DefaultButton addConfirmedButton(String txt, IIconRef icon, String msg, IClicked<DefaultButton> click, int order);

	@NonNull
	default DefaultButton addConfirmedButton(IBundleCode txt, IIconRef icon, String msg, IClicked<DefaultButton> click, int order) {
		return addConfirmedButton(txt.getString(), icon, msg, click, order);
	}
}
