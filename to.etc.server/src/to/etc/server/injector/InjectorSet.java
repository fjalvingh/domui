package to.etc.server.injector;

import java.util.*;

/**
 * A set of rules that are to be executed at injection time.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 11, 2006
 */
final public class InjectorSet {
	private SetterInjector[]	m_list;

	private Class				m_sourceClass;

	public InjectorSet(SetterInjector[] list, Class sourcecl) {
		m_list = list;
		m_sourceClass = sourcecl;
	}

	public InjectorSet(List<SetterInjector> list, Class scl) {
		this(list.toArray(new SetterInjector[list.size()]), scl);
	}

	public Class getSourceClass() {
		return m_sourceClass;
	}

	public ObjectList apply(Object source, Object target) throws Exception {
		Object[] ar = new Object[m_list.length]; // Allot an allocated-object table
		ObjectList ol = new ObjectList(ar, null, m_list);
		boolean ok = false;
		try {
			int i = 0;
			for(SetterInjector si : m_list)
				ar[i++] = si.apply(source, target);
			ok = true;
			return ol;
		} finally {
			if(!ok)
				ol.release();
		}
	}

	public void release(Object[] ar) {
		if(ar == null)
			return;
		for(int i = ar.length; --i >= 0;) {
			try {
				m_list[i].releaseObject(ar[i]);
			} catch(Exception x) {}
		}
	}
}
