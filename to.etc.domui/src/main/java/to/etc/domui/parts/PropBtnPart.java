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
package to.etc.domui.parts;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.resources.*;

/**
 * A generated button image from a button definition file. This works like
 * the normal button part but uses a property file (web or application resource) to
 * define the button's layout and colors. The only parameters that are specified
 * by the user of the button are the button's text and an optional button icon. All
 * other thingies come from a resource file.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 21, 2008
 */
public class PropBtnPart implements IBufferedPartFactory<ButtonPartKey> {
	static public final PropBtnPart INSTANCE = new PropBtnPart();

	/**
	 * Decode the parameters for this button thingy.
	 */
	@Override
	public @Nonnull ButtonPartKey decodeKey(DomApplication application, @Nonnull IExtendedParameterInfo info) throws Exception {
		return ButtonPartKey.decode(info);
	}

	/**
	 * Generate the button data.
	 */
	@Override
	public void generate(@Nonnull PartResponse pr, @Nonnull DomApplication da, @Nonnull ButtonPartKey k, @Nonnull IResourceDependencyList rdl) throws Exception {
		Properties p = PartUtil.loadProperties(da, k.getPropFile(), rdl);
//		if(p == null)
//			throw new ThingyNotFoundException("The button property file '" + k.m_propfile + "' was not found.");

		//-- Instantiate the renderer class
		String rc = p.getProperty("renderer");
		PropButtonRenderer r = null;
		if(rc == null)
			r = new PropButtonRenderer();
		else {
			try {
				Class< ? > cl = getClass().getClassLoader().loadClass(rc);
				if(!PropButtonRenderer.class.isAssignableFrom(cl))
					throw new IllegalStateException("The class does not extend PropButtonRenderer");
				r = (PropButtonRenderer) cl.newInstance();
			} catch(Exception x) {
				throw new ThingyNotFoundException("Cannot locate/instantiate the button renderer class '" + rc + "' (specified in " + k.getPropFile() + ")");
			}
		}

		//-- Delegate.
		if(p.getProperty("webui.webapp") == null || !da.inDevelopmentMode()) { // Not gotten from WebContent or not in DEBUG mode? Then we may cache!
			pr.setCacheTime(da.getDefaultExpiryTime());
		}
		r.generate(pr, da, k, p, rdl);
	}
}
