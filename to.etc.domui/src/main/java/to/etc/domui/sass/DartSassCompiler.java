package to.etc.domui.sass;

import com.sass_lang.embedded_protocol.InboundMessage.CompileRequest;
import com.sass_lang.embedded_protocol.InboundMessage.ImportResponse.ImportSuccess;
import com.sass_lang.embedded_protocol.OutputStyle;
import de.larsgrefer.sass.embedded.CompileSuccess;
import de.larsgrefer.sass.embedded.SassCompilationFailedException;
import de.larsgrefer.sass.embedded.SassCompiler;
import de.larsgrefer.sass.embedded.connection.ConnectionFactory;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.state.IPageParameters;
import to.etc.domui.trouble.ThingyNotFoundException;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.util.DeveloperOptions;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;

/**
 * Compiles scss/sass using Dart Sass, the reference implementation, which is spoken to over the
 * Sass embedded protocol: the compiler is a separate process, and this is the "host" side of that
 * conversation.
 *
 * <p>Starting the process is expensive and a single process compiles one sheet at a time, so
 * processes are pooled: a compilation borrows one and returns it when it is done. The pool is
 * closed when the application terminates.</p>
 *
 * <p>By default the dart-sass binary bundled in the sass-embedded-bundled jar is used, which
 * covers all common platforms. Set the developer option or system property
 * "domui.sass.executable" to the path of a dart-sass executable to use that instead.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class DartSassCompiler implements ISassCompiler {
	static private final Logger LOG = LoggerFactory.getLogger(DartSassCompiler.class);

	/** Developer option/system property holding the path of the dart-sass executable to use instead of the bundled one. */
	static public final String EXECUTABLE_PROPERTY = "domui.sass.executable";

	/** The max #of idle dart-sass processes kept around. */
	static private final int MAX_IDLE = 4;

	/**
	 * A dart-sass process plus the importer registered on it. An importer cannot be unregistered, so
	 * every process has exactly one which is pointed at the resolver for the compilation running on it.
	 */
	static private final class Instance {
		private final SassCompiler m_compiler;

		private final DartSassImporter m_importer;

		public Instance(SassCompiler compiler, DartSassImporter importer) {
			m_compiler = compiler;
			m_importer = importer;
		}

		public SassCompiler getCompiler() {
			return m_compiler;
		}

		public DartSassImporter getImporter() {
			return m_importer;
		}

		public void close() {
			try {
				m_compiler.close();
			} catch(Exception x) {
				LOG.warn("Failed to close a dart-sass process: " + x, x);
			}
		}
	}

	private final Deque<Instance> m_idleList = new ArrayDeque<>();

	@Nullable
	private Boolean m_available;

	@Override
	public void compiler(String rurl, Writer output, @NonNull IPageParameters params, @NonNull IResourceDependencyList rdl) throws Exception {
		DartSassResolver resolver = new DartSassResolver(params, rdl);
		String url = resolver.canonicalize(rurl);
		if(null == url)
			throw new ThingyNotFoundException("The sass/scss file " + rurl + " could not be found");
		ImportSuccess entry = resolver.load(url);
		if(null == entry)
			throw new ThingyNotFoundException("The sass/scss file " + rurl + " could not be loaded");

		boolean sourceMap = params.getString("__nomap", null) == null;

		Instance instance = borrow();
		boolean reusable = false;
		try {
			SassCompiler compiler = instance.getCompiler();
			compiler.setGenerateSourceMaps(sourceMap);
			compiler.setSourceMapIncludeSources(sourceMap);
			instance.getImporter().setResolver(resolver);

			CompileRequest.StringInput input = CompileRequest.StringInput.newBuilder()
				.setSource(entry.getContents())
				.setSyntax(entry.getSyntax())
				.setUrl(url)
				.setImporter(CompileRequest.Importer.newBuilder().setImporterId(instance.getImporter().getId()))
				.build();

			CompileSuccess result;
			try {
				result = compiler.compileString(input, OutputStyle.EXPANDED);
			} catch(SassCompilationFailedException sx) {
				reusable = true;						// A sheet that does not compile leaves the process perfectly usable.
				throw new SassException(sx.getCompileFailure().getFormatted(), sx);
			}
			reusable = true;

			output.write(result.getCss());
			String map = result.getSourceMap();
			if(!map.isEmpty()) {
				output.write("\n/*# sourceMappingURL=data:application/json;charset=utf-8;base64,");
				output.write(Base64.getEncoder().encodeToString(map.getBytes(StandardCharsets.UTF_8)));
				output.write(" */\n");
			}
		} finally {
			instance.getImporter().setResolver(null);
			if(reusable) {
				release(instance);
			} else {
				instance.close();
			}
			resolver.close();
		}
	}

	@Override
	public boolean available() {
		Boolean available;
		synchronized(this) {
			available = m_available;
		}
		if(null != available)
			return available.booleanValue();

		//-- Try to start a process; if that works we keep it for the first compilation.
		try {
			release(create());
			available = Boolean.TRUE;
		} catch(Exception | LinkageError x) {
			LOG.warn("Dart Sass is not available, falling back to the next compiler: " + x, x);
			available = Boolean.FALSE;
		}
		synchronized(this) {
			m_available = available;
		}
		return available.booleanValue();
	}

	@Override
	public void close() {
		while(true) {
			Instance instance;
			synchronized(this) {
				instance = m_idleList.poll();
				if(null == instance) {
					m_available = null;
					return;
				}
			}
			instance.close();
		}
	}

	private Instance borrow() throws IOException {
		synchronized(this) {
			Instance instance = m_idleList.poll();
			if(null != instance)
				return instance;
		}
		return create();
	}

	private void release(Instance instance) {
		synchronized(this) {
			if(m_idleList.size() < MAX_IDLE) {
				m_idleList.add(instance);
				return;
			}
		}
		instance.close();
	}

	private Instance create() throws IOException {
		long ts = System.nanoTime();
		String executable = DeveloperOptions.getString(EXECUTABLE_PROPERTY, System.getProperty(EXECUTABLE_PROPERTY, ""));
		SassCompiler compiler = executable.isEmpty()
			? de.larsgrefer.sass.embedded.SassCompilerFactory.bundled()
			: new SassCompiler(ConnectionFactory.ofExecutable(new File(executable)));

		/*
		 * The theme is on the module system, but its sheets still use slash division instead of
		 * math.div and the global colour functions (lighten, darken) instead of the sass:color
		 * module, and the application sheets in the demo still @import. All are deprecated in
		 * Dart Sass and warn on every compile, so they are silenced until that is done too. Every
		 * other warning must stay visible.
		 */
		compiler.addSilenceDeprecation("import");
		compiler.addSilenceDeprecation("slash-div");
		compiler.addSilenceDeprecation("global-builtin");
		compiler.addSilenceDeprecation("color-functions");

		DartSassImporter importer = new DartSassImporter();
		compiler.registerImporter(importer);
		LOG.info("Started a dart-sass process in " + (System.nanoTime() - ts) / 1000000 + "ms");
		return new Instance(compiler, importer);
	}
}
