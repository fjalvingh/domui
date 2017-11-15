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

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.parts.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.domui.util.upload.*;

import javax.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Please use {@link FileUpload2} or {@link FileUploadMultiple}.
 *
 * Represents a file upload thingy which handles ajaxy uploads. The basic model
 * is as follows:
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
 * the input filename and a delete button. If the input thingy allows for multiple files to be uploaded
 * another input type="file" gets added above that.
 * </p>
 * <h3>Important info regarding IE use of iframe: http://p2p.wrox.com/topic.asp?whichpage=1&TOPIC_ID=62981&#153594</h3>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 13, 2008
 */
@Deprecated
public class FileUpload extends Div implements IUploadAcceptingComponent, IControl<UploadItem> /* implements IHasChangeListener */ {
	@Nonnull
	private List<String> m_allowedExtensions;

	private int m_maxSize;

	private boolean m_mandatory;

	private int m_maxFiles = 1;

	private List<UploadItem> m_files = new ArrayList<UploadItem>();

	private FileInput m_input;

	private IValueChanged< ? > m_onValueChanged;

	private boolean m_disabled;

	private boolean m_readOnly;

	public FileUpload() {
		m_allowedExtensions = new ArrayList<>();
	}

	/**
	 * Create an upload item that accepts a max #of files and a set of extensions.
	 */
	public FileUpload(int maxfiles, @Nonnull List<String> allowedExtensions) {
		m_maxFiles = maxfiles;
		m_allowedExtensions = allowedExtensions;
	}

	public FileUpload(String...allowedExt) {
		m_maxFiles = 1;
		m_allowedExtensions = Arrays.asList(allowedExt);
	}


	/**
	 * Renders the presentation.
	 */
	@Override
	public void createContent() throws Exception {
		renderSelectedFiles();
	}

	@Nullable @Override protected String getFocusID() {
		FileInput input = m_input;
		return null == input ? null : input.getActualID();
	}

	@Nullable @Override public NodeBase getForTarget() {
		return m_input;
	}


	private void renderSelectedFiles() {
		Table t = new Table();
		t.addCssClass("ui-fu-selected");
		add(t);
		TBody b = new TBody();
		t.add(b);

		if(!isFull()) {
			b.addRow();
			TD td = b.addCell();
			td.setColspan(2);

			Form f = new Form();
			td.add(f);
			f.setCssClass("ui-szless");
			f.setEnctype("multipart/form-data");
			f.setMethod("POST");
			StringBuilder sb = new StringBuilder();
			ComponentPartRenderer.appendComponentURL(sb, UploadPart.class, this, UIContext.getRequestContext());
			sb.append("?uniq=" + System.currentTimeMillis()); // Uniq the URL to prevent IE's caching.
			f.setAction(sb.toString());

			FileInput fi = new FileInput();
			f.add(fi);
			// Prevent IE 11 to submit form on keypress on file input
			fi.setSpecialAttribute("onkeypress", "WebUI.preventIE11DefaultAction(event)");
			fi.setSpecialAttribute("onchange", "WebUI.fileUploadChange(event)");
			fi.setDisabled(isDisabled() || isReadOnly());
			if(m_allowedExtensions.size() > 0) {
				String values = m_allowedExtensions.stream().map(s -> s.startsWith(".") || s.contains("/") ? s : "." + s).collect(Collectors.joining(","));
				fi.setSpecialAttribute("fuallowed", values);
				fi.setSpecialAttribute("accept", values);
			}
			//			fi.setSpecialAttribute("fumaxsz", Integer.toString(m_maxSize));
			int maxSize = getMaxSize();
			if(maxSize <= 0)
				maxSize = 100*1024*1024;
			fi.setSpecialAttribute("fumaxsize", Integer.toString(maxSize));
			m_input = fi;
		}
		for(final UploadItem ufi : m_files) {
			b.addRow();
			TD td = b.addCell();
			td.setText(ufi.getRemoteFileName() + " (" + ufi.getContentType() + ")");
			td = b.addCell();
			if(!isDisabled() && ! isReadOnly()) {
				td.add(new DefaultButton(Msgs.BUNDLE.getString("upld.delete"), "THEME/btnDelete.png", new IClicked<DefaultButton>() {
					@Override
					public void clicked(@Nonnull DefaultButton bx) throws Exception {
						removeUploadItem(ufi);
						if(m_onValueChanged != null)
							((IValueChanged<FileUpload>) m_onValueChanged).onValueChanged(FileUpload.this);
					}
				}));
			}
		}
	}

