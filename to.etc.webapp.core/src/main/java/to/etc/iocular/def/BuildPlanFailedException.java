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
package to.etc.iocular.def;

import java.io.*;
import java.util.*;

import to.etc.iocular.ioccontainer.*;
import to.etc.util.*;

public class BuildPlanFailedException extends IocConfigurationException {
	private List<FailedAlternative> m_list;

	private ComponentBuilder m_b;

	public BuildPlanFailedException(ComponentBuilder b, String why, List<FailedAlternative> list) {
		super(b, why);
		m_list = list;
		m_b = b;
	}

	@Override
	public String getMessage() {
		StringWriter sw = new StringWriter();
		IndentWriter iw = new IndentWriter(sw);
		sw.append(super.getMessage() + ":\n");
		try {
			iw.print("- The failed object was a ");
			iw.println(m_b.toString());
			if(getLocationText() != null) {
				iw.print("- Defined at ");
				iw.println(getLocationText());
			}
			if(m_list != null && m_list.size() > 0) {
				iw.println("- The failed alternatives were:");
				iw.inc();
				for(FailedAlternative fa : m_list) {
					fa.dump(iw);
				}
				iw.dec();
			}
			sw.close();
		} catch(IOException ioioioio) {
			ioioioio.printStackTrace();
		}
		return sw.getBuffer().toString();
	}
}
