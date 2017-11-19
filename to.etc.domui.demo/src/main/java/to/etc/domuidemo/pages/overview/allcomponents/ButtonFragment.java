package to.etc.domuidemo.pages.overview.allcomponents;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TH;
import to.etc.domui.dom.html.THead;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.Table;
import to.etc.domui.themes.Theme;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
public class ButtonFragment extends Div {
	private TBody m_body;

	private List<Consumer<DefaultButton>> m_actionList = new ArrayList<>();

	private List<String> m_actionNameList = new ArrayList<>();

	private TH m_h1;

	private TR m_headSubRow;

	private TR m_headRow;

	@Override public void createContent() throws Exception {
		addAction("Normal", a -> {});
		addAction("is-dark", a -> a.css("is-dark"));
		addAction("is-light", a -> a.css("is-light"));
		addAction("is-black", a -> a.css("is-black"));

		addAction("primary", a -> a.css("is-primary"));
		addAction("is-link", a -> a.css("is-link"));
		addAction("is-info", a -> a.css("is-info"));
		addAction("is-success", a -> a.css("is-success"));
		addAction("is-warning", a -> a.css("is-warning"));
		addAction("is-danger", a -> a.css("is-danger"));

		addAction("is-outlined", a -> a.css("is-outlined"));
		addAction("outlined primary", a -> a.css("is-primary is-outlined"));
		addAction("outlined link", a -> a.css("is-link is-outlined"));
		addAction("outlined info", a -> a.css("is-info is-outlined"));
		addAction("outlined success", a -> a.css("is-success is-outlined"));
		addAction("outlined warning", a -> a.css("is-warning is-outlined"));
		addAction("outlined danger", a -> a.css("is-danger is-outlined"));

		//-- loading
		addAction("is-loading", a -> a.css("is-loading"));
		addAction("outlined primary", a -> a.css("is-primary is-loading"));
		addAction("outlined link", a -> a.css("is-link is-loading"));
		addAction("outlined info", a -> a.css("is-info is-loading"));
		addAction("outlined success", a -> a.css("is-success is-loading"));
		addAction("outlined warning", a -> a.css("is-warning is-loading"));
		addAction("outlined danger", a -> a.css("is-danger is-loading"));


		addAction("is-", a -> a.css("is-"));

		Table tbl = new Table();
		add(tbl);
		THead hd = tbl.getHead();
		TR headRow = m_headRow = new TR();
		hd.add(headRow);
		TR headSubRow = m_headSubRow = new TR();
		hd.add(headSubRow);

		TH	th = new TH();
		headRow.add(th);
		th.add("Variant");

		th = new TH();
		m_headSubRow.add(th);
		m_body = tbl.addBody();

		//-- Names of variants
		for(String a : m_actionNameList) {
			m_body.addRowAndCell().add(a);
		}

		addHead("Normal");
		addSub("Text", () -> new DefaultButton("Click me", a -> {}));
		addSub("+icon", () -> new DefaultButton("click", Theme.BTN_CHECKMARK, a-> {}));
		addSub("+FaIcon", () -> new DefaultButton("click", FaIcon.faHeart, a-> {}));
		addSub("IconOnly", () -> new DefaultButton("", FaIcon.faEye, a-> {}));

		addHead("Disabled");
		Consumer<DefaultButton> disabler = b -> b.setDisabled(true);
		addSub("Text", () -> new DefaultButton("Click me", a -> {}), disabler);
		addSub("+icon", () -> new DefaultButton("click", Theme.BTN_CHECKMARK, a-> {}), disabler);
		addSub("+FaIcon", () -> new DefaultButton("click", FaIcon.faHeart, a-> {}), disabler);
		addSub("IconOnly", () -> new DefaultButton("", FaIcon.faEye, a-> {}),disabler);

	}

	private void addAction(String name, Consumer<DefaultButton> c) {
		m_actionList.add(c);
		m_actionNameList.add(name);
	}

	private void addHead(String text) {
		TH th = new TH();
		th.add(text);
		m_headRow.add(th);
		m_h1 = th;
	}

	private void addSub(String text, Supplier<DefaultButton> s) {
		addSub(text, s, null);
	}

	private void addSub(String text, Supplier<DefaultButton> s, Consumer<DefaultButton> modifier) {
		TH th = new TH();
		m_headSubRow.add(th);
		th.add(text);
		m_h1.setColspan(m_h1.getColspan() + 1);

		for(int i = 0; i < m_actionList.size(); i++) {
			DefaultButton button = s.get();
			if(null != modifier)
				modifier.accept(button);
			m_actionList.get(i).accept(button);
			m_body.getRow(i).addCell().add(button);
		}
	}
}
