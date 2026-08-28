package to.etc.domuidemo.pages.overview.menu;

import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.menu.PopupMenu;
import to.etc.domui.component.menu.SimplePopupMenu;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.UrlPage;

public class DemoPopupMenu extends UrlPage {
	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		cp.add("Click ");
		LinkButton lb = new LinkButton("here", (IClicked<LinkButton>) clickednode -> createPopup(clickednode));
		cp.add(lb);
		cp.add(" for the simple popup menu");

		cp.add(new VerticalSpacer(30));

		final PopupMenu pm = new PopupMenu();
		pm.addItem("Happy", Icon.of("img/btnSmileySmiley.gif"), null);
		pm.addItem("Sad", Icon.of("img/btnSmileySad.gif"), null);

		cp.add("Click ");
		lb = new LinkButton("here", (IClicked<LinkButton>) clickednode -> pm.show(clickednode, null));
		cp.add(lb);
		cp.add(" for the auto popup menu");
	}

	protected void createPopup(LinkButton clickednode) {
		SimplePopupMenu pm = new SimplePopupMenu(clickednode);
		pm.addItem("Happy", Icon.of("img/btnSmileySmiley.gif"), null);
		pm.addItem("Sad", Icon.of("img/btnSmileySad.gif"), null);
		clickednode.appendBeforeMe(pm);
	}


}
