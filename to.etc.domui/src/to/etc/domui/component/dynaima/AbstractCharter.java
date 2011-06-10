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
package to.etc.domui.component.dynaima;

import java.awt.*;
import java.util.*;
import java.util.List;

import org.jCharts.properties.*;


/**
 * This is a utility for creating charts to be displayed through {@link JGraphChartSource}.
 * 
 *
 * @author <a href="mailto:nmaksimovic@execom.eu">Nemanja Maksimovic</a>
 * Created on 11 May 2011
 */
public abstract class AbstractCharter implements ICharterHelper {

	protected JGraphChartSource m_source;
	protected ChartProperties m_properties = new ChartProperties();
	protected AxisProperties m_axisProperties = new AxisProperties();
	protected LegendProperties m_legendProperties = new LegendProperties();

	protected int m_width = 0;

	protected int m_height = 0;
	protected String m_title;

	private List<ChartDataElement> m_chartDataElements;

	private static final Paint[] AVAILABLE_PAINTS = new Paint[]{Color.yellow, Color.red, Color.green, Color.blue, Color.PINK};


	public AbstractCharter(JGraphChartSource source, String title, int width, int height) {
		super();
		m_source = source;
		m_width = width;
		m_height = height;
		m_title = title;
		m_chartDataElements = new ArrayList<ChartDataElement>();
	}

	public void addChartDataElemet(ChartDataElement element) {
		m_chartDataElements.add(element);
	}

	public double[] getChartDataValues() {
		double[] values = new double[m_chartDataElements.size()];
		for(int i = 0; i < m_chartDataElements.size(); i++) {
			values[i] = m_chartDataElements.get(i).getValue();
		}
		return values;
	}

	public String[] getChartDataLabels() {
		String[] values = new String[m_chartDataElements.size()];
		for(int i = 0; i < m_chartDataElements.size(); i++) {
			values[i] = m_chartDataElements.get(i).getLabel();
		}
		return values;
	}

	protected Paint[] selectPaints() {
		Paint[] resultSet = new Paint[m_chartDataElements.size()];
		for(int i = 0; i < m_chartDataElements.size(); i++) {
			resultSet[i] = AVAILABLE_PAINTS[new Random().nextInt(AVAILABLE_PAINTS.length)];
		}
		return resultSet;
	}

}
