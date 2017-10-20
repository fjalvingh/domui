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

import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IExtendedParameterInfo;
import to.etc.domui.server.parts.IBufferedPartFactory;
import to.etc.domui.server.parts.PartResponse;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.util.FileTool;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class FileTypePart implements IBufferedPartFactory<String> {
	static private final String PREFIX = "$filetype$";

	/**
	 * Decodes the request into a resource to generate. This simply returns
	 * the resource name preceded with a prefix to make it unique.
	 */
	@Override
	public @Nonnull String decodeKey(DomApplication application, @Nonnull IExtendedParameterInfo param) throws Exception {
		return PREFIX + param.getInputPath();
	}

	@Override
	public void generate(@Nonnull PartResponse pr, @Nonnull DomApplication da, @Nonnull String key, @Nonnull IResourceDependencyList rdl) throws Exception {
		String ext = key.substring(PREFIX.length()).toLowerCase();

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
