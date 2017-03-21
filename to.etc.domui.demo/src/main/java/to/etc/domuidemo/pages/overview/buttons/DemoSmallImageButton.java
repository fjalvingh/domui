package to.etc.domuidemo.pages.overview.buttons;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoSmallImageButton extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);
		d.add(new BR());

		d.add("Please press ");
		SmallImgButton sib = new SmallImgButton("img/btnBaby.png");
		sib.setMarginLeft("20px");
		sib.setTitle("Title for SmallImgButton");
		sib.setClicked(new IClicked<SmallImgButton>() {
			@Override
			public void clicked(SmallImgButton b) throws Exception {
				MsgBox.message(d, MsgBox.Type.INFO, "Small image clicked");
			}
		});
		d.add(" and see what happens");
		d.add(sib);
	}
}
