package to.etc.domuidemo.sourceviewer;

import to.etc.domui.annotations.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;
import to.etc.syntaxer.*;
import to.etc.syntaxer.TokenMarker.*;
import to.etc.util.*;

import javax.swing.text.*;
import java.io.*;
import java.util.*;

/**
 * This page will attempt to show the source code for a given Java class or other resource. It is
 * VERY QUICK AND VERY DIRTY.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 18, 2010
 */
public class SourcePage extends UrlPage {
	private String m_name;

	private int m_tabSize = 4;

	private String m_encoding = "UTF-8";

	private List<LineContext> m_ctxList;

	private Colorizer.Mode m_mode;

	private HtmlTokenHandler m_th = new HtmlTokenHandler();

	private Segment m_seg = new Segment();

	private List<String> m_importList = new ArrayList<String>();

	@UIUrlParameter(name = "name")
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	@Override
	public void createContent() throws Exception {
		String name = getName();

		SourceFile sf = SourceLocator.getInstance().findSource(name);
		CaptionedHeader ch = new CaptionedHeader("Source for " + name);
		add(ch);

		if(sf == null) {
			add(new InfoPanel("The source code for " + name + " could not be found."));
			return;
		}

		//-- Syntax highlighter
		String ext = FileTool.getFileExtension(name);
		if(ext.length() != 0)
			m_mode = Colorizer.getModeForExtension(ext);
		m_th.setTabsize(m_tabSize);
		m_th.setImportList(m_importList);
		Div scrolldiv = new Div();
		add(scrolldiv);
		scrolldiv.addCssClass("dm-srcp-scrl");

		TBody tb = scrolldiv.addTable();

		InputStream	is	= sf.getContent();
		try {
			//-- Start rendering file's contents.
			LineNumberReader lr = new LineNumberReader(new InputStreamReader(is, m_encoding));
			int linenr = 0;
			String line;
			LineContext lc = null;
			while(null != (line = lr.readLine())) {
				scanForImport(line);
				linenr++;
				lc = appendLine(tb, linenr, line, lc);
			}
		} finally {
			try { if(is != null) is.close(); } catch(Exception x) {}
		}
	}

	private void scanForImport(String line) {
		line = line.trim();
		if(!line.startsWith("import"))
			return;
		line = line.substring(6).trim();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if(c == ';')
				break;
			sb.append(c);
		}
		String name = sb.toString();
		m_importList.add(name);
		System.out.println("Added import=" + name);

	}

	private LineContext appendLine(TBody tb, int linenr, String line, LineContext lc) {
		TD td = tb.addRowAndCell();
		td.setCssClass("dm-srcp-lnr");
		td.setText(Integer.toString(linenr));
		td = tb.addCell("dm-srcp-txt");
		m_th.setTarget(td);

		m_seg.array = line.toCharArray();
		m_seg.offset = 0;
		m_seg.count = m_seg.array.length;
		return m_mode.getTokenMarker().markTokens(lc, m_th, m_seg);
	}
}
