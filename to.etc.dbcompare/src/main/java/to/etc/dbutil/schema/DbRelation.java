package to.etc.dbutil.schema;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A relation between tables (constraint).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2006
 */
public class DbRelation implements Serializable {
	@Nonnull
	final private DbTable m_parent;

	@Nonnull
	final private DbTable m_child;

	@Nonnull
	final private List<FieldPair> m_pairList = new ArrayList<FieldPair>();

	private String m_name;

	public DbRelation(@Nonnull DbTable parent, @Nonnull DbTable child) {
		m_parent = parent;
		m_child = child;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	@Nonnull
	public List<FieldPair> getPairList() {
		return m_pairList;
	}

	@Nonnull
	public DbTable getChild() {
		return m_child;
	}

	@Nonnull
	public DbTable getParent() {
		return m_parent;
	}

	public void addPair(@Nonnull DbColumn pkc, @Nonnull DbColumn fkc) {
		m_pairList.add(new FieldPair(pkc, fkc));
	}

	@Override public int hashCode() {
		return Objects.hash(m_parent, m_child, m_pairList, m_name);
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		DbRelation other = (DbRelation) obj;
		if(m_child != other.m_child)
			return false;
		if(m_parent != other.m_parent)
			return false;
		if(m_name == null) {
			if(other.m_name != null)
				return false;
		} else if(!m_name.equals(other.m_name))
			return false;

		if(m_pairList.size() != other.m_pairList.size())
			return false;

		for(int i = m_pairList.size(); --i >= 0;) {
			FieldPair fa = m_pairList.get(i);
			FieldPair fb = other.m_pairList.get(i);
			if(!fa.equals(fb))
				return false;
		}
		return true;
	}
}
