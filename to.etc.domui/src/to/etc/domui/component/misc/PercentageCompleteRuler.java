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

import to.etc.domui.dom.html.*;

public class PercentageCompleteRuler extends Div {
	private int m_percentage;

	private int m_pixelwidth;

	public PercentageCompleteRuler() {
		setWidth("100px");
		m_pixelwidth = 100;
	}

	public void setWidth(int pixels) {
		m_pixelwidth = pixels;
		setWidth(pixels + "px");
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-pct-rlr");
		updateValues();
	}

	public int getPercentage() {
		return m_percentage;
	}

	public void setPercentage(int percentage) {
		if(percentage > 100)
			percentage = 100;
		else if(percentage < 0)
			percentage = 0;
		if(m_percentage != percentage) {
			m_percentage = percentage;
			updateValues();
		}
	}

	private void updateValues() {
		setText(Integer.valueOf(m_percentage) + "%");

		//-- Set background position.
		int pxl = (m_percentage * m_pixelwidth / 100);
		setBackgroundPosition((-400 + pxl) + "px 0px");
	}
}
