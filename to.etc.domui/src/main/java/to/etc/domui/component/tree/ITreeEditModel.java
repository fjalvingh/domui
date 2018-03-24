package to.etc.domui.component.tree;

import javax.annotation.*;

@DefaultNonNull
public interface ITreeEditModel<T> extends ITreeModel<T> {
	/**
	 * @param node
	 * @throws Exception
	 */
	void update(T node) throws Exception;

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
