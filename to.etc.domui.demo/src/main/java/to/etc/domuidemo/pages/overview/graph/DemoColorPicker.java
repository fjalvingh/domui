package to.etc.domuidemo.pages.overview.graph;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.graph.ColorPicker;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.MsgDiv;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domuidemo.logic.MyRandom;
import to.etc.util.StringTool;

public class DemoColorPicker extends UrlPage {

	@Override
	public void createContent() throws Exception {
		final ColorPicker cp = new ColorPicker();
		add(cp);
		cp.setValue("#aa00ff");

		add(new VerticalSpacer(30));
		add(new DefaultButton("Random", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				String old = cp.getValue();
				int color = MyRandom.getRandom().nextInt(0x1000000);
				String nw = StringTool.intToStr(color, 16, 6);
				cp.setValue(nw);
				add(new MsgDiv("Old color=" + old + ", new color=" + nw));
			}
		}));
	}

}
