package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.XmlTextNode;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.state.UIContext;
import to.etc.domui.trouble.ThingyNotFoundException;
import to.etc.domui.util.resources.IResourceRef;
import to.etc.domui.util.resources.ResourceDependencyList;
import to.etc.util.FileTool;

import java.io.InputStream;
import java.util.Objects;

/**
 * An SVG icon that embeds the SVG in the HTML.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-11-18.
 */
final public class SvgIcon extends Span {
	@Nullable
	private String m_src;

	public SvgIcon() {
	}

	public SvgIcon(ISvgIcon icon) {
		m_src = icon == null ? null : icon.getSvg();
	}

	public SvgIcon(String src) {
		m_src = src;
	}

	@Override public void createContent() throws Exception {
		addCssClass("ui-svgi");
		String svg = m_src;
		if(null != svg) {
			svg = loadSvg(svg);

			StringBuilder sb = new StringBuilder();
			//sb.append("<svg aria-hidden='true'>");
			sb.append(svg);
			//sb.append("</svg>");
			XmlTextNode xn = new XmlTextNode(sb.toString());
			add(xn);
		}
	}

	private String loadSvg(String svg) throws Exception {
		if(svg.startsWith("<"))
			return svg;

		IRequestContext ctx = UIContext.getRequestContext();
		IResourceRef iconres = ctx.getApplication().getResource(svg, ResourceDependencyList.NULL);
		if(! iconres.exists())
			throw new ThingyNotFoundException("The resource " + svg + " is not found in this svg icon " + this);
		try(InputStream is = iconres.getInputStream()) {
			String data = FileTool.readStreamAsString(is, "utf-8");
			return data;
		}
	}

	public String getSrc() {
		return m_src;
	}

	public void setSrc(String src) {
		if(Objects.equals(m_src, src))
			return;
		m_src = src;
		forceRebuild();
	}

	@Override
	public SvgIcon css(String... classes) {
		super.css(classes);
		return this;
	}

	public void setIcon(ISvgIcon icon) {
		setSrc(null == icon ? null : icon.getSvg());
	}
}
