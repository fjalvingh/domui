package to.etc.domuidemo.pages.overview.tree2;

import to.etc.domui.component.tree2.Tree2;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.UrlPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-8-17.
 */
final public class Tree2DemoPage extends UrlPage {
	@Override public void createContent() throws Exception {
		Tree2DemoModel model = new Tree2DemoModel(this);
		Tree2<DemoNode> tree = new Tree2<>(model);
		add(tree);

		tree.setContentRenderer((node, object) -> {
			NodeBase icon = object.getIcon().createNode();
			node.add(icon);
			icon.addCssClass("dm-tree2-icon");
			node.add(object.getText());
		});
	}
}
