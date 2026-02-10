package to.etc.log;

import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * CODING - Markers are still not supported.
 * Continue this in case that markers are needed.
 * Current minimal implementation is need just to support slf4j interface, but use of Markers is ignored.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 31, 2012
 */
public class EtcMarkerFactory implements IMarkerFactory {
	final Map<String, Marker> m_markerMap = new HashMap<>();

	final Map<String, Marker> m_detachedMap = new HashMap<>();

	/**
	 * CODING - just basic implementation - has only support for name so far
	 */
	private static class MyMarker implements Marker {
		final String m_name;

		MyMarker(String name) {
			m_name = name;
		}

		@Override
		public void add(Marker arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean contains(Marker arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean contains(String arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public String getName() {
			return m_name;
		}

		@Override
		public boolean hasChildren() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean hasReferences() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Iterator iterator() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean remove(Marker arg0) {
			// TODO Auto-generated method stub
			return false;
		}
	}

	@Override
	public boolean detachMarker(String arg0) {
		synchronized(m_markerMap) {
			Marker m = m_markerMap.remove(arg0);
			if(m != null) {
				m_detachedMap.put(arg0, m);
			}
			return true;
		}
	}

	@Override
	public boolean exists(String arg0) {
		synchronized(m_markerMap) {
			return m_markerMap.containsKey(arg0);
		}
	}

	@Override
	public Marker getDetachedMarker(String arg0) {
		synchronized(m_detachedMap) {
			Marker deatachedm = m_detachedMap.get(arg0);
			return deatachedm;
		}
	}

	@Override
	public Marker getMarker(final String arg0) {
		synchronized(m_markerMap) {
			Marker m = m_markerMap.get(arg0);
			if(m != null) {
				return m;
			}
		}
		Marker dm;
		synchronized(m_detachedMap) {
			dm = m_detachedMap.remove(arg0);
		}

		if(dm != null) {
			synchronized(m_markerMap) {
				m_markerMap.put(arg0, dm);
				return dm;
			}
		} else {
			Marker nm = new MyMarker(arg0);
			synchronized(m_markerMap) {
				m_markerMap.put(arg0, nm);
				return nm;
			}
		}
	}
}
