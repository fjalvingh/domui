/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.component.meta;

import to.etc.domui.component.input.ComboComponentBase;
import to.etc.domui.component.meta.impl.UndefinedComboDataSet;
import to.etc.domui.util.IComboDataSet;
import to.etc.domui.util.ILabelStringRenderer;
import to.etc.domui.util.IRenderInto;
import to.etc.domui.util.UndefinedLabelStringRenderer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies how an object is shown when presented in a Combo Box. This annotation
 * can be used on a class itself or on a property of a class type; the latter
 * will "override" any class-level definition. Presence of this annotation at property
 * level will also indicate a preference for a combobox over a Lookup form.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 13, 2008
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaCombo {
	/**
	 * Define a class that will generate the data to show in the combo's list-of-values. The
	 * method called must return all data in the order to show because nothing sorts after. When
	 * unset this defaults to a generic "select all" query.
	 *
	 * @return
	 */
	Class< ? extends IComboDataSet< ? >> dataSet() default UndefinedComboDataSet.class;

	/**
	 *
	 * @return
	 */
	Class< ? extends ILabelStringRenderer< ? >> labelRenderer() default UndefinedLabelStringRenderer.class;

	/**
	 * Defines a custom node content renderer for the combobox's OPTION values. This can be used when options need
	 * more than just text, like images or colors for instance. The renderer is responsible for generating the entire
	 * contents of an OPTION tag, and it gets passed the uncooked value to render (as obtained from the {@link ComboComponentBase#getData()}
	 * call). All other options in the annotation are useless once a custom renderer is used!
	 * If this is not used a content renderer is calculated from the other data in this annotation using
	 * {@link MetaManager#createDefaultComboRenderer(PropertyMetaModel, ClassMetaModel)}.
	 * @return
	 */
	Class< ? extends IRenderInto< ? >> nodeRenderer() default UndefinedLabelStringRenderer.class;

	/**
	 * The list of properties that should be shown. This is needed ONLY when the class metadata of the
	 * parent record does not specify a default display column or columnset. This usually shows a single
	 * property (because a combobox is not a table), but if multiple properties are used their values will
	 * be appended to form a single string separated by either a space, or the value of {@link MetaComboProperty#join()}.
	 * @return
	 */
	MetaComboProperty[] properties() default {};

	/**
	 * Define a preference for this being shown as a combobox.
	 * @return
	 */
	boolean preferred() default false;
}
