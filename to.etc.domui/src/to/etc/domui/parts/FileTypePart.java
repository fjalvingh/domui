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

import java.io.*;

import javax.annotation.*;

import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.util.resources.*;
import to.etc.util.*;

public class FileTypePart implements IBufferedPartFactory {
	static private final String PREFIX = "$filetype$";

	/**
	 * Decodes the request into a resource to generate. This simply returns
	 * the resource name preceded with a prefix to make it unique.
	 */
	@Override
	public @Nonnull Object decodeKey(@Nonnull String rurl, @Nonnull IExtendedParameterInfo param) throws Exception {
		return PREFIX + rurl;
	}

	@Override
	public void generate(@Nonnull PartResponse pr, @Nonnull DomApplication da, @Nonnull Object key, @Nonnull IResourceDependencyList rdl) throws Exception {
		String ext = ((String) key).substring(PREFIX.length()).toLowerCase();

		//-- Can we locate a filetype of that type in the web resources?
		if(ext.length() == 0)
			ext = "generic";

		//-- Is a web-based resource available?
		InputStream is = null;
		try {
			File src = da.getAppFile("resources/filetypes/" + ext + ".png");
			if(src.exists())
				is = new FileInputStream(src);
			else {
				//-- Can we locate an internal resource?
				is = da.getClass().getResourceAsStream("/resources/filetypes/" + ext + ".png");
				if(is == null)
					is = da.getClass().getResourceAsStream("/resources/filetypes/generic.png");
			}
			if(is == null)
				throw new IllegalStateException("File type " + ext + " not found.");
			FileTool.copyFile(pr.getOutputStream(), is);
			pr.setMime("image/png");
			pr.setCacheTime(da.getDefaultExpiryTime());
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
		}
	}

	static public String getURL(String ext) {
		return FileTypePart.class.getName() + ".part/" + ext;
	}
}
