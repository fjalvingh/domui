package to.etc.domuidemo.pages.test.binding.binderrors;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.TextArea;
import to.etc.domui.dom.html.UrlPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-11-22.
 */
final public class BindvalidationErrorPage extends UrlPage {
	private String m_value;

	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		Div resultDiv = new Div();
		resultDiv.setTestID("result");

		m_value = "bad";
		TextArea ta = new TextArea(80, 2);
		ta.addValidator(new TestValueValidator());
		FormBuilder fb = new FormBuilder(cp);
		fb.property(this, "value").control(ta);

		ta.setTestID("text");

		DefaultButton click = new DefaultButton("Click", a -> handleClick(resultDiv));
		cp.add(click);
		click.setTestID("click");

		cp.add(new VerticalSpacer(10));
		cp.add(resultDiv);
	}

	private void handleClick(Div resultDiv) throws Exception {
		resultDiv.removeAllChildren();
		if(bindErrors()) {
			resultDiv.add("Failed");
			resultDiv.addCssClass("test-failed");
			resultDiv.removeCssClass("test-ok");
		} else {
			resultDiv.add("worked");
			resultDiv.removeCssClass("test-failed");
			resultDiv.addCssClass("test-ok");
		}
	}

	public String getValue() {
		return m_value;
	}

	public void setValue(String value) {
		m_value = value;
	}
}
