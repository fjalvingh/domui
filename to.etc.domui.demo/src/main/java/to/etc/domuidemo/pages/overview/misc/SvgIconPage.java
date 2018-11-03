package to.etc.domuidemo.pages.overview.misc;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.htmleditor.DisplayHtml;
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
		add(new HTag(1, "Font icon sets"));
		add(new DisplayHtml("We can use <a target='_blank' href='https://fontawesome.com/'><b>FontAwesome</b></a> and other font icon sets easily as icons. Support for FontAwesome 4.7 and FontAwesome 5 free comes out-of-the-box."));
		Div d = add(new Div());
		d
			.append("This uses FontAwesome 5 free icons like ")
			.append(FaIcon.faCalculator.createNode())
			.append(" and ")
			.append(FaIcon.faBlenderPhone.createNode())
			.append(", supported out-of the box by including the maven project fontawesome5free, using ")
			.append(new EmbeddedCode("add(FaIcon.faCalculator.createNode());"))
		;

		d = add(new Div());
		d.append("You can alter its size using standard css classes like: is-size-1 (big) to is-size-7 (small), or is-size-normal, is-size=-large, is-size-medium and is-size-small.");

		d = add(new Div());
		d
			.append("This ").append(FaIcon.faWind.css("is-size-large").createNode()).append(" is large, for instance, done like ")
			.append(new EmbeddedCode("add(FaIcon.faWind.css(\"is-size-large\").createNode())"));



	}

	private void addSvgIcons() {
		add(new HTag(1, "SVG Icons"));

		Div d = add(new Div());
		d.add("This is text with a ");
		d.add(new SvgIcon("img/xmas-tree-2.svg"));
		d.add(" embedded as an SVG icon, using ")
			.append(new EmbeddedCode("add(new SvgIcon(\"img/xmas-tree-2.svg\"))"));

		d = add(new Div());
		d.append("You can size these using size classes too, for example ");
		d.append(new SvgIcon("img/xmas-tree-2.svg").css("is-size-1"))
			.append(" which is done using")
			.append(new EmbeddedCode("add(new SvgIcon(\"img/xmas-tree-2.svg\").css(\"is-size-1\"))"))
		;

		//-- Button
		add(new HTag(3, "Embedding into a DefaultButton"));
		DefaultButton button = new DefaultButton("Click me", Icon.of("img/checkmark.svg"), a -> MsgBox.info(this, "Clicked"));
		add(button);

		//-- LinkButton
		add(new HTag(3, "Inside a LinkButton"));
		LinkButton lb = new LinkButton("link button", Icon.of("img/checkmark.svg"), a -> MsgBox.info(this, "Clicked"));
		d = add(new Div());
		d.add("This is a ");
		d.add(lb);
		d.add(" inside text");
	}
}
