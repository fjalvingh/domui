package to.etc.syntaxer;

import java.io.*;

import javax.swing.text.*;

import to.etc.syntaxer.TokenMarker.*;
import to.etc.util.*;

public class HtmlColorizer implements TokenHandler
{
	private Appendable	m_a;

	private String		m_lastClass = null;

	private int			m_tabsize;

	private int			m_x;

	public HtmlColorizer(Appendable a, int tabsize)
	{
		m_a = a;
		m_tabsize	= tabsize;
	}

	public void handleToken(Segment seg, byte id, int offset, int length, LineContext context)
	{
		try
		{
			if(id == 127 || (length == 1 && seg.array[seg.offset + offset] == '\n'))
			{
				flush();
				m_a.append("\n");
				m_x	= 0;
				return;
			}

			String	css = Token.tokenToString(id).toLowerCase();
			if(! css.equals(m_lastClass))
			{
				if(m_lastClass != null)
					m_a.append("</span>");
				m_a.append("<span class=\"navi");
				m_a.append(css);
				m_a.append("\">");
				m_lastClass = css;
			}
			for(int i = seg.offset+offset; --length >= 0 ; i++)
			{
				char c = seg.array[i];
				if(c == '<')
					m_a.append("&lt;");
				else if(c == '>')
					m_a.append("&gt;");
				else if(c == '&')
					m_a.append("&amp;");
				else if(c == '\t')
				{
					int m = m_tabsize - (m_x % m_tabsize);
					while(m-- >= 0)
					{
						m_a.append(' ');
						m_x++;
					}
					continue;
				}
				else
					m_a.append(c);
				m_x++;
			}

//			String	txt	= new String(seg.array, seg.offset+offset, length);
//			m_a.append(StringTool.htmlStringize(txt));
		}
		catch(RuntimeException x)
		{
			throw x;
		}
		catch(Exception x)
		{
			throw new WrappedException(x);
		}
	}

	public void	flush() throws IOException
	{
		if(m_lastClass != null)
			m_a.append("</span>");
		m_lastClass = null;
	}

	public void setLineContext(LineContext lineContext)
	{
		// TODO Auto-generated method stub

	}
}
