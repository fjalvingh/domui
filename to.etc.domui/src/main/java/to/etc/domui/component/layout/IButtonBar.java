package to.etc.domui.component.layout;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.menu.*;
import to.etc.domui.dom.html.*;

public interface IButtonBar {
//	void addButton(@Nonnull NodeBase b, int order);
//
	/**
	 * Add a normal button.
	 * @param txt
	 * @param icon
	 * @param click
	 * @return
	 */
	@Nonnull
	DefaultButton addButton(String txt, String icon, IClicked<DefaultButton> click, int order);

	@Nonnull
	DefaultButton addButton(String txt, String icon, IClicked<DefaultButton> click);

	@Nonnull
	DefaultButton addButton(@Nonnull IUIAction<Void> action) throws Exception;

	@Nonnull
	DefaultButton addButton(@Nonnull IUIAction<Void> action, int order) throws Exception;

	@Nonnull
	DefaultButton addButton(String txt, IClicked<DefaultButton> click);

	@Nonnull
	DefaultButton addButton(String txt, IClicked<DefaultButton> click, int order);

	@Nonnull
	DefaultButton addBackButton(String txt, String icon);

	@Nonnull
	DefaultButton addBackButton(String txt, String icon, int order);

	@Nonnull
	DefaultButton addBackButton();

	@Nonnull
	DefaultButton addBackButton(int order);

	@Nonnull
	DefaultButton addCloseButton(@Nonnull String txt, @Nonnull String icon);

	@Nonnull
	DefaultButton addCloseButton(@Nonnull String txt, @Nonnull String icon, int order);

	@Nonnull
	DefaultButton addCloseButton();

	@Nonnull
	DefaultButton addCloseButton(int order);

	@Nullable
	DefaultButton addBackButtonConditional();

	@Nullable
	DefaultButton addBackButtonConditional(int order);

	@Nonnull
	LinkButton addLinkButton(String txt, String img, IClicked<LinkButton> click, int order);

	@Nonnull
	LinkButton addLinkButton(String txt, String img, IClicked<LinkButton> click);

	@Nonnull
	<T> DefaultButton addAction(T instance, IUIAction<T> action) throws Exception;

	@Nonnull
	<T> DefaultButton addAction(T instance, IUIAction<T> action, int order) throws Exception;

	@Nonnull DefaultButton addConfirmedButton(final String txt, final String msg, final IClicked<DefaultButton> click);

	@Nonnull DefaultButton addConfirmedButton(final String txt, final String icon, final String msg, final IClicked<DefaultButton> click);

	@Nonnull DefaultButton addConfirmedButton(final String txt, final String msg, final IClicked<DefaultButton> click, int order);

	DefaultButton addConfirmedButton(final String txt, final String icon, final String msg, final IClicked<DefaultButton> click, int order);
}
