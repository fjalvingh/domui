package to.etc.domui.component.layout;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.menu.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.themes.*;
import to.etc.domui.util.*;

/**
 * A thing creating all kinds of buttons all over the place, inside some kind of button
 * container.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 11, 2013
 */
public class ButtonFactory implements IButtonBar {
	@Nonnull
	final private IButtonContainer m_container;

	public ButtonFactory(@Nonnull IButtonContainer container) {
		m_container = container;
	}

	public void addButton(@Nonnull NodeBase button, int order) {
		m_container.addButton(button, order);
	}

	/**
	 * @see to.etc.domui.component.layout.IButtonBar#addButton(java.lang.String, java.lang.String, to.etc.domui.dom.html.IClicked)
	 */
	@Override
	@Nonnull
	public DefaultButton addButton(final String txt, final String icon, final IClicked<DefaultButton> click, int order) {
		DefaultButton b = new DefaultButton(txt, icon, click);
		m_container.addButton(b, order);
		return b;
	}

	@Nonnull
	@Override
	public DefaultButton addButton(final String txt, final String icon, final IClicked<DefaultButton> click) {
		return addButton(txt, icon, click, -1);
	}

	/**
	 * @see to.etc.domui.component.layout.IButtonBar#addButton(to.etc.domui.component.menu.IUIAction)
	 */
	@Override
	@Nonnull
	public DefaultButton addButton(@Nonnull IUIAction<Void> action, int order) throws Exception {
		DefaultButton b = new DefaultButton(action);
		m_container.addButton(b, order);
		return b;
	}

	@Override
	@Nonnull
	public DefaultButton addButton(@Nonnull IUIAction<Void> action) throws Exception {
		return addButton(action, -1);
	}

	/**
	 * @see to.etc.domui.component.layout.IButtonBar#addButton(java.lang.String, to.etc.domui.dom.html.IClicked)
	 */
	@Override
	@Nonnull
	public DefaultButton addButton(final String txt, final IClicked<DefaultButton> click, int order) {
		DefaultButton b = new DefaultButton(txt, click);
		m_container.addButton(b, order);
		return b;
	}

	@Override
	@Nonnull
	public DefaultButton addButton(final String txt, final IClicked<DefaultButton> click) {
		return addButton(txt, click, -1);
	}

	/**
	 * @see to.etc.domui.component.layout.IButtonBar#addBackButton(java.lang.String, java.lang.String)
	 */
	@Override
	@Nonnull
	public DefaultButton addBackButton(final String txt, final String icon, int order) {
		DefaultButton b = new DefaultButton(txt, icon, new IClicked<DefaultButton>() {
			@Override
			public void clicked(final @Nonnull DefaultButton bxx) throws Exception {
				UIGoto.back();
			}
		});
		m_container.addButton(b, order);
		return b;
	}

	@Override
	@Nonnull
	public DefaultButton addBackButton(final String txt, final String icon) {
		return addBackButton(txt, icon, -1);
	}

	/**
	 * @see to.etc.domui.component.layout.IButtonBar#addBackButton()
	 */
	@Override
	@Nonnull
	public DefaultButton addBackButton(int order) {
		List<IShelvedEntry> ps = m_container.getPage().getConversation().getWindowSession().getShelvedPageStack();
		if(ps.size() > 1) {									// Nothing to go back to (only myself is on page) -> exit
			IShelvedEntry se = ps.get(ps.size() - 2);		// Get the page before me
			if(se.isClose()) {
				return addCloseButton(order);
			}
		}else{
			return addCloseButton(order);
		}

		//-- Nothing worked: just add a default back button that will go back to application home if the stack is empty
		return addBackButton(Msgs.BUNDLE.getString("ui.buttonbar.back"), Theme.BTN_CANCEL, order);
	}

	/**
	 * @see to.etc.domui.component.layout.IButtonBar#addBackButton()
	 */
	@Override
	@Nonnull
	public DefaultButton addBackButton() {
		return addBackButton(-1);
	}

	/**
	 * @see to.etc.domui.component.layout.IButtonBar#addCloseButton(java.lang.String, java.lang.String)
	 */
	@Override
	@Nonnull
	public DefaultButton addCloseButton(@Nonnull String txt, @Nonnull String icon, int order) {
		DefaultButton b = new DefaultButton(txt, icon, new IClicked<DefaultButton>() {
			@Override
			public void clicked(@Nonnull DefaultButton clickednode) throws Exception {
				m_container.getPage().getBody().closeWindow();
			}
		});
		m_container.addButton(b, order);
		return b;
	}

