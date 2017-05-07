package to.etc.domui.util.upload;

import to.etc.domui.util.*;

/**
 * Thrown when EOF or connection reset is detected during upload.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2/26/16.
 */
final public class FileUploadInterruptedException extends FileUploadException {
	public FileUploadInterruptedException(Exception x) {
		super(x, Msgs.BUNDLE, Msgs.UPLOAD_INTERRUPTED);
	}
}
