package to.etc.domui.component.image;

import java.io.*;

import javax.annotation.*;
import javax.annotation.concurrent.*;

/**
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 5, 2014
 */
@Immutable
public class LoadedImageInstance implements IUIImageInstance {
	@Nonnull
	final private File m_imageFile;

	@Nonnull
	private final String m_mime;

	@Nonnull
	final private Dimension m_dimension;

	public LoadedImageInstance(@Nonnull File imageFile, @Nonnull String mime, @Nonnull Dimension dimension) {
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
