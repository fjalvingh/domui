package to.etc.domui.util.images.cache;

import java.util.*;

/**
 * Contains the data for the ROOT (original) image. It also holds the list of permutations
 * currently available in the cache. This object and it's list-of-images is locked thru locking
 * the image cache instance.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
public class ImageRoot {
	private ImageCache			m_lock;
	private Object				m_imageKey;
	private String				m_filenameBase;
//	private String				m_mimeType;
//	private Dimension			m_originalDimension;

	private List<ImageInstance>	m_instanceList = new ArrayList<ImageInstance>();

	protected ImageRoot(ImageCache ic, Object imageKey, String filenameBase) {
		m_lock = ic;
		m_imageKey = imageKey;
		m_filenameBase = filenameBase;
	}

	/**
	 * If the original image reference is present locate and return it.
	 * @return
	 */
	ImageInstance		findOriginal() {
		synchronized(m_lock) {
			for(ImageInstance ii : m_instanceList) {
				if(ii.getPermutation().length() == 0)
					return ii;
			}
			return null;
		}
	}

	ImageInstance		findPermutation(String perm) {
		synchronized(m_lock) {
			for(ImageInstance ii : m_instanceList) {
				if(perm.equals(ii.getPermutation()))
					return ii;
			}
			return null;
		}
	}
	public ImageCache getCache() {
		return m_lock;
	}

	/**
	 * Adds the image to the list of instances. This does not register it in the
	 * LRU cache nor does it register it's cache load.
	 *
	 * @param ii
	 */
	void	registerInstance(ImageInstance ii) {
		synchronized(m_lock) {
			m_instanceList.add(ii);
		}
	}
	boolean unregisterInstance(ImageInstance ii) {
		synchronized(m_lock) {
			m_instanceList.remove(ii);
			return m_instanceList.size() == 0;
		}
	}
	int		getInstanceCount() {
		return m_instanceList.size();
	}
	public Object getImageKey() {
		return m_imageKey;
	}

//	public Dimension getOriginalDimension() {
//		return m_originalDimension;
//	}

//	void setOriginalDimension(Dimension originalDimension) {
//		m_originalDimension = originalDimension;
//	}
	String getFilenameBase() {
		return m_filenameBase;
	}
	
}
