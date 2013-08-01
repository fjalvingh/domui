package to.etc.launcher.misc;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Specific utils related to ParallelTestLauncher tool.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Aug 1, 2013
 */
public class XmlHelper {

	private static XmlHelper	m_instance	= new XmlHelper();

	private DocumentBuilder		m_builder;

	private XmlHelper() {
	}

	public static @Nonnull
	XmlHelper getInstance() {
		return m_instance;
	}

	public synchronized DocumentBuilder getBuilder() throws ParserConfigurationException {
		if(m_builder == null) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			m_builder = factory.newDocumentBuilder();
		}
		return m_builder;
	}

	public @Nonnull
	Document parseFile(@Nonnull File file) throws SAXException, IOException, ParserConfigurationException {
		return getBuilder().parse(file);
	}

	public @Nonnull
	Document parseString(@Nonnull String xmlSource) throws SAXException, IOException, ParserConfigurationException {
		InputStream is = new ByteArrayInputStream(xmlSource.getBytes());
		try {
			return getBuilder().parse(is);
		} finally {
			is.close();
		}
	}

	public @Nullable
	Node locateDirectChild(@Nonnull Element element, @Nonnull String tagName) {
		NodeList codes = element.getChildNodes();
		for(int index = 0; index < codes.getLength(); index++) {
			if(tagName.equals(codes.item(index).getNodeName())) {
				return codes.item(index);
			}
		}
		return null;
	}

	public @Nullable
	List<Node> locateDirectChilds(@Nonnull Element element, @Nonnull String tagName) {
		ArrayList<Node> res = new ArrayList<Node>();
		NodeList codes = element.getChildNodes();
		for(int index = 0; index < codes.getLength(); index++) {
			if(tagName.equals(codes.item(index).getNodeName())) {
				res.add(codes.item(index));
			}
		}
		return res;
	}

	public void saveToFile(@Nonnull Document doc, @Nonnull File file) throws Exception {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		//transformer.setParameter(OutputKeys.INDENT, "yes");
		//transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		Result output = new StreamResult(file);
		Source input = new DOMSource(doc);
		transformer.transform(input, output);
	}


}
