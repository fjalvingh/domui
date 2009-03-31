package to.etc.domui.util.images.converters;

import java.util.*;

/**
 * Base class for all image data descriptors.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 10, 2008
 */
public class ImageData {
	private String				m_mime;

	private List<ImagePage>		m_pageList;

	public ImageData(String mime, List<ImagePage> pageList) {
		m_mime = mime;
		m_pageList = Collections.unmodifiableList(pageList);
	}
	public String getMime() {
		return m_mime;
	}
	public List<ImagePage> getPageList() {
		return m_pageList;
	}
	public int		getPageCount() {
		return m_pageList.size();
	}
	public ImagePage		getPage(int ix) {
		return m_pageList.get(ix);
	}
}
