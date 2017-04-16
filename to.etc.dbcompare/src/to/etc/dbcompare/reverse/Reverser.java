package to.etc.dbcompare.reverse;

import to.etc.dbcompare.db.*;

/**
 * Thingy which reads a DB schema.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2006
 */
public interface Reverser {
	public String getIdent();

	public Schema loadSchema() throws Exception;
}
