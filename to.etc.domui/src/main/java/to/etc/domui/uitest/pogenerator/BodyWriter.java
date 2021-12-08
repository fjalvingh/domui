package to.etc.domui.uitest.pogenerator;

import to.etc.util.IndentWriter;
import to.etc.util.Pair;
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

	protected T appendType(PoClass clz, Pair<String, String> type) throws Exception {
		append(getTypeName(clz, type.get1(), type.get2()));
		return (T) this;
	}

	protected T appendType(PoClass clz, String packageName, String typeName) throws Exception {
		append(getTypeName(clz, packageName, typeName));
		return (T) this;
	}

	protected String getTypeName(PoClass clz, String packageName, String typeName) {
		if(packageName.length() == 0)
			return typeName;
		String fullName = packageName + "." + typeName;
		if(clz.hasImport(fullName))
			return typeName;
		return fullName;
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
