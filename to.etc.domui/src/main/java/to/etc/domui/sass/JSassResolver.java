package to.etc.domui.sass;

import io.bit3.jsass.importer.Import;
import io.bit3.jsass.importer.Importer;
import to.etc.domui.parts.ParameterInfoImpl;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.util.WrappedException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
class JSassResolver extends AbstractSassResolver<Import> implements Importer {
	private final String m_basePath;

	public JSassResolver(ParameterInfoImpl params, String basePath, IResourceDependencyList rdl) {
		super(params, rdl);
		m_basePath = basePath;
	}

	@Override public Collection<Import> apply(String s, Import anImport) {
		Import resolve = resolve(s, anImport.getAbsoluteUri().toString());
		if(null == resolve)
			throw new WrappedException(new IOException((s + ": sass import not found")));	// CHECKED EXCEPTIONS SUCK
		return Collections.singletonList(resolve);
	}

	@Override protected Import createInput(String name, String content) {
		try {
			return new Import(name, name, content);
		} catch(URISyntaxException e) {
			throw WrappedException.wrap(e);
		}
	}
}
