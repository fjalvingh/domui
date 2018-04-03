package to.etc.domuidemo.pages.test.binding.binderrors;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.MessageLine;
import to.etc.domui.component.meta.MetaProperty;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.html.UrlPage;

/**
 * Test for the following:
 *
 * - a mandatoty control bound to a model has an initial value through the model (i.e. the start value is not null)
 * - clear the value in the control, then press the click button which validates the bindings
 *
 * expected result: as the control is now empty it is invalid, and a mandatory error must be shown.
 * actual result before: the validation fails, but no message is shown.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-4-18.
 */
public class BindError1Page extends UrlPage {
	private String m_fullName = "Hello ladies";

	@Override public void createContent() throws Exception {
		Text2<String> text = new Text2<>(String.class);
		add(text);
		text.setMandatory(true);
		text.setTestID("edit");

		text.bind().to(this, "fullName");

		add(new DefaultButton("click", a-> save()));
	}

	private void save() throws Exception {
		if(bindErrors())
			return;
		add(new MessageLine(MsgType.INFO, "Validated"));
	}

	@MetaProperty(required = YesNoType.YES)
	public String getFullName() {
		return m_fullName;
	}

	public void setFullName(String fullName) {
		m_fullName = fullName;
	}
}