	/**
	 * Internal: get the input type="file" thingy.
	 * @return
	 */
	FileInput getInput() {
		return m_input;
	}

	/**
	 * Return the current value: the list of files that have been uploaded and
	 * their related data. The {@link UploadItem} contains a reference to the
	 * actual file {@link UploadItem#getFile()}; this file remains present only
	 * while the page is still alive. If the page is destroyed all of it's uploaded
	 * files will be deleted. So if you need to retain the file somehow after upload
	 * it's contents needs to be <b>copied</b> to either another file that you control
	 * or to a BLOB in a database.
	 * @return
	 */
	@Nonnull
	public List<UploadItem> getFiles() {
		return m_files;
	}

	@Override
	@Nullable
	public UploadItem getValue() {
		if(m_maxFiles != 1)
			throw new IllegalStateException("Can only be called for max files = 1");
		if(m_files.size() == 0) {
			if(isMandatory()) {
				setMessage(UIMessage.error(Msgs.BUNDLE, Msgs.MANDATORY));
				throw new ValidationException(Msgs.BUNDLE, Msgs.MANDATORY);
			}
			clearMessage();
			return null;
		}
		clearMessage();
		return m_files.get(0);
	}

	@Override public void setValue(@Nullable UploadItem v) {
		if(null == v) {
			clear();
			return;
		}
		if(m_files.size() > 1 || m_files.get(0) != v) {
			clear();
			m_files.add(v);
			forceRebuild();
		}
	}

	@Override
	@Nullable
	public UploadItem getValueSafe() {
		if(m_maxFiles != 1)
			throw new IllegalStateException("Can only be called for max files = 1");
		if(m_files.size() == 0)
			return null;
		return m_files.get(0);
	}

	/**
	 * Return T if the max. #of files has been reached.
	 * @return
	 */
	public boolean isFull() {
		return m_files.size() >= m_maxFiles;
	}

	/**
	 * Removes specified upload item.
	 * @param ufi
	 */
	public void removeUploadItem(UploadItem ufi) {
		// FIXME We allow the case where the UploadItem is not in the list.... Is that correct or should an exception be thrown?
		if(m_files.remove(ufi))
			forceRebuild();
	}

	public void removeAllUploads() {
		if(m_files.size() == 0)
			return;
		m_files.clear();
		forceRebuild();
	}

	public void clear() {
		removeAllUploads();
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

	//	public int getMaxSize() {
	//		return m_maxSize;
	//	}
	//
	//	public void setMaxSize(int maxSize) {
	//		m_maxSize = maxSize;
	//	}

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

	public int getMaxFiles() {
		return m_maxFiles;
	}

	public void setMaxFiles(int maxFiles) {
		m_maxFiles = maxFiles;
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
					getFiles().add(ui);
					conversation.registerTempFile(ui.getFile());
				}
			}
		} catch(FileUploadException fxu) {
			MessageFlare.display(this, fxu.getMessage());
			return true;
		}
		forceRebuild();
		// We need this page reference since in onValueChanged() force rebuild might happen again
		// and then we'll lose the page reference neede for renderOptimalDelta().
		Page p = getPage();
		if(m_onValueChanged != null)
			((IValueChanged<FileUpload>) m_onValueChanged).onValueChanged(this);
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

