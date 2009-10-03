package to.etc.xml;

import java.util.*;

import org.w3c.dom.*;

class DOMNodeIterator implements Iterator<DOMDecoder>, Iterable<DOMDecoder> {
	//	private Node				m_rootNode;
	private NodeList	m_list;

	private int			m_listIndex	= 0;

	/** The namespace that was used to encode the types herein. */
	private String		m_encodingNamespace;

	/** When not null, this is the namespace to look for values. If the empty string it requires a namespaceless lookup. */
	private String		m_defaultNamespace;

	private String		m_filterTag, m_filterTagNamespace;

	private boolean		m_skipText	= true;

	DOMNodeIterator() {
	}

	DOMNodeIterator(DOMDecoderBase base, Node root, String tagName, String tagNS, boolean skipText) {
		m_defaultNamespace = base.getDefaultNamespace();
		m_encodingNamespace = base.getEncodingNamespace();
		//		m_rootNode = root;
		m_list = root.getChildNodes();
		m_listIndex = findNextAfter(0);
		m_filterTag = tagName;
		m_filterTagNamespace = tagNS;
		m_skipText = skipText;
	}

	DOMNodeIterator(DOMDecoderBase base, Node root) {
		this(base, root, null, null, true);
	}

	private int findNextAfter(int ix) {
		if(m_filterTag == null && !m_skipText)
			return ix + 1;
		while(++ix < m_list.getLength()) {
			Node next = m_list.item(ix);
			if(m_skipText && (next.getNodeType() == Node.TEXT_NODE || next.getNodeType() == Node.COMMENT_NODE || next.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE))
				continue;
			if(m_filterTag == null)
				return ix;
			if(next.getNodeName().equals(m_filterTag) || next.getLocalName().equals(m_filterTag)) {
				if(m_filterTagNamespace == null)
					return ix;
				if(m_filterTagNamespace.equals(next.getNamespaceURI()))
					return ix;
			}
		}
		return ix;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Iterable<T> implementation.							*/
	/*--------------------------------------------------------------*/
	/**
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<DOMDecoder> iterator() {
		return this;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Iterator<T> implementation.							*/
	/*--------------------------------------------------------------*/
	/**
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return m_list != null && m_listIndex < m_list.getLength();
	}

	public DOMDecoder next() {
		if(!hasNext())
			throw new IllegalStateException("No more elements");
		DOMDecoder sli = new DOMDecoder(m_list.item(m_listIndex), m_defaultNamespace, m_encodingNamespace);
		m_listIndex = findNextAfter(m_listIndex);
		return sli;
	}

	public void remove() {
		throw new RuntimeException("Remove not allowed");
	}
}
