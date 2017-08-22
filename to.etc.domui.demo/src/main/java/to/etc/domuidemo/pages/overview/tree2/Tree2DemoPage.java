package to.etc.domuidemo.pages.overview.tree2;

import to.etc.domui.component.tree2.Tree2;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.INodeContentRenderer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-8-17.
 */
public class Tree2DemoPage extends UrlPage {
	@Override public void createContent() throws Exception {
		Tree2DemoModel model = new Tree2DemoModel(this);
		Tree2<DemoNode> tree = new Tree2<>(model);
		add(tree);

		tree.setContentRenderer(new INodeContentRenderer<DemoNode>() {
			@Override public void renderNodeContent(@Nonnull NodeBase component, @Nonnull NodeContainer node,
				@Nullable DemoNode object, @Nullable Object parameters) throws Exception {
				if(null == object)
					return;

				node.add(object.getText());
			}
		});
	}
}
