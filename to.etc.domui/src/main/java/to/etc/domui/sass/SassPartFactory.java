package to.etc.domui.sass;

import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.util.resources.*;

import javax.annotation.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-4-17.
 */
final public class SassPartFactory implements IBufferedPartFactory, IUrlPart {
	/**
	 * Accepts .scss resources as sass stylesheets, and passes them through the
	 * sass compiler, returning the result as a normal .css stylesheet.
	 */
	@Override
	public boolean accepts(@Nonnull String rurl) {
		return rurl.endsWith(".scss");
	}

	@Nonnull @Override public Object decodeKey(@Nonnull String rurl, @Nonnull IExtendedParameterInfo param) throws Exception {
		return rurl;
	}

	@Override public void generate(@Nonnull PartResponse pr, @Nonnull DomApplication da, @Nonnull Object key, @Nonnull IResourceDependencyList rdl) throws Exception {



	}
}
