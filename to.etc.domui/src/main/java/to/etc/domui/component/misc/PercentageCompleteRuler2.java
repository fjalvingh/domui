/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.Div;

import java.util.Objects;

public class PercentageCompleteRuler2 extends Div {
	@Nullable
	private Double m_value;

	private int m_pixelwidth;

	private final Div m_slider = new Div("ui-pct-rlr2-sl");

	private final Div m_textDiv = new Div("ui-pct-rlr2-txt");

	public PercentageCompleteRuler2() {
		setWidth(100);
	}

	public void setWidth(int pixels) {
		m_pixelwidth = pixels;
		String w = pixels + "px";
		setWidth(w);
		//m_slider.setWidth(w);
		m_textDiv.setWidth(w);
		//updateValues();
	}

	public void setHeight(int pixels) {
		String w = pixels + "px";
		setHeight(w);
		m_slider.setHeight(w);
		m_textDiv.setHeight(w);
	}

	@Override
	public void createContent() throws Exception {
		add(m_slider);
		add(m_textDiv);
		addCssClass("ui-pct-rlr2");
		updateValues();
	}

	public void setPercentageColor(String color) {
		m_textDiv.setColor(color);
	}

	public void setPercentageClass(String color) {
		m_textDiv.setCssClass("ui-pct-rlr2-txt " + color);
	}

	@Nullable
	public Double getValue() {
		return m_value;
	}

	public void setValue(@Nullable Double value) {
		if(value == null) {
			if(m_value == null)
				return;
			m_value = null;
		} else {
			if(value > 100.0D)
				value = 100.0D;
			else if(value < 0.0D)
				value = 0.0D;
			if(Objects.equals(m_value, value)) {
				return;
			}
			m_value = value;
		}
		updateValues();
	}

	private void updateValues() {
		Double v = m_value;
		double value = v == null ? 0.0D : v;

		m_textDiv.setText(String.format("%.1f %%", value));

		//-- Set background position.
		int pxl = 100 - (int) (value * m_pixelwidth / 100);
		m_slider.setWidth(pxl + "px");
	}
}
