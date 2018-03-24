package to.etc.domuidemo.pages.overview.allcomponents;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.meta.MetaProperty;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.component2.combo.ComboFixed2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.css.PositionType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;

import java.math.BigDecimal;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-11-17.
 */
public class Combo2FFragment extends Div {
	private PositionType m_f10;

	private PositionType m_f11 = PositionType.ABSOLUTE;

	private PositionType m_f12;

	private ExampleCurrency m_f20c = ExampleCurrency.Bitcoin;


	@Override public void createContent() throws Exception {
		add(new HTag(2, "ComboXxxx2 components").css("ui-header"));

		FormBuilder fb = new FormBuilder(this);

		ComboFixed2<PositionType> c10 = ComboFixed2.createEnumCombo(PositionType.class);
		fb.label("empty-enum").property(this, "f10").control(c10);
		ComboFixed2<PositionType> c11 = ComboFixed2.createEnumCombo(PositionType.class);
		fb.label("value-enum").property(this, "f11").control(c11);
		ComboFixed2<PositionType> c12 = ComboFixed2.createEnumCombo(PositionType.class);
		c12.setMandatory(true);
		fb.label("manda-enum").property(this, "f12").control(c12);

		ComboFixed2<ExampleCurrency> c20 = ComboFixed2.createEnumCombo(ExampleCurrency.class);
		fb.property(this, "f20c").cssControl("ui-has-addons").control(c20);

		Text2<BigDecimal> c20a = new Text2<>(BigDecimal.class);
		fb.appendAfterControl(c20a);

		DefaultButton validate = new DefaultButton("validate", a -> bindErrors());
		add(validate);
	}

	public PositionType getF10() {
		return m_f10;
	}

	public void setF10(PositionType f10) {
		m_f10 = f10;
	}

	public PositionType getF11() {
		return m_f11;
	}

	public void setF11(PositionType f11) {
		m_f11 = f11;
	}

	@MetaProperty(required = YesNoType.YES)
	public PositionType getF12() {
		return m_f12;
	}

	public void setF12(PositionType f12) {
		m_f12 = f12;
	}

	public ExampleCurrency getF20c() {
		return m_f20c;
	}

	public void setF20c(ExampleCurrency f20c) {
		m_f20c = f20c;
	}
}
