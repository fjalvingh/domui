package to.etc.server.injector;

import java.lang.annotation.*;

import to.etc.server.ajax.*;

abstract public class AnnotatedRetrieverProvider implements RetrieverProvider {
	abstract protected Retriever accepts(Class sourcecl, Class targetcl, String name, Annotation[] ann, AjaxParam ap);

	final public Retriever makeRetriever(Class sourcecl, Class targetcl, String name, Annotation[] pann) {
		AjaxParam ap = findAjaxParam(pann);
		if(name == null) // No name?
		{
			if(ap == null) // And no annotation?
				return null; // Then do not accept
			name = ap.value(); // Else use annotation's name
		}
		return accepts(sourcecl, targetcl, name, pann, ap);
	}

	static private final AjaxParam findAjaxParam(Annotation[] ar) {
		for(Annotation a : ar) {
			if(a instanceof AjaxParam)
				return (AjaxParam) a;
		}
		return null;
	}
}
