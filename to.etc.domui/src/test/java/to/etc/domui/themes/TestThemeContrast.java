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
 * least 4.5:1 (AA for ordinary text), or 3:1 where the text is large or bold, or is the label
 * of a disabled or inactive control - which WCAG exempts, but which must still be readable -
 * in every variant.
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

	/** The colour families of the roles: each has -wash, -tint, -solid, -on-solid, -text and -border. */
	static private final String[] FAMILIES = {"primary", "control", "neutral", "info", "success", "warning", "danger", "chrome"};

	/** label, foreground variable, ground variable, minimum ratio */
	static private final Object[][] PAIRS = pairs();

	/**
	 * The pairs: first the contrast rules of the colour roles themselves (finished-plans/COLOR-ROLES.md 3.1),
	 * which every scheme must keep; then the pairs a component puts together, which hold as long
	 * as the component colours map to the right roles.
	 */
	static private Object[][] pairs() {
		List<Object[]> res = new ArrayList<>();
		//-- Text on the surfaces
		for(String ground : new String[]{"surface-page", "surface-raised", "surface-overlay", "surface-band"}) {
			res.add(new Object[]{"text on " + ground, "text-default", ground, AA});
			res.add(new Object[]{"strong text on " + ground, "text-strong", ground, AA});
			res.add(new Object[]{"subtle text on " + ground, "text-subtle", ground, AA});
		}
		res.add(new Object[]{"text on the inverse surface", "text-inverse", "surface-inverse", AA});
		res.add(new Object[]{"a value in an input", "text-strong", "field-surface", AA});
		res.add(new Object[]{"a value in a read-only input", "text-strong", "field-surface-readonly", AA});
		res.add(new Object[]{"a link", "link-text", "surface-page", AA});
		res.add(new Object[]{"a visited link", "link-text-visited", "surface-page", AA});
		res.add(new Object[]{"a link on a panel", "link-text", "surface-raised", AA});
		//-- States
		res.add(new Object[]{"text on a hovered item", "text-default", "hover-wash", AA});
		res.add(new Object[]{"text on a selected item", "text-default", "selected-wash", AA});
		res.add(new Object[]{"a link on a selected item", "link-text", "selected-wash", AA});
		res.add(new Object[]{"text on the selected solid", "selected-on-solid", "selected-solid", AA});
		res.add(new Object[]{"strong text on a highlight", "text-strong", "highlight", AA});
		res.add(new Object[]{"a disabled control's text", "disabled-text", "disabled-surface", AA_LARGE});
		//-- Every family's steps
		for(String f : FAMILIES) {
			res.add(new Object[]{f + "-text on the page", f + "-text", "surface-page", AA});
			res.add(new Object[]{f + "-text on its wash", f + "-text", f + "-wash", AA});
			res.add(new Object[]{"text on " + f + "-wash", "text-default", f + "-wash", AA});
			res.add(new Object[]{"strong text on " + f + "-tint", "text-strong", f + "-tint", AA});
			res.add(new Object[]{f + "-on-solid on its solid", f + "-on-solid", f + "-solid", AA});
		}

		//-- What components put together
		Object[][] components = {
			{"a form label", "text-strong", "surface-page", AA},
			{"a title bar", "caption-color", "caption-bg", AA},
			{"an error text on the page", "error-text-color", "surface-page", AA},
			{"an error message", "emd-error-color", "emd-error-bg", AA},
			{"a warning message", "emd-warning-color", "emd-warning-bg", AA},
			{"an info message", "emd-info-color", "emd-info-bg", AA},
			{"the DataTable header", "dt-hdr-color", "dt-hdr-bg", AA},
			{"text on a selected DataTable row", "text-default", "dt-selected-bg", AA},
			{"text on an even DataTable row", "text-default", "dt-even-row-bg", AA},
			{"text on a hovered DataTable row", "text-default", "dt-hover-bg", AA},
			{"a tab's title", "tab-color", "tab-bg", AA},
			{"the selected tab's title", "tab-selected-color", "tab-content-bg", AA},
			{"a popup menu item", "pmnu-color", "pmnu-bg", AA},
			{"a PopupMenu2 item", "pome2-color", "pome2-bg", AA},
			{"a primary button's text", "primary-on-solid", "primary-solid", AA},
			{"a breadcrumb", "brcr2-color", "brcr2-bg", AA},
			{"the current breadcrumb", "brcr2-sel-color", "brcr2-sel-bg", AA},
			{"the calendar", "cal-color", "cal-bg", AA},
			{"the calendar's month title", "cal-month-title-color", "cal-band-active-bg", AA},
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
			{"a switch's other label, off", "ckb-off-idle-color", "ckb-off-track-bg", AA_LARGE},
			{"a switch's other label, on", "ckb-on-idle-color", "ckb-on-bg", AA_LARGE},
			{"a switch's knob, on", "ckb-on-knob-color", "ckb-on-knob-bg", AA_LARGE},
			{"the chosen radio button", "rbb-chosen-color", "rbb-chosen-bg", AA},
			{"a disabled radio button", "rbb-disabled-label-color", "rbb-disabled-bg", AA_LARGE},
			{"a chip", "tag-color", "tag-bg", AA},
			{"a pager button", "dp2-button-color", "dp2-button-bg", AA},
		};
		res.addAll(List.of(components));
		return res.toArray(new Object[0][]);
	}

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

	/**
	 * Every dark colour scheme the theme offers.
	 */
	@Test
	public void darkSchemes() throws Exception {
		List<String> failures = new ArrayList<>();
		for(IThemeVariant variant : SassThemeFactory.INSTANCE.getVariants()) {
			if(ThemeNature.DARK.getName().equals(variant.getColorScheme()))
				check(variant.getVariantName()).forEach(f -> failures.add(variant.getVariantName() + ": " + f));
		}
		Assert.assertEquals("Pairs below their minimum contrast in the dark schemes", List.of(), failures);
	}

	@Test
	public void lightVariant() throws Exception {
		Assert.assertEquals("Pairs below their minimum contrast in the light scheme", List.of(), check(SchemeVariant.WINTER.getVariantName()));
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
		sb.append(".page { c: ch(t.$surface-page); }\n");
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
