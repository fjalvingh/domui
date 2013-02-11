package to.etc.domui.logic;

import javax.annotation.*;

import to.etc.domui.util.*;

public class ObjectIdentifier<T> {
	
	final @Nonnull Class< ? > m_type;
	
	final @Nonnull T m_id; 
	
	public ObjectIdentifier(Class< ? > type, T id) {
		super();
		m_type = type;
		m_id = id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		ObjectIdentifier< T > other = (ObjectIdentifier < T >) obj;
		if(m_type != other.getClass())
			return false;
		if (!DomUtil.isEqual(m_id, other.getId())){
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + m_type.hashCode();
		result = prime * result + m_id.hashCode();
		return result;
	}

	public @Nonnull Class< ? > getType() {
		return m_type;
	}

	public @Nonnull T getId() {
		return m_id;
	}
}
