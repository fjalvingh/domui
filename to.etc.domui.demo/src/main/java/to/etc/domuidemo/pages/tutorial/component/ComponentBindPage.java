package to.etc.domuidemo.pages.tutorial.component;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

/**
 * Tutorial, "writing a component", step 2: because the control has a bindValue
 * property, binding it is the same one line as binding a Text2.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ComponentBindPage extends UrlPage {
	private final Review m_review = new Review();

	@Override
	public void createContent() throws Exception {
		setPageTitle("The control, bound");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "The control, bound"));

		Text2<String> reviewer = new Text2<>(String.class);
		reviewer.bind().to(m_review, Review_.reviewer());

		StarRating rating = new StarRating();
		rating.setMandatory(true);
		rating.bind().to(m_review, Review_.rating());       // Binds bindValue

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Reviewer").control(reviewer);
		fb.label("Rating").mandatory().control(rating);

		Div result = new Div("dm-tut");
		Div buttons = new Div();
		cp.add(buttons);

		buttons.add(new DefaultButton("Save", a -> {
			if(bindErrors()) {                             // Mandatory, and nothing chosen?
				return;
			}
			say(result, "saved: " + m_review.getReviewer() + " gave it " + m_review.getRating());
		}));

		//-- The other direction: change the model and let the binding update the control.
		buttons.add(new DefaultButton("Set the model to 5 stars", a -> {
			m_review.setRating(5);
			say(result, "the model now says 5; the control follows at the end of this request");
		}));
		buttons.add(new DefaultButton("Clear the model", a -> {
			m_review.setRating(null);
			m_review.setReviewer(null);
			say(result, "the model was cleared");
		}));

		cp.add(result);
		result.add("Pick a rating and press Save. Or change the model and watch the stars follow.");
	}

	private static void say(Div result, String what) {
		result.removeAllChildren();
		result.add(what);
	}
}
