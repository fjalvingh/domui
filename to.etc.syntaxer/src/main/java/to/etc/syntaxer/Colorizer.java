package to.etc.syntaxer;

import java.io.*;
import java.util.*;

import javax.swing.text.*;

import to.etc.syntaxer.TokenMarker.*;
import to.etc.util.*;

/**
 * QD thingy to color-code syntax.
 *
 *
 * @author jal
 * Created on Jun 1, 2006
 */
public class Colorizer
{
	static public class Mode
	{
		private TokenMarker			m_tokenMarker;

		private String				m_name;

		private Hashtable			m_properties;

		public Hashtable getProperties()
		{
			return m_properties;
		}

		public void setProperties(Hashtable properties)
		{
			m_properties = properties;
		}

		public Mode(String name)
		{
			m_name = name;
		}

		public TokenMarker getTokenMarker()
		{
			return m_tokenMarker;
		}

		public void setTokenMarker(TokenMarker tokenMarker)
		{
			m_tokenMarker = tokenMarker;
		}

		public String getName()
		{
			return m_name;
		}
	}

	static private Map<String, Mode>	m_modeMap = new HashMap<String, Mode>();

	static private Map<String, String>	m_extMap = new HashMap<String, String>();

	static private void	p(String a, String... suf) {
		for(String s: suf)
			m_extMap.put(s, a);
	}

	static {
		p("antlr", "g");
		p("asp", "asp");
		p("assembly-x86", "x86", "masm");
		p("awk", "awk");
		p("batch", "bat", "cmd");
		p("bcel", "bcel");
		p("css", "css");
		p("hex", "hex");
		p("html", "html", "htm");
		p("ini", "ini");
		p("jsp", "jsp", "jspx", "inc");
		p("patch", "patch", "diff");
		p("php", "php");
		p("postscript", "ps");
		p("python", "py");
		p("relax-ng", "rng");
		p("sh", "shellscript");
		p("xsl", "xsl");

		p("c", "c");
		p("csharp", "cs", "c#");
		p("cobol", "cob", "cbl");
		p("java", "java");

		p("javascript", "js");
		p("pl-sql", "plb", "plc", "pck", "dbs", "trg", "vw");
		p("tsql", "sql");
		p("xml", "tld", "xml", "classpath", "project", "component", "settings", "xhtml");
		p("props", "prefs", "properties");
	}


	static private Mode	findMode(String name)
	{
		return m_modeMap.get(name.toLowerCase());
	}
	static synchronized public Mode	getMode(String name) throws Exception
	{
		Mode m = findMode(name);
		if(m != null)
			return m;
		System.out.println("Trying to load mode "+name);
//		StringTool.dumpLocation("mode");
		m = new Mode(name);
		InputStream	is	= Colorizer.class.getResourceAsStream("/modes/"+name.toLowerCase()+".xml");
		if(is == null) {
			System.out.println("Missing resource for mode="+name);
			return getMode("text");
		}
		try
		{
			InputStreamReader	r	= new InputStreamReader(is);
			XModeHandler	mh	= new XModeHandler(name)
			{
				@Override
				protected void error(String msg, Object subst)
				{
					System.out.println("error: "+msg+", "+subst);
				}
				@Override
				protected TokenMarker getTokenMarker(String mode)
				{
					try
					{
						Mode md = getMode(mode);
						return md == null ? null : md.getTokenMarker();
					}
					catch(Exception x)
					{
						x.printStackTrace();
						throw new WrappedException(x);
					}
				}
			};
			m_modeMap.put(name.toLowerCase(), m);
			XmlParser	xp	= new XmlParser();
			xp.setHandler(mh);
			m.setTokenMarker(mh.getTokenMarker());
			xp.parse(null, null, r);
			m.setProperties(mh.getModeProperties());
			return m;
		}
		finally
		{
			try { if(is != null) is.close(); } catch(Exception x) {}
		}
	}

	static public Mode	getModeForExtension(String ext) throws Exception {
		String s = m_extMap.get(ext.trim().toLowerCase());
		return getMode(s == null ? ext : s);
	}

	public void	colorize(String text, String mode, int tabsize, Appendable a) throws Exception
	{
		Mode	m	= getMode(mode);

		//-- Start to tokenize
		char[]	ch = text.toCharArray();
		TokenMarker	tm = m.getTokenMarker();

//		TokenHandler	th	= new TokenHandler()
//		{
//			public void handleToken(Segment seg, byte id, int offset, int length, LineContext context)
//			{
//				String	ttext = new String(seg.array, seg.offset + offset, length);
//				System.out.println("t="+ttext+", id="+id+": "+Token.tokenToString(id));
//			}
//			public void setLineContext(LineContext lineContext)
//			{
//			}
//		};
		HtmlColorizer	th	= new HtmlColorizer(a, tabsize);
		Segment	s	= new Segment(ch, 0, 0);
		int	len	= ch.length;
		int	off	= 0;
		LineContext	lc	= null;
		while(off < len)
		{
			int	pos	= off;
			while(pos < len && ch[pos] != '\n')
				pos++;
			int	eol = pos;
			if(eol > off && ch[eol-1] == '\r')
				eol--;
			s.offset	= off;
			s.count		= eol - off;

//			String txt = new String(s.array, s.offset, s.count);
//			System.out.println("markTokens: [["+txt+"]]");
//			if(txt.contains("m_withbody"))
//				return;

			lc = tm.markTokens(lc, th, s);

			//-- Move to next
			off	= pos+1;
		}
	}

	static public void main(String[] args)
	{
		try
		{
			System.out.println("Starting");
			Colorizer	c	= new Colorizer();
			StringBuilder	sb	= new StringBuilder(8192);
//			c.colorize(FileTool.readFileAsString(new File("./src/to/etc/syntaxer/Colorizer.java")), "java", 4, sb);
			c.colorize(FileTool.readFileAsString(new File("/home/jal/wsplanner/navi.web/WebContent/manual/CalendarPartGenerator.java")), "java", 4, sb);
			System.out.println("HTML:\n"+sb.toString());
		}
		catch(Exception x)
		{
			x.printStackTrace();
		}
	}


}

