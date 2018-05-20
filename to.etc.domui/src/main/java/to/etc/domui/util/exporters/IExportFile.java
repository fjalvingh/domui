package to.etc.domui.util.exporters;

import org.eclipse.jdt.annotation.NonNull;

import java.io.File;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
public interface IExportFile {
	@NonNull File getOutputFile();
	@NonNull String getOutputName();
	@NonNull String getMimeType();
}
