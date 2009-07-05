package to.etc.domui.component.meta;

import java.lang.annotation.*;

import to.etc.domui.converter.*;
import to.etc.domui.util.*;

/**
 * TEMP indicator on how to display some property; used in lookup and combo
 * definitions. This is part of a working test for metadata, do not use
 * because it can change heavily (or be deleted alltogether).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 13, 2008
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaDisplayProperty {
	/**
	 * The name of the property to show. This will be replaced with a Property references once 
	 * the JDK 7 team gets off it's ass and starts to bloody define something useful.
	 * @return
	 */
	public String name();

	public String defaultLabel() default Constants.NO_DEFAULT_LABEL;

	public SortableType defaultSortable() default SortableType.UNKNOWN;

	public int displayLength() default -1;

	public Class<IConverter> converterClass() default IConverter.class;

	public String join() default Constants.NO_JOIN;

	public YesNoType readOnly() default YesNoType.UNKNOWN;
	//	public DateType				dateType() default DateType.UNKNOWN;
}
