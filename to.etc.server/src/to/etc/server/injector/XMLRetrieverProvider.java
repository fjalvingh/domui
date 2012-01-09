package to.etc.server.injector;

import java.lang.annotation.*;

import org.w3c.dom.*;

import to.etc.server.ajax.*;
import to.etc.xml.*;

public class XMLRetrieverProvider extends AnnotatedRetrieverProvider {
	@Override
	protected Retriever accepts(Class sourcecl, final Class targetcl, final String name, Annotation[] ann, final AjaxParam ap) {
		if(Node.class.isAssignableFrom(sourcecl)) {
			return new AnnotatedRetriever(Node.class, name, ap) {
				@Override
				protected Object retrieveValuePrimitive(Object source) throws Exception {
					assert (source instanceof Node);
					Node n = (Node) source;

					//-- Is a single attribute with the specified name available?
					if(n.getAttributes() != null && name.length() > 0) {
						Node an = n.getAttributes().getNamedItem(name);
						if(an != null)
							return an;
					}

					//-- If the name is empty we want the root node
					if(name.equals("/") || name.equals("$root") || name.length() == 0)
						return n;

					//-- Nope. Is a single child available with the appropriate name?
					Node value = DomTools.nodeFind(n, name);
					if(value != null)
						return value;
					return NO_VALUE;
				}

				@Override
				public void releaseObject(Object o) {
				}

				public String getDisplayName() {
					return "xmlNode[" + name + "]";
				}
			};
		}
		return null;
	}

}
