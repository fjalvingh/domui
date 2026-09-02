package to.etc.domuidemo.pages.components.tables;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.tree3.Tree3;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * Tree3: a tree over an ITreeModel, with the nodes rendered by the page and
 * selection handled by the tree.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class Tree3Page extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Tree3: showing a tree");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Tree3: showing a tree"));

		Div shown = new Div("dm-tut-q");
		shown.add("Click a node, or double-click one to open it.");

		//-- The model answers three questions: what is the root, how many children has a
		//-- node, and which child is number n. The tree asks them as the user opens nodes.
		TreeDemoModel model = new TreeDemoModel(this);
		Tree3<DemoNode> tree = new Tree3<>(model);
		cp.add(tree);

		//-- What a node looks like is the page's business, not the tree's.
		tree.setContentRenderer((node, object) -> {
			NodeBase icon = object.getIcon().createNode();
			node.add(icon);
			icon.addCssClass("dm-tree2-icon");
			node.add(object.getText());
		});

		//-- Only the leaves may be picked.
		tree.setNodeSelectablePredicate(node -> node.getChildren().isEmpty());

		tree.setCellClicked2((node, clickInfo) -> {
			shown.removeAllChildren();
			shown.add("Clicked: " + node.getText()
				+ "\nPath:    " + String.join(" / ", tree.getTreePath(node).stream()
				.map(DemoNode::getText).toList()));
		});

		Div buttons = new Div("dm-tut");
		cp.add(buttons);
		buttons.add(new DefaultButton("Collapse everything", a -> tree.collapseAll()));
		buttons.add(new DefaultButton("What is selected?", a -> {
			DemoNode selected = tree.getSelectedValue();
			shown.removeAllChildren();
			shown.add(selected == null ? "Nothing is selected" : "Selected: " + selected.getText());
		}));
		cp.add(shown);

		cp.add(new Para().add("The tree does not hold the data: the model does, and it is asked "
			+ "for children only when a node is opened. This one groups every artist in the "
			+ "database under its first letter, and reads an artist's albums when that artist "
			+ "is expanded - so opening the tree does not read the whole database."));
		cp.add(new Para().add("Only leaves can be selected here, because the tree was given a "
			+ "predicate saying so. Clicking a node that may be selected marks it and calls "
			+ "the click handler; double-clicking any node opens or closes it."));
	}
}
