package to.etc.domui.component.upload;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.header.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.parts.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.domui.util.upload.*;

/**
 * This component uses <a href="http://code.google.com/p/swfupload/">swfupload</a> to handle selecting multiple
 * files in a browser and uploading them in here. It consists of a "select files" button and a "cancel" button,
 * with an upload file list below it. The upload file list is managed by Javascript(!) in addition to this
 * component.
 *
 * <p>See <a href="http://www.domui.org/wiki/bin/view/Documentation/DemoBulkUpload">the wiki</a> for a complete description.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 21, 2012
 */
public class BulkUpload extends Div implements IUploadAcceptingComponent {
	/** The list of upload items not yet claimed by the UI code (coming in from Flash event). */
	final private List<UploadItem> m_newItemList = new ArrayList<UploadItem>();

	/** The list of claimed items. */
	final private List<UploadItem> m_itemList = new ArrayList<UploadItem>();

	@Nullable
	private IUpload m_onUpload;

	/** Event handler called when the file(s) have been selected and the upload of the 1st one has started, */
	@Nullable
	private IClicked<BulkUpload> m_onUploadsStarted;

	/** Event handler called when all uploads have completed. */
	@Nullable
	private IClicked<BulkUpload> m_onUploadsComplete;

	private DefaultButton m_startButton;

	/**
	 * Event interface for {@link BulkUpload#getOnUpload()} event.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jun 22, 2012
	 */
	public interface IUpload {
		/**
		 * Called as soon as an upload has completed. When the method returns true the uploaded file will
		 * be deleted immediately to prevent the server from running out of disk space.
		 *
		 * @param item
		 * @return
		 * @throws Exception
		 */
		boolean fileUploaded(UploadItem item) throws Exception;
	}

	public BulkUpload() {
		setCssClass("ui-bupl");
	}

	@Override
	public void createContent() throws Exception {
		Div buttonBar = new Div();
		add(buttonBar);
		Span s = new Span();									// Flash button placeholder: this transparant thing will overlay the button below.
		buttonBar.add(s);

		m_startButton = new DefaultButton(Msgs.BUNDLE.getString(Msgs.BULKUPLD_SELECT_FILES));
		buttonBar.add(m_startButton);

		add(new VerticalSpacer(10));

		Div wrapper = new Div();
		add(wrapper);
		wrapper.setCssClass("ui-bupl-wrp");

		Div ttl = new Div();
		wrapper.add(ttl);
		ttl.setCssClass("ui-bupl-ttl");
		Span legend = new Span();
		ttl.add(legend);
		legend.setCssClass("ui-bupl-legend");
		legend.add(Msgs.BUNDLE.getString(Msgs.BULKUPLD_UPLOAD_QUEUE));

		Div outputDiv = new Div();
		wrapper.add(outputDiv);
		outputDiv.setCssClass("ui-bupl-queue");

		//-- Create the upload URL to UploadPart.
		StringBuilder sb = new StringBuilder();
		ComponentPartRenderer.appendComponentURL(sb, UploadPart.class, this, UIContext.getRequestContext());
		sb.append("?uniq=" + System.currentTimeMillis()); // Uniq the URL to prevent IE's stupid caching.
		String url = sb.toString();
		//		System.out.println("URL  = " + url);

		appendCreateJS("WebUI.bulkUpload('" + getActualID() + "','" + s.getActualID() + "','" + url + "');");
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void onAddedToPage(Page p) {
		//-- Add the required javascript
		getPage().addHeaderContributor(HeaderContributor.loadJavascript("$js/swfupload.js"), 10);
		getPage().addHeaderContributor(HeaderContributor.loadJavascript("$js/jquery.swfupload.js"), 10);
		getPage().addHeaderContributor(HeaderContributor.loadJavascript("$js/swfupload.queue.js"), 10);
		getPage().addHeaderContributor(HeaderContributor.loadJavascript("$js/swfupload.swfobject.js"), 10);
		super.onAddedToPage(p);
	}

	@Nullable
	public IUpload getOnUpload() {
		return m_onUpload;
	}

	public void setOnUpload(IUpload onUpload) {
		m_onUpload = onUpload;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handling callbacks.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Handle an upload completion event. This event is called by the upload that finishes in the Flash
	 * plugin. Hence the response cannot be a delta. We register the added file with the "unseen file list", then
	 * we wait for the DomUI calling us - so we can render a proper response.
	 *
	 * @see to.etc.domui.component.upload.IUploadAcceptingComponent#handleUploadRequest(to.etc.domui.server.RequestContextImpl, to.etc.domui.state.ConversationContext)
	 */
	@Override
	public boolean handleUploadRequest(@Nonnull RequestContextImpl param, @Nonnull ConversationContext conversation) throws Exception {
		UploadItem[] uiar = param.getFileParameter("filedata");
		if(uiar != null) {
			for(UploadItem ui : uiar) {
				conversation.registerTempFile(ui.getFile());
				m_newItemList.add(ui);
			}
		}
		return false;
	}

	@Override
	public void componentHandleWebAction(@Nonnull RequestContextImpl ctx, @Nonnull String action) throws Exception {
		if("uploadDone".equals(action)) {
			handleUploadDone();
		} else if("queueComplete".equals(action)) {
			m_startButton.setDisabled(false);
			m_startButton.setTitle("");

			IClicked<BulkUpload> eh = getOnUploadsComplete();
			if(null != eh)
				eh.clicked(this);
		} else if("queueStart".equals(action)) {
			m_startButton.setDisabled(true);
			m_startButton.setTitle(Msgs.BUNDLE.getString(Msgs.BULKUPLD_DISABLED));
			IClicked<BulkUpload> eh = getOnUploadsStarted();
			if(null != eh)
				eh.clicked(this);
		} else
			super.componentHandleWebAction(ctx, action);
	}

	private void handleUploadDone() throws Exception {
		while(m_newItemList.size() > 0) {
			UploadItem item = m_newItemList.remove(0);
			boolean remove = true;
			IUpload onUpload = getOnUpload();
			if(onUpload != null) {
				//-- We have an upload handler. Pass the file there and be done
				try {
					remove = onUpload.fileUploaded(item);
				} finally {
					if(remove)
						item.getFile().delete();				// Discard.
					else
						m_itemList.add(item);
				}
			}
		}
	}

	/**
	 * Return the list of files uploaded to this control. This contains only those files that the {@link #getOnUpload()} handler returned
	 * "false" for, or all files if there is no onUpload handler at all.
	 * @return
	 */
	public List<UploadItem> getUploadFileList() {
		return new ArrayList<UploadItem>(m_itemList);
	}

	/**
	 * Event handler called when the file(s) have been selected and the upload of the 1st one has started.
	 * @return
	 */
	@Nullable
	public IClicked<BulkUpload> getOnUploadsStarted() {
		return m_onUploadsStarted;
	}

	public void setOnUploadsStarted(@Nullable IClicked<BulkUpload> onUploadsStarted) {
		m_onUploadsStarted = onUploadsStarted;
	}

	/**
	 * Event handler called when all uploads have completed.
	 * @return
	 */
	@Nullable
	public IClicked<BulkUpload> getOnUploadsComplete() {
		return m_onUploadsComplete;
	}

	public void setOnUploadsComplete(@Nullable IClicked<BulkUpload> onUploadsComplete) {
		m_onUploadsComplete = onUploadsComplete;
	}
}
