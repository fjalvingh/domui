package to.etc.domui.fts;

import java.util.*;

/**
 * Pool of charbufs.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 12, 2008
 */
public class CharBufPool {
	private List<CharBuf>		m_free;
	private int					m_nAlloc;

	public CharBuf	getBuf() {
		if(m_free.size() > 0)
			return m_free.remove(m_free.size()-1);
		m_nAlloc++;
		return new CharBuf();
	}

	public void		release(CharBuf b) {
		m_free.add(b);
	}
}
