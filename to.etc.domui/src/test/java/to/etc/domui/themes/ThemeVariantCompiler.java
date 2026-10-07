package to.etc.domui.themes;

import com.sass_lang.embedded_protocol.InboundMessage.CompileRequest;
import com.sass_lang.embedded_protocol.InboundMessage.ImportResponse.ImportSuccess;
import com.sass_lang.embedded_protocol.OutputStyle;
import com.sass_lang.embedded_protocol.Syntax;
import de.larsgrefer.sass.embedded.SassCompilationFailedException;
import de.larsgrefer.sass.embedded.SassCompiler;
import de.larsgrefer.sass.embedded.SassCompilerFactory;
import de.larsgrefer.sass.embedded.importer.CustomImporter;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Compiles the winter theme's style.scss for one variant straight from the source tree, without
 * a running DomApplication, so that its variants can be compiled and compared in a test.
 *
 * <p>It reproduces what DomUI's own resolver does (see DartSassResolver and SassThemeFactory):
 * every sheet is known as <code>domui:/[variant]/[path]</code> and is looked up in the scheme's
 * directory, the nature's and then the theme directory, so a file in <code>dark/</code> shadows
 * the one with the same name in the theme directory; <code>theme</code> is the variant's
 * <code>_index.scss</code>, from whatever directory it is asked for; and <code>parameters</code>
 * is generated, holding <code>$themeVariant</code>, <code>$themeNature</code> and
 * <code>$themeScheme</code>.</p>
 *
 * <p>An application's own theme directory - what its webapp has under themes/scss/winter - can
 * be passed as well. As in DomUI, a file there wins over the framework's file of the same name
 * in the same search-path directory.</p>
 */
final public class ThemeVariantCompiler implements AutoCloseable {
	static private final String SCHEME = "domui:/";

	static private final List<String> SUFFIXES = List.of(".scss", ".sass", ".css");

	private final File m_themeDir;

	@Nullable
	private final File m_appThemeDir;

	private final SassCompiler m_compiler;

	private final Importer m_importer = new Importer();

	@Nullable
	private String m_variant;

	/** The name of the in-memory module {@link #compileConfigured} loads, and its source while it runs. */
	static private final String PROBE = "configured-probe";

	@Nullable
	private String m_probe;

	public ThemeVariantCompiler(File themeDir) throws Exception {
		this(themeDir, null);
	}

	public ThemeVariantCompiler(File themeDir, @Nullable File appThemeDir) throws Exception {
		m_themeDir = themeDir;
		m_appThemeDir = appThemeDir;
		m_compiler = SassCompilerFactory.bundled();
		m_compiler.registerImporter(m_importer);
	}

	/**
	 * The theme directory as it is in this source tree, found from the working directory a test
	 * runs in (the module) or the one above it (the project).
	 */
	static public File findThemeDir() {
		String rel = "src/main/resources/resources/themes/scss/winter";
		for(String base : new String[]{".", "to.etc.domui"}) {
			File f = new File(base, rel);
			if(new File(f, "style.scss").exists())
				return f;
		}
		throw new IllegalStateException("Cannot find the winter theme from " + new File(".").getAbsolutePath());
	}

	/**
	 * Compile style.scss for the variant, named as a {@link SchemeVariant}: "light-winter",
	 * "dark-nord".
	 */
	public String compile(String variant) throws Exception {
		File file = locate("style.scss");
		if(null == file)
			throw new IllegalStateException("No style.scss in " + m_themeDir);
		return compileSource(variant, "style.scss", Files.readString(file.toPath(), StandardCharsets.UTF_8));
	}

	/**
	 * Compile a sheet of the caller's own as if it were the file <code>name</code> in the theme
	 * directory, for the variant: it can <code>&#64;use "theme"</code> and read the variant's values.
	 */
	public String compileSource(String variant, String name, String source) throws Exception {
		return compileRoot(variant, name, source, null);
	}

	/**
	 * Compile a sheet of the caller's own that <code>&#64;use</code>s "theme", with the theme
	 * configured as style.scss configures it (_theme-configuration.scss): the exceptions and the
	 * application's custominit files applied. A plain <code>&#64;use "theme"</code> in
	 * {@link #compileSource} reads the unconfigured values instead.
	 */
	public String compileConfigured(String variant, String source) throws Exception {
		String root = "@use \"sass:meta\";\n@use \"theme-configuration\" as c;\n"
			+ "@include meta.load-css(\"" + PROBE + "\", $with: c.$configuration);\n";
		return compileRoot(variant, "probe-root.scss", root, "@forward \"theme\";\n" + source);
	}

