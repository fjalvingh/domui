package to.etc.domuidemo.pages.test.binding.buildorder;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.MessageLine;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.UrlPage;

/**
 * DomUI builds nodes lazily, so a binding must not be evaluated while the
 * component it reads from is still unbuilt. Clicking the button adds a
 * component whose child sets "disabled" during its own build; the button bound
 * to that property must come out disabled anyway.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-6-17.
 */
public class BuildOrderPage extends UrlPage {
	@Override public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		cp.add(new DefaultButton("ClickMe", c -> openNext(cp)));
	}

	private void openNext(NodeContainer target) {
		target.add(new TestComp1());
	}

	public static class TestComp1 extends Div {
		@Override public void createContent() throws Exception {
			TestComp2 t2 = new TestComp2();
			add(t2);

			DefaultButton next = new DefaultButton("NextButton", a -> MsgBox2.on(this).error("Should not be possible to get this"));
			add(next);
			next.bind("disabled").to(t2, "disabled");
			add(new MessageLine(MsgType.INFO, "The 'next' button must be disabled"));
		}
	}

	public static class TestComp2 extends Div {
		private boolean m_disabled;

		@Override public void createContent() throws Exception {
			add(new HTag(2, "Component 2 added and disabled set to TRUE"));
			setDisabled(true);
		}

		public boolean isDisabled() {
			return m_disabled;
		}

		public void setDisabled(boolean disabled) {
			m_disabled = disabled;
		}
	}
}
