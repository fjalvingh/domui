package to.etc.domui.maxgraph;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.maxgraph.model.GraphCell;
import to.etc.domui.maxgraph.model.GraphChange;
import to.etc.domui.maxgraph.model.GraphEdge;
import to.etc.domui.maxgraph.model.GraphGeometry;
import to.etc.domui.maxgraph.model.GraphModel;
import to.etc.domui.maxgraph.model.GraphNode;
import to.etc.domui.maxgraph.model.GraphPoint;
import to.etc.domui.maxgraph.model.GraphStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reads what the browser sends when a user changes a drawing, in the same vocabulary
 * {@link GraphJsonRenderer} writes. Between the two of them they are the whole of the
 * contract with the Typescript; nothing else on either side knows the format.
 *
 * <pre>
 * { "base": 12, "ops": [
 *   { "op": "geometry", "id": "n1", "x": 40, "y": 60, "w": 120, "h": 40 },
 *   { "op": "remove", "id": "n3" } ] }
 * </pre>
 *
 * <p>A change about a cell the model no longer has is dropped: the browser can only have
 * sent it because the two sides crossed, and the drawing is corrected anyway.</p>
 *
 * <p>Four of the operations are not changes but requests: {@code requestNode} and
 * {@code requestEdge} ask for a cell that does not exist yet, and {@code requestUndo} and
 * {@code requestRedo} ask the model's history to move. They come out as
 * {@link GraphRequest} and only the browser ever sends them.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class GraphChangeParser {
	static private final ObjectMapper MAPPER = new ObjectMapper();

	public GraphChangeSet parse(GraphModel model, String json) throws Exception {
		JsonNode root = MAPPER.readTree(json);
		List<GraphChange> changes = new ArrayList<>();
		List<GraphRequest> requests = new ArrayList<>();
		for(JsonNode node : root.path("ops")) {
			GraphRequest request = parseRequest(model, node);
			if(null != request) {
				requests.add(request);
				continue;
			}
			GraphChange change = parseChange(model, node);
			if(null != change) {
				changes.add(change);
			}
		}
		return new GraphChangeSet(root.path("base").asInt(-1), changes, requests);
	}

	/**
	 * The two operations that are not about a cell that exists, but ask for one that does
	 * not. Anything else is a change, and is parsed as one.
	 */
	@Nullable
	private GraphRequest parseRequest(GraphModel model, JsonNode node) {
		switch(node.path("op").asText()) {
			default:
				return null;

			case "requestNode":
				return GraphRequest.node(node.path("key").asText(), node.path("x").asDouble(), node.path("y").asDouble());

			case "requestEdge":
				GraphNode source = terminal(model, node.get("source"));
				GraphNode target = terminal(model, node.get("target"));
				//-- A connection to a node that is gone here is not a connection at all.
				return null == source || null == target ? null : GraphRequest.edge(source, target);

			case "requestUndo":
				return GraphRequest.undo();

			case "requestRedo":
				return GraphRequest.redo();
		}
	}

	@Nullable
	private GraphChange parseChange(GraphModel model, JsonNode node) {
		String op = node.path("op").asText();
		GraphCell cell = model.getCell(node.path("id").asText());
		if(null == cell) {
			return null;
		}
		switch(op) {
			default:
				throw new IllegalStateException("Unknown graph change '" + op + "' from the browser");

			case "geometry":
				if(!(cell instanceof GraphNode)) {
					return null;
				}
				return GraphChange.geometry((GraphNode) cell, new GraphGeometry(
					node.path("x").asDouble(), node.path("y").asDouble(),
					node.path("w").asDouble(), node.path("h").asDouble()));

			case "label":
				return GraphChange.label(cell, node.path("label").asText(""));

			case "terminal":
				if(!(cell instanceof GraphEdge)) {
					return null;
				}
				return GraphChange.terminal((GraphEdge) cell, terminal(model, node.get("source")), terminal(model, node.get("target")));

			case "points":
				if(!(cell instanceof GraphEdge)) {
					return null;
				}
				return GraphChange.points((GraphEdge) cell, points(node.path("points")));

			case "style":
				return GraphChange.style(cell, style(node.path("style")));

			case "remove":
				return GraphChange.remove(cell);
		}
	}

	@Nullable
	private GraphNode terminal(GraphModel model, @Nullable JsonNode node) {
		if(null == node || node.isNull()) {
			return null;
		}
		GraphCell cell = model.getCell(node.asText());
		return cell instanceof GraphNode graphNode ? graphNode : null;
	}

	private List<GraphPoint> points(JsonNode node) {
		List<GraphPoint> points = new ArrayList<>();
		for(JsonNode point : node) {
			points.add(new GraphPoint(point.path(0).asDouble(), point.path(1).asDouble()));
		}
		return points;
	}

	/**
	 * The style as the browser has it. Numbers come back as numbers and booleans as
	 * booleans, so what goes out styled comes back styled the same way.
	 */
	private GraphStyle style(JsonNode node) {
		GraphStyle style = new GraphStyle();
		for(Map.Entry<String, JsonNode> entry : node.properties()) {
			JsonNode value = entry.getValue();
			if(value.isBoolean()) {
				style.raw(entry.getKey(), Boolean.valueOf(value.asBoolean()));
			} else if(value.isInt()) {
				style.raw(entry.getKey(), Integer.valueOf(value.asInt()));
			} else if(value.isNumber()) {
				style.raw(entry.getKey(), Double.valueOf(value.asDouble()));
			} else if(!value.isNull()) {
				style.raw(entry.getKey(), value.asText());
			}
		}
		return style;
	}
}
