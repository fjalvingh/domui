package to.etc.domui.maxgraph.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Undo is the model's, so it is testable without a browser and without a page - which is
 * the point of a model that knows about neither.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TestGraphUndo {
	/**
	 * A drawing that is only being built has nothing to take back, and remembering it would
	 * cost the whole of it.
	 */
	@Test
	public void nothingIsRememberedUntilItIsAskedFor() {
		GraphModel model = new GraphModel();
		model.addNode("Start", 0, 0, 80, 40);
		Assert.assertFalse("Building a drawing is not undoable", model.canUndo());

		model.setUndoEnabled(true);
		model.addNode("Second", 0, 60, 80, 40);
		Assert.assertTrue("What happens after that is", model.canUndo());
	}

	@Test
	public void undoingAnAddRemovesItAgain() {
		GraphModel model = new GraphModel();
		model.setUndoEnabled(true);
		GraphNode node = model.addNode("Start", 0, 0, 80, 40);

		Assert.assertTrue(model.undo());
		Assert.assertTrue("The node must be gone", model.isEmpty());

		Assert.assertTrue(model.redo());
		Assert.assertSame("And redo brings back the very same node", node, model.getCell("n1"));
	}

	/**
	 * The reason undo is this side's: a removed cell is still whole here, so it comes back
	 * as itself - the same object, the same id, the application's own data still on it.
	 */
	@Test
	public void undoingARemovalBringsTheCellBackWhole() {
		GraphModel model = new GraphModel();
		GraphNode node = model.addNode("Start", 10, 20, 80, 40);
		Object data = new Object();
		node.setUserObject(data);
		model.setUndoEnabled(true);

		model.remove(node);
		Assert.assertTrue(model.isEmpty());

		Assert.assertTrue(model.undo());
		GraphCell back = model.getCell(node.getId());
		Assert.assertSame("The same cell must be back under the same id", node, back);
		Assert.assertSame("With the application's data on it", data, node.getUserObject());
	}

	/**
	 * Removing a node removes what cannot exist without it, so undoing it has to put all of
	 * that back - and in an order in which every cell has what it needs.
	 */
	@Test
	public void undoingARemovalBringsBackWhatWentWithIt() {
		GraphModel model = new GraphModel();
		GraphNode hub = model.addNode("Hub", 0, 0, 80, 40);
		GraphNode child = model.addNode(hub, "Inside", 5, 5, 40, 20);
		GraphNode other = model.addNode("Other", 200, 0, 80, 40);
		GraphEdge edge = model.addEdge(hub, other);
		GraphEdge childEdge = model.addEdge(child, other);
		model.setUndoEnabled(true);

		model.remove(hub);
		Assert.assertEquals("Only the node that has nothing to do with it is left", 1, model.getCells().size());

		Assert.assertTrue(model.undo());
		Assert.assertEquals("Everything must be back", 5, model.getCells().size());
		Assert.assertSame(hub, model.getCell(hub.getId()));
		Assert.assertSame(child, model.getCell(child.getId()));
		Assert.assertSame(edge, model.getCell(edge.getId()));
		Assert.assertSame(childEdge, model.getCell(childEdge.getId()));
		Assert.assertEquals("The child must be in its parent again", List.of(child), hub.getChildren());

		//-- A cell has to be back before anything that cannot exist without it.
		List<GraphCell> cells = model.getCells();
		Assert.assertTrue("A node comes back before the edges on it", cells.indexOf(hub) < cells.indexOf(edge));
		Assert.assertTrue("A parent comes back before its child", cells.indexOf(hub) < cells.indexOf(child));
		Assert.assertTrue("A child comes back before the edges on it", cells.indexOf(child) < cells.indexOf(childEdge));
	}

	@Test
	public void undoingRestoresWhatEveryKindOfChangeChanged() {
		GraphModel model = new GraphModel();
		GraphNode one = model.addNode("One", 10, 20, 80, 40);
		GraphNode two = model.addNode("Two", 200, 20, 80, 40);
		GraphEdge edge = model.addEdge(one, two, "yes");
		one.style().fillColor("#fff");
		model.setUndoEnabled(true);

		one.setLabel("Renamed");
		one.getGeometry().setBounds(100, 120, 60, 30);
		one.style().fillColor("#000");
		edge.setTarget(null);
		edge.setWaypoints(List.of(new GraphPoint(5, 5)));

		Assert.assertTrue(model.undo());
		Assert.assertEquals("The route it took", 0, edge.getWaypoints().size());
		Assert.assertTrue(model.undo());
		Assert.assertSame("The end it pointed at", two, edge.getTarget());
		Assert.assertTrue(model.undo());
		Assert.assertEquals("The colour it had", "#fff", one.style().getProperties().get("fillColor"));
		Assert.assertTrue(model.undo());
		Assert.assertEquals("Where it was", 10.0, one.getGeometry().getX(), 0.001);
		Assert.assertEquals(80.0, one.getGeometry().getWidth(), 0.001);
		Assert.assertTrue(model.undo());
		Assert.assertEquals("What it was called", "One", one.getLabel());

		Assert.assertFalse("And that was everything", model.canUndo());
	}

	/** One gesture is often several changes, and one step is what a boundary says it is. */
	@Test
	public void oneBoundaryIsOneStep() {
		GraphModel model = new GraphModel();
		GraphNode node = model.addNode("One", 0, 0, 80, 40);
		model.setUndoEnabled(true);

		model.edit(() -> {
			node.setLabel("Renamed");
			node.at(100, 100);
			model.addNode("Second", 0, 60, 80, 40);
		});

		Assert.assertTrue(model.undo());
		Assert.assertEquals("All three changes go back together", "One", node.getLabel());
		Assert.assertEquals(0.0, node.getGeometry().getX(), 0.001);
		Assert.assertEquals(1, model.getCells().size());
		Assert.assertFalse("And they were one step", model.canUndo());
	}

	@Test
	public void aNewChangeMakesTheRedoneStepsUnreachable() {
		GraphModel model = new GraphModel();
		model.setUndoEnabled(true);
		model.addNode("One", 0, 0, 80, 40);

		model.undo();
		Assert.assertTrue(model.canRedo());
		model.addNode("Another", 0, 60, 80, 40);
		Assert.assertFalse("What was undone cannot be redone past a new change", model.canRedo());
	}

	/**
	 * An undo is a change like any other, so a drawing on a screen hears about it in the
	 * ordinary way, and the version keeps moving forward - it never rewinds.
	 */
	@Test
	public void anUndoIsAnOrdinaryChangeToWhateverIsListening() {
		GraphModel model = new GraphModel();
		GraphNode node = model.addNode("One", 0, 0, 80, 40);
		model.setUndoEnabled(true);

		List<GraphOp> opList = new ArrayList<>();
		model.addChangeListener((m, op) -> opList.add(op));
		model.remove(node);
		int version = model.getVersion();
		opList.clear();

		model.undo();
		Assert.assertEquals("The cell coming back is one operation", 1, opList.size());
		Assert.assertEquals(GraphOpType.AddNode, opList.get(0).getType());
		Assert.assertTrue("And the version moved on, it did not go back", model.getVersion() > version);
	}

	/** A step that removed cells holds on to them, so there is a limit to how many are kept. */
	@Test
	public void theOldestStepsAreDropped() {
		GraphModel model = new GraphModel();
		model.setUndoEnabled(true).setUndoLimit(2);
		GraphNode node = model.addNode("One", 0, 0, 80, 40);
		node.setLabel("Two");
		node.setLabel("Three");

		Assert.assertTrue(model.undo());
		Assert.assertTrue(model.undo());
		Assert.assertFalse("Only the last two steps are kept", model.canUndo());
		Assert.assertEquals("So the node itself is still there", "One", node.getLabel());
	}

	@Test
	public void switchingUndoOffForgetsTheHistory() {
		GraphModel model = new GraphModel();
		model.setUndoEnabled(true);
		model.addNode("One", 0, 0, 80, 40);

		model.setUndoEnabled(false);
		Assert.assertFalse(model.canUndo());
		Assert.assertFalse("And nothing happens when it is asked anyway", model.undo());
	}
}