	private String compileRoot(String variant, String name, String source, @Nullable String probe) throws Exception {
		m_variant = variant;
		m_probe = probe;
		try {
			String url = SCHEME + variant + "/" + name;
			CompileRequest.StringInput input = CompileRequest.StringInput.newBuilder()
				.setSource(source)
				.setSyntax(Syntax.SCSS)
				.setUrl(url)
				.setImporter(CompileRequest.Importer.newBuilder().setImporterId(m_importer.getId()))
				.build();
			try {
				return m_compiler.compileString(input, OutputStyle.EXPANDED).getCss();
			} catch(SassCompilationFailedException x) {
				throw new IllegalStateException("The " + variant + " variant does not compile:\n" + x.getCompileFailure().getFormatted(), x);
			}
		} finally {
			m_variant = null;
			m_probe = null;
		}
	}

	/**
	 * The file for a theme-relative path: from the variant directory if it has it, else from the
	 * theme directory.
	 */
	@Nullable
	private File locate(String path) {
		List<File> dirs = new ArrayList<>();
		File app = m_appThemeDir;
		String variant = m_variant;
		if(null != variant) {
			SchemeVariant scheme = SchemeVariant.parse(variant);
			if(null == scheme)
				throw new IllegalStateException("Not a variant name: " + variant);
			String nature = scheme.getNature().getName();
			for(String vd : List.of(nature + "/" + scheme.getSchemeName(), nature)) {	// the search path SassThemeFactory gives every variant
				if(null != app)
					dirs.add(new File(app, vd));
				dirs.add(new File(m_themeDir, vd));
			}
		}
		if(null != app)
			dirs.add(app);
		dirs.add(m_themeDir);
		for(File dir : dirs) {
			File f = new File(dir, path);
			if(f.isFile())
				return f;
		}
		return null;
	}

	@Nullable
	private String resolveName(String path) {
		String last = path.substring(path.lastIndexOf('/') + 1);
		String bare = last.startsWith("_") ? last.substring(1) : last;
		if(bare.equals("parameters") || bare.startsWith("parameters."))
			return "parameters";
		if(bare.equals("theme") || bare.startsWith("theme."))
			return "_index.scss";
		if(bare.equals(PROBE) && null != m_probe)
			return PROBE;

		String dir = path.substring(0, path.length() - last.length());
		List<String> candidates = new ArrayList<>();
		boolean hasSuffix = SUFFIXES.stream().anyMatch(last::endsWith);
		for(String name : hasSuffix ? List.of(last) : SUFFIXES.stream().map(s -> last + s).toList()) {
			candidates.add(dir + name);
			if(!name.startsWith("_"))
				candidates.add(dir + "_" + name);
		}
		for(String c : candidates) {
			if(null != locate(c))
				return c;
		}
		return null;
	}

	private final class Importer extends CustomImporter {
		@Override
		@Nullable
		public String canonicalize(String url, boolean fromImport) {
			String path;
			if(url.startsWith(SCHEME)) {
				path = url.substring(SCHEME.length());
				int ix = path.indexOf('/');				// Strip the variant segment
				path = ix < 0 ? path : path.substring(ix + 1);
			} else if(url.contains(":")) {
				return null;
			} else {
				path = url;
			}
			String name = resolveName(path);
			return null == name ? null : SCHEME + m_variant + "/" + name;
		}

		@Override
		public ImportSuccess handleImport(String url) throws Exception {
			String path = url.substring(SCHEME.length());
			path = path.substring(path.indexOf('/') + 1);
			String probe = m_probe;
			if(path.equals(PROBE) && null != probe) {
				return ImportSuccess.newBuilder()
					.setContents(probe)
					.setSyntax(Syntax.SCSS)
					.build();
			}
			if(path.equals("parameters")) {
				return ImportSuccess.newBuilder()
					.setContents(parameters(m_variant))
					.setSyntax(Syntax.SCSS)
					.build();
			}
			File file = locate(path);
			if(null == file)
				throw new IllegalStateException("Not found: " + url);
			Syntax syntax = path.endsWith(".css") ? Syntax.CSS : path.endsWith(".sass") ? Syntax.INDENTED : Syntax.SCSS;
			return ImportSuccess.newBuilder()
				.setContents(Files.readString(file.toPath(), StandardCharsets.UTF_8))
				.setSyntax(syntax)
				.setSourceMapUrl(url)
				.build();
		}
	}

	/** The generated parameters module, as AbstractSassResolver writes it for the variant. */
	static private String parameters(@Nullable String variant) {
		SchemeVariant scheme = null == variant ? null : SchemeVariant.parse(variant);
		if(null == scheme)
			return "$themeVariant: \"" + variant + "\";\n";
		return "$themeVariant: \"" + variant + "\";\n$themeNature: \"" + scheme.getNature().getName() + "\";\n$themeScheme: \"" + scheme.getSchemeName() + "\";\n";
	}

	@Override
	public void close() throws Exception {
		m_compiler.close();
	}
}
