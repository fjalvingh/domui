package to.etc.xml;

/**
 * Well-known namespaces.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 28, 2009
 */
final public class XMLNameSpaces {
	private XMLNameSpaces() {
	}

	static public final String	SOAP1_1				= "http://schemas.xmlsoap.org/soap/envelope/";

	static public final String	SOAP_WSDL_SOAP		= "http://schemas.xmlsoap.org/wsdl/soap/";

	static public final String	SOAP_WSDL			= "http://schemas.xmlsoap.org/wsdl/";

	static public final String	SOAP1_2				= "http://schemas.xmlsoap.org/wsdl/soap12/";

	static public final String	SOAP_ENCODING		= "http://schemas.xmlsoap.org/soap/encoding/";

	static public final String	XMLSCHEMA			= "http://www.w3.org/2001/XMLSchema";

	static public final String	XMLSCHEMA_INSTANCE	= "http://www.w3.org/2001/XMLSchema-instance";

	/**
	 * The mother of all namespaces bound by definition to the prefix xml
	 */
	static public final String	XMLNAMESPACE		= "http://www.w3.org/XML/1998/namespace";

	static public final String	XHTML1_0			= "http://www.w3.org/1999/xhtml";

}
