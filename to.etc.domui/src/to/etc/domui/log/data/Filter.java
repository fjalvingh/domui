package to.etc.domui.log.data;

import org.w3c.dom.*;

import to.etc.domui.component.meta.*;
import to.etc.log.handler.*;

public class Filter {
	private LogFilterType m_type;

	public static final String pTYPE = "type";

	private String m_key;

	public static final String pKEY = "key";

	private String m_value;

	public static final String pVALUE = "value";

	public Filter(LogFilterType type, String key, String value) {
		super();
		m_type = type;
		m_key = key;
		m_value = value;
	}

	@MetaProperty(length = 20, required = YesNoType.YES)
	public String getKey() {
		return m_key;
	}

	public void setKey(String key) {
		m_key = key;
	}

	@MetaProperty(required = YesNoType.YES)
	public LogFilterType getType() {
		return m_type;
	}

	public void setType(LogFilterType type) {
		m_type = type;
	}

	@MetaProperty(length = 100, required = YesNoType.YES)
	public String getValue() {
		return m_value;
	}

	public void setValue(String value) {
		m_value = value;
	}

	void saveToXml(Document doc, Element filterNode) {
		filterNode.setAttribute("type", m_type.name());
		if(m_type != LogFilterType.SESSION) {
			filterNode.setAttribute("key", m_key);
		}
		filterNode.setAttribute("value", m_value);
	}
}
