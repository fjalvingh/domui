package to.etc.domui.component.tree;

public interface INodePredicate<T> {
	boolean predicate(T node) throws Exception;
}
