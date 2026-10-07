package to.etc.domui.themes;

import org.eclipse.jdt.annotation.NonNull;

/**
 * A variant of the application's theme. The theme itself is fixed at application
 * initialization time (see {@link to.etc.domui.server.DomApplication#setThemeFactory}); the
 * variant is the one thing that can differ per user session: which colour scheme, light or
 * dark, the page is rendered in. DomUI's variants are {@link SchemeVariant}s.
 *
 * <p>A variant's name becomes part of every themed resource URL ($THEME/[variantName]/...),
 * so it must be usable inside an URL path segment. A session keeps its variant as that name
 * only, and gets it back with {@link to.etc.domui.server.DomApplication#findThemeVariant(String)}.</p>
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
	String getColorScheme();
}
