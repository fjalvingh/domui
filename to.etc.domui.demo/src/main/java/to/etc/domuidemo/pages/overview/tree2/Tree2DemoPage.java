package to.etc.domuidemo.pages.overview.tree2;

import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.tree2.Tree2;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.IRenderInto;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-8-17.
 */
public class Tree2DemoPage extends UrlPage {
	@Override public void createContent() throws Exception {
		Tree2DemoModel model = new Tree2DemoModel(this);
		Tree2<DemoNode> tree = new Tree2<>(model);
		add(tree);

		tree.setContentRenderer(new IRenderInto<DemoNode>() {
			@Override public void render(@Nonnull NodeContainer node, @Nonnull DemoNode object) throws Exception {
				FaIcon icon = new FaIcon(object.getIcon());
				node.add(icon);
				icon.addCssClass("dm-tree2-icon");
				node.add(object.getText());
			}
		});
	}
}
