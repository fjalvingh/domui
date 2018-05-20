package to.etc.domui.component.layout;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.menu.IUIAction;
import to.etc.domui.dom.html.IClicked;

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
	DefaultButton addButton(String txt, String icon, IClicked<DefaultButton> click, int order);

	@NonNull
	DefaultButton addButton(String txt, String icon, IClicked<DefaultButton> click);

	@NonNull
	DefaultButton addButton(@NonNull IUIAction<Void> action) throws Exception;

	@NonNull
	DefaultButton addButton(@NonNull IUIAction<Void> action, int order) throws Exception;

	@NonNull
	DefaultButton addButton(String txt, IClicked<DefaultButton> click);

	@NonNull
	DefaultButton addButton(String txt, IClicked<DefaultButton> click, int order);

	@NonNull
	DefaultButton addBackButton(String txt, String icon);

	@NonNull
	DefaultButton addBackButton(String txt, String icon, int order);

	@NonNull
	DefaultButton addBackButton();

	@NonNull
	DefaultButton addBackButton(int order);

	@NonNull
	DefaultButton addCloseButton(@NonNull String txt, @NonNull String icon);

	@NonNull
	DefaultButton addCloseButton(@NonNull String txt, @NonNull String icon, int order);

	@NonNull
	DefaultButton addCloseButton();

	@NonNull
	DefaultButton addCloseButton(int order);

	@Nullable
	DefaultButton addBackButtonConditional();

	@Nullable
	DefaultButton addBackButtonConditional(int order);

	@NonNull
	LinkButton addLinkButton(String txt, String img, IClicked<LinkButton> click, int order);

	@NonNull
	LinkButton addLinkButton(String txt, String img, IClicked<LinkButton> click);

	@NonNull
	<T> DefaultButton addAction(T instance, IUIAction<T> action) throws Exception;

	@NonNull
	<T> DefaultButton addAction(T instance, IUIAction<T> action, int order) throws Exception;

	@NonNull DefaultButton addConfirmedButton(final String txt, final String msg, final IClicked<DefaultButton> click);

	@NonNull DefaultButton addConfirmedButton(final String txt, final String icon, final String msg, final IClicked<DefaultButton> click);

	@NonNull DefaultButton addConfirmedButton(final String txt, final String msg, final IClicked<DefaultButton> click, int order);

	DefaultButton addConfirmedButton(final String txt, final String icon, final String msg, final IClicked<DefaultButton> click, int order);
}
