package to.etc.log;

import java.util.*;

import org.slf4j.*;

/**
 * CODING - Markers are still not supported.
 * Continue this in case that markers are needed.
 * Current minimal implementation is need just to support slf4j interface, but use of Markers is ignored.  
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 31, 2012
 */
public class EtcMarkerFactory implements IMarkerFactory {
	final Map<String, Marker>	markers		= new HashMap<String, Marker>();

	final Map<String, Marker>	deatached	= new HashMap<String, Marker>();

	/** 
	 * CODING - just basic implementation - has only support for name so far 
	 */
	private static class MyMarker implements Marker {
		final String	m_name;

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
		synchronized(markers) {
			Marker m = markers.remove(arg0);
			if(m != null) {
				deatached.put(arg0, m);
			}
			return true;
		}
	}

	@Override
	public boolean exists(String arg0) {
		synchronized(markers) {
			return markers.containsKey(arg0);
		}
	}

	@Override
	public Marker getDetachedMarker(String arg0) {
		synchronized(deatached) {
			Marker deatachedm = deatached.get(arg0);
			return deatachedm;
		}
	}

	@Override
	public Marker getMarker(final String arg0) {
		synchronized(markers) {
			Marker m = markers.get(arg0);
			if(m != null) {
				return m;
			}
		}
		Marker dm = null;
		synchronized(deatached) {
			dm = deatached.get(arg0);
			if(dm != null) {
				deatached.remove(dm);
			}
		}

		if(dm != null) {
			synchronized(markers) {
				markers.put(arg0, dm);
				return dm;
			}
		} else {
			Marker nm = new MyMarker(arg0);
			synchronized(markers) {
				markers.put(arg0, nm);
				return nm;
			}
		}
	}
}
