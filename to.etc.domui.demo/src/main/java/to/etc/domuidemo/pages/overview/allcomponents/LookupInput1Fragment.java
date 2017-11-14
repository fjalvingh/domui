package to.etc.domuidemo.pages.overview.allcomponents;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.LookupInput;
import to.etc.domui.component.meta.MetaProperty;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.webapp.query.QCriteria;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
public class LookupInput1Fragment extends Div {
	private Artist m_f10;

	private Album m_f11;

	private Album m_f12;

	private Album m_f13;

	private Artist m_f20;

	private Album m_f21;

	private Album m_f22;

	private Album m_f23;

	private Artist m_f30;

	private Album m_f31;

	private Album m_f32;

	private Album m_f33;

	@Override public void createContent() throws Exception {
		add(new HTag(2, "LookupInput component").css("ui-header"));

		QCriteria<Album> q = QCriteria.create(Album.class).eq("title", "Angel Dust");
		m_f13 = m_f12 = m_f23 = m_f22 = m_f32 = m_f33 = getSharedContext().queryOne(q);

		FormBuilder fb = new FormBuilder(this);

		LookupInput<Artist> lf10 = new LookupInput<>(Artist.class);
		lf10.setMandatory(true);
		fb.property(this, "f10").label("empty no quick").control(lf10);

		LookupInput<Album> lf11 = new LookupInput<>(Album.class);
		lf11.setMandatory(true);
		fb.property(this, "f11").label("empty quicks").control(lf11);

		LookupInput<Album> lf12 = new LookupInput<>(Album.class);
		lf12.setMandatory(true);
		fb.property(this, "f12").label("filled 1line val").control(lf12);

		LookupInput<Album> lf13 = new LookupInput<>(Album.class);
		lf13.setValueColumns("title", "artist.name");
		lf13.setMandatory(true);
		fb.property(this, "f13").label("filled 2line val").control(lf13);

		//-- Readonly set
		add(new HTag(3, "ReadOnly").css("ui-header"));
		fb = new FormBuilder(this);
		LookupInput<Artist> lf20 = new LookupInput<>(Artist.class);
		lf20.setMandatory(true);
		lf20.setReadOnly(true);
		fb.property(this, "f20").label("empty no quick").control(lf20);

		LookupInput<Album> lf21 = new LookupInput<>(Album.class);
		lf21.setMandatory(true);
		lf21.setReadOnly(true);
		fb.property(this, "f21").label("empty quicks").control(lf21);

		LookupInput<Album> lf22 = new LookupInput<>(Album.class);
		lf22.setMandatory(true);
		lf22.setReadOnly(true);
		fb.property(this, "f22").label("filled 1line val").control(lf22);

		LookupInput<Album> lf23 = new LookupInput<>(Album.class);
		lf23.setValueColumns("title", "artist.name");
		lf23.setReadOnly(true);
		lf23.setMandatory(true);
		fb.property(this, "f23").label("filled 2line val").control(lf23);

		//-- disabled set
		add(new HTag(3, "disabled").css("ui-header"));
		fb = new FormBuilder(this);
		LookupInput<Artist> lf30 = new LookupInput<>(Artist.class);
		lf30.setMandatory(true);
		lf30.setDisabled(true);
		fb.property(this, "f30").label("empty no quick").control(lf30);

		LookupInput<Album> lf31 = new LookupInput<>(Album.class);
		lf31.setMandatory(true);
		lf31.setDisabled(true);
		fb.property(this, "f31").label("empty quicks").control(lf31);

		LookupInput<Album> lf32 = new LookupInput<>(Album.class);
		lf32.setMandatory(true);
		lf32.setDisabled(true);
		fb.property(this, "f32").label("filled 1line val").control(lf32);

		LookupInput<Album> lf33 = new LookupInput<>(Album.class);
		lf33.setValueColumns("title", "artist.name");
		lf33.setDisabled(true);
		lf33.setMandatory(true);
		fb.property(this, "f33").label("filled 2line val").control(lf33);

		DefaultButton validate = new DefaultButton("validate", a -> bindErrors());
		add(validate);
	}

	@MetaProperty(required = YesNoType.YES)
	public Artist getF10() {
		return m_f10;
	}

	public void setF10(Artist f10) {
		m_f10 = f10;
	}

	@MetaProperty(required = YesNoType.YES)
	public Album getF11() {
		return m_f11;
	}

	public void setF11(Album f11) {
		m_f11 = f11;
	}

	@MetaProperty(required = YesNoType.YES)
	public Album getF12() {
		return m_f12;
	}

	public void setF12(Album f12) {
		m_f12 = f12;
	}

	@MetaProperty(required = YesNoType.YES)
	public Album getF13() {
		return m_f13;
	}

	public void setF13(Album f13) {
		m_f13 = f13;
	}

	public Artist getF20() {
		return m_f20;
	}

	public void setF20(Artist f20) {
		m_f20 = f20;
	}

	public Album getF21() {
		return m_f21;
	}

	public void setF21(Album f21) {
		m_f21 = f21;
	}

	public Album getF22() {
		return m_f22;
	}

	public void setF22(Album f22) {
		m_f22 = f22;
	}

	public Album getF23() {
		return m_f23;
	}

	public void setF23(Album f23) {
		m_f23 = f23;
	}

	public Artist getF30() {
		return m_f30;
	}

	public void setF30(Artist f30) {
		m_f30 = f30;
	}

	public Album getF31() {
		return m_f31;
	}

	public void setF31(Album f31) {
		m_f31 = f31;
	}

	public Album getF32() {
		return m_f32;
	}

	public void setF32(Album f32) {
		m_f32 = f32;
	}

	public Album getF33() {
		return m_f33;
	}

	public void setF33(Album f33) {
		m_f33 = f33;
	}
}
