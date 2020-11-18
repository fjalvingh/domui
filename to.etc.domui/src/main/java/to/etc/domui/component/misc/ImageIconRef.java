package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Span;
import to.etc.util.FileTool;

/**
 * Reference to an icon that is made from an image.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-10-18.
 */
@NonNullByDefault
final public class ImageIconRef implements IIconRef {
	private final String m_path;

	@NonNull
	private final String m_cssClasses;

	public ImageIconRef(String path) {
		m_path = path;
		m_cssClasses = "";
	}

	public ImageIconRef(String path, String[] cssClasses) {
		m_path = path;
		StringBuilder sb = new StringBuilder();
		for(String cssClass : cssClasses) {
			sb.append(" ").append(cssClass);
		}
		m_cssClasses = sb.toString();
	}

	@Override
	public String getClasses() {
		return m_cssClasses;
	}

	public String getPath() {
		return m_path;
	}

	@Override public NodeBase createNode(String cssClasses) {
		String path = getPath();
		if(null == path) {
			return new Span("ui-icon-empty " + cssClasses, "");
		}
		String ext = FileTool.getFileExtension(path).toLowerCase();
		if("svg".equals(ext))
			return new SvgIcon(path).css(cssClasses);
		else if(ext.isEmpty()) {
			return new FontIcon(path).css(cssClasses);
		} else {
			return new ImgIcon(path).css(cssClasses);
		}
	}

	@Override public IIconRef css(@NonNull String... classes) {
		return new ImageIconRef(m_path, classes);
	}
}
