package to.etc.domuidemo.pages.components.graph;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.maxgraph.GraphExport;
import to.etc.domui.maxgraph.GraphExportFormat;
import to.etc.domui.maxgraph.MaxGraphPanel;
import to.etc.domui.maxgraph.model.GraphEdgeStyle;
import to.etc.domui.maxgraph.model.GraphModel;
import to.etc.domui.maxgraph.model.GraphNode;
import to.etc.domui.maxgraph.model.GraphShape;
import to.etc.domui.parts.TempFilePart;
import to.etc.domui.parts.TempFilePart.Disposition;

import java.io.File;

/**
 * A picture of the drawing, made by the browser because that is the only place the drawing
 * exists. Where it goes is the page's choice: the user keeps it, or it comes here.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ExportGraphPage extends UrlPage {
	private final GraphModel m_model = createModel();

	@Override
	public void createContent() throws Exception {
		setPageTitle("A picture of a diagram");
		MaxGraphPanel.initialize(this);

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "A picture of a diagram"));
		cp.add(new Para().add("Move a shape and take the picture again: what is exported is the "
			+ "drawing as it is now, all of it, whatever part of it is in view and whatever it is "
			+ "zoomed to."));

		//-- The panel is made here so the buttons can talk to it, and is added below them.
		MaxGraphPanel panel = new MaxGraphPanel();
		Div result = new Div();
		result.setTestID("exported");

		ButtonBar2 bb = new ButtonBar2();
		cp.add(bb);
		bb.addButton("Save as SVG", () -> panel.download(GraphExportFormat.Svg, "diagram.svg"));
		bb.addButton("Save as PNG", () -> panel.download(GraphExportFormat.Png, "diagram.png"));
		bb.addButton("Send the PNG here", () -> panel.export(GraphExportFormat.Png, 2.0, image -> show(result, image)));
		bb.addButton("Send the SVG here", () -> panel.export(GraphExportFormat.Svg, image -> show(result, image)));

		cp.add(panel);
		panel.size("100%", "360px").setEditable(true).setModel(m_model);

		cp.add(result);
		cp.add(new Para().add("The two Save buttons never touch the server: the drawing is in the "
			+ "browser and so is the file, so the picture is made there and handed straight to "
			+ "you. Look in the browser's downloads for diagram.svg or diagram.png."));

		cp.add(new Para().add("The two Send buttons post the picture back here instead, which is "
			+ "what an application does that has to keep it - put it in a report, mail it, store "
			+ "it. What arrives is the bytes of the file; this page writes them to a temporary "
			+ "file and shows you what it got, which is the picture above served by this server "
			+ "rather than the drawing in the browser."));

		cp.add(new Para().add("The png is asked for at twice the size, because a png is pixels "
			+ "and a printed one wants more of them. The svg does not need it: it is vector, and "
			+ "the same document at any size."));
	}

	/**
	 * Show what came back. The bytes are written to a temporary file and served from there,
	 * because that is what proves the picture is here: what is shown is a file this server
	 * has, not the drawing that is on the screen.
	 */
	private void show(Div result, GraphExport image) throws Exception {
		File file = image.saveTo(File.createTempFile("diagram", "." + image.getFormat().getExtension()));
		String url = TempFilePart.registerTempFile(file, image.getMimeType(), Disposition.Inline,
			"diagram." + image.getFormat().getExtension());

		result.removeAllChildren();
		result.add(new Para().add("The " + image.getFormat().getName() + " arrived here: "
			+ image.getWidth() + " by " + image.getHeight() + " pixels, "
			+ image.getData().length + " bytes."));
		Img img = new Img(url);
		img.setImgWidth("400");
		result.add(img);
	}

	/** A small flow chart, so that there is something with a shape to it to take a picture of. */
	private static GraphModel createModel() {
		GraphModel model = new GraphModel();

		GraphNode start = model.addNode("Order arrives", 40, 20, 160, 40)
			.styled(s -> s.shape(GraphShape.Ellipse).fillColor("#d5e8d4").strokeColor("#82b366"));

		GraphNode check = model.addNode("In stock?", 40, 110, 160, 60)
			.styled(s -> s.shape(GraphShape.Rhombus).fillColor("#ffe6cc").strokeColor("#d79b00"));

		GraphNode ship = model.addNode("Ship it", 300, 120, 140, 40)
			.styled(s -> s.rounded(true).fillColor("#dae8fc").strokeColor("#6c8ebf"));

		model.addEdge(start, check);
		model.addEdge(check, ship, "yes")
			.styled(s -> s.edgeStyle(GraphEdgeStyle.Orthogonal));

		return model;
	}
}
