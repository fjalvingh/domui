package to.etc.domui.util.images.converters;

public class ImageResize implements IImageConversionSpecifier {
	private int m_width, m_height;

	private int m_filterSpec;

	private String m_targetMime;

	public ImageResize(int width, int height) {
		m_height = height;
		m_width = width;
	}

	public ImageResize(int width, int height, int filterspec) {
		m_height = height;
		m_width = width;
		m_filterSpec = filterspec;
	}

	public ImageResize(int width, int height, int filterspec, String targetMime) {
		m_height = height;
		m_width = width;
		m_filterSpec = filterspec;
		m_targetMime = targetMime;
	}

	public ImageResize(int width, int height, String targetMime) {
		m_height = height;
		m_width = width;
		m_targetMime = targetMime;
	}

	public int getWidth() {
		return m_width;
	}

	public int getHeight() {
		return m_height;
	}

	public int getFilterSpec() {
		return m_filterSpec;
	}

	public String getConversionKey() {
		return "rsz-" + m_width + "x" + m_height + "$" + m_filterSpec;
	}

	public String getTargetMime() {
		return m_targetMime;
	}
}