	@Override
	@Nonnull
	public DefaultButton addCloseButton(@Nonnull String txt, @Nonnull String icon) {
		return addCloseButton(txt, icon, -1);
	}

	/**
	 * @see to.etc.domui.component.layout.IButtonBar#addCloseButton()
	 */
	@Override
	@Nonnull
	public DefaultButton addCloseButton(int order) {
		return addCloseButton(Msgs.BUNDLE.getString("ui.buttonbar.close"), Theme.BTN_CLOSE, order);
	}

	@Override
	@Nonnull
	public DefaultButton addCloseButton() {
		return addCloseButton(-1);
	}

	/**
	 * @see to.etc.domui.component.layout.IButtonBar#addBackButtonConditional()
	 */
	@Override
	@Nullable
	public DefaultButton addBackButtonConditional(int order) {
		List<IShelvedEntry> ps = m_container.getPage().getConversation().getWindowSession().getShelvedPageStack();
		if(ps.size() <= 1)									// Nothing to go back to (only myself is on page) -> exit
			return null;

		IShelvedEntry se = ps.get(ps.size() - 2);			// Get the page before me
		if(se.isClose()) {
			return addCloseButton(order);
		}
		return addBackButton(order);
	}

	@Override
	@Nullable
	public DefaultButton addBackButtonConditional() {
		return addBackButtonConditional(-1);
	}

	@Nonnull
	@Override
	public DefaultButton addConfirmedButton(final String txt, final String msg, final IClicked<DefaultButton> click, int order) {
		DefaultButton b = MsgBox.areYouSureButton(txt, msg, click);
		m_container.addButton(b, order);
		return b;
	}

	@Nonnull
	@Override
	public DefaultButton addConfirmedButton(final String txt, final String msg, final IClicked<DefaultButton> click) {
		return addConfirmedButton(txt, msg, click, -1);
	}

	@Nonnull
	@Override
	public DefaultButton addConfirmedButton(final String txt, final String icon, final String msg, final IClicked<DefaultButton> click, int order) {
		DefaultButton b = MsgBox.areYouSureButton(txt, icon, msg, click);
		m_container.addButton(b, order);
		return b;
	}

	@Nonnull
	@Override
	public DefaultButton addConfirmedButton(final String txt, final String icon, final String msg, final IClicked<DefaultButton> click) {
		return addConfirmedButton(txt, icon, msg, click, -1);
	}

	/**
	 * @see to.etc.domui.component.layout.IButtonBar#addLinkButton(java.lang.String, java.lang.String, to.etc.domui.dom.html.IClicked)
	 */
	@Nonnull
	@Override
	public LinkButton addLinkButton(final String txt, final String img, final IClicked<LinkButton> click, int order) {
		LinkButton b = new LinkButton(txt, img, click);
		m_container.addButton(b, order);
		return b;
	}

	@Nonnull
	@Override
	public LinkButton addLinkButton(final String txt, final String img, final IClicked<LinkButton> click) {
		return addLinkButton(txt, img, click, -1);
	}

	@Nonnull
	public LinkButton addConfirmedLinkButton(final String txt, final String img, String msg, final IClicked<LinkButton> click, int order) {
		LinkButton b = MsgBox.areYouSureLinkButton(txt, img, msg, click);
		m_container.addButton(b, order);
		return b;
	}

	@Nonnull
	public LinkButton addConfirmedLinkButton(final String txt, final String img, String msg, final IClicked<LinkButton> click) {
		return addConfirmedLinkButton(txt, img, msg, click, -1);
	}

	/**
	 * @see to.etc.domui.component.layout.IButtonBar#addAction(T, to.etc.domui.component.menu.IUIAction)
	 */
	@Override
	@Nonnull
	public <T> DefaultButton addAction(T instance, IUIAction<T> action, int order) throws Exception {
		DefaultButton b = new DefaultButton(instance, action);
		m_container.addButton(b, order);
		return b;
	}

	@Override
	@Nonnull
	public <T> DefaultButton addAction(T instance, IUIAction<T> action) throws Exception {
		return addAction(instance, action, -1);
	}
}
