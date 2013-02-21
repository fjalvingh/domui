package to.etc.domui.log.data;

import org.w3c.dom.*;

import to.etc.domui.component.meta.*;
import to.etc.log.*;

public class Matcher {
	private String m_name;

	public static final String pNAME = "name";

	private Level m_level;

	public static final String pLEVEL = "level";

	public Matcher(String name, Level level) {
		super();
		m_name = name;
		m_level = level;
	}

	@MetaProperty(length = 255, required = YesNoType.NO)
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	@MetaProperty(required = YesNoType.YES)
	public Level getLevel() {
		return m_level;
	}

	public void setLevel(Level level) {
		m_level = level;
	}

	public void saveToXml(Document doc, Element logNode) {
		logNode.setAttribute("name", m_name != null ? m_name : "");
		logNode.setAttribute("level", m_level.name());
	}
}
