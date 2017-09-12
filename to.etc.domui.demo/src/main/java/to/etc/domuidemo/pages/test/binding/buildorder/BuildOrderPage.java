package to.etc.domuidemo.pages.test.binding.buildorder;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;

/**
 * See <a href="https://etc.to/confluence/display/DOM/Tests%3A+data+binding#TestBuildOrder">the wiki</a>.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-6-17.
 */
public class BuildOrderPage extends UrlPage {
	@Override public void createContent() throws Exception {
		add(new DefaultButton("ClickMe", c -> openNext()));
	}

	private void openNext() {
		add(new TestComp1());
	}

	public static class TestComp1 extends Div {
		@Override public void createContent() throws Exception {
			TestComp2 t2 = new TestComp2();
			add(t2);

			DefaultButton next = new DefaultButton("NextButton", a -> MsgBox.error(this, "Should not be possible to get this"));
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
