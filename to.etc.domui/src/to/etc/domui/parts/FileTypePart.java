package to.etc.domui.parts;

import java.io.*;

import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.util.resources.*;
import to.etc.util.*;

public class FileTypePart implements BufferedPartFactory {
	static private final String PREFIX = "$filetype$";

	/**
	 * Decodes the request into a resource to generate. This simply returns 
	 * the resource name preceded with a prefix to make it unique.
	 */
	public Object decodeKey(String rurl, IParameterInfo param) throws Exception {
		return PREFIX + rurl;
	}

	public void generate(PartResponse pr, DomApplication da, Object key, ResourceDependencyList rdl) throws Exception {
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
