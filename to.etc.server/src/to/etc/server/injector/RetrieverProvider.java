package to.etc.server.injector;

import java.lang.annotation.*;

public interface RetrieverProvider {
	/**
	 * Checks if this provider can provide a retriever to retrieve the specified name
	 * off the source object. If so it returns the retriever which DOES so, else it
	 * returns null.
	 *
	 * @param sourcecl
	 * @param name
	 * @param pann
	 * @return
	 */
	public Retriever makeRetriever(Class sourcecl, Class targetcl, String name, Annotation[] pann);
}
