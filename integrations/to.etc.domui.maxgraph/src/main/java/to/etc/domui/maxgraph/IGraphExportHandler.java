package to.etc.domui.maxgraph;

/**
 * Handed the picture asked for with
 * {@link MaxGraphPanel#export(GraphExportFormat, IGraphExportHandler)}, once the browser
 * has made it.
 *
 * <pre>
 * panel.export(GraphExportFormat.Png, image -&gt; {
 *     File png = image.saveTo(File.createTempFile("drawing", ".png"));
 *     TempFilePart.createDownloadAction(this, png, image.getMimeType(), Disposition.Attachment, "drawing.png");
 * });
 * </pre>
 *
 * <p>The picture arrives in a request of its own, a moment after the one that asked for
 * it: making it takes the browser as long as it takes to draw. That request is an ordinary
 * one, so this may change the page like any other handler - which is what lets a page show
 * the picture, or hand it to the user as a download.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public interface IGraphExportHandler {
	void exported(GraphExport image) throws Exception;
}
