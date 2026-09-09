package to.etc.domui.maxgraph;

import to.etc.domui.maxgraph.model.GraphCell;
import to.etc.domui.maxgraph.model.GraphEdge;
import to.etc.domui.maxgraph.model.GraphGeometry;
import to.etc.domui.maxgraph.model.GraphModel;
import to.etc.domui.maxgraph.model.GraphNode;
import to.etc.domui.maxgraph.model.GraphPoint;
import to.etc.domui.maxgraph.model.GraphStyle;
import to.etc.domui.util.javascript.JsonBuilder;

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

	private void renderCell(JsonBuilder b, GraphCell cell) throws Exception {
		b.objField("id", cell.getId());
		if(cell instanceof GraphNode node) {
			b.objField("kind", "node");
			GraphNode parent = node.getParent();
			if(null != parent) {
				b.objField("parent", parent.getId());
			}
			GraphGeometry geometry = node.getGeometry();
			b.objField("x", geometry.getX());
			b.objField("y", geometry.getY());
			b.objField("w", geometry.getWidth());
			b.objField("h", geometry.getHeight());
		} else if(cell instanceof GraphEdge edge) {
			b.objField("kind", "edge");
			GraphNode source = edge.getSource();
			if(null != source) {
				b.objField("source", source.getId());
			}
			GraphNode target = edge.getTarget();
			if(null != target) {
				b.objField("target", target.getId());
			}
			if(!edge.getWaypoints().isEmpty()) {
				b.objArrayField("points");
				for(GraphPoint point : edge.getWaypoints()) {
					b.itemArray();
					b.item(point.getX());
					b.item(point.getY());
					b.arrayEnd();
				}
				b.arrayEnd();
			}
		} else {
			throw new IllegalStateException("Unknown cell type " + cell.getClass().getName());
		}

		b.objFieldOpt("label", cell.getLabel());
		renderStyle(b, cell.style());
	}

	private void renderStyle(JsonBuilder b, GraphStyle style) throws Exception {
		if(style.isEmpty()) {
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
