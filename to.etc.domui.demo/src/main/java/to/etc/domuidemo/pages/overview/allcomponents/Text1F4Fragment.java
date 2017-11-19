package to.etc.domuidemo.pages.overview.allcomponents;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text;
import to.etc.domui.component.meta.MetaProperty;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;

import java.math.BigDecimal;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
public class Text1F4Fragment extends Div {
	private String		m_t20;

	private String		m_t21 = "zzaabb";

	private Integer	m_t22 = Integer.valueOf("12345");

	private String		m_t23;

	private BigDecimal m_t30;

	private BigDecimal m_t31 = new BigDecimal("123.45");

	@Override public void createContent() throws Exception {
		add(new HTag(2, "Text components").css("ui-header"));

		FormBuilder fb = new FormBuilder(this);

		fb.label("$ mandatory").property(this, "t20").control();
		fb.label("$ optional").property(this, "t21").control();
		fb.label("integer").property(this, "t22").control();

		Text<String> t23 = fb.label("string").property(this, "t23").control(Text.class);

		fb.label("bigdecimal empty").property(this, "t30").control();
		fb.label("bigdecimal 123.45").property(this, "t31").control();

		add(new DefaultButton("validate", a -> bindErrors()));
	}

	@MetaProperty(required = YesNoType.YES)
	public String getT20() {
		return m_t20;
	}

	public void setT20(String t20) {
		m_t20 = t20;
	}

	public String getT21() {
		return m_t21;
	}

	public void setT21(String t21) {
		m_t21 = t21;
	}

	public Integer getT22() {
		return m_t22;
	}

	public void setT22(Integer t22) {
		m_t22 = t22;
	}

	public BigDecimal getT30() {
		return m_t30;
	}

	public void setT30(BigDecimal t30) {
		m_t30 = t30;
	}

	public BigDecimal getT31() {
		return m_t31;
	}

	public void setT31(BigDecimal t31) {
		m_t31 = t31;
	}

	@MetaProperty(required = YesNoType.YES)
	public String getT23() {
		return m_t23;
	}

	public void setT23(String t23) {
		m_t23 = t23;
	}
}
