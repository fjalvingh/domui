package to.etc.iocular.container;

import java.io.IOException;

import to.etc.iocular.def.ComponentRef;
import to.etc.util.IndentWriter;

/**
 * Encapsulates a method of building an instance of a given object
 * from a given container.
 *
 * @author jal
 * Created on Mar 28, 2007
 */
public interface BuildPlan {
	static public final ComponentRef[]	EMPTY_PLANS = new ComponentRef[0];

	public Object		getObject(BasicContainer c) throws Exception;

	public void			dump(IndentWriter iw) throws IOException;

	public boolean		needsStaticInitialization();

	public void			staticStart(BasicContainer c) throws Exception;
}
