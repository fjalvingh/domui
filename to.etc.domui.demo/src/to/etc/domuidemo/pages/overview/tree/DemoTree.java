package to.etc.domuidemo.pages.overview.tree;

import to.etc.domui.component.tree.Tree;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TextNode;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.parts.FileTypePart;
import to.etc.domui.util.INodeContentRenderer;
import to.etc.util.FileTool;

import java.io.File;

public class DemoTree extends UrlPage {
	private DemoTreeModel				m_model;
	private Tree<File>						m_tree;

	@Override
	public void createContent() throws Exception {
		m_tree = new Tree<>();
		add(m_tree);
		m_model = new DemoTreeModel(new File(System.getProperty("user.home")));
		m_tree.setModel(m_model);

		m_tree.setContentRenderer(new INodeContentRenderer<File>() {
			@Override
			public void renderNodeContent(NodeBase component, NodeContainer node, File object, Object parameters) throws Exception {
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
