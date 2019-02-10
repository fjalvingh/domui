package to.etc.domuidemo.pages.overview.misc;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.htmleditor.DisplayHtml;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.EmbeddedCode;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.component.misc.SvgIcon;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.fontawesome.FaIcon;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-11-18.
 */
public class SvgIconPage extends UrlPage {
	@Override public void createContent() throws Exception {
		addFontIcons();

		addSvgIcons();
	}

	private void addFontIcons() {
		ContentPanel cp = add(new ContentPanel());
		cp.add(new HTag(1, "Font icon sets"));
		cp.add(new DisplayHtml("We can use <a target='_blank' href='https://fontawesome.com/'><b>FontAwesome</b></a> and other font icon sets easily as icons. Support for FontAwesome 4.7 and FontAwesome 5 free comes out-of-the-box."));
		Div d = cp.add(new Div());
		d
			.append("This uses FontAwesome 5 free icons like ")
			.append(FaIcon.faCalculator.createNode())
			.append(" and ")
			.append(FaIcon.faBlenderPhone.createNode())
			.append(", supported out-of the box by including the maven project fontawesome5free, using ")
			.append(new EmbeddedCode("add(FaIcon.faCalculator.createNode());"))
		;

		d = cp.add(new Div());
		d.append("You can alter its size using standard css classes like: is-size-1 (big) to is-size-7 (small), or is-size-normal, is-size=-large, is-size-medium and is-size-small.");

		d = cp.add(new Div());
		d
			.append("This ").append(FaIcon.faWind.css("is-size-large").createNode()).append(" is large, for instance, done like ")
			.append(new EmbeddedCode("add(FaIcon.faWind.css(\"is-size-large\").createNode())"));

		d = cp.add(new Div());
		d.append("Font icons can be colored by using the default set of is-xxxx color names, like ")
			.append(FaIcon.faSkullCrossbones.css("is-danger").createNode())
			.append(", done like ")
			.append(new EmbeddedCode("FaIcon.faSkullCrossbones.css(\"is-danger\").createNode()"));

	}

	private void addSvgIcons() {
		ContentPanel cp = add(new ContentPanel());
		cp.add(new HTag(1, "SVG Icons"));

		Div d = cp.add(new Div());
		d.add("This is text with a ");
		d.add(new SvgIcon("img/xmas-tree-2.svg"));
		d.add(" embedded as an SVG icon, using ")
			.append(new EmbeddedCode("add(new SvgIcon(\"img/xmas-tree-2.svg\"))"));

		d = cp.add(new Div());
		d.append("Like Font icons, you can add css classes to change the icon's behavior. for example change size using is-size-1: ");
		d.append(new SvgIcon("img/xmas-tree-2.svg").css("is-size-1"))
			.append(" which is done using")
			.append(new EmbeddedCode("add(new SvgIcon(\"img/xmas-tree-2.svg\").css(\"is-size-1\"))"))
		;

		d = cp.add(new Div());
		d.append("Or even changing the color (for single-color SVG's only). For example change ")
			.append(new SvgIcon("img/checkmark.svg"))
			.append(" into ")
			.append(new SvgIcon("img/checkmark.svg").css("is-size-1", "is-danger"))
			.append(" using ")
			.append(new EmbeddedCode("new SvgIcon(\"img/checkmark.svg\").css(\"is-size-1\", \"is-danger\")"))
		;

		//-- Button
		cp.add(new HTag(3, "Embedding into a DefaultButton"));
		d = cp.add(new Div());
		DefaultButton button = new DefaultButton("Click me", Icon.of("img/checkmark.svg"), a -> MsgBox.info(this, "Clicked"));
		d.append(button).append(" is created using ").append(new EmbeddedCode("new DefaultButton(\"Click me\", Icon.of(\"img/checkmark.svg\"), ....)"));



		//-- LinkButton
		cp.add(new HTag(3, "Inside a LinkButton"));
		LinkButton lb = new LinkButton("link button", Icon.of("img/checkmark.svg"), a -> MsgBox.info(this, "Clicked"));
		d = cp.add(new Div());
		d.add("This is a ");
		d.add(lb);
		d.add(" inside text");
	}
}
