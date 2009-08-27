package to.etc.qte;

import javax.servlet.jsp.el.*;

/**
 * An abstract thingy which contains a compiled QTE template.
 * <p>Created on Nov 25, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public interface Template {
	public void generate(Appendable a, VariableResolver vr) throws Exception;
}
