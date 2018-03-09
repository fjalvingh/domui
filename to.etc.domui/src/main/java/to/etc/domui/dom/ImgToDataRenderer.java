package to.etc.domui.dom;

import to.etc.domui.server.DomApplication;
import to.etc.domui.util.resources.IResourceRef;
import to.etc.domui.util.resources.ResourceDependencyList;
import to.etc.util.FileTool;
import to.etc.util.StringTool;
import to.etc.webapp.core.ServerTools;

import java.io.InputStream;

import static java.util.Objects.requireNonNull;

/**
 * Converts a DomUI image URL to a data: encoded string. Used to render report html without external references.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 9-3-18.
 */
final public class ImgToDataRenderer {
	public String imageToData(String imageURL) throws Exception {
		//-- Try to locate the image

		IResourceRef resource = DomApplication.get().getResource(imageURL, new ResourceDependencyList());
		if(! resource.exists()) {
			System.out.println(imageURL + ": image resource not found");
			return "";
		}

		byte[] data;
		try(InputStream is = requireNonNull(resource.getInputStream())) {
			data = FileTool.readByteArray(is);
		}
		String mimeType = ServerTools.getExtMimeType(FileTool.getFileExtension(imageURL));

		StringBuilder sb = new StringBuilder();
		sb.append("data:").append(mimeType).append(";base64,");
		sb.append(StringTool.encodeBase64ToString(data));
		return sb.toString();
	}
}
