package to.etc.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import to.etc.function.IExecute;
import to.etc.util.WrappedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-3-2018.
 */
public class StackedContentHandler implements ContentHandler {
	private final List<Level> m_stack = new ArrayList<>();

	private final List<Elem> m_elementStack = new ArrayList<>();

	private final Map<String, IExecute> m_onPathMap = new HashMap<>();

	public StackedContentHandler() {
		addHandler(new ContentHandler() {
			@Override public void setDocumentLocator(Locator locator) {
			}

			@Override public void startDocument() throws SAXException {
			}

			@Override public void endDocument() throws SAXException {
			}

			@Override public void startPrefixMapping(String prefix, String uri) throws SAXException {
			}

			@Override public void endPrefixMapping(String prefix) throws SAXException {
			}

			@Override public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
			}

			@Override public void endElement(String uri, String localName, String qName) throws SAXException {
			}

			@Override public void characters(char[] ch, int start, int length) throws SAXException {
			}

			@Override public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
			}

			@Override public void processingInstruction(String target, String data) throws SAXException {
			}

			@Override public void skippedEntity(String name) throws SAXException {
			}
		}, () -> {});
		level().m_level = 1;
	}

	@Override public void setDocumentLocator(Locator locator) {
		level().getContentHandler().setDocumentLocator(locator);
	}

	@Override public void startDocument() throws SAXException {
		level().getContentHandler().startDocument();
	}

	@Override public void endDocument() throws SAXException {
		level().getContentHandler().endDocument();
	}

	@Override public void startPrefixMapping(String prefix, String uri) throws SAXException {
		level().getContentHandler().startPrefixMapping(prefix, uri);
	}

	@Override public void endPrefixMapping(String prefix) throws SAXException {
		level().getContentHandler().endPrefixMapping(prefix);
	}

	@Override public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		try {
			Elem elem = new Elem(uri, localName, qName, atts);
			m_elementStack.add(elem);

			String currentPath = m_elementStack.stream().map(a -> a.getLocalName()).collect(Collectors.joining("/"));
			IExecute execute = m_onPathMap.get(currentPath);
			if(null != execute) {
				execute.execute();
			}

			//-- Called on the NEW handler.
			level().m_level++;
			level().getElemStack().add(elem);
			level().getContentHandler().startElement(uri, localName, qName, atts);
		} catch(SAXException x) {
			throw x;
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	@Override public void endElement(String uri, String localName, String qName) throws SAXException {
		Level level = level();
		level.getContentHandler().endElement(uri, localName, qName);
		m_elementStack.remove(m_elementStack.size() - 1);

		if(0 == --level.m_level) {
			m_stack.remove(m_stack.size() - 1);
			try {
				level.getOnFinished().execute();
			} catch(SAXException x) {
				throw x;
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}
		}
		level.getElemStack().remove(level.getElemStack().size() - 1);
	}

	@Override public void characters(char[] ch, int start, int length) throws SAXException {
		level().getContentHandler().characters(ch, start, length);
	}

	@Override public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		level().getContentHandler().ignorableWhitespace(ch, start, length);
	}

	@Override public void processingInstruction(String target, String data) throws SAXException {
		level().getContentHandler().processingInstruction(target, data);
	}

	@Override public void skippedEntity(String name) throws SAXException {
		level().getContentHandler().skippedEntity(name);
	}

	private Level level() {
		return m_stack.get(m_stack.size() - 1);
	}

	public void addHandler(ContentHandler ch, IExecute onFinished) {
		m_stack.add(new Level(ch, onFinished));
	}

	public void addPathHandler(String path, IExecute onPath) {
		m_onPathMap.put(path, onPath);
	}

	static private final class Level {
		private ContentHandler m_contentHandler;

		private IExecute m_onFinished;

		private int m_level;

		private final List<Elem> m_elemStack = new ArrayList<>();

		public Level(ContentHandler contentHandler, IExecute onFinished) {
			m_contentHandler = contentHandler;
			m_onFinished = onFinished;
		}

		public ContentHandler getContentHandler() {
			return m_contentHandler;
		}

		public IExecute getOnFinished() {
			return m_onFinished;
		}

		public List<Elem> getElemStack() {
			return m_elemStack;
		}
	}

	static public final class Elem {
		private final String m_uri;

		private final String m_localName;

		private final String m_qName;

		private final Attributes m_atts;

		public Elem(String uri, String localName, String qName, Attributes atts) {
			m_uri = uri;
			m_localName = localName;
			m_qName = qName;
			m_atts = atts;
		}

		public String getUri() {
			return m_uri;
		}

		public String getLocalName() {
			return m_localName;
		}

		public String getqName() {
			return m_qName;
		}

		public Attributes getAtts() {
			return m_atts;
		}
	}
}
