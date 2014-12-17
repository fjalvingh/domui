package to.etc.domui.component.image;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.upload.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.parts.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.domui.util.upload.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;

import javax.annotation.*;
import java.io.*;

/**
 * This control allows selecting a single image as an upload. The image is
 * stored in VpBlob. The uploaded image is resized to a max. size which can
 * be specified and shown as a thumbnail of (max) 32x32. All images are
 * saved as png.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 5, 2014
 */
public class ImageSelectControl extends Div implements IUploadAcceptingComponent, IControl<IUIImage> {
	static private final BundleRef BUNDLE = BundleRef.create(ImageSelectControl.class, "messages");

	@Nonnull
	private Dimension m_displayDimensions = new Dimension(32, 32);

	@Nonnull
	private Dimension m_maxDimensions = new Dimension(1024, 1024);

	@Nullable
	private IUIImage m_value;

	private boolean m_disabled;

	private boolean m_mandatory;

	@Nullable
	private FileInput m_input;

	private boolean m_readOnly;

	public ImageSelectControl(@Nullable IUIImage value) {
		m_value = value;
	}

	public ImageSelectControl() {}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-isct");

		//-- Show as the thumbnail followed by [clear] [load another].
		Img img = new Img();
		add(img);
		img.setWidth(Integer.toString(m_displayDimensions.getWidth()));
		img.setHeight(Integer.toString(m_displayDimensions.getHeight()));
		img.setAlign(ImgAlign.LEFT);

		if(m_value == null) {
			img.setSrc(DomUtil.getJavaResourceRURL(ImageSelectControl.class, "empty.png"));
		} else {
			String url = getComponentDataURL("THUMB", new PageParameters("datx", System.currentTimeMillis() + ""));
			img.setSrc(url);
		}
		add(" ");
		SmallImgButton sib = new SmallImgButton("THEME/btnClear.png", new IClicked<SmallImgButton>() {
			@Override
			public void clicked(SmallImgButton clickednode) throws Exception {
				setValue(null);
				forceRebuild();
			}
		});
		add(sib);
		sib.setTitle("Clear image (make it empty)");

		add(" ");

		if(!isDisabled()) {
			Form f = new Form();
			add(f);
			f.setCssClass("ui-szless ui-isct-form");
			f.setEnctype("multipart/form-data");
			f.setMethod("POST");
			StringBuilder sb = new StringBuilder();
			ComponentPartRenderer.appendComponentURL(sb, UploadPart.class, this, UIContext.getRequestContext());
			sb.append("?uniq=" + System.currentTimeMillis()); // Uniq the URL to prevent IE's caching.
			f.setAction(sb.toString());

			FileInput fi = new FileInput();
			m_input = fi;
			f.add(fi);
			fi.setSpecialAttribute("onchange", "WebUI.fileUploadChange(event)");
			fi.setSpecialAttribute("fuallowed", "jpg,jpeg,png");
		}
	}

	/**
	 * Called to render the image inside the component.
	 * @see to.etc.domui.dom.html.NodeBase#componentHandleWebDataRequest(to.etc.domui.server.RequestContextImpl, java.lang.String)
	 */
	@Override
	public void componentHandleWebDataRequest(RequestContextImpl ctx, String action) throws Exception {
		if("THUMB".equals(action)) {
			IUIImage image = m_value;
			if(null != image)
				renderImage(ctx, image);
			return;
		}

		super.componentHandleWebDataRequest(ctx, action);
	}

	private void renderImage(@Nonnull RequestContextImpl ctx, @Nonnull IUIImage thumbnail) throws Exception {
		IUIImageInstance ii = thumbnail.getImage(m_displayDimensions, true);
		OutputStream os = ctx.getRequestResponse().getOutputStream(ii.getMimeType(), null, ii.getImageSize());
		InputStream is = ii.getImage();
		try {
			FileTool.copyFile(os, is);
			os.close();
		} finally {
			FileTool.closeAll(os, is);
		}
	}

	@Override
	public void handleUploadRequest(RequestContextImpl param, ConversationContext conversation) throws Exception {
		FileInput fi = m_input;
		if(null == fi)
			return;

		UploadItem[] uiar = param.getFileParameter(fi.getActualID());
		if(uiar != null) {
			if(uiar.length != 1)
				throw new IllegalStateException("Upload presented <> 1 file!?");

			updateImage(conversation, uiar[0]);
		}
		forceRebuild();
//		if(m_onValueChanged != null)
//			((IValueChanged<FileUpload>) m_onValueChanged).onValueChanged(this);

		//-- Render an optimal delta as the response,
		param.getRequestResponse().setNoCache();
		ApplicationRequestHandler.renderOptimalDelta(param, getPage());

	}

	private void updateImage(@Nonnull ConversationContext cc, @Nonnull UploadItem ui) throws Exception {
		File newUploadedImage = ui.getFile();
		cc.registerTempFile(newUploadedImage);

		try {
			m_value = LoadedImage.create(newUploadedImage, m_maxDimensions, null);
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	@Nonnull public Dimension getDisplayDimensions() {
		return m_displayDimensions;
	}

	public void setDisplayDimensions(@Nonnull Dimension displayDimensions) {
		m_displayDimensions = displayDimensions;
	}

	@Nonnull public Dimension getMaxDimensions() {
		return m_maxDimensions;
	}

	public void setMaxDimensions(@Nonnull Dimension maxDimensions) {
		m_maxDimensions = maxDimensions;
	}

	@Override
	@Nullable
	public IUIImage getValue() {
		return m_value;
	}

	@Override
	public void setValue(@Nullable IUIImage value) {
		m_value = value;
	}

	@Override
	public IValueChanged< ? > getOnValueChanged() {
		return null;
	}

	@Override
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
	}

	@Override
	public IUIImage getValueSafe() {
		return DomUtil.getValueSafe(this);
	}

	@Override
	public boolean isReadOnly() {
		return m_readOnly;
	}

	@Override
	public void setReadOnly(boolean ro) {
		if(m_readOnly == ro)
			return;
		m_readOnly = ro;
		forceRebuild();
	}

	@Override
	public boolean isMandatory() {
		return m_mandatory;
	}

	@Override
	public void setMandatory(boolean ro) {
		if(m_mandatory == ro)
			return;
		m_mandatory = ro;
		forceRebuild();
	}

	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		if(disabled == m_disabled)
			return;
		m_disabled = disabled;
		forceRebuild();
	}
}
