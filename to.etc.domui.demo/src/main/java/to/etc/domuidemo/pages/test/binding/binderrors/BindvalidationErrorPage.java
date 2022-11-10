package to.etc.domuidemo.pages.test.binding.binderrors;

import to.etc.domui.component.buttons.DefaultButton;
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

	private final Div m_resultDiv = new Div();

	@Override
	public void createContent() throws Exception {
		m_value = "bad";
		TextArea ta = new TextArea(80, 2);
		ta.addValidator(new TestValueValidator());
		FormBuilder fb = new FormBuilder(this);
		fb.property(this, "value").control(ta);

		ta.setTestID("text");

		DefaultButton click = new DefaultButton("Click", a -> handleClick());
		add(click);
		click.setTestID("click");

		add(new VerticalSpacer(10));
		add(m_resultDiv);
		m_resultDiv.setTestID("result");
	}

	private void handleClick() throws Exception {
		m_resultDiv.removeAllChildren();
		if(bindErrors()) {
			m_resultDiv.add("Failed");
			m_resultDiv.addCssClass("test-failed");
			m_resultDiv.removeCssClass("test-ok");
		} else {
			m_resultDiv.add("worked");
			m_resultDiv.removeCssClass("test-failed");
			m_resultDiv.addCssClass("test-ok");
		}
	}

	public String getValue() {
		return m_value;
	}

	public void setValue(String value) {
		m_value = value;
	}
}
