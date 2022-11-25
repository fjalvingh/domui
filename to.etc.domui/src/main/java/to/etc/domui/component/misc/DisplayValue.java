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
package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.PropertyMetaModel;

/**
 * Deprecated: this control cannot properly align its content: when used in a FormBuilder the
 * rendered values all align to the top of the row which looks terrible. This is because there
 * is no content member inside this control: the value is direct text which cannot be aligned.
 *
 * Use the new DisplayControl class instead. This consists of an inline-flex part which has the
 * required size as defined by the form classes, and has an inner element that is then flex
 * aligned center.
 *
 * If you want an item without any associated style use DisplaySpan.
 *
 * This is a special control which can be used to display all kinds of values. It behaves as a "span" containing some
 * value that can be converted, translated and whatnot. It is meant for not too complex values that are usually
 * represented as a span.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 15, 2010
 */
@Deprecated
public class DisplayValue<T> extends DisplaySpan<T> {
	public DisplayValue(PropertyMetaModel<T> pmm) {
		super(pmm);
		setCssClass("ui-dspv");
	}

	public DisplayValue(PropertyMetaModel<T> pmm, @Nullable T value) {
		super(pmm, value);
		setCssClass("ui-dspv");
	}

	public DisplayValue() {
		setCssClass("ui-dspv");
	}

	public DisplayValue(@NonNull Class<T> valueClass) {
		super(valueClass);
		setCssClass("ui-dspv");
	}

	public DisplayValue(@NonNull Class<T> valueClass, @Nullable T value) {
		super(valueClass, value);
		setCssClass("ui-dspv");
	}

	public DisplayValue(@Nullable T literal) {
		super(literal);
		setCssClass("ui-dspv");
	}
}
