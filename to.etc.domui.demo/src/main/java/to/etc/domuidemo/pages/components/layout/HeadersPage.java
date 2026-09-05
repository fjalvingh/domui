package to.etc.domuidemo.pages.components.layout;

import to.etc.domui.component.headers.ExpandHeader;
import to.etc.domui.component.headers.GenericHeader;
import to.etc.domui.component.headers.GenericHeader.Type;
import to.etc.domui.component.layout.Caption2;
import to.etc.domui.component.layout.CaptionType;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * The headers: a title above a section, with or without buttons, and one that
 * folds what is under it away.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class HeadersPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Headers");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Headers"));

		//-- The plain header: a text in a bigger font, in one of six styles.
		cp.add(new GenericHeader(Type.SIMPLE, "GenericHeader, SIMPLE"));
		cp.add(new GenericHeader(Type.BLUE, "GenericHeader, BLUE"));
		cp.add(new GenericHeader(Type.HEADER_1, "GenericHeader, HEADER_1"));
		cp.add(new GenericHeader(Type.HEADER_2, "GenericHeader, HEADER_2"));
		cp.add(new GenericHeader(Type.HEADER_3, "GenericHeader, HEADER_3"));

		//-- ...and one with a button in it.
		GenericHeader withButton = new GenericHeader(Type.HEADER_2, "With a button");
		withButton.addButton(Icon.faPencil, "Rename this section",
			a -> MsgBox2.on(this).info("The pencil was pressed"));
		cp.add(withButton);

		cp.add(new Para().add("A GenericHeader is a header text plus optional buttons at its "
			+ "right. Its type decides what it looks like, and nothing else."));

		//-- The caption bar: a title bar with an icon and buttons.
		cp.add(new HTag(2, "Caption2"));
		Caption2 caption = new Caption2(CaptionType.Default, "A caption with buttons");
		cp.add(caption);
		caption.addButton(Icon.faPlus, "Add something", a -> MsgBox2.on(this).info("Add"));
		caption.addButton(Icon.faTrash, "Delete it", a -> MsgBox2.on(this).info("Delete"));

		Caption2 panelCaption = new Caption2(CaptionType.Panel, "A caption in panel style");
		cp.add(panelCaption);

		cp.add(new Para().add("A Caption2 is a bar rather than a line: it fills its width, it "
			+ "can hold an icon, and its buttons sit at the right end of the bar. The two "
			+ "types are the standalone bar and the one meant to sit on top of a panel."));

		//-- The header that folds its content away.
		cp.add(new HTag(2, "ExpandHeader"));
		ExpandHeader expand = new ExpandHeader("Press me to fold this section away");
		cp.add(expand);

		Div content = new Div("dm-tut");
		content.add("This content belongs to the ExpandHeader above: pressing the header "
			+ "folds it away and pressing it again brings it back.");
		expand.setContent(content);

		cp.add(new Para().add("The ExpandHeader owns what is under it: it is given the content "
			+ "node, and shows or hides it. That is the difference with the other two, which "
			+ "are only a line above whatever happens to follow them."));
	}
}
