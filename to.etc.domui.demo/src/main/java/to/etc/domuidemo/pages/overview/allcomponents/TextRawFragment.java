package to.etc.domuidemo.pages.overview.allcomponents;

import to.etc.domui.component.input.Text;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Label;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
public class TextRawFragment extends Div {
	@Override public void createContent() throws Exception {
		add(new HTag(2, "Text components").css("ui-header"));

		//-- Single text
		Div d = new Div("ui-f4-line");
		add(d);
		Text<String> t1	= new Text<>(String.class);
		d.add(new Label(t1,"zzzzzzzz"));
		d.add(t1);
		t1.setValue("zzzzzzzzzzzzzzzzz");

		//-- Single text with button
		add(new VerticalSpacer(10));
		d = new Div("ui-f4-line");
		add(d);
		Text<String> t2	= new Text<>(String.class);
		d.add(new Label(t2,"z22222222"));
		d.add(t2);
		t2.setValue("zzzzzzzzzzzzzzzzz");

		//-- Single text with 2 buttons
		add(new VerticalSpacer(10));
		d = new Div("ui-f4-line");
		add(d);
		Text<String> t3	= new Text<>(String.class);
		d.add(new Label(t3,"z3333333"));
		d.add(t3);
		t3.setValue("zzzzzzzzzzzzzzzzz");

		//-- Button with image
		add(new VerticalSpacer(10));
		d = new Div("ui-f4-line");
		add(d);
		Text<String> t4	= new Text<>(String.class);
		d.add(new Label(t4,"zzzzzzzzzzz4"));
		d.add(t4);
		t4.setValue("zzzzzzzzzzzzzzzzz");
	}
}
