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
package to.etc.domui.component.upload;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.misc.MessageFlare;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.Button;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.FileInput;
import to.etc.domui.dom.html.Form;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Page;
import to.etc.domui.parts.ComponentPartRenderer;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.state.ConversationContext;
import to.etc.domui.state.UIContext;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.Msgs;
import to.etc.domui.util.upload.FileUploadException;
import to.etc.domui.util.upload.UploadItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Like {@link FileUpload2}, but allows for multiple files to be uploaded.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 14, 2017
 */
public class FileUploadMultiple extends Div implements IUploadAcceptingComponent, IControl<List<UploadItem>> /* implements IHasChangeListener */ {
	@Nonnull
	private List<String> m_allowedExtensions;

	private int m_maxSize;

	private boolean m_mandatory;

	final private List<UploadItem> m_value = new ArrayList<>();

	private FileInput m_input;

	private IValueChanged< ? > m_onValueChanged;

	private boolean m_disabled;

	private boolean m_readOnly;

	@Nullable
	private String m_buttonText = Msgs.BUNDLE.getString(Msgs.UI_UPLOADMULTI_TEXT);

	@Nullable
	private String m_clearButtonText;

	@Nullable
	private String m_buttonIcon;

	@Nullable
	private String m_clearButtonIcon = FaIcon.faWindowClose;

	public FileUploadMultiple() {
		m_allowedExtensions = new ArrayList<>();
	}

	/**
	 * Create an upload item that accepts a max #of files and a set of extensions.
	 */
	public FileUploadMultiple(@Nonnull List<String> allowedExtensions) {
		m_allowedExtensions = allowedExtensions;
	}

	public FileUploadMultiple(String...allowedExt) {
		m_allowedExtensions = Arrays.asList(allowedExt);
	}

	/**
	 * Renders the presentation: either uploaded or selectable depending on whether a value is present.
	 */
	@Override
	public void createContent() throws Exception {
		addCssClass("ui-fup2 ui-control ctl-has-addons");
		render();
	}

	private void renderSelected() {
		Div valueD = new Div("ui-fup2-value");
		add(valueD);

		//-- render the selected filse as a name
		for(UploadItem item : m_value) {
			Div d = new Div();
			valueD.add(d);
			d.add(item.getRemoteFileName());
		}

		String clearButtonIcon = m_clearButtonIcon;
		if(clearButtonIcon != null) {
			add(new DefaultButton("", clearButtonIcon, b -> clear()));
		} else {
			add(new DefaultButton(m_clearButtonText, b -> clear()));
		}
	}

	private void render() {
		Div valueD = new Div("ui-control ui-input");
		add(valueD);
		if(m_value.size() > 0) {
			valueD.addCssClass("ui-fup2-value");
			for(UploadItem uploadItem : m_value) {
				renderValue(valueD, uploadItem);
			}
		} else {
			valueD.addCssClass("ui-fup2-value-empty ");
		}

		Div btn = new Div("ui-button ui-control");
		add(btn);
		String buttonText = m_buttonText;
		if(null != buttonText) {
			btn.add(buttonText);
		}

		Form f = new Form();
		btn.add(f);
		f.setCssClass("ui-szless");
		f.setEnctype("multipart/form-data");
		f.setMethod("POST");
		StringBuilder sb = new StringBuilder();
		ComponentPartRenderer.appendComponentURL(sb, UploadPart.class, this, UIContext.getRequestContext());
		sb.append("?uniq=" + System.currentTimeMillis()); // Uniq the URL to prevent IE's caching.
		f.setAction(sb.toString());

		FileInput input = m_input = new FileInput();
		f.add(input);
		input.setSpecialAttribute("multiple", "multiple");
		input.setSpecialAttribute("onkeypress", "WebUI.preventIE11DefaultAction(event)");
		input.setSpecialAttribute("onchange", "WebUI.fileUploadChange(event)");
		input.setDisabled(isDisabled() || isReadOnly());
		if(m_allowedExtensions.size() > 0) {
			String values = m_allowedExtensions.stream().map(s -> s.startsWith(".") || s.contains("/") ? s : "." + s).collect(Collectors.joining(","));
			input.setSpecialAttribute("fuallowed", values);
			input.setSpecialAttribute("accept", values);
		}

		int maxSize = getMaxSize();
		if(maxSize <= 0)
			maxSize = 100 * 1024 * 1024;
		input.setSpecialAttribute("fumaxsize", Integer.toString(maxSize));
	}

