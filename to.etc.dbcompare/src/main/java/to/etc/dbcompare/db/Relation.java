package to.etc.dbcompare.db;

import java.io.*;
import java.util.*;

/**
 * A relation between tables (constraint).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2006
 */
public class Relation implements Serializable {
	private Table			m_parent;

	private Table			m_child;

	private List<FieldPair>	m_pairList	= new ArrayList<FieldPair>();

	private String			m_name;

	public Relation(Table parent, Table child) {
		m_parent = parent;
		m_child = child;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public List<FieldPair> getPairList() {
		return m_pairList;
	}

	public void setPairList(List<FieldPair> pairList) {
		m_pairList = pairList;
	}

	public Table getChild() {
		return m_child;
	}

	public Table getParent() {
		return m_parent;
	}

	public void addPair(Column pkc, Column fkc) {
		m_pairList.add(new FieldPair(pkc, fkc));
	}
}
