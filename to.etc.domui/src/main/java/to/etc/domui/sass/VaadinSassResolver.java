package to.etc.domui.sass;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.resolver.ScssStylesheetResolver;
import org.w3c.css.sac.InputSource;
import to.etc.domui.parts.ParameterInfoImpl;
import to.etc.domui.util.resources.IResourceDependencyList;

import javax.annotation.Nonnull;

/**
 * Resolves sass resources using DomUI's resolution mechanisms, and tracks
 * the resources used for auto recompile.
 */
final class VaadinSassResolver extends AbstractSassResolver<InputSource> implements ScssStylesheetResolver {
	public VaadinSassResolver(@Nonnull IResourceDependencyList dependencyList, @Nonnull String basePath, @Nonnull ParameterInfoImpl params) {
		super(params, dependencyList);
	}

	@Override protected InputSource createInput(String path, String data) {
		return new InputSource(data);
	}

	/**
	 * Resolve scss paths. Quite the mess, as this also resolves all the myriad ways that a @import xxx
	 * can mangle the xxx to get some result.
	 * <ul>
	 *	<li>If the path has </li>
	 * </ul>
	 */
	@Override public InputSource resolve(ScssStylesheet parentStylesheet, String original) {
		String fileBase = parentStylesheet.getFileName();    // not a file name at all; a path. Sigh.
		int ixof = fileBase.lastIndexOf("/");
		if(ixof > 0) {
			fileBase = fileBase.substring(0, ixof);            // Base directory exclusive final slash
		}

		return resolve(original, fileBase);
	}
}
