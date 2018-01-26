package to.etc.domuidemo.pages.overview.tree;

import to.etc.domui.component.tree.Tree;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TextNode;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.parts.FileTypePart;
import to.etc.domui.util.IRenderInto;
import to.etc.util.FileTool;

import java.io.File;

public class DemoTree extends UrlPage {
	@Override
	public void createContent() throws Exception {
		Tree<File> tree = new Tree<>();
		add(tree);
		DemoTreeModel model = new DemoTreeModel(new File(System.getProperty("user.home")));
		tree.setModel(model);

		tree.setContentRenderer(new IRenderInto<File>() {
			@Override
			public void render(NodeContainer node, File object) throws Exception {
				if(null == object)
					return;
				String url;
				if(object.isDirectory())
					url = FileTypePart.getURL("folder");
				else
					url = FileTypePart.getURL(FileTool.getFileExtension(object.getName()));

				//-- Append the image
				Img	fi = new Img(url);
				fi.setBorder(0);
				fi.setImgWidth("16");
				fi.setImgHeight("16");
				node.add(fi);
				node.add(new TextNode(object.getName()));
			}
		});
	}
}
