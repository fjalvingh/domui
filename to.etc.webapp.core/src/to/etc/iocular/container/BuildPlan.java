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

	/**
	 * When T this component has a static (one-time only) initialization requirement.
	 * @return
	 */
	public boolean		needsStaticInitialization();

	/**
	 * When this has a static initializer this should execute it. This gets called before an actual object
	 * is created from this definition.
	 * @param c
	 * @throws Exception
	 */
	public void			staticStart(BasicContainer c) throws Exception;

	/**
	 * Call the after-construction methods specified for this object (start methods). When present these are
	 * called after construction of the object, with the instance of the object as a possible parameter.
	 * @param bc
	 * @param self
	 * @throws Exception
	 */
	public void			start(BasicContainer bc, Object self) throws Exception;

	/**
	 * Call the before-destruction methods specified for this object.
	 * @param bc
	 * @param self
	 * @throws Exception
	 */
	public void			destroy(BasicContainer bc, Object self);

	boolean	hasDestructors();
}
