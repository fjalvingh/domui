package to.etc.domui.annotations;

import java.lang.annotation.*;

/**
 * When present on an UrlPage, this defines the basic data that is needed to have the page be
 * accessible from a menu. This can be used to maintain menu data close to a page. All texts
 * accessed herein
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 3, 2009
 */
@Target(value=ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIMenu {
	/** The class from where we lookup the bundle by name */
	Class<?>	bundleBase() default Object.class;

	/** The name of the message bundle defining the texts for this entry. If not present it uses the global message bundles. */
	String		bundleName() default "";

	String		baseKey() default "";

	/** The unique ID for this page in the menu. If not present the page name will be used as a menu ID. */
	String		menuID() default "";

	/** The bundle key for the page title. This decides the page title in the PageBar, and if labelKey is empty it also defines the menu's label. */
	String		titleKey() default "";

	/** Defines the bundle key for the set of keywords to search for this item. */
	String		searchKey() default "";

	/** Defines the bundle key for a short description of this page, to be used in the menu. */
	String		descKey() default "";

	/** The application-relative URL of the icon that should be used to show on the menu entry and the title bar. If an iconBase is present this name is treated as a classpath resource starting at the base class specified. */
	String		iconName() default "";

	/** When present this indicates that the icon is a classpath resource. The name is looked up relative to this class. */
	Class<?>	iconBase() default Object.class;
}
