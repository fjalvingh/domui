package to.etc.domui.util.javascript;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.util.StringTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-21.
 */
public class JsonBuilder implements AutoCloseable {
	private final Appendable m_sb;

	private enum Stacked {
		Object,
		Array,
		Literal
	}

	private static final class Level {
		final public Stacked type;
		public int count;

		public Level(Stacked type) {
			this.type = type;
		}
	}

	final private List<Level> m_stack = new ArrayList<>(40);

	public JsonBuilder(Appendable sb) {
		m_sb = sb;
	}

	public JsonBuilder obj() throws IOException {
		push(Stacked.Object);
		m_sb.append('{');
		return this;
	}

	/**
	 * Starts a field for an object without a value part; the next call must define the value.
	 */
	public JsonBuilder objField(String fieldName) throws IOException {
		ensureObject();
		next();
		string(fieldName);
		m_sb.append(':');
		return this;
	}

	public JsonBuilder objField(String fieldName, @Nullable String value) throws IOException {
		ensureObject();
		next();
		string(fieldName);
		m_sb.append(':');
		string(value);
		return this;
	}

	public JsonBuilder objField(String fieldName, int value) throws IOException {
		ensureObject();
		next();
		string(fieldName);
		m_sb.append(':');
		m_sb.append(Integer.toString(value));
		return this;
	}

	public JsonBuilder objField(String fieldName, long value) throws IOException {
		ensureObject();
		next();
		string(fieldName);
		m_sb.append(':');
		m_sb.append(Long.toString(value));
		return this;
	}

	public JsonBuilder objField(String fieldName, double value) throws IOException {
		ensureObject();
		next();
		string(fieldName);
		m_sb.append(':');
		m_sb.append(Double.toString(value));
		return this;
	}

	private void ensureObject() {
		if(top().type != Stacked.Object)
			throw new IllegalStateException("Not currently inside an object but in " + top().type);
	}

	private void ensureArray() {
		if(top().type != Stacked.Array)
			throw new IllegalStateException("Not currently inside an array but in " + top().type);
	}

	public JsonBuilder objEnd() throws IOException {
		pop(Stacked.Object);
		m_sb.append('}');
		return this;
	}

	public JsonBuilder array() throws IOException {
		push(Stacked.Array);
		m_sb.append('[');
		return this;
	}

	public JsonBuilder arrayEnd() throws IOException {
		pop(Stacked.Array);
		m_sb.append(']');
		return this;
	}

	public JsonBuilder item(@Nullable String s) throws IOException {
		ensureArray();
		next();
		string(s);
		return this;
	}

	public JsonBuilder item(int s) throws IOException {
		ensureArray();
		next();
		m_sb.append(Integer.toString(s));
		return this;
	}

	public JsonBuilder item(long s) throws IOException {
		ensureArray();
		next();
		m_sb.append(Long.toString(s));
		return this;
	}

	public JsonBuilder item(double s) throws IOException {
		ensureArray();
		next();
		m_sb.append(Double.toString(s));
		return this;
	}

	public JsonBuilder string(@Nullable String value) throws IOException {
		if(value == null) {
			m_sb.append("null");
		} else {
			StringTool.strToJavascriptString(m_sb, value, true);
		}
		return this;
	}

	private void next() throws IOException {
		if(m_stack.size() != 0) {
			if(top().count++ > 0) {
				m_sb.append(',');
			}
		}
	}

	private Level top() {
		return m_stack.get(m_stack.size() - 1);
	}

	private void push(Stacked s) {
		m_stack.add(new Level(s));
	}

	private void pop(Stacked s) {
		if(top().type != s)
			throw new IllegalStateException("Stack type error: expecting " + s + " but got " + top().type);
		m_stack.remove(m_stack.size() - 1);
	}

	@Override
	public void close() throws Exception {
		for(int i = m_stack.size() - 1; i >= 0; i--) {
			Level stacked = m_stack.get(i);
			switch(stacked.type) {
				case Array:
					arrayEnd();
					break;

				case Literal:
					break;

				case Object:
					objEnd();
					break;
			}
		}
	}
}
