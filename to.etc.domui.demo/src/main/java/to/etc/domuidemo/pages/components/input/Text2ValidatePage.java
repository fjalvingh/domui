package to.etc.domuidemo.pages.components.input;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.converter.EmailValidator;
import to.etc.domui.converter.MaxMinValidator;
import to.etc.domui.converter.MoneyBigDecimalFullConverter;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

import java.math.BigDecimal;

/**
 * Text2: everything getValue() checks before it hands a value back - mandatory,
 * the regular expression, the validators and the converter.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class Text2ValidatePage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Text2: what getValue() checks");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Text2: what getValue() checks"));

		Text2<String> name = new Text2<>(String.class);
		name.setMandatory(true);

		Text2<String> email = new Text2<>(String.class);
		email.addValidator(new EmailValidator());

		Text2<String> postcode = new Text2<>(String.class);
		postcode.setValidationRegexp("[0-9]{4}\\s?[A-Za-z]{2}");
		postcode.setRegexpUserString("9999 AA");

		Text2<Integer> copies = new Text2<>(Integer.class);
		copies.addValidator(new MaxMinValidator(1, 99, null));

		Text2<BigDecimal> price = new Text2<>(BigDecimal.class);
		price.setConverter(ConverterRegistry.getConverterInstance(MoneyBigDecimalFullConverter.class));
		price.setValue(new BigDecimal("14.95"));

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Customer").control(name);
		fb.label("Email address").control(email);
		fb.label("Postcode").control(postcode);
		fb.label("Copies (1-99)").control(copies);
		fb.label("Price").control(price);

		Div result = new Div("dm-tut");
		result.add("Fill something wrong in and press Order.");

		cp.add(new DefaultButton("Order", a -> {
			//-- The first getValue() that cannot deliver ends this handler.
			String nameValue = name.getValue();
			String emailValue = email.getValue();
			String postcodeValue = postcode.getValue();
			Integer copiesValue = copies.getValue();
			BigDecimal priceValue = price.getValue();

			result.removeAllChildren();
			result.add("Ordered: " + copiesValue + " x " + priceValue
				+ " for " + nameValue + " (" + emailValue + ", " + postcodeValue + ")");
		}));

		cp.add(new DefaultButton("Which fields are wrong?", a -> {
			result.removeAllChildren();
			line(result, "Customer", name.hasError());
			line(result, "Email address", email.hasError());
			line(result, "Postcode", postcode.hasError());
			line(result, "Copies", copies.hasError());
			line(result, "Price", price.hasError());
		}));
		cp.add(result);

		//-- The three states the control can be in.
		cp.add(new HTag(2, "Read only, disabled, and disabled with a reason"));

		Text2<String> readOnly = new Text2<>(String.class);
		readOnly.setValue("Rubber Soul");
		readOnly.setReadOnly(true);

		Text2<String> disabled = new Text2<>(String.class);
		disabled.setValue("Rubber Soul");
		disabled.setDisabled(true);

		Text2<String> because = new Text2<>(String.class);
		because.setValue("Rubber Soul");
		because.setDisabledBecause("The album has already been shipped");

		FormBuilder fb2 = new FormBuilder(cp);
		fb2.label("setReadOnly(true)").control(readOnly);
		fb2.label("setDisabled(true)").control(disabled);
		fb2.label("setDisabledBecause()").control(because);

		cp.add(new Para().add("Hover over the last one: the reason is its tooltip."));
	}

	private static void line(Div into, String what, boolean wrong) {
		Para para = new Para();
		into.add(para);
		para.add(what + " is " + (wrong ? "wrong or missing" : "ok"));
	}
}
