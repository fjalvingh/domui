package to.etc.domuidemo.pages.overview.buttons;

import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.themes.Theme;

public class DemoLinkButton extends UrlPage {
	@Override
	public void createContent() throws Exception {
		addCssClass("ui-content");

		add("Without an icon", new LinkButton("this link", clickednode -> tell()));
		add("With an image icon", new LinkButton("with image", Theme.BTN_SAVE, clickednode -> tell()));
		add("With a FontIcon", new LinkButton("fontIcon", Icon.faFile, a -> tell()));

		addDisabled("Without an icon", new LinkButton("this link", clickednode -> tell()));
		addDisabled("With an image icon", new LinkButton("with image", Theme.BTN_SAVE, clickednode -> tell()));
		addDisabled("With a FontIcon", new LinkButton("fontIcon", Icon.faFile, a -> tell()));
	}

	private void addDisabled(String text, LinkButton lb) {
		lb.setDisabled(true);
		add(text + " (disabled)", lb);
	}

	private void add(String text, LinkButton lb) {
		Div d = new Div();
		add(d);
		d.add(text + " ");
		d.add(lb);
		d.add(" and text after");
		add(new VerticalSpacer(30));
	}

	private void tell() {
		MsgBox.info(DemoLinkButton.this, "The link was clicked");
	}
}
