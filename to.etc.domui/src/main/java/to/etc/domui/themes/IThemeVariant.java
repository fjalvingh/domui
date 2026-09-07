package to.etc.domui.themes;

import org.eclipse.jdt.annotation.NonNull;

/**
 * A variant of the application's theme. The theme itself is fixed at application
 * initialization time (see {@link to.etc.domui.server.DomApplication#setThemeFactory}); the
 * variant is the one thing that can differ per user session, and it is what makes a
 * dark/light switch possible.
 *
 * <p>A variant is nothing but a name. It becomes part of every themed resource URL
 * ($THEME/[variantName]/...), so it must be usable inside an URL path segment.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 9/2/15.
 */
public interface IThemeVariant {
	@NonNull String getVariantName();

	/**
	 * Create a variant with the name passed. Define these as constants:
	 * <pre>
	 *	static public final IThemeVariant DARK = IThemeVariant.of("dark");
	 * </pre>
	 */
	@NonNull
	static IThemeVariant of(@NonNull String name) {
		if(name.isEmpty() || name.indexOf('/') != -1)
			throw new IllegalArgumentException("Bad theme variant name: '" + name + "'");
		if(DefaultThemeVariant.INSTANCE.getVariantName().equals(name))
			return DefaultThemeVariant.INSTANCE;
		return () -> name;
	}
}
