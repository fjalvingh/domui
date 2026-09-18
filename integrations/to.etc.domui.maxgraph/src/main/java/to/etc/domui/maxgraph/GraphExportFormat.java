package to.etc.domui.maxgraph;

/**
 * What a picture of a drawing is made in, asked for with
 * {@link MaxGraphPanel#export(GraphExportFormat, IGraphExportHandler)} or
 * {@link MaxGraphPanel#download(GraphExportFormat, String)}.
 *
 * <p>Both are made in the browser, because the drawing only exists there: the model says
 * what is drawn, but not what it looks like once maxGraph has routed the edges and
 * measured the labels.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public enum GraphExportFormat {
	/**
	 * The drawing as vector, and the one to take when it is to be printed, scaled or
	 * edited afterwards. It is a document of its own: what maxGraph drew, without the
	 * page's stylesheets and without anything the drawing does not contain.
	 */
	Svg("svg", "image/svg+xml", "svg"),

	/**
	 * The drawing as pixels, at the size the scale asks for. What a node's shape is an
	 * image cannot be drawn this way - the picture is rasterized from the vector one, and
	 * a browser refuses to rasterize anything it had to fetch.
	 */
	Png("png", "image/png", "png");

	private final String m_name;

	private final String m_mimeType;

	private final String m_extension;

	GraphExportFormat(String name, String mimeType, String extension) {
		m_name = name;
		m_mimeType = mimeType;
		m_extension = extension;
	}

	/** The name this format is known by in the wire protocol. */
	public String getName() {
		return m_name;
	}

	public String getMimeType() {
		return m_mimeType;
	}

	/** The file extension, without the dot. */
	public String getExtension() {
		return m_extension;
	}
}
