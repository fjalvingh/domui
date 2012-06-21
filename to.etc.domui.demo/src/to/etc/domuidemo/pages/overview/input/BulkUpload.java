package to.etc.domuidemo.pages.overview.input;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.component.upload.*;
import to.etc.domui.dom.header.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.parts.*;
import to.etc.domui.state.*;

/**
 * This component uses <a href="http://code.google.com/p/swfupload/">swfupload</a> to handle selecting multiple
 * files in a browser and uploading them in here. It consists of a "select files" button and a "cancel" button,
 * with an upload file list below it. The upload file list is managed by Javascript(!) in addition to this
 * component.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 21, 2012
 */
public class BulkUpload extends Div {
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

		Div outputDiv = new Div();
		add(outputDiv);
		outputDiv.setCssClass("ui-bupl-queue");
		Span legend = new Span();
		outputDiv.add(legend);
		legend.setCssClass("ui-bupl-legend");
		legend.add("Upload Queue");

		//-- Create the upload URL to UploadPart.
		StringBuilder sb = new StringBuilder();
		ComponentPartRenderer.appendComponentURL(sb, UploadPart.class, this, UIContext.getRequestContext());
		sb.append("?uniq=" + System.currentTimeMillis()); // Uniq the URL to prevent IE's stupid caching.
		appendCreateJS("WebUI.bulkUpload('" + getActualID() + "','" + s.getActualID() + "','" + sb.toString() + "');");
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

}
