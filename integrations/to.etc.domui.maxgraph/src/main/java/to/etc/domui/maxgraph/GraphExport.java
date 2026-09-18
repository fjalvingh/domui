package to.etc.domui.maxgraph;

import java.io.File;
import java.nio.file.Files;

/**
 * A picture of a drawing, as it arrived from the browser. This is what
 * {@link IGraphExportHandler} is handed: the bytes of the file, and how big the picture
 * is - which the server cannot know by itself, because the size of a drawing is decided
 * by what maxGraph made of it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public final class GraphExport {
	private final GraphExportFormat m_format;

	private final byte[] m_data;

	private final int m_width;

	private final int m_height;

	public GraphExport(GraphExportFormat format, byte[] data, int width, int height) {
		m_format = format;
		m_data = data;
		m_width = width;
		m_height = height;
	}

	public GraphExportFormat getFormat() {
		return m_format;
	}

	/** The file itself: the svg document as utf-8, or the png. */
	public byte[] getData() {
		return m_data;
	}

	/** The width of the picture in pixels, at the scale it was asked for. */
	public int getWidth() {
		return m_width;
	}

	public int getHeight() {
		return m_height;
	}

	public String getMimeType() {
		return m_format.getMimeType();
	}

	/** Write the picture to a file, which is what a report or a mail attachment wants. */
	public File saveTo(File target) throws Exception {
		Files.write(target.toPath(), m_data);
		return target;
	}

	@Override
	public String toString() {
		return m_format.getName() + " " + m_width + "x" + m_height + ", " + m_data.length + " bytes";
	}
}
