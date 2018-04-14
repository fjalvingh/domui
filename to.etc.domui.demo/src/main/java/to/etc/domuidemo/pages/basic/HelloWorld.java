package to.etc.domuidemo.pages.basic;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.UrlPage;

public class HelloWorld extends UrlPage {
	@Override
	public void createContent() throws Exception {
		Div d = new Div();
		add(d);
		d.add("Hello world! Click me!");
		d.setClicked(new IClicked<Div>() {
			@Override
			public void clicked(@NonNull Div clickednode) throws Exception {
				if("red".equals(clickednode.getBackgroundColor()))
					clickednode.setBackgroundColor("green");
				else
					clickednode.setBackgroundColor("red");
			}
		});
	}
}
