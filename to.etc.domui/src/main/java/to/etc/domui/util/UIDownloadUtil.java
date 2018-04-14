package to.etc.domui.util;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.ATag;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.parts.TempFilePart;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.state.UIContext;
import to.etc.util.StringTool;

import java.io.File;

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
