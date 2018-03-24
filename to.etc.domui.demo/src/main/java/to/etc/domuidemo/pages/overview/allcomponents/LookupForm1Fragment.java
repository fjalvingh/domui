package to.etc.domuidemo.pages.overview.allcomponents;

import to.etc.domui.component.headers.GenericHeader;
import to.etc.domui.component.headers.GenericHeader.Type;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.lookup.LookupForm;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.webapp.query.QCriteria;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-11-17.
 */
public class LookupForm1Fragment extends Div {
	@Override public void createContent() throws Exception {
		add(new HTag(2, "LookupForm"));

		ContentPanel cp = new ContentPanel();
		add(cp);

		LookupForm<Invoice> lf = new LookupForm<>(Invoice.class);
		cp.add(lf);

		Div result = new Div();
		add(result);

		lf.setClicked(c -> {
			result.removeAllChildren();
			result.add(new GenericHeader(Type.HEADER_2, "Resulting query"));
			QCriteria<Invoice> enteredCriteria = lf.getEnteredCriteria();
			if(enteredCriteria == null) {
				result.add("No data entered");
			} else {
				result.add(enteredCriteria.toString());
			}
		});

	}
}
