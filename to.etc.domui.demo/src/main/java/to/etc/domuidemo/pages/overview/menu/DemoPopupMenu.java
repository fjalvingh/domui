package to.etc.domuidemo.pages.overview.menu;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.menu.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoPopupMenu extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add("Click ");
		LinkButton lb = new LinkButton("here", new IClicked<LinkButton>() {

			@Override
			public void clicked(LinkButton clickednode) throws Exception {
				createPopup(clickednode);
			}
		});
		add(lb);
		add(" for the simple popup menu");

		add(new VerticalSpacer(30));

		final PopupMenu pm = new PopupMenu();
		pm.addItem("Happy", "img/btnSmileySmiley.gif", null);
		pm.addItem("Sad", "img/btnSmileySad.gif", null);

		add("Click ");
		lb = new LinkButton("here", new IClicked<LinkButton>() {
			@Override
			public void clicked(LinkButton clickednode) throws Exception {
				pm.show(clickednode, null);
			}
		});
		add(lb);
		add(" for the auto popup menu");
	}

	protected void createPopup(LinkButton clickednode) {
		SimplePopupMenu pm = new SimplePopupMenu(clickednode);
		pm.addItem("Happy", "img/btnSmileySmiley.gif", null);
		pm.addItem("Sad", "img/btnSmileySad.gif", null);
		clickednode.appendBeforeMe(pm);
	}


}
