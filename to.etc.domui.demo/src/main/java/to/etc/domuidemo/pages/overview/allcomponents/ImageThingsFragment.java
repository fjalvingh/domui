package to.etc.domuidemo.pages.overview.allcomponents;

import to.etc.domui.component.image.Dimension;
import to.etc.domui.component.image.IUIImage;
import to.etc.domui.component.image.ImageSelectControl;
import to.etc.domui.component.image.LoadedImage;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;

import java.io.InputStream;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-11-17.
 */
public class ImageThingsFragment extends Div {
	private IUIImage m_f10;


	@Override public void createContent() throws Exception {
		add(new HTag(2, "Image components").css("ui-header"));

		try(InputStream is = getClass().getResourceAsStream("demo-animal.png")) {
			m_f10 = LoadedImage.create(is, new Dimension(64, 64), null);
		}

		ImageSelectControl isc = new ImageSelectControl();
		isc.setDisplayDimensions(new Dimension(64, 64));
		ContentPanel cp = new ContentPanel();
		add(cp);
		FormBuilder fb = new FormBuilder(cp);
		fb.property(this, "f10").label("Your avatar").control(isc);
	}

	public IUIImage getF10() {
		return m_f10;
	}

	public void setF10(IUIImage f10) {
		m_f10 = f10;
	}
}
