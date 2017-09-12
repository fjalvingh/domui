package to.etc.domuidemo.pages.test.componenterrors;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.htmleditor.HtmlEditor;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.UrlPage;
import to.etc.util.FileTool;

/**
 * Page with a HtmlEditor component. This checks whether the component is properly
 * handling update and error indicators.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-8-17.
 */
public class HtmlEditorTestPage extends UrlPage {
	private String m_oneText;

	private String m_twoText;

	@Override public void createContent() throws Exception {
		m_oneText = FileTool.readResourceAsString(getClass(), "editor1.html", "utf-8");

		FormBuilder fb = new FormBuilder(this);

		HtmlEditor one = new HtmlEditor(80, 10);
		one.setMandatory(true);
		fb.label("One").property(this, "oneText").control(one);

		HtmlEditor two = new HtmlEditor(80, 10);
		two.setMandatory(true);
		fb.label("Two").property(this, "twoText").control(two);

		add(new DefaultButton("validate", a -> validate()));
	}

	private void validate() throws Exception {
		bindErrors();
	}

	public String getOneText() {
		return m_oneText;
	}

	public void setOneText(String oneText) {
		m_oneText = oneText;
	}

	public String getTwoText() {
		return m_twoText;
	}

	public void setTwoText(String twoText) {
		m_twoText = twoText;
	}
}
