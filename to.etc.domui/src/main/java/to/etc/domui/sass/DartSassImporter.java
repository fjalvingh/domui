package to.etc.domui.sass;

import com.sass_lang.embedded_protocol.InboundMessage.ImportResponse.ImportSuccess;
import de.larsgrefer.sass.embedded.importer.CustomImporter;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The importer that a dart-sass process asks to locate the sheets it needs. An importer can only be
 * registered with a compiler and never removed again, so one of these is registered per compiler
 * instance and it delegates to the resolver for the compilation that is currently running on it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
final class DartSassImporter extends CustomImporter {
	@Nullable
	private DartSassResolver m_resolver;

	/**
	 * Set the resolver for the compilation that is about to run, or null when it has finished. The
	 * compiler this importer belongs to is only ever used by one thread at a time, see DartSassCompiler.
	 */
	void setResolver(@Nullable DartSassResolver resolver) {
		m_resolver = resolver;
	}

	@Override
	@Nullable
	public String canonicalize(String url, boolean fromImport) {
		DartSassResolver resolver = m_resolver;
		return null == resolver ? null : resolver.canonicalize(url);
	}

	@Override
	public ImportSuccess handleImport(String url) {
		DartSassResolver resolver = m_resolver;
		ImportSuccess is = null == resolver ? null : resolver.load(url);
		if(null == is)
			throw new SassException("dart-sass asks for '" + url + "' which was not canonicalized by this importer");
		return is;
	}
}
