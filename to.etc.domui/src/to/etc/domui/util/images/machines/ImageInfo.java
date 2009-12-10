package to.etc.domui.util.images.machines;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import javax.annotation.concurrent.*;

/**
 * The decoded data for an <i>original</i> image.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 10, 2008
 */
@Immutable
final public class ImageInfo implements Serializable {
	/** The mime type of the original image */
	private String m_mime;

	/** The decoded list of per-page information, if available. */
	private List<OriginalImagePage> m_pageList;

	public ImageInfo(String mime, List<OriginalImagePage> pageList) {
		m_mime = mime;
		m_pageList = pageList == null ? null : Collections.unmodifiableList(pageList);
	}

	public String getMime() {
		return m_mime;
	}

	@Nullable
	public List<OriginalImagePage> getPageList() {
		return m_pageList;
	}

	public int getPageCount() {
		return m_pageList.size();
	}

	@Nullable
	public OriginalImagePage getPage(int ix) {
		return m_pageList == null ? null : m_pageList.get(ix);
	}
}
