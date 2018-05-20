package to.etc.domui.util.exporters;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

import java.io.File;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
@NonNullByDefault
public interface IExportFormat {
	/** The descriptive name for the format */
	String name();

	/** The file extension without "." */
	String extension();

	/** The actual exporter */
	IExportWriter<?> createWriter(@NonNull File output) throws Exception;
}
