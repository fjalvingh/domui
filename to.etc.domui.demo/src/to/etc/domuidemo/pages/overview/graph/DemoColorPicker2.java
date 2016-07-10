package to.etc.domuidemo.pages.overview.graph;

import java.util.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.graph.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;
import to.etc.util.*;

public class DemoColorPicker2 extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final ColorPickerButton cp = new ColorPickerButton();
		add(cp);
		cp.setValue("#aa00ff");

		add(new VerticalSpacer(30));
		add(new DefaultButton("Random", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				String old = cp.getValue();
				int color = new Random().nextInt(0x1000000);
				String nw = StringTool.intToStr(color, 16, 6);
				cp.setValue(nw);
				add(new MsgDiv("Old color=" + old + ", new color=" + nw));
			}
		}));
	}
}
