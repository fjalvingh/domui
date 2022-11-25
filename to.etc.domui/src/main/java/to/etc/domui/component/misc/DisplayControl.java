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
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.converter.IConvertable;
import to.etc.domui.converter.IConverter;
import to.etc.domui.converter.NumericUtil;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IDisplayControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Span;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IRenderInto;
import to.etc.webapp.nls.NlsContext;

/**
 * Similar to DisplaySpan and DisplayValue, this renders a simple value
 * but acts as a control: it has an embedded value span that itself gets
 * centered.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2022/09/05.
 */
public class DisplayControl<T> extends Div implements IDisplayControl<T>, IConvertable<T> {
	//@NonNull
	//private Class<T> m_valueClass;

	@Nullable
	private T m_value;

	/**If the value is to be converted use this converter for it. */
	@Nullable
	private IConverter<T> m_converter;

	@Nullable
	private IRenderInto<T> m_renderer;

	/** The string to display when the field is empty. */
	@Nullable
	private String m_emptyString;

	private Span m_contentSpan = new Span();

	public DisplayControl() {}

	public DisplayControl(PropertyMetaModel<T> pmm) {
		defineFrom(pmm);
		addCssClass("ui-dspctl");
	}

	public DisplayControl(PropertyMetaModel<T> pmm, @Nullable T value) {
		addCssClass("ui-dspctl");
		defineFrom(pmm);
		m_value = value;
	}


	public DisplayControl(@NonNull Class<T> valueClass) {
		this(valueClass, null);
	}

	public DisplayControl(@NonNull Class<T> valueClass, @Nullable T value) {
		m_value = value;
		addCssClass("ui-dspctl");
	}

	public DisplayControl(@Nullable T literal) {
		m_value = literal;
		addCssClass("ui-dspctl");
	}

	/**
	 * Render the content in some way. It uses the following logic:
	 * <ul>
	 *	<li>If the value is null leave the cell with the "empty" value.</li>
	 * </ul>If a converter is present it MUST convert the value, and it's result is shown.</li>
	 */
	@Override
	public void createContent() throws Exception {
		m_contentSpan.removeAllChildren();
		add(m_contentSpan);

		T val = getValue();

		//-- For an empty value put the empty string
		if(val == null) {
			setString(null);
			return;
		}

		//-- If a converter is present it *must* convert the value
		IConverter<T> converter = getConverter();
		if(converter != null) {
			String converted = converter.convertObjectToString(NlsContext.getLocale(), val);
			setString(converted);
			return;
		}

		//-- If a node renderer is set ask it to render content inside me. It is required to render proper info.
		IRenderInto<T> renderer = getRenderer();
		if(renderer != null) {
			renderer.render(m_contentSpan, val); // Ask node renderer.
			if(m_contentSpan.getChildCount() == 0 && m_emptyString != null)
				m_contentSpan.add(m_emptyString);
			return;
		}

		//-- Getting slightly desperate here... Is there a "default converter" that we can use?
		IConverter<T> c = ConverterRegistry.findConverter((Class<T>) val.getClass()); // This version does return null if nothing is found, not a toString converter.
		if(c != null) {
			String converted = c.convertObjectToString(NlsContext.getLocale(), val);
			setString(converted);
			return;
		}

		//-- Ok: full-blown panic now. Let's try to get metadata on the value passed to see if something to show is available there.
		ClassMetaModel cmm = MetaManager.findClassMeta(val.getClass());
		if(cmm.getDomainValues() != null) {
			//-- This is a domain-based value like boolean or enum. Try to use it's default labels.
			String label = cmm.getDomainLabel(NlsContext.getLocale(), val);
			if(label == null)
				label = val.toString();
			setString(label);
			return;
		}

		/*
		 * In utter desperation try to create an INodeContentRenderer from the class meta data; this
		 * will create a toString renderer if all else fails..
		 */
		IRenderInto<T> ncr = (IRenderInto<T>) MetaManager.createDefaultComboRenderer(null, cmm);
		ncr.render(m_contentSpan, val);
		if(m_contentSpan.getChildCount() == 0 && m_emptyString != null) {
			m_contentSpan.setText(m_emptyString);
		}
	}

	private void setString(@Nullable String v) {
		if(v == null)
			v = m_emptyString;
		if(v != null) {
			m_contentSpan.setText(v);
		} else {
			m_contentSpan.removeAllChildren();
		}
	}

	/**
	 * This returns null if no converter has been set. It also returns null if a default converter is used.
	 */
	@Override
	@Nullable
	public IConverter<T> getConverter() {
		return m_converter;
	}

	@Override
	public void setConverter(@Nullable IConverter<T> converter) {
		if(m_renderer != null && converter != null)
			throw new IllegalStateException("You cannot both use a renderer AND a converter. Set the renderer to null before setting a converter.");
		m_converter = converter;
	}

	/**
	 * The content renderer to use. <b>This gets called only if no converter is set</b>.
	 */
	@Nullable
	public IRenderInto<T> getRenderer() {
		return m_renderer;
	}

	public void setRenderer(@Nullable IRenderInto<T> renderer) {
		if(m_renderer == renderer)
			return;
		if(m_converter != null && renderer != null)
			throw new IllegalStateException("You cannot both use a renderer AND a converter. Set the converter to null before setting a renderer.");
		m_renderer = renderer;
		forceRebuild();
	}

	@Nullable
	public String getEmptyString() {
		return m_emptyString;
	}

	public void setEmptyString(@Nullable String emptyString) {
		m_emptyString = emptyString;
	}

	private <V extends Number> void setNumericConfig(PropertyMetaModel<V> pmm) {
		IConverter<V> numericConverter = NumericUtil.createNumericConverter(pmm, pmm.getActualType());
		setConverter((IConverter<T>) numericConverter);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IDisplayControl interface.							*/
	/*--------------------------------------------------------------*/
	@Override
	@Nullable
	public T getValue() {
		return m_value;
	}

	@Override
	public void setValue(@Nullable T v) {
		if(DomUtil.isEqual(m_value, v)) {
			return;
		}
		T oldvalue = m_value;
		m_value = v;
		forceRebuild();
	}

	public void defineFrom(@NonNull PropertyMetaModel<T> pmm) {
		UIControlUtil.configureHint(this, pmm);
		if(Number.class.isAssignableFrom(pmm.getActualType())) {
			setNumericConfig((PropertyMetaModel<? extends Number>) pmm);
		}
	}

	@Nullable
	@Override
	public NodeBase getForTarget() {
		return null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IControl implementation.							*/
	/*--------------------------------------------------------------*/
	@Override
	public IValueChanged<?> getOnValueChanged() {
		return null;
	}

	@Override
	public void setOnValueChanged(IValueChanged<?> onValueChanged) {
		//FIXME 20120802 vmijic - currently we prevent exception throwing since it raises lot of issues in pages that are using this code, introduced by switching readonly instances of components by DisplayValue...
		//throw new UnsupportedOperationException("Display control");
	}

	@Override
	public T getValueSafe() {
		return getValue();
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public void setReadOnly(boolean ro) {
	}

	@Override
	public boolean isDisabled() {
		return false;
	}

	@Override
	public boolean isMandatory() {
		return false;
	}

	@Override
	public void setMandatory(boolean ro) {
	}

	@Override
	public void setDisabled(boolean d) {
	}

	@Override
	public void setHint(String hintText) {
		setTitle(hintText);
	}
}
