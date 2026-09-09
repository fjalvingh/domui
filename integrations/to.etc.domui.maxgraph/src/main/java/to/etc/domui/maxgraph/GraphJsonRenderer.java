package to.etc.domui.maxgraph;

import to.etc.domui.maxgraph.model.GraphCell;
import to.etc.domui.maxgraph.model.GraphEdge;
import to.etc.domui.maxgraph.model.GraphGeometry;
import to.etc.domui.maxgraph.model.GraphModel;
import to.etc.domui.maxgraph.model.GraphNode;
import to.etc.domui.maxgraph.model.GraphOp;
import to.etc.domui.maxgraph.model.GraphPoint;
import to.etc.domui.maxgraph.model.GraphStyle;
import to.etc.domui.util.javascript.JsonBuilder;

import java.util.List;
import java.util.Map;

/**
 * Renders a {@link GraphModel} as the document the browser side builds a drawing from.
 * The model itself knows nothing about this, and neither side knows anything of the
 * other's types: this class and the Typescript that reads what it writes are the whole
 * of the contract.
 *
 * <pre>
 * { "version": 12,
 *   "options": { "panning": true },
 *   "cells": [
 *     { "id": "n1", "kind": "node", "label": "Start", "x": 20, "y": 20, "w": 120, "h": 40,
 *       "style": { "shape": "ellipse" } },
 *     { "id": "e1", "kind": "edge", "source": "n1", "target": "n2", "label": "yes" }
 *   ] }
 * </pre>
 *
 * <p>Cells come in the model's order, which is the order they can be created in: a parent
 * before its children, an edge after both of its ends.</p>
 *
 * <p>{@link #renderOps(JsonBuilder, int, GraphModel, List)} writes the other half of the
 * protocol: what changed since the browser was last told, in the same vocabulary.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class GraphJsonRenderer {
	public void render(JsonBuilder b, GraphModel model, boolean panning) throws Exception {
		b.obj();
		b.objField("version", model.getVersion());

		b.objObjField("options");
		b.objField("panning", panning);
		b.objEnd();

		b.objArrayField("cells");
		for(GraphCell cell : model.getCells()) {
			b.itemObj();
			renderCell(b, cell);
			b.objEnd();
		}
		b.arrayEnd();
		b.objEnd();
	}

	/**
	 * Render a change list: the model version it was made against, the version it results
	 * in, and the changes themselves in the order they were made.
	 *
	 * <pre>
	 * { "base": 12, "version": 14, "ops": [
	 *   { "op": "geometry", "id": "n1", "x": 40, "y": 60, "w": 120, "h": 40 },
	 *   { "op": "remove", "id": "n3" } ] }
	 * </pre>
	 *
	 * <p>What an operation says about a cell is read from the cell now, not when the change
	 * was made, so the browser is told where things ended up.</p>
	 */
	public void renderOps(JsonBuilder b, int base, GraphModel model, List<GraphOp> ops) throws Exception {
		b.obj();
		b.objField("base", base);
		b.objField("version", model.getVersion());
		b.objArrayField("ops");
		for(GraphOp op : ops) {
			b.itemObj();
			renderOp(b, op);
			b.objEnd();
		}
		b.arrayEnd();
		b.objEnd();
	}

	private void renderOp(JsonBuilder b, GraphOp op) throws Exception {
		b.objField("op", op.getType().getName());
		GraphCell cell = op.getCell();
		if(null == cell) {
			return;                                        // Reload is about the drawing, not about a cell.
		}
		switch(op.getType()) {
			default:
				throw new IllegalStateException("Unhandled operation " + op.getType());

			case AddNode:
			case AddEdge:
				renderCell(b, cell);
				break;

			case Remove:
				b.objField("id", cell.getId());
				break;

			case Label:
				b.objField("id", cell.getId());
				renderLabel(b, cell);
				break;

			case Style:
				b.objField("id", cell.getId());
				renderStyle(b, cell.style(), true);
				break;

			case Geometry:
				b.objField("id", cell.getId());
				renderGeometry(b, ((GraphNode) cell).getGeometry());
				break;

			case Terminal:
				b.objField("id", cell.getId());
				renderTerminals(b, (GraphEdge) cell);
				break;

			case Points:
				b.objField("id", cell.getId());
				renderPoints(b, (GraphEdge) cell);
				break;
		}
	}

	private void renderCell(JsonBuilder b, GraphCell cell) throws Exception {
		b.objField("id", cell.getId());
		if(cell instanceof GraphNode node) {
			b.objField("kind", "node");
			GraphNode parent = node.getParent();
			if(null != parent) {
				b.objField("parent", parent.getId());
			}
			renderGeometry(b, node.getGeometry());
		} else if(cell instanceof GraphEdge edge) {
			b.objField("kind", "edge");
			renderTerminals(b, edge);
			if(!edge.getWaypoints().isEmpty()) {
				renderPoints(b, edge);
			}
		} else {
			throw new IllegalStateException("Unknown cell type " + cell.getClass().getName());
		}

		b.objFieldOpt("label", cell.getLabel());
		renderStyle(b, cell.style(), false);
	}

	private void renderGeometry(JsonBuilder b, GraphGeometry geometry) throws Exception {
		b.objField("x", geometry.getX());
		b.objField("y", geometry.getY());
		b.objField("w", geometry.getWidth());
		b.objField("h", geometry.getHeight());
	}

	/**
	 * Both ends of an edge, null included: an end that was removed has to be sent as
	 * nothing, not left out.
	 */
	private void renderTerminals(JsonBuilder b, GraphEdge edge) throws Exception {
		GraphNode source = edge.getSource();
		b.objField("source", null == source ? null : source.getId());
		GraphNode target = edge.getTarget();
		b.objField("target", null == target ? null : target.getId());
	}

	private void renderPoints(JsonBuilder b, GraphEdge edge) throws Exception {
		b.objArrayField("points");
		for(GraphPoint point : edge.getWaypoints()) {
			b.itemArray();
			b.item(point.getX());
			b.item(point.getY());
			b.arrayEnd();
		}
		b.arrayEnd();
	}

	private void renderLabel(JsonBuilder b, GraphCell cell) throws Exception {
		String label = cell.getLabel();
		b.objField("label", null == label ? "" : label);
	}

	/**
	 * The style, as the complete set of properties the cell has: a change replaces the
	 * style in the browser, it does not add to it. An empty style is only worth sending
	 * when it is a change - when the drawing is built, "no style" is the default anyway.
	 */
	private void renderStyle(JsonBuilder b, GraphStyle style, boolean evenWhenEmpty) throws Exception {
		if(style.isEmpty() && !evenWhenEmpty) {
			return;
		}
		b.objObjField("style");
		for(Map.Entry<String, Object> entry : style.getProperties().entrySet()) {
			Object value = entry.getValue();
			if(value instanceof Boolean bool) {
				b.objField(entry.getKey(), bool.booleanValue());
			} else if(value instanceof Integer integer) {
				b.objField(entry.getKey(), integer.intValue());
			} else if(value instanceof Number number) {
				b.objField(entry.getKey(), number.doubleValue());
			} else {
				b.objField(entry.getKey(), String.valueOf(value));
			}
		}
		b.objEnd();
	}
}
