package to.etc.domui.util.exporters;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
public interface IExportFile {
	@Nonnull File getOutputFile();
	@Nonnull String getOutputName();
	@Nonnull String getMimeType();
}
