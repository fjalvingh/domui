package to.etc.domuidemo.pages.overview.allcomponents;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.meta.MetaProperty;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;

import java.math.BigDecimal;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
public class Text2F4Fragment extends Div {
	private String		m_t20;

	private String		m_t21 = "zzaabb";

	private Integer	m_t22 = Integer.valueOf("12345");

	private String		m_t23;

	private String		m_t24;

	private BigDecimal m_t30;

	private BigDecimal m_t31 = new BigDecimal("123.45");

	private String 		m_t40 = "Hello, world";

	private String m_t41 = "The time has come, the Walrus said";

	private String m_t50;

	private String m_t51;

	@Override public void createContent() throws Exception {
		add(new HTag(2, "Text2 components").css("ui-header"));

		FormBuilder fb = new FormBuilder(this);

		fb.label("$ mandatory").property(this, "t20").control();
		fb.label("$ optional").property(this, "t21").control();
		fb.label("integer").property(this, "t22").control();

		Text2<String> t23 = fb.label("string").property(this, "t23").control(Text2.class);
			Text2<String> t24 = fb.label("string w/btn").property(this, "t24").control(Text2.class);

		fb.label("bigdecimal empty").property(this, "t30").control();
		fb.label("bigdecimal 123.45").property(this, "t31").control();

		add(new VerticalSpacer(10));
		add(new HTag(3, "Text2 allows adding buttons").css("ui-header"));

		Text2<String> t40 = new Text2<>(String.class);
		t40.addButton(FaIcon.faBell, a -> {});
		t40.addButton(FaIcon.faFile, a -> {});
		fb = new FormBuilder(this);
		fb.property(this, "t40").label("default buttons").control(t40);

		Text2<String> t41 = new Text2<>(String.class);
		t41.addButton(FaIcon.faBell, a -> {}).css("is-primary");
		t41.addButton(FaIcon.faCloud, a -> {}).css("is-danger");
		fb.property(this, "t41").label("is-primary, is-danger").control(t41);

		Text2<String>	t50 = new Text2<>(String.class);
		t50.addButton(new DefaultButton("Big Button", a -> {}));
		fb.property(this,"t50").label("Big button").control(t50);

		Text2<String>	t51 = new Text2<>(String.class);
		t51.addButton(new DefaultButton("Colored Button", a -> {}).css("is-danger"));
		fb.property(this,"t51").label("Colored button").control(t51);

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

	public String getT24() {
		return m_t24;
	}

	public void setT24(String t24) {
		m_t24 = t24;
	}

	public String getT40() {
		return m_t40;
	}

	public void setT40(String t40) {
		m_t40 = t40;
	}

	public String getT41() {
		return m_t41;
	}

	public void setT41(String t41) {
		m_t41 = t41;
	}

	public String getT50() {
		return m_t50;
	}

	public void setT50(String t50) {
		m_t50 = t50;
	}

	public String getT51() {
		return m_t51;
	}

	public void setT51(String t51) {
		m_t51 = t51;
	}
}
