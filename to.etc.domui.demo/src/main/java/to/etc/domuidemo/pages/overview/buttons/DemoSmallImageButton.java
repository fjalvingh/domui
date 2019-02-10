package to.etc.domuidemo.pages.overview.buttons;

import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.component.misc.MsgBox.Type;
import to.etc.domui.dom.html.BR;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.UrlPage;

public class DemoSmallImageButton extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);
		d.add(new BR());

		d.add("Please press ");
		SmallImgButton sib = new SmallImgButton(Icon.of("img/btnBaby.png"));
		sib.setMarginLeft("20px");
		sib.setTitle("Title for SmallImgButton");
		sib.setClicked((IClicked<SmallImgButton>) b -> MsgBox.message(d, Type.INFO, "Small image clicked"));
		d.add(" and see what happens");
		d.add(sib);
	}
}
