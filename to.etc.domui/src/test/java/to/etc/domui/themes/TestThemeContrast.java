package to.etc.domui.themes;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import to.etc.domui.themes.sass.SassThemeFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The text and ground colours of the theme that belong together have a WCAG contrast of at
 * least 4.5:1 (AA for ordinary text), or 3:1 where the text is large or bold, in every variant.
 *
 * <p>The colours are resolved by compiling a small sheet against the theme for the variant, which
 * writes out the red, green, blue and alpha channels of each pair; the ratio is computed here, with
 * the sRGB definition. (The theme's own contrastRatio() approximates, on purpose, and is only good
 * for comparing two ratios.) A foreground with an alpha is composited over its ground first, and a
 * ground that is not opaque over the page.</p>
 */
public class TestThemeContrast {
	static private final double AA = 4.5;

	static private final double AA_LARGE = 3.0;

	/** label, foreground variable, ground variable, minimum ratio */
	static private final Object[][] PAIRS = {
		{"text on the page", "text-color", "body-bg", AA},
		{"strong text on the page", "text-strong-color", "body-bg", AA},
		{"muted text on the page", "text-muted", "body-bg", AA},
		{"muted text on a panel", "text-muted", "surface-bg", AA},
		{"body text on the page", "body-color", "body-bg", AA},
		{"text on a panel", "surface-color", "surface-bg", AA},
		{"text on a floating window", "surface-color", "window-bg", AA},
		{"text on a band in a panel", "surface-color", "surface-alt-bg", AA},
		{"a form label", "form-label-color", "body-bg", AA},
		{"text in an input", "input-color", "input-bg", AA},
		{"a link", "link-color", "body-bg", AA},
		{"a visited link", "link-visited-color", "body-bg", AA},
		{"the page title", "title-color", "title-bg", AA},
		{"an error text on the page", "errors-color", "body-bg", AA},
		{"an error message", "errors-color", "errors-wash", AA},
		{"a warning message", "warnings-color", "warnings-bg", AA},
		{"an info message", "info-color", "info-bg", AA},
		{"text on a hovered menu entry", "surface-color", "menu-hover-bg", AA},
		{"text on a hovered row", "body-color", "row-hover-bg", AA},
		{"text on a marked row", "body-color", "highlight-bg", AA},
		{"the DataTable header", "dt-hdr-color", "dt-hdr-bg", AA_LARGE},
		{"text on a selected DataTable row", "body-color", "dt-selected-bg", AA},
		{"text on an even DataTable row", "body-color", "dt-even-row-bg", AA},
		{"a tab's title", "tab-color", "tab-bg", AA},
		{"the selected tab's title", "tab-selected-color", "tab-content-bg", AA},
		{"a popup menu item", "pmnu-color", "pmnu-bg", AA},
		{"a primary button's text", "primary-invert", "primary", AA_LARGE},
		{"a breadcrumb", "brcr2-color", "brcr2-bg", AA},
		{"the current breadcrumb", "brcr2-sel-color", "brcr2-sel-bg", AA},
		{"the calendar", "cal-color", "cal-bg", AA},
		{"an info explanation", "expl-color", "expl-info-bg", AA},
		{"a warning explanation", "expl-color", "expl-warning-bg", AA},
		{"an error explanation", "expl-color", "expl-error-bg", AA},
		{"an info explanation's marker", "expl-info-marker-color", "expl-info-accent", AA_LARGE},
		{"a warning explanation's marker", "expl-warning-marker-color", "expl-warning-accent", AA_LARGE},
		{"an error explanation's marker", "expl-error-marker-color", "expl-error-accent", AA_LARGE},
		{"a question box's marker", "sev-question-color", "sev-question-bg", AA_LARGE},
		{"an error flare", "flare-error-color", "flare-error-bg", AA},
		{"an info flare", "flare-info-color", "flare-info-bg", AA},
		{"a warning flare", "flare-warning-color", "flare-warning-bg", AA},
	};

	/**
	 * Light-variant pairs that are below their minimum today. They are listed rather than fixed
	 * because fixing them changes the light theme, which this work does not do; the test fails
	 * when one of them is fixed, so the list is kept honest.
	 */
	static private final List<String> KNOWN_LIGHT = List.of(
		"muted text on the page", "muted text on a panel", "the page title", "an error text on the page",
		"an error message", "an info message", "a breadcrumb", "the current breadcrumb"
	);

	static private ThemeVariantCompiler m_compiler;

	@BeforeClass
	static public void setUp() throws Exception {
		m_compiler = new ThemeVariantCompiler(ThemeVariantCompiler.findThemeDir());
	}

	@AfterClass
	static public void tearDown() throws Exception {
		if(null != m_compiler)
			m_compiler.close();
	}

