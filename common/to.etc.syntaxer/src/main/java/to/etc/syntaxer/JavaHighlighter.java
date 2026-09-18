package to.etc.syntaxer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Highlighter for Java source code. The generic rules in {@link HiParser} already
 * implement Java's lexical structure (// and slash-star comments, string escapes
 * including \ u sequences, and the numeric formats including hex, binary, octal and
 * digit separators), so all this needs to add is the keyword sets.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-05-22.
 */
public class JavaHighlighter extends HiParser implements IHighlighter {
	public JavaHighlighter() {
		//-- Java is case sensitive, so the keyword map is left case dependent.
		initKeywords();
	}

	private void initKeywords() {
		//-- The primitive types, plus the java.lang types that are visible without an import.
		addKeywords(HighlightTokenType.type,
			"boolean",
			"byte",
			"char",
			"double",
			"float",
			"int",
			"long",
			"short",
			"void",

			"Boolean",
			"Byte",
			"Character",
			"CharSequence",
			"Class",
			"Comparable",
			"Double",
			"Enum",
			"Exception",
			"Float",
			"Integer",
			"Iterable",
			"Long",
			"Math",
			"Number",
			"Object",
			"Runnable",
			"RuntimeException",
			"Short",
			"String",
			"StringBuilder",
			"System",
			"Thread",
			"Throwable"
		);

		//-- The reserved keywords of the language.
		addKeywords(HighlightTokenType.keyword1,
			"abstract",
			"assert",
			"break",
			"case",
			"catch",
			"class",
			"const",
			"continue",
			"default",
			"do",
			"else",
			"enum",
			"extends",
			"final",
			"finally",
			"for",
			"goto",
			"if",
			"implements",
			"import",
			"instanceof",
			"interface",
			"native",
			"new",
			"package",
			"private",
			"protected",
			"public",
			"return",
			"static",
			"strictfp",
			"super",
			"switch",
			"synchronized",
			"this",
			"throw",
			"throws",
			"transient",
			"try",
			"volatile",
			"while"
		);

		//-- Contextual keywords: these are only keywords in some places, and remain
		//-- valid identifiers elsewhere. They are highlighted differently for that reason.
		addKeywords(HighlightTokenType.keyword2,
			"permits",
			"record",
			"sealed",
			"var",
			"yield"
		);

		//-- The literal values.
		addKeywords(HighlightTokenType.keyword3,
			"false",
			"null",
			"true"
		);
	}

	@Override
	protected void tokenFound(HighlightTokenType type, String text, int characterIndex) {
		m_renderer.renderToken(type, text, characterIndex);
	}

	@NonNull
	@Override
	public LineContext highlightLine(IHighlightRenderer renderer, @Nullable LineContext previous, @NonNull String line) {
		m_renderer = renderer;
		return start(line, previous);
	}
}
