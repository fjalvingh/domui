package to.etc.domuidemo.sourceviewer;

import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.XmlTextNode;
import to.etc.syntaxer.HighlightTokenType;
import to.etc.syntaxer.IHighlightRenderer;
import to.etc.util.StringTool;
import to.etc.util.WrappedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Highlight token renderer which generates an XmlTextNode containing spans
 * for tokens, to colorize them.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 18, 2010
 */
public class HtmlHighlightRenderer implements IHighlightRenderer {
	private String m_lastEnding;

	private HighlightTokenType m_lastType = null;

	private int m_tabSize = 4;

	private int m_x;

	private String m_baseClass;

	private NodeContainer m_target;

	private StringBuilder m_sb = new StringBuilder(128);

	private List<String> m_importList;

	/** All looked-up cross-references. */
	private Map<String, String> m_xrefMap = new HashMap<String, String>();

	public HtmlHighlightRenderer() {
	}

	@Override
	public void renderToken(HighlightTokenType tokenType, String token, int characterIndex) {
		try {
			if(tokenType == HighlightTokenType.newline) {
				if(m_lastType != null)
					m_sb.append(m_lastEnding);
				XmlTextNode l = new XmlTextNode();
				l.setText(m_sb.toString());
				m_target.add(l);
				l.setCssClass("dsdp-cl");
				m_sb.setLength(0);
				m_x = 0;
				return;
			}

			//-- Check for known ids
			boolean done = false;
			if(tokenType == HighlightTokenType.id) {
				String ref = getXref(token);
				if(ref != null) {
					if(m_lastType != null)
						m_sb.append(m_lastEnding);
					m_lastType = HighlightTokenType.newline;
					String css = tokenType.name().toLowerCase();
					m_lastEnding = "</a>";
					m_sb.append("<a class=\"s-");
					m_sb.append(css);
					if(m_baseClass != null && m_baseClass.length() > 0) {
						m_sb.append(' ');
						m_sb.append(m_baseClass);
					}
					m_sb.append("\" href=\"");
					m_sb.append(SourcePage.class.getName());
					m_sb.append(".ui?name=");
					m_sb.append(StringTool.encodeURLEncoded(ref));
					m_sb.append("\">");
					done = true;
				}
			}

			if(!done && m_lastType != tokenType) {
				String css = tokenType.name().toLowerCase();
				if(m_lastType != null)
					m_sb.append(m_lastEnding);

				m_lastEnding = "</span>";
				m_sb.append("<span class=\"s-");
				m_sb.append(css);
				if(m_baseClass != null && m_baseClass.length() > 0) {
					m_sb.append(' ');
					m_sb.append(m_baseClass);
				}
				m_sb.append("\">");
				m_lastType = tokenType;
			}

			int nsp = 0;
			for(int i = 0; i < token.length(); i++) {
				char c = token.charAt(i);
				if(c != ' ' && c != '\t')
					nsp = 0;
				if(c == '<')
					m_sb.append("&lt;");
				else if(c == '>')
					m_sb.append("&gt;");
				else if(c == '&')
					m_sb.append("&amp;");
				else if(c == ' ') {
					if(++nsp == 1)
						m_sb.append(' ');
					else
						m_sb.append('\u00a0'); // NBSP
				} else if(c == '\t') {
					int m = m_tabSize - (m_x % m_tabSize);
					while(m-- >= 0) {
						if(++nsp == 1)
							m_sb.append(' ');
						else
							m_sb.append('\u00a0'); // NBSP
						m_x++;
					}
					continue;
				} else
					m_sb.append(c);
				m_x++;
			}
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	private String getXref(String name) {
		String ref = m_xrefMap.get(name);
		if(ref != null) {
			if(ref.length() == 0) 							// Empty string is not found
				return null;
			return ref;
		}

		//-- Need to lookup.
		ref = "";
		for(String path : m_importList) {
			String lookup = null;
			if(path.endsWith(".*")) {
				lookup = path.substring(0, path.length() - 2) + "." + name;
			} else if(path.endsWith("." + name)) {
				lookup = path;
			}
			if(lookup != null) {
				lookup = lookup.replace('.', '/') + ".java";
				SourceFile sf = SourceLocator.getInstance().findSource(lookup);
				if(sf != null) {
					ref = lookup;
					break;
				}
			}
		}
		m_xrefMap.put(name, ref);
		if(ref.length() == 0)
			return null;
		return ref;
	}

	public void setTabSize(int tabSize) {
		m_tabSize = tabSize;
	}

	public void setTarget(NodeContainer nc) {
		m_target = nc;
		m_sb.setLength(0);
		m_lastType = null;
		m_x = 0;
	}

	public void setBaseClass(String s) {
		m_baseClass = s;
	}

	public void setImportList(List<String> l) {
		m_importList = l;
	}
}
