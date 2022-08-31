package to.etc.log;

import org.eclipse.jdt.annotation.NonNull;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;

/**
 * Internal file tool related utils. It has to be inside logger project to minimize external dependencies for logger project itself.
 * Code found here is pasted from original code existed in FileTool - please don't try to make it same source again ;)
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on May 23, 2014
 */
class LogUtil {
	@NonNull
	static final String readResourceAsString(Class< ? > base, String name, String encoding) throws Exception {
		InputStream is = base.getResourceAsStream(name);
		if(null == is)
			throw new IllegalStateException(base + ":" + name + " resource not found");
		try {
			return readStreamAsString(is, encoding);
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	static String readStreamAsString(final InputStream is, final String enc) throws Exception {
		StringBuilder sb = new StringBuilder(128);
		readStreamAsString(sb, is, enc);
		return sb.toString();
	}

	static void readStreamAsString(final Appendable o, final InputStream f, final String enc) throws Exception {
		Reader r = new InputStreamReader(f, enc);
		readStreamAsString(o, r);
	}

	static void readStreamAsString(final Appendable o, final Reader r) throws Exception {
		char[] buf = new char[4096];
		for(;;) {
			int ct = r.read(buf);
			if(ct < 0)
				break;
			o.append(new String(buf, 0, ct));
		}
	}

	static void readFileAsString(final Appendable o, final File f) throws Exception {
		LineNumberReader lr = new LineNumberReader(new FileReader(f));
		try {
			String line;
			while(null != (line = lr.readLine())) {
				o.append(line);
				o.append("\n");
			}
		} finally {
			lr.close();
		}
	}

	/**
	 * Read a file into a string using the specified encoding.
	 *
	 * @param f
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	static String readFileAsString(final File f, final String encoding) throws Exception {
		InputStream is = null;
		try {
			is = new FileInputStream(f);
			return readStreamAsString(is, encoding);
		} finally {
			if(is != null)
				is.close();
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Directory maintenance and bulk code.				*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns the java.io.tmpdir directory. Throws an exception if it does not exist or
	 * is inaccessible.
	 */
	static public File getTmpDir() {
		String v = System.getProperty("java.io.tmpdir");
		if(v == null)
			v = "/tmp";
		File tmp = new File(v);
		if(!tmp.exists() || !tmp.isDirectory())
			throw new IllegalStateException("The 'java.io.tmpdir' variable does not point to an existing directory (" + tmp + ")");
		return tmp;
	}

	static DocumentBuilderFactory createDocumentBuilderFactory() {
		String feature = null;
		String errMsg = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// to be compliant, completely disable DOCTYPE declaration:
			feature = "http://apache.org/xml/features/disallow-doctype-decl";
			factory.setFeature(feature, true);
			// or completely disable external entities declarations:
			feature = "http://xml.org/sax/features/external-general-entities";
			factory.setFeature(feature, false);
			feature = "http://xml.org/sax/features/external-parameter-entities";
			factory.setFeature(feature, false);
			// or prohibit the use of all protocols by external entities:
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			// or disable entity expansion but keep in mind that this doesn't prevent fetching external entities
			// and this solution is not correct for OpenJDK < 13 due to a bug: https://bugs.openjdk.java.net/browse/JDK-8206132
			factory.setExpandEntityReferences(false);
			return factory;
		} catch (ParserConfigurationException e) {
			// This should catch a failed setFeature feature
			errMsg = "ParserConfigurationException was thrown. The feature '" + feature + "' is probably not supported by your XML processor.";
			System.err.println(errMsg);
			throw new RuntimeException("error in createDocumentBuilderFactory: " + errMsg, e);
		}
	}

	/**
	 * Creates TransformerFactory using high security recommendations by disabling vulnerable factory attributes.
	 * @return Instance of TransformerFactory.
	 */
	public static TransformerFactory createTransformerFactory() {
		TransformerFactory factory = TransformerFactory.newInstance();
		// to be compliant, prohibit the use of all protocols by external entities:
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		return factory;
	}
}
