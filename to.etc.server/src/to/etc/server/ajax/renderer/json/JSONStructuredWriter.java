package to.etc.server.ajax.renderer.json;

import java.io.*;
import java.util.*;

import to.etc.server.ajax.renderer.*;

public class JSONStructuredWriter extends StructuredWriter {
	//	static private final byte	SOR		= 0;
	static private final byte	REC		= 1;

	static private final byte	LIST	= 2;

	private int					m_lvl	= 0;

	private byte[]				m_type	= new byte[50];

	private JSONRenderer		m_r;

	public JSONStructuredWriter(JSONRenderer r) {
		super(r.getWriter());
		m_r = r;
	}

	@Override
	public void end() throws IOException {
		if(m_lvl <= 0)
			throw new IllegalStateException("Underflow.");
		byte type = m_type[m_lvl--];
		switch(type){
			case LIST:
				m_r.getWriter().dec();
				m_r.getWriter().forceNewline();
				append("],");
				break;
			case REC:
				m_r.getWriter().dec();
				m_r.getWriter().forceNewline();
				append("},");
				break;
		}
	}

	/**
	 * Only allowed in a record.
	 * @see to.etc.server.ajax.renderer.StructuredWriter#field(java.lang.String, java.lang.String)
	 */
	@Override
	public void field(String name, String value) throws Exception {
		m_r.getWriter().forceNewline();
		byte type = m_type[m_lvl];
		if(type != REC)
			throw new IllegalStateException("A field is valid in a record only (field=" + name + ", value=" + value + ")");
		m_r.render(name);
		append(':');
		m_r.render(value);
		append(",\n");
	}

	@Override
	public void field(String name, boolean value) throws Exception {
		m_r.getWriter().forceNewline();
		byte type = m_type[m_lvl];
		if(type != REC)
			throw new IllegalStateException("A field is valid in a record only (field=" + name + ", value=" + value + ")");
		m_r.render(name);
		append(':');
		append(value ? "true" : "false");
		append(",\n");
	}

	@Override
	public void field(String name, Date value) throws Exception {
		m_r.getWriter().forceNewline();
		byte type = m_type[m_lvl];
		if(type != REC)
			throw new IllegalStateException("A field is valid in a record only (field=" + name + ", value=" + value + ")");
		m_r.render(name);
		append(':');
		m_r.render(value);
		append(",\n");
	}

	@Override
	public void field(String name, Number value) throws Exception {
		m_r.getWriter().forceNewline();
		byte type = m_type[m_lvl];
		if(type != REC)
			throw new IllegalStateException("A field is valid in a record only (field=" + name + ", value=" + value + ")");
		m_r.render(name);
		append(':');
		m_r.render(value);
		append(",\n");
	}

	/**
	 * Starts a JSON list. If this is part of a record this outputs:
	 * <pre>
	 * 	name: [
	 * </pre>
	 * If this is part of another list or the root then it outputs '[' only.
	 *
	 * @see to.etc.server.ajax.renderer.StructuredWriter#list(java.lang.String)
	 */
	@Override
	public void list(String name) throws Exception {
		byte type = m_type[m_lvl];
		if(type == REC) {
			m_r.render(name);
			append(": [\n");
		} else {
			append("[\n");

		}
		m_r.getWriter().inc();
		m_type[++m_lvl] = LIST;
	}

	/**
	 * Starts a JSON record. If in a record this adds the name.
	 *
	 * @see to.etc.server.ajax.renderer.StructuredWriter#record(java.lang.String)
	 */
	@Override
	public void record(String name) throws Exception {
		byte type = m_type[m_lvl];
		if(type == REC) {
			m_r.render(name);
			append(": {\n");
		} else {
			append("{\n");

		}
		m_r.getWriter().inc();
		m_type[++m_lvl] = REC;
	}

	@Override
	public void close() throws IOException {
		while(m_lvl > 0) {
			byte type = m_type[m_lvl--];
			switch(type){
				case LIST:
					append("\n]");
					m_r.getWriter().dec();
					break;
				case REC:
					append("\n}");
					m_r.getWriter().dec();
					break;
			}
		}
		append("\n");
	}

	@Override
	public void flush() throws IOException {
	}
}
