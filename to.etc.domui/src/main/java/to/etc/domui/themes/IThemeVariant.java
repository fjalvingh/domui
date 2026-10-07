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
	 * What a user is offered this variant as, when the application lets them choose from
	 * {@link to.etc.domui.server.DomApplication#getThemeVariants()}.
	 */
	@NonNull
	default String getLabel() {
		return getVariantName();
	}

	/**
	 * The CSS <code>color-scheme</code> this variant renders in: "light" or "dark". It is
	 * written into the page head as a meta tag, before the stylesheet link, so that the
	 * browser paints the right canvas - and picks the right scrollbars and native control
	 * colours - from the moment the document is committed, instead of showing its default
	 * white until the theme's stylesheet has arrived and been parsed.
	 */
	@NonNull
	default String getColorScheme() {
		return "light";
	}

	/**
	 * Create a variant with the name passed. Define these as constants:
	 * <pre>
	 *	static public final IThemeVariant DARK = IThemeVariant.of("dark");
	 * </pre>
	 *
	 * <p>The two variants that DomUI itself ships map back onto their instances: a variant
	 * survives a session as its name only ({@link to.etc.domui.server.IRequestContext#getThemeVariant()}
	 * reconstructs it with this method), so anything a variant knows besides its name - its
	 * colour scheme - would be lost on the next request otherwise. For the same reason a
	 * name starting with {@link DarkSchemeVariant#PREFIX} becomes a {@link DarkSchemeVariant}.
	 * The request context restores a session's variant with
	 * {@link to.etc.domui.server.DomApplication#findThemeVariant(String)}, which returns the
	 * application's own instance when it offers one by that name.</p>
	 */
	@NonNull
	static IThemeVariant of(@NonNull String name) {
		if(name.isEmpty() || name.indexOf('/') != -1)
			throw new IllegalArgumentException("Bad theme variant name: '" + name + "'");
		if(DefaultThemeVariant.INSTANCE.getVariantName().equals(name))
			return DefaultThemeVariant.INSTANCE;
		if(DarkThemeVariant.INSTANCE.getVariantName().equals(name))
			return DarkThemeVariant.INSTANCE;
		if(name.startsWith(DarkSchemeVariant.PREFIX) && name.length() > DarkSchemeVariant.PREFIX.length()) {
			String scheme = name.substring(DarkSchemeVariant.PREFIX.length());
			return new DarkSchemeVariant(scheme, scheme);
		}
		return () -> name;
	}
}
