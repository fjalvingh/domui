package to.etc.el.node;

import java.io.*;

import javax.servlet.jsp.el.*;

/**
 * 
 * 
 *
 * @author jal
 * Created on May 17, 2005
 */
abstract public class NdBase {
	public String getNodeName() {
		String s = getClass().getName();
		return s.substring(s.lastIndexOf('.') + 3);
	}

	public void dump(IndentWriter w) throws IOException {
		w.println(getNodeName());
	}

	final public void dump() {
		OutputStreamWriter w = new OutputStreamWriter(System.out);
		IndentWriter iw = new IndentWriter(w, true);
		try {
			dump(iw);
		} catch(IOException x) {
			x.printStackTrace();
		} finally {
			try {
				w.flush();
			} catch(Exception x) {}
		}
	}

	abstract public Object evaluate(VariableResolver vr) throws ELException;

	//	{
	//		throw new IllegalStateException("No expression evaluator for "+getClass().getName());
	//	}

	abstract public void getExpression(Appendable a) throws IOException;

	final public String getExpression() {
		try {
			StringBuilder sb = new StringBuilder();
			getExpression(sb);
			return sb.toString();
		} catch(IOException x) {
			return x.toString();
		}
	}
}
