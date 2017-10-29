package to.etc.domuidemo.pages.test.componenterrors;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.meta.MetaProperty;
import to.etc.domui.component.meta.TemporalPresentationType;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;

import java.util.Date;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
public class DateInput1Fragment extends Div {
	private Date m_f10;
	private Date	m_f11;
	private Date	m_f12;
	private Date	m_f13;

	private Date	m_f20;
	private Date	m_f21;
	private Date	m_f22;
	private Date	m_f23;


	@Override public void createContent() throws Exception {
		m_f20 = m_f21 = m_f22 = m_f23 = new Date();

		FormBuilder fb = new FormBuilder(this);
		fb.property(this, "f10").control();
		fb.property(this, "f11").control();
		fb.property(this, "f12").control();
		fb.property(this, "f13").control();


		fb.property(this, "f20").control();
		fb.property(this, "f21").control();
		fb.property(this, "f22").control();
		fb.property(this, "f23").control();


		DefaultButton validate = new DefaultButton("validate", a -> bindErrors());
		add(validate);
	}

	@MetaProperty(required = YesNoType.YES)
	public Date getF10() {
		return m_f10;
	}

	public void setF10(Date f10) {
		m_f10 = f10;
	}

	public Date getF11() {
		return m_f11;
	}

	public void setF11(Date f11) {
		m_f11 = f11;
	}

	@MetaProperty(required = YesNoType.YES, temporal = TemporalPresentationType.DATETIME)
	public Date getF12() {
		return m_f12;
	}

	public void setF12(Date f12) {
		m_f12 = f12;
	}

	@MetaProperty(temporal = TemporalPresentationType.DATETIME)
	public Date getF13() {
		return m_f13;
	}

	public void setF13(Date f13) {
		m_f13 = f13;
	}


	@MetaProperty(required = YesNoType.YES)
	public Date getF20() {
		return m_f20;
	}

	public void setF20(Date f20) {
		m_f20 = f20;
	}

	public Date getF21() {
		return m_f21;
	}

	public void setF21(Date f21) {
		m_f21 = f21;
	}

	@MetaProperty(required = YesNoType.YES, temporal = TemporalPresentationType.DATETIME)
	public Date getF22() {
		return m_f22;
	}

	public void setF22(Date f22) {
		m_f22 = f22;
	}

	@MetaProperty(temporal = TemporalPresentationType.DATETIME)
	public Date getF23() {
		return m_f23;
	}

	public void setF23(Date f23) {
		m_f23 = f23;
	}
}
