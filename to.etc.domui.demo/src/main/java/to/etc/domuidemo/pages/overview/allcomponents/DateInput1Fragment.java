package to.etc.domuidemo.pages.overview.allcomponents;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.DateInput;
import to.etc.domui.component.meta.MetaProperty;
import to.etc.domui.component.meta.TemporalPresentationType;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;

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
	private Date	m_f14;
	private Date	m_f15;

	private Date	m_f20;
	private Date	m_f21;
	private Date	m_f22;
	private Date	m_f23;
	private Date	m_f24;
	private Date	m_f25;


	@Override public void createContent() throws Exception {
		add(new HTag(2, "DateInput"));

		m_f20 = m_f21 = m_f22 = m_f23 = m_f24 = m_f25 = new Date();

		FormBuilder fb = new FormBuilder(this);
		DateInput d10 = new DateInput();
		fb.property(this, "f10").label("mandatory date").control(d10);
		DateInput d11 = new DateInput();
		fb.property(this, "f11").label("optional date").control(d11);
		DateInput d12 = new DateInput();
		d12.setWithTime(true);
		fb.property(this, "f12").label("mandatory datetime").control(d12);
		DateInput d13 = new DateInput();
		d13.setWithTime(true);
		fb.property(this, "f13").label("optional datetime").control(d13);
		DateInput d14 = new DateInput();
		d14.setDisabled(true);
		fb.property(this, "f14").label("disabled date").control(d14);
		DateInput d15 = new DateInput();
		d15.setReadOnly(true);
		fb.property(this, "f15").label("readonly date").control(d15);

		DateInput d20 = new DateInput();
		fb.property(this, "f20").label("mand date").control(d20);
		DateInput d21 = new DateInput();
		fb.property(this, "f21").label("optional date").control(d21);
		DateInput d22 = new DateInput();
		d22.setWithTime(true);
		fb.property(this, "f22").label("mand datetime").control(d22);
		DateInput d23 = new DateInput();
		d23.setWithTime(true);
		fb.property(this, "f23").label("opt datetime").control(d23);
		DateInput d24 = new DateInput();
		d24.setDisabled(true);
		fb.property(this, "f24").label("disabled date").control(d24);
		DateInput d25 = new DateInput();
		d25.setReadOnly(true);
		fb.property(this, "f25").label("readonly date").control(d25);

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

	public Date getF14() {
		return m_f14;
	}

	public void setF14(Date f14) {
		m_f14 = f14;
	}

	public Date getF15() {
		return m_f15;
	}

	public void setF15(Date f15) {
		m_f15 = f15;
	}

	public Date getF24() {
		return m_f24;
	}

	public void setF24(Date f24) {
		m_f24 = f24;
	}

	public Date getF25() {
		return m_f25;
	}

	public void setF25(Date f25) {
		m_f25 = f25;
	}
}
