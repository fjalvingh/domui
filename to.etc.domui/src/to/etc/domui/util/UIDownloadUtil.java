package to.etc.domui.util;

import java.io.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.parts.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.util.*;

final public class UIDownloadUtil {
	private UIDownloadUtil() {}

	static public void	createDownloadImmediate(NodeContainer target, String downloadURL) {
		target.appendJavascript("location.href=" + StringTool.strToJavascriptString(downloadURL, true) + ";");
	}

	/**
	 *
	 */
	static public ATag createDownloadLink(File tmpfile, String mime, @Nullable String remoteFileName, boolean downloadImmediate) {
		String type = null != remoteFileName ? "attachment" : null;

		IRequestContext ctx = UIContext.getRequestContext();
		String tf = TempFilePart.registerTempFile(ctx, tmpfile, mime, type, remoteFileName);		// Register file for download
		String url = ctx.getRelativePath(tf);				// Make path server-absolute.

		//-- Create the link
		ATag at = new ATag();
		at.setHref(url);

		if(downloadImmediate) {
			createDownloadImmediate(at, url);
		}
		return at;
	}


}
