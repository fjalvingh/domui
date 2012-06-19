package to.etc.domuidemo.pages.basic;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

public class HelloWorld extends UrlPage {
	@Override
	public void createContent() throws Exception {
		Div d = new Div();
		add(d);
		d.add("Hello world! Click me!");
		d.setClicked(new IClicked<Div>() {
			@Override
			public void clicked(@Nonnull Div clickednode) throws Exception {
				if("red".equals(clickednode.getBackgroundColor()))
					clickednode.setBackgroundColor("green");
				else
					clickednode.setBackgroundColor("red");
			}
		});
	}
}
