package to.etc.domuidemo.pages.test.proxies;

import to.etc.domui.component.buttons.CheckboxButton;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component2.combo.ComboFixed2;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.Checkbox;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IActionControl;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.RadioGroup;
import to.etc.domui.dom.html.UrlPage;

/**
 * This page shows a number of components that are targeted
 * by a generated Page Object with proxies. The accompanying
 * test will test the capabilities of these proxies.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 28-12-21.
 */
public class ProxyTestPage1 extends UrlPage {
	public enum MyValues {
		My, Name, Is, Ozymandias, King, Of, Kings;
	}

	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		addComponent(cp, new Text2<String>(String.class), "text2");

		ComboFixed2<MyValues> eco2 = ComboFixed2.createEnumCombo(MyValues.class);
		addComponent(cp, eco2, "cf2");

		CheckboxButton cbb = new CheckboxButton();
		addComponent(cp, cbb, "cbb");

		Checkbox cb = new Checkbox();
		addComponent(cp, cb, "checkbox");

		addActionComponent(cp, new DefaultButton("PingPong"), "defbtn");
		addActionComponent(cp, new SmallImgButton(Icon.faAdjust), "sib");

		RadioGroup<MyValues> erg = RadioGroup.createEnumRadioGroup(MyValues.class).asButtons();
		addComponent(cp, erg, "ragrou");
	}

	private <V, T extends NodeBase & IControl<V>> void addComponent(NodeContainer target, T comp, String id) {
		Div pair = new Div();
		target.add(pair);
		Div cod = new Div();
		pair.add(cod);
		cod.setDisplay(DisplayType.INLINE_BLOCK);
		cod.add(comp);
		comp.setTestID(id);

		Div val = new Div();
		pair.add(val);
		val.setTestID(id + "_v");
		val.setDisplay(DisplayType.INLINE_BLOCK);

		comp.setOnValueChanged(component -> {
			V value = comp.getValue();
			val.removeAllChildren();
			val.add(String.valueOf(value));
		});

	}

	private <T extends NodeBase & IActionControl> void addActionComponent(NodeContainer target, T comp, String id) {
		Div pair = new Div();
		target.add(pair);
		Div cod = new Div();
		pair.add(cod);
		cod.setDisplay(DisplayType.INLINE_BLOCK);
		cod.add(comp);
		comp.setTestID(id);

		boolean[] idiots = new boolean[1];
		Div val = new Div();
		pair.add(val);
		val.setTestID(id + "_v");
		val.setDisplay(DisplayType.INLINE_BLOCK);

		comp.setClicked(clickednode -> {
			idiots[0] = ! idiots[0];
			val.setText(idiots[0] ? "Ping" : "Pong");
		});
	}

}