	private void renderValue(Div valueD, UploadItem uploadItem) {
		Div d = new Div("ui-fup2-item");
		valueD.add(d);
		Button btn = new Button().css("ui-fup2-del");
		d.add(btn);
		btn.add(new FaIcon(FaIcon.faWindowCloseO));
		d.add(uploadItem.getRemoteFileName());
		btn.setClicked(a -> {
			m_value.remove(uploadItem);
			forceRebuild();
		});
	}

	@Nullable @Override protected String getFocusID() {
		FileInput input = m_input;
		return null == input ? null : input.getActualID();
	}

	@Nullable @Override public NodeBase getForTarget() {
		return m_input;
	}


	/**
	 * Internal: get the input type="file" thingy.
	 * @return
	 */
	FileInput getInput() {
		return m_input;
	}

	@Nullable
	public List<UploadItem> getBindValue() {
		validate();
		return m_value;
	}

	public void setBindValue(@Nullable List<UploadItem> value) {
		setValue(value);
	}

	@Override
	@Nullable
	public List<UploadItem> getValue() {
		try {
			validate();
			setMessage(null);
			List<UploadItem> value = m_value;
			if(value == null)
				return Collections.emptyList();
			return Collections.unmodifiableList(value);
		} catch(ValidationException vx) {
			setMessage(UIMessage.error(vx));
			throw vx;
		}
	}

	private void validate() {
		if(m_value == null && isMandatory()) {
			throw new ValidationException(Msgs.BUNDLE, Msgs.MANDATORY);
		}
	}

	@Override public void setValue(@Nullable List<UploadItem> value) {
		if(value == null)
			value = Collections.emptyList();

		if(MetaManager.areObjectsEqual(m_value, value)) {
			return;
		}
		m_value.clear();
		m_value.addAll(value);
		forceRebuild();
	}

	@Override
	@Nullable
	public List<UploadItem> getValueSafe() {
		return m_value;
	}

	public void clear() {
		setValue(Collections.emptyList());
	}

	/**
	 * Return the space separated list of allowed file extensions.
	 * @return
	 */
	public List<String> getAllowedExtensions() {
		return m_allowedExtensions;
	}

	/**
	 * Set the list of allowed file extensions.
	 * @param allowedExtensions
	 */
	public void setAllowedExtensions(List<String> allowedExtensions) {
		if(DomUtil.isEqual(allowedExtensions, m_allowedExtensions))
			return;

		m_allowedExtensions = new ArrayList<>(allowedExtensions);
		changed();
	}

	/**
	 * T if at least 1 file needs to be uploaded.
	 */
	@Override
	public boolean isMandatory() {
		return m_mandatory;
	}

	/**
	 * Set to T if at least one file needs to have been uploaded.
	 * @param required
	 */
	@Override
	public void setMandatory(boolean required) {
		m_mandatory = required;
	}

	@Override
	public IValueChanged< ? > getOnValueChanged() {
		return m_onValueChanged;
	}

	@Override
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}

	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		if(m_disabled == disabled)
			return;
		m_disabled = disabled;
		forceRebuild();
	}

	public int getMaxSize() {
		return m_maxSize;
	}

	public void setMaxSize(int maxSize) {
		m_maxSize = maxSize;
	}

	@Override
	public boolean handleUploadRequest(@Nonnull RequestContextImpl param, @Nonnull ConversationContext conversation) throws Exception {
		try {
			if(isDisabled())
				return true;

			UploadItem[] uiar = param.getFileParameter(getInput().getActualID());
			if(uiar != null) {
				for(UploadItem ui : uiar) {
					m_value.add(ui);
				}
			}
		} catch(FileUploadException fxu) {
			MessageFlare.display(this, fxu.getMessage());
			return true;
		}
		forceRebuild();
		// We need this page reference since in onValueChanged() force rebuild might happen again
		// and then we'll lose the page reference needed for renderOptimalDelta().
		Page p = getPage();
		if(m_onValueChanged != null)
			((IValueChanged<FileUploadMultiple>) m_onValueChanged).onValueChanged(this);
		return true;
	}

	@Override public boolean isReadOnly() {
		return m_readOnly;
	}

	@Override public void setReadOnly(boolean readOnly) {
		if(m_readOnly == readOnly)
			return;
		m_readOnly = readOnly;
		forceRebuild();
	}
}

