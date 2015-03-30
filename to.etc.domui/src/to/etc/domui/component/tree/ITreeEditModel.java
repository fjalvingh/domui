package to.etc.domui.component.tree;

import javax.annotation.*;

import to.etc.domui.logic.errors.*;

@DefaultNonNull
public interface ITreeEditModel<T> extends ITreeModel<T> {

	/**
	 * @param node
	 * @throws Exception
	 */
	void update(T node) throws Exception;

	/**
	 * @param pm
	 * @throws Exception
	 */
	void save(ProblemModel pm) throws Exception;

	/**
	 * @param node
	 * @throws Exception
	 */
	void remove(T node) throws Exception;
	
	/**
	 * @param newParent
	 * @param newIndex
	 * @param nodeToAdd
	 * @throws Exception
	 */
	void add(T newParent, int newIndex, T nodeToAdd) throws Exception;
}
