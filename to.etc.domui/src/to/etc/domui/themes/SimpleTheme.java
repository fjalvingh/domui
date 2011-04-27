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
package to.etc.domui.themes;

import java.util.*;

import javax.annotation.concurrent.*;

import to.etc.domui.server.*;
import to.etc.domui.util.resources.*;

/**
 * The result of a "simple" theme. It only contains the properties map for colors
 * and icons, and a directory for theme resources.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 27, 2011
 */
@Immutable
public final class SimpleTheme implements ITheme {
	final private DomApplication m_da;

	final private String m_styleName;

	final private ResourceDependencies m_rd;

	final private Map<String, Object> m_themeProperties;

	public SimpleTheme(DomApplication da, String styleName, Map<String, Object> themeProperties, ResourceDependencies rd) {
		m_da = da;
		m_styleName = styleName;
		m_themeProperties = Collections.unmodifiableMap(themeProperties);
		m_rd = rd;
	}

	@Override
	public ResourceDependencies getDependencies() {
		return m_rd;
	}

	@Override
	public Map<String, Object> getThemeProperties() {
		return m_themeProperties;
	}

	@Override
	public IResourceRef getThemeResource(String name, IResourceDependencyList rdl) throws Exception {
		return m_da.getResource("$themes/" + m_styleName + "/" + name, rdl);
	}
}
