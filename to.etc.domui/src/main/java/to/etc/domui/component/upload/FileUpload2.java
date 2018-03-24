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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a file upload thingy which handles ajaxy uploads. This version only allows single file uploads.
 *
 * The basic model is as follows:
 * <ul>
 *	<li>FileUpload components use a hidden iframe to handle the actual uploading in the background.</li>
 *	<li>The background upload gets started as soon as a selection is made.</li>
 *	<li>While the upload is running the user interface is blocking</li>
 *	<li>The uploaded file will be attached as a tempfile to the form's component. It will be deleted
 *		as soon as the context or the page is destroyed.</li>
 *	<li>No upload progress reporting is done.</li>
 * </ul>
 * <h2>Client side upload thingy</h2>
 * <p>In the browser the primary upload thingy is an input type='file' component. This gets used to allow
 * the user to add a file. As soon as a file is selected an onchange event will ensure the posting of
 * the file to the server using an AJAX/IFrame upload. This upload will be received by the Upload part
 * which will attach the file to the control doing the upload.
 * When the upload is complete the input type="file" thing gets replaced by a "file reference" containing
 * the input filename and a delete button.
 * </p>
 * <h3>Important info regarding IE use of iframe: http://p2p.wrox.com/topic.asp?whichpage=1&TOPIC_ID=62981&#153594</h3>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 14, 2017
 */
public class FileUpload2 extends Div implements IUploadAcceptingComponent, IControl<UploadItem> /* implements IHasChangeListener */ {
	@Nonnull
	private List<String> m_allowedExtensions;

	private int m_maxSize;

	private boolean m_mandatory;

	private UploadItem m_value;

	private FileInput m_input;

	private IValueChanged< ? > m_onValueChanged;

	private boolean m_disabled;

	private boolean m_readOnly;

	@Nullable
	private String m_buttonText = Msgs.BUNDLE.getString(Msgs.UI_UPLOAD_TEXT);

	@Nullable
	private String m_clearButtonText;

	@Nullable
	private String m_buttonIcon;

	@Nullable
	private String m_clearButtonIcon = FaIcon.faWindowClose;

	public FileUpload2() {
		m_allowedExtensions = new ArrayList<>();
	}

	/**
	 * Create an upload item that accepts a max #of files and a set of extensions.
	 */
	public FileUpload2(@Nonnull List<String> allowedExtensions) {
		m_allowedExtensions = allowedExtensions;
	}

	public FileUpload2(String...allowedExt) {
		m_allowedExtensions = Arrays.asList(allowedExt);
	}

	/**
	 * Renders the presentation: either uploaded or selectable depending on whether a value is present.
	 */
	@Override
	public void createContent() throws Exception {
		addCssClass("ui-fup2 ctl-has-addons");
		UploadItem value = m_value;
		if(null == value)
			renderEmpty();
		else
			renderSelected(value);
	}

	private void renderSelected(@Nonnull UploadItem value) {
		Div valueD = new Div("ui-fup2-value ui-input");
		add(valueD);

		//-- render the selected file as a name
		valueD.add(value.getRemoteFileName());
		String clearButtonIcon = m_clearButtonIcon;
		if(clearButtonIcon != null) {
			add(new DefaultButton("", clearButtonIcon, b -> clear()));
		} else {
			add(new DefaultButton(m_clearButtonText, b -> clear()));
		}
	}

	private void renderEmpty() {
		if(true) {
			Div valueD = new Div("ui-fup2-value-empty ui-control ui-input");
			add(valueD);

			Div buttonDiv = new Div("ui-fup2-button ui-button ui-control ui-input");
			add(buttonDiv);

			Form f = new Form();
			buttonDiv.add(f);
			f.setCssClass("ui-szless");
			f.setEnctype("multipart/form-data");
			f.setMethod("POST");
			StringBuilder sb = new StringBuilder();
			ComponentPartRenderer.appendComponentURL(sb, UploadPart.class, this, UIContext.getRequestContext());
			sb.append("?uniq=" + System.currentTimeMillis()); // Uniq the URL to prevent IE's caching.
			f.setAction(sb.toString());

			//Div btn = new Div("ui-fup2-button ui-button ui-control");
			Div btn = new Div("ui-fup2-button");
			f.add(btn);
			String buttonText = m_buttonText;
			if(null != buttonText) {
				btn.add(buttonText);
			}

			FileInput input = m_input = new FileInput();
			f.add(input);
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
		} else {
			Form f = new Form();
			add(f);
			f.setCssClass("ui-szless");
			f.setEnctype("multipart/form-data");
			f.setMethod("POST");
			StringBuilder sb = new StringBuilder();
			ComponentPartRenderer.appendComponentURL(sb, UploadPart.class, this, UIContext.getRequestContext());
			sb.append("?uniq=" + System.currentTimeMillis()); // Uniq the URL to prevent IE's caching.
			f.setAction(sb.toString());

			Div valueD = new Div("ui-fup2-value-empty ui-control ui-input");
			f.add(valueD);
			Div btn = new Div("ui-fup2-button ui-button ui-control ui-input");
			f.add(btn);
			String buttonText = m_buttonText;
			if(null != buttonText) {
				btn.add(buttonText);
			}

			FileInput input = m_input = new FileInput();
			f.add(input);
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
	public UploadItem getBindValue() {
		validate();
		return m_value;
	}

	public void setBindValue(@Nullable UploadItem value) {
		setValue(value);
	}

	@Override
	@Nullable
	public UploadItem getValue() {
		try {
			validate();
			setMessage(null);
			return m_value;
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

	@Override public void setValue(@Nullable UploadItem value) {
		if(MetaManager.areObjectsEqual(m_value, value)) {
			return;
		}
		m_value = value;
		forceRebuild();
	}

	@Override
	@Nullable
	public UploadItem getValueSafe() {
		return m_value;
	}

	public void clear() {
		setValue(null);
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
		forceRebuild();
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
					m_value = ui;
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
			((IValueChanged<FileUpload2>) m_onValueChanged).onValueChanged(this);
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

