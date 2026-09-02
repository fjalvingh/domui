package to.etc.domuidemo.pages.components.display;

import to.etc.domui.component.htmleditor.DisplayHtml;
import to.etc.domui.component.htmleditor.DisplayHtml.Mode;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * DisplayHtml: showing html that came from somewhere else, with the unsafe
 * parts taken out of it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class DisplayHtmlPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("DisplayHtml: showing html");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "DisplayHtml: showing html"));

		String review = "<b>The best album they made.</b><br>Side two <i>especially</i>.";

		DisplayHtml block = new DisplayHtml(review);
		block.setWidth("400px");

		DisplayHtml inline = new DisplayHtml(review);
		inline.setMode(Mode.INLINE);

		//-- What the sanitizer does with html it does not allow.
		String nasty = "Nice album<script>ignored()</script>"
			+ "<table><tr><td>a table</td></tr></table>"
			+ "<a href=\"https://domui.org/\">a link</a>"
			+ "<b>but this survives</b>";

		DisplayHtml cleaned = new DisplayHtml(nasty);
		cleaned.setWidth("400px");

		FormBuilder fb = new FormBuilder(cp);
		fb.label("A review (BLOCK)").control(block);
		fb.label("The same, INLINE").control(inline);
		fb.label("Html with tags it does not allow").control(cleaned);

		//-- Show what was kept, as text, so the difference is visible.
		Div what = new Div("dm-tut-q");
		cp.add(what);
		what.add("Given:  " + nasty + "\nKept:   " + cleaned.getValue());

		cp.add(new Para().add("The value went through the sanitizer on the way in. It keeps "
			+ "an allow-list of elements - b, i, u, p, br, a, ol, ul, li, code, div, strike, "
			+ "strong, blockquote, sup, sub and hr - and throws the tags of everything else "
			+ "away: the table is gone but its cell text is not, because that text is text."));
		cp.add(new Para().add("A script is different: it and everything inside it are removed, "
			+ "because its content is code rather than text. So are style, iframe, object and "
			+ "the rest of that family."));
		cp.add(new Para().add("The attributes that survive have their values checked too: an "
			+ "href may only use http, https, mailto, ftp or tel or be relative, and a style "
			+ "may format but not load or run anything. A link that fails that check keeps "
			+ "its text and loses its href."));
		cp.add(new Para().add("setUnchecked(true) skips all of it, and should only be used for "
			+ "html the application produced itself."));
		cp.add(new Para().add("The three modes decide the box, not the content: BLOCK is a "
			+ "block of its own, INLINE flows with the text around it, INLINEBLOCK is a block "
			+ "that sits in a line."));
	}
}
