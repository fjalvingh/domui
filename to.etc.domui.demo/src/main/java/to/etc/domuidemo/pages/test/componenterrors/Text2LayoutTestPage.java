/*
 * DomUI Java User Interface library
 * Copyright (c) 2017 by Frits Jalvingh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://github.com/fjalvingh/domui
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */

package to.etc.domuidemo.pages.test.componenterrors;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.Caption;
import to.etc.domui.component.meta.MetaProperty;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.themes.Theme;

import java.math.BigDecimal;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 5-10-17.
 */
public class Text2LayoutTestPage extends UrlPage {
	private String		m_t20;

	private String		m_t21 = "zzaabb";

	private Integer	m_t22 = Integer.valueOf("12345");

	private String		m_t23;

	private BigDecimal m_t30;

	private BigDecimal m_t31 = new BigDecimal("123.45");


	@Override public void createContent() throws Exception {
		add(new Caption("Without form4 builder"));

		//-- Single text
		Div d = new Div("ui-f4-line");
		add(d);
		Text2<String> t1	= new Text2<>(String.class);
		d.add(new Label(t1,"zzzzzzzz"));
		d.add(t1);
		t1.setValue("zzzzzzzzzzzzzzzzz");

		//-- Single text with button
		add(new VerticalSpacer(10));
		d = new Div("ui-f4-line");
		add(d);
		Text2<String> t2	= new Text2<>(String.class);
		d.add(new Label(t2,"z22222222"));
		d.add(t2);
		t2.addButton(FaIcon.faAddressBookO, a -> {});
		t2.setValue("zzzzzzzzzzzzzzzzz");

		//-- Single text with 2 buttons
		add(new VerticalSpacer(10));
		d = new Div("ui-f4-line");
		add(d);
		Text2<String> t3	= new Text2<>(String.class);
		d.add(new Label(t3,"z3333333"));
		d.add(t3);
		t3.addButton(FaIcon.faAddressBookO, a -> {});
		t3.addButton(FaIcon.faAnchor, a -> {});
		t3.setValue("zzzzzzzzzzzzzzzzz");

		//-- Button with image
		add(new VerticalSpacer(10));
		d = new Div("ui-f4-line");
		add(d);
		Text2<String> t4	= new Text2<>(String.class);
		d.add(new Label(t4,"zzzzzzzzzzz4"));
		d.add(t4);
		t4.addButton(Theme.BTN_CLEAR, a -> {});
		t4.setValue("zzzzzzzzzzzzzzzzz");

		add(new VerticalSpacer(20));
		add(new Caption("Form4"));

		FormBuilder	fb = new FormBuilder(this);

		fb.label("$ mandatory").property(this, "t20").control();
		fb.label("$ optional").property(this, "t21").control();
		fb.label("integer").property(this, "t22").control();

		Text2<String> t23 = fb.label("string").property(this, "t23").control(Text2.class);
		t23.addButton(FaIcon.faFile, a -> {}).css("is-primary");
		t23.addButton(Theme.BTN_EDIT, a -> {}).css("is-link");

		fb.label("bigdecimal empty").property(this, "t30").control();
		fb.label("bigdecimal 123.45").property(this, "t31").control();

		add(new Caption("Old Text<> control"));
		fb = new FormBuilder(this);
		fb.label("Text 41").control(new Text<>(String.class));
		Text<String> t42 = new Text<String>(String.class);
		t42.setValue("zzzzaaaXXX");
		fb.label("Text 42").control(t42);

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
