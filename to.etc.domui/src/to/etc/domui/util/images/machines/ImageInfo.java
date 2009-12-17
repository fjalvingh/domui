package to.etc.domui.util.images.machines;

import java.io.*;
import java.util.*;

import javax.annotation.*;

/**
 * The decoded data for an <i>original</i> image.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 10, 2008
 */
final public class ImageInfo implements Serializable {
	/** The mime type of the original image, if known */
	private String m_mime;

	/** The type name of the file, if known (identified by the 'file' command, if available) */
	private String m_typeName;

	/** If false this image format cannot be converted, and can only be downloaded. This is usually an indication that identify failed. */
	private boolean m_convertable;

	/** The decoded list of per-page information, if available. */
	private List<OriginalImagePage> m_pageList;

	public ImageInfo(String mime, String typeName, boolean convertible, List<OriginalImagePage> pageList) {
		m_mime = mime;
		m_pageList = pageList == null ? null : Collections.unmodifiableList(pageList);
		m_convertable = convertible;
		m_typeName = typeName;
	}

	public String getMime() {
		return m_mime;
	}

	public void setMime(String mime) {
		m_mime = mime;
	}

	public String getTypeName() {
		return m_typeName;
	}

	public boolean isConvertable() {
		return m_convertable;
	}

	@Nullable
	public List<OriginalImagePage> getPageList() {
		return m_pageList;
	}

	public int getPageCount() {
		return m_pageList == null ? -1 : m_pageList.size();
	}

	@Nullable
	public OriginalImagePage getPage(int ix) {
		return m_pageList == null ? null : m_pageList.get(ix);
	}
}