	@Test
	public void darkVariant() throws Exception {
		List<String> failures = check("dark");
		Assert.assertEquals("Pairs below their minimum contrast in the dark variant", List.of(), failures);
	}

	/**
	 * Every dark colour scheme the theme offers, held to the same pairs as the dark variant.
	 */
	@Test
	public void darkSchemes() throws Exception {
		List<String> failures = new ArrayList<>();
		for(IThemeVariant variant : SassThemeFactory.INSTANCE.getVariants()) {
			if(variant instanceof DarkSchemeVariant)
				check(variant.getVariantName()).forEach(f -> failures.add(variant.getVariantName() + ": " + f));
		}
		Assert.assertEquals("Pairs below their minimum contrast in the dark schemes", List.of(), failures);
	}

	@Test
	public void lightVariant() throws Exception {
		List<String> failures = check("default");
		List<String> labels = failures.stream().map(f -> f.substring(0, f.indexOf(':'))).toList();
		Assert.assertEquals("Light-variant pairs below their minimum, against the known list", KNOWN_LIGHT, labels);
	}

	/**
	 * The pairs below their minimum in the variant, as "label: ratio < minimum (fg on bg)", and all
	 * of them printed for whoever runs the test.
	 */
	static private List<String> check(String variant) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("@use \"sass:color\";\n@use \"sass:meta\";\n@use \"theme\" as t;\n");
		sb.append("@function ch($c) {\n")
			.append("\t@if meta.type-of($c) != color { @return none; }\n")
			.append("\t$c: color.to-space($c, rgb);\n")
			.append("\t@return color.channel($c, \"red\", $space: rgb) color.channel($c, \"green\", $space: rgb) color.channel($c, \"blue\", $space: rgb) color.channel($c, \"alpha\");\n")
			.append("}\n");
		sb.append(".page { c: ch(t.$body-bg); }\n");
		for(int i = 0; i < PAIRS.length; i++) {
			sb.append(".p").append(i).append(" { fg: ch(t.$").append(PAIRS[i][1]).append("); bg: ch(t.$").append(PAIRS[i][2]).append("); }\n");
		}
		String css = m_compiler.compileConfigured(variant, sb.toString());

		double[] page = channels(css, "page", "c");
		List<String> failures = new ArrayList<>();
		System.out.println("Contrast in the " + variant + " variant:");
		for(int i = 0; i < PAIRS.length; i++) {
			String label = (String) PAIRS[i][0];
			double min = (Double) PAIRS[i][3];
			double[] fg = channels(css, "p" + i, "fg");
			double[] bg = channels(css, "p" + i, "bg");
			if(null == fg || null == bg || null == page) {
				System.out.println(String.format(Locale.ROOT, "  %-36s (not a colour in this variant)", label));
				continue;
			}
			double[] ground = over(bg, page);
			double ratio = ratio(over(fg, ground), ground);
			String line = String.format(Locale.ROOT, "%s: %.2f < %.1f ($%s on $%s)", label, ratio, min, PAIRS[i][1], PAIRS[i][2]);
			System.out.println(String.format(Locale.ROOT, "  %-36s %5.2f %s", label, ratio, ratio < min ? "BELOW " + min : ""));
			if(ratio < min)
				failures.add(line);
		}
		return failures;
	}

	/** The channels the check sheet wrote for a property of a rule, or null when it was not a colour. */
	static private double[] channels(String css, String rule, String property) {
		Matcher m = Pattern.compile("\\." + rule + " \\{[^}]*?\\b" + property + ": ([^;]+);").matcher(css);
		if(!m.find())
			throw new IllegalStateException("No ." + rule + " " + property + " in the compiled check");
		String v = m.group(1).trim();
		if(v.equals("none"))
			return null;
		String[] parts = v.split("\\s+");
		double[] res = new double[4];
		for(int i = 0; i < 4; i++)
			res[i] = Double.parseDouble(parts[i]);
		return res;
	}

	/** The colour composited over an opaque ground. */
	static private double[] over(double[] c, double[] ground) {
		double a = c[3];
		return new double[]{c[0] * a + ground[0] * (1 - a), c[1] * a + ground[1] * (1 - a), c[2] * a + ground[2] * (1 - a), 1};
	}

	static private double ratio(double[] a, double[] b) {
		double la = luminance(a);
		double lb = luminance(b);
		return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
	}

	static private double luminance(double[] c) {
		return 0.2126 * lin(c[0]) + 0.7152 * lin(c[1]) + 0.0722 * lin(c[2]);
	}

	static private double lin(double channel) {
		double v = channel / 255.0;
		return v <= 0.04045 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
	}
}
