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
import to.etc.domui.util.upload.*;

/**
 * This component uses <a href="http://code.google.com/p/swfupload/">swfupload</a> to handle selecting multiple
 * files in a browser and uploading them in here. It consists of a "select files" button and a "cancel" button,
 * with an upload file list below it. The upload file list is managed by Javascript(!) in addition to this
 * component.
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

	public interface IUpload {
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

		DefaultButton b = new DefaultButton("Select Files");	// Overlay button
		buttonBar.add(b);

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
		legend.add("Upload Queue");

		Div outputDiv = new Div();
		wrapper.add(outputDiv);
		outputDiv.setCssClass("ui-bupl-queue");

		//-- Create the upload URL to UploadPart.
		StringBuilder sb = new StringBuilder();
		ComponentPartRenderer.appendComponentURL(sb, UploadPart.class, this, UIContext.getRequestContext());
		sb.append("?uniq=" + System.currentTimeMillis()); // Uniq the URL to prevent IE's stupid caching.
		String url = sb.toString();
		System.out.println("URL  = " + url);

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
	public void handleUploadRequest(@Nonnull RequestContextImpl param, @Nonnull ConversationContext conversation) throws Exception {
		UploadItem[] uiar = param.getFileParameter("filedata");
		if(uiar != null) {
			for(UploadItem ui : uiar) {
				conversation.registerUploadTempFile(ui.getFile());
				m_newItemList.add(ui);
			}
		}
	}

	@Override
	public void componentHandleWebAction(@Nonnull RequestContextImpl ctx, @Nonnull String action) throws Exception {
		if("uploadDone".equals(action)) {
			handleUploadDone();
			return;
		}
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
}
