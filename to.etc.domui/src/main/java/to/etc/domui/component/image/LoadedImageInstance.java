package to.etc.domui.component.image;

import org.eclipse.jdt.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 5, 2014
 */
//@Immutable
public class LoadedImageInstance implements IUIImageInstance {
	@NonNull
	final private File m_imageFile;

	@NonNull
	private final String m_mime;

	@NonNull
	final private Dimension m_dimension;

	public LoadedImageInstance(@NonNull File imageFile, @NonNull String mime, @NonNull Dimension dimension) {
		m_imageFile = imageFile;
		m_mime = mime;
		m_dimension = dimension;
	}

	@Override
	public InputStream getImage() throws Exception {
		return new FileInputStream(m_imageFile);
	}

	@Override
	public Dimension getDimension() {
		return m_dimension;
	}

	@Override
	public int getImageSize() {
		return (int) m_imageFile.length();
	}

	@Override
	public String getMimeType() {
		return m_mime;
	}
}
