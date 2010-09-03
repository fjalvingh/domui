package to.etc.domui.component.layout;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;

abstract public class ButtonMakerBase {
	abstract protected void addButton(NodeBase b);

	/**
	 * Add a normal button.
	 * @param txt
	 * @param icon
	 * @param click
	 * @return
	 */
	public DefaultButton addButton(final String txt, final String icon, final IClicked<DefaultButton> click) {
		DefaultButton b = new DefaultButton(txt, icon, click);
		addButton(b);
		return b;
	}

	public DefaultButton addButton(final String txt, final IClicked<DefaultButton> click) {
		DefaultButton b = new DefaultButton(txt, click);
		addButton(b);
		return b;
	}

	public DefaultButton addBackButton(final String txt, final String icon) {
		DefaultButton b = new DefaultButton(txt, icon, new IClicked<DefaultButton>() {
			@Override
			public void clicked(final DefaultButton bxx) throws Exception {
				UIGoto.back();
			}
		});
		addButton(b);
		return b;
	}

	public DefaultButton addBackButton() {
		return addBackButton("Terug", "THEME/btnCancel.png");
	}

	public DefaultButton addConfirmedButton(final String txt, final String msg, final IClicked<DefaultButton> click) {
		DefaultButton b = MsgBox.areYouSureButton(txt, msg, click);
		addButton(b);
		return b;
	}

	public DefaultButton addConfirmedButton(final String txt, final String icon, final String msg, final IClicked<DefaultButton> click) {
		DefaultButton b = MsgBox.areYouSureButton(txt, icon, msg, click);
		addButton(b);
		return b;
	}

	public LinkButton addLinkButton(final String txt, final String img, final IClicked<LinkButton> click) {
		LinkButton b = new LinkButton(txt, img, click);
		addButton(b);
		return b;
	}

	public LinkButton addConfirmedLinkButton(final String txt, final String img, String msg, final IClicked<LinkButton> click) {
		LinkButton b = MsgBox.areYouSureLinkButton(txt, img, msg, click);
		addButton(b);
		return b;
	}
}
