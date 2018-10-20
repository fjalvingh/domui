package to.etc.domuidemo.pages.overview.tree2;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.misc.FontIcon;
import to.etc.domui.component.tree2.Tree2;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.IRenderInto;

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
			@Override public void render(@NonNull NodeContainer node, @NonNull DemoNode object) throws Exception {
				FontIcon icon = new FontIcon(object.getIcon());
				node.add(icon);
				icon.addCssClass("dm-tree2-icon");
				node.add(object.getText());
			}
		});
	}
}
