package to.etc.domuidemo.pages.overview.allcomponents;

import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-11-17.
 */
public class AllComponents1Page extends UrlPage {
	@Override public void createContent() throws Exception {
		Div container = new Div("flex-table");
		add(container);

		container.add(new HTag(1, "Miscellany").css("ui-header flex-table-header"));
		container.add(new FileUploadFragment());
		container.add(new ImageThingsFragment());

		container.add(new HTag(1, "LookupForm component").css("ui-header flex-table-header"));
		container.add(new LookupForm1Fragment());

		container.add(new HTag(1, "Text components inside a form").css("ui-header flex-table-header"));
		container.add(new Text2F4Fragment().css("flex-table-column"));
		container.add(new Text1F4Fragment().css("flex-table-column"));

		container.add(new HTag(1, "Combo components inside form").css("ui-header flex-table-header"));
		container.add(new Combo2FFragment().css("flex-table-column"));
		container.add(new ComboF4Fragment().css("flex-table-column"));

		container.add(new HTag(1, "LookupInput components").css("ui-header flex-table-header"));
		container.add(new LookupInput2Fragment().css("flex-table-column"));
		container.add(new LookupInput1Fragment().css("flex-table-column"));

		container.add(new HTag(1, "DateInput component").css("ui-header flex-table-header"));
		container.add(new DateInput2Fragment().css("flex-table-column"));
		container.add(new DateInput1Fragment().css("flex-table-column"));

		container.add(new HTag(1, "Buttons").css("ui-header flex-table-header"));
		container.add(new ButtonFragment().css("flex-table-column"));

		container.add(new HTag(1, "Text components outside a form").css("ui-header flex-table-header"));
		container.add(new Text2RawFragment().css("flex-table-column"));
		container.add(new TextRawFragment().css("flex-table-column"));

	}
}
