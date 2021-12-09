package to.etc.domui.uitest.pogenerator;

import to.etc.util.IndentWriter;
import to.etc.util.StringTool;

import java.io.StringWriter;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
public class BodyWriter<T extends BodyWriter<T>> {
	private final StringWriter m_sw = new StringWriter(1024);

	private final IndentWriter m_writer = new IndentWriter(m_sw);

	public T append(String s) throws Exception {
		m_writer.append(s);
		return (T) this;
	}

	public T nl() throws Exception {
		m_writer.append("\n");
		return (T) this;
	}


	public T inc() {
		m_writer.inc();
		return (T) this;
	}

	public T dec() {
		m_writer.dec();
		return (T) this;
	}


	public String getResult() {
		return m_sw.getBuffer().toString();
	}

	protected T appendType(PoClass clz, RefType type) throws Exception {
		append(getTypeName(clz, type));
		return (T) this;
	}

	protected String getTypeName(PoClass clz, RefType type) {
		return type.asTypeString();
	}

	protected T appendString(String s) throws Exception {
		if(null == s)
			append("null");
		else {
			append(StringTool.strToJavascriptString(s, true));
		}
		return (T) this;
	}

}
