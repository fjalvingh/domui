package to.etc.server.ajax.renderer.xml;

import java.io.*;
import java.util.*;

import to.etc.server.ajax.renderer.*;
import to.etc.xml.*;

public class XMLStructuredWriter extends StructuredWriter {
	//	static private final byte	SOR		= 0;
	static private final byte	REC		= 1;

	static private final byte	LIST	= 2;

	private int					m_lvl	= 0;

	private byte[]				m_type	= new byte[50];

	private XmlRenderer			m_r;

	public XMLStructuredWriter(XmlRenderer r) {
		super(r.getWriter());
		m_r = r;
	}

	public XmlWriter xw() {
		return m_r.xw();
	}

	@Override
	public void end() throws IOException {
		if(m_lvl <= 0)
			throw new IllegalStateException("Underflow.");
		byte type = m_type[m_lvl--];
		switch(type){
			case LIST:
			case REC:
				xw().tagendnl();
				break;
		}
	}

	/**
	 * Only allowed in a record.
	 * @see to.etc.server.ajax.renderer.StructuredWriter#field(java.lang.String, java.lang.String)
	 */
	@Override
	public void field(String name, String value) throws Exception {
		byte type = m_type[m_lvl];
		if(type != REC)
			throw new IllegalStateException("A field is valid in a record only (field=" + name + ", value=" + value + ")");
		xw().tagfull(name, value);
	}

	@Override
	public void field(String name, boolean value) throws Exception {
		byte type = m_type[m_lvl];
		if(type != REC)
			throw new IllegalStateException("A field is valid in a record only (field=" + name + ", value=" + value + ")");
		xw().tagfull(name, value);
	}

	@Override
	public void field(String name, Date value) throws Exception {
		byte type = m_type[m_lvl];
		if(type != REC)
			throw new IllegalStateException("A field is valid in a record only (field=" + name + ", value=" + value + ")");
		xw().tagfull(name, value);
	}

	@Override
	public void field(String name, Number value) throws Exception {
		byte type = m_type[m_lvl];
		if(type != REC)
			throw new IllegalStateException("A field is valid in a record only (field=" + name + ", value=" + value + ")");
		xw().tagfull(name, value);
	}

	/**
	 * Starts a list. The list is simply a tag with the specified name.
	 *
	 * @see to.etc.server.ajax.renderer.StructuredWriter#list(java.lang.String)
	 */
	@Override
	public void list(String name) throws Exception {
		xw().tag(name);
		m_type[++m_lvl] = LIST;
	}

	/**
	 * Starts a record. Yes, another tag.
	 *
	 * @see to.etc.server.ajax.renderer.StructuredWriter#record(java.lang.String)
	 */
	@Override
	public void record(String name) throws Exception {
		xw().tag(name);
		m_type[++m_lvl] = REC;
	}

	@Override
	public void close() throws IOException {
		while(m_lvl > 0)
			end();
	}

	@Override
	public void flush() throws IOException {
	}
}
