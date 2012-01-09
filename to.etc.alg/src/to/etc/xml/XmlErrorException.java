package to.etc.xml;

import org.w3c.dom.*;

public class XmlErrorException extends Exception {
	private String	m_msg;

	private Node	m_tag;

	private Node	m_attr;

	private Object	m_src;

	public XmlErrorException(String msg) {
		m_msg = msg;
	}

	/**
	 * @return Returns the attr.
	 */
	public Node getAttr() {
		return m_attr;
	}


	/**
	 * @param attr The attr to set.
	 */
	public void setAttr(Node attr) {
		m_attr = attr;
	}


	/**
	 * @return Returns the src.
	 */
	public Object getSrc() {
		return m_src;
	}


	/**
	 * @param src The src to set.
	 */
	public void setSrc(Object src) {
		m_src = src;
	}


	/**
	 * @return Returns the tag.
	 */
	public Node getTag() {
		return m_tag;
	}


	/**
	 * @param tag The tag to set.
	 */
	public void setTag(Node tag) {
		m_tag = tag;
	}

	@Override
	public String getMessage() {
		StringBuffer sb = new StringBuffer();
		if(m_src != null) {
			sb.append(m_src.toString());
		}
		if(m_tag != null) {
			if(sb.length() > 0)
				sb.append('\n');
			sb.append("Tag ");
			XmlReader.nodePath(sb, m_tag);
		}
		if(m_attr != null) {
			if(sb.length() > 0)
				sb.append('\n');
			sb.append("attribute ");
			sb.append(m_attr.getNodeName());
		}
		if(sb.length() > 0)
			sb.append("\n");
		sb.append(m_msg);
		return sb.toString();
	}
}
