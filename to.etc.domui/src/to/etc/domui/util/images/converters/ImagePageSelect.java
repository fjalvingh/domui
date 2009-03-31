package to.etc.domui.util.images.converters;

public class ImagePageSelect implements IImageConversionSpecifier {
	private int				m_pageNumber;

	public ImagePageSelect(int pageNumber) {
		m_pageNumber = pageNumber;
	}

	public int getPageNumber() {
		return m_pageNumber;
	}
	public String getConversionKey() {
		return "psel-"+m_pageNumber;
	}
}
