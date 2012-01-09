package to.etc.xml;

import org.w3c.dom.*;

/**
 * Helper class to marshal and unmarshal SOAP encoded nodes (using SOAP encoding http://www.w3.org/2001/XMLSchema).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 24, 2009
 */
public class DOMDecoder extends DOMDecoderBase {
	public DOMDecoder() {
	}

	public DOMDecoder(Node currentRoot, String defaultNamespace, String encodingNamespace) {
		super(currentRoot, defaultNamespace, encodingNamespace);
	}

	public DOMDecoder(Node root) {
		super(root);
	}

	public DOMDecoder(String encodingNamespace) {
		super(encodingNamespace);
	}

}
