package to.etc.domui.themes;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import to.etc.domui.themes.sass.SassThemeFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The colour schemes of the winter theme: every one compiles in its nature, each nature's files
 * agree with the other's, every scheme states the tokens its nature reads, and an application's
 * custominit files still configure them.
 */
public class TestThemeVariants {
	static private final Pattern DECLARATION = Pattern.compile("^\\$([a-z0-9-]+)\\s*:", Pattern.MULTILINE);

	static private final String LIGHT = SchemeVariant.WINTER.getVariantName();

	static private final String DARK = SchemeVariant.MIDNIGHT.getVariantName();

	static private ThemeVariantCompiler m_compiler;

	@Rule
	public TemporaryFolder m_tmp = new TemporaryFolder();

	@BeforeClass
	static public void setUp() throws Exception {
		m_compiler = new ThemeVariantCompiler(ThemeVariantCompiler.findThemeDir());
	}

	@AfterClass
	static public void tearDown() throws Exception {
		if(null != m_compiler)
			m_compiler.close();
	}

	static private List<SchemeVariant> schemes() {
		List<SchemeVariant> res = new ArrayList<>();
		for(IThemeVariant v : SassThemeFactory.INSTANCE.getVariants())
			res.add((SchemeVariant) v);
		return res;
	}

	/**
	 * Every scheme the theme offers has its directory, compiles, renders in its nature's colour
	 * scheme, and is a sheet of its own; the first is light and the first dark one is Midnight.
	 */
	@Test
	public void everySchemeCompiles() throws Exception {
		File theme = ThemeVariantCompiler.findThemeDir();
		List<SchemeVariant> schemes = schemes();
		Assert.assertEquals("The first scheme", SchemeVariant.WINTER, schemes.get(0));
		Assert.assertEquals("The first dark scheme", SchemeVariant.MIDNIGHT, schemes.stream().filter(s -> s.getNature() == ThemeNature.DARK).findFirst().orElse(null));
		Set<String> sheets = new TreeSet<>();
		for(SchemeVariant scheme : schemes) {
			String name = scheme.getVariantName();
			File dir = new File(theme, scheme.getNature().getName() + "/" + scheme.getSchemeName());
			Assert.assertTrue(name + ": no " + dir + "/_scheme.scss", new File(dir, "_scheme.scss").isFile());
			String css = m_compiler.compile(name);
			Assert.assertTrue(name + ": the sheet is suspiciously small", css.length() > 100_000);
			Assert.assertTrue(name + ": does not say color-scheme: " + scheme.getNature().getName(), css.contains("color-scheme: " + scheme.getNature().getName()));
			Assert.assertTrue(name + ": compiles to the sheet of another scheme", sheets.add(css));
			Assert.assertEquals(name + ": does not parse back", scheme, SchemeVariant.parse(name));
		}
	}

	/**
	 * Each nature has its own palette, so a variable that only one of them declares is a compile
	 * error in the other - but only once some sheet reads it. This finds it before that. The
	 * component colours are one file for every variant.
	 */
	@Test
	public void palettesDeclareTheSameColours() throws Exception {
		File theme = ThemeVariantCompiler.findThemeDir();
		Set<String> light = declared(new File(theme, "light/_palette.scss"));
		Set<String> dark = declared(new File(theme, "dark/_palette.scss"));
		Set<String> onlyLight = new TreeSet<>(light);
		onlyLight.removeAll(dark);
		Set<String> onlyDark = new TreeSet<>(dark);
		onlyDark.removeAll(light);
		Assert.assertEquals("Declared in light/_palette.scss but not in dark/_palette.scss", Set.of(), onlyLight);
		Assert.assertEquals("Declared in dark/_palette.scss but not in light/_palette.scss", Set.of(), onlyDark);
		for(ThemeNature nature : ThemeNature.values())
			Assert.assertFalse(nature.getName() + "/ has component colours of its own", new File(theme, nature.getName() + "/_component-colors.scss").exists());
		Assert.assertFalse("The theme directory has a palette of its own", new File(theme, "_palette.scss").exists());
	}

	/**
	 * Every scheme declares exactly the tokens its nature's palette reads (<code>s.$...</code>).
	 */
	@Test
	public void schemesDeclareTheirNaturesTokens() throws Exception {
		File theme = ThemeVariantCompiler.findThemeDir();
		for(SchemeVariant scheme : schemes()) {
			String nature = scheme.getNature().getName();
			Set<String> read = tokensRead(new File(theme, nature + "/_palette.scss"));
			Assert.assertEquals(scheme + "'s tokens", read, declared(new File(theme, nature + "/" + scheme.getSchemeName() + "/_scheme.scss")));
		}
	}

	/**
	 * Every exception names a component colour that _component-colors.scss declares; an
	 * exception for a colour that no longer exists is a compile error.
	 */
	@Test
	public void exceptionsNameComponentColours() throws Exception {
		File theme = ThemeVariantCompiler.findThemeDir();
		Set<String> component = declared(new File(theme, "_component-colors.scss"));
		List<File> files = new ArrayList<>();
		for(ThemeNature nature : ThemeNature.values())
			files.add(new File(theme, nature.getName() + "/_nature-exceptions.scss"));
		for(SchemeVariant scheme : schemes())
			files.add(new File(theme, scheme.getNature().getName() + "/" + scheme.getSchemeName() + "/_scheme-exceptions.scss"));
		for(File f : files) {
			if(!f.exists())
				continue;
			Set<String> unknown = new TreeSet<>(declared(f));
			unknown.removeAll(component);
			Assert.assertEquals("Exceptions in " + f + " for colours that do not exist", Set.of(), unknown);
		}
	}

	/**
	 * An application's _custominit.scss outranks the theme: a colour set there is used by every
	 * scheme, and what is computed from it is computed from the application's value.
	 */
	@Test
	public void customInitWinsOverTheVariant() throws Exception {
		File app = m_tmp.newFolder("winter");
		write(new File(app, "_custominit.scss"), "$link-text: #123456;\n$primary-solid: #00aa00;\n");
		try(ThemeVariantCompiler c = new ThemeVariantCompiler(ThemeVariantCompiler.findThemeDir(), app)) {
			for(String variant : new String[]{LIGHT, DARK}) {
				String css = c.compile(variant);
				Assert.assertTrue(variant + ": the application's $link-text is not used", css.contains("#123456"));
				Assert.assertTrue(variant + ": the application's $primary-solid is not used", css.contains("#00aa00"));
			}
		}
	}

	/**
	 * An application's _variant-custominit.scss in a nature's directory applies to that nature's
	 * schemes only, and wins over _custominit.scss; one in a scheme's directory applies to that
	 * scheme only, instead of the nature's.
	 */
	@Test
	public void variantCustomInitIsPerNatureAndScheme() throws Exception {
		File app = m_tmp.newFolder("winter");
		File appLight = new File(app, "light");
		File appDark = new File(app, "dark");
		File appNord = new File(appDark, "nord");
		appNord.mkdirs();
		appLight.mkdirs();
		write(new File(app, "_custominit.scss"), "$primary-solid: #00aa00;\n$link-text: #0000aa;\n");
		write(new File(appLight, "_variant-custominit.scss"), "$link-text: #111111;\n");
		write(new File(appDark, "_variant-custominit.scss"), "$link-text: #222222;\n$primary-solid: #00bb00;\n");
		write(new File(appNord, "_variant-custominit.scss"), "$link-text: #333333;\n");
		try(ThemeVariantCompiler c = new ThemeVariantCompiler(ThemeVariantCompiler.findThemeDir(), app)) {
			String light = c.compile(LIGHT);
			String dark = c.compile(DARK);
			String nord = c.compile(SchemeVariant.NORD.getVariantName());
			Assert.assertTrue("light: its own $link-text is not used", light.contains("#111111"));
			Assert.assertFalse("light: the dark nature's file is used", light.contains("#222222") || light.contains("#00bb00"));
			Assert.assertFalse("light: _custominit wins over _variant-custominit", light.contains("#0000aa"));
			Assert.assertTrue("light: _custominit's $primary-solid is not used", light.contains("#00aa00"));

			Assert.assertTrue("dark: its own $link-text is not used", dark.contains("#222222"));
			Assert.assertFalse("dark: the light nature's file is used", dark.contains("#111111"));
			Assert.assertTrue("dark: its own $primary-solid is not used", dark.contains("#00bb00"));
			Assert.assertFalse("dark: _custominit wins over _variant-custominit", dark.contains("#00aa00") || dark.contains("#0000aa"));

			Assert.assertTrue("nord: its own $link-text is not used", nord.contains("#333333"));
			Assert.assertFalse("nord: the dark nature's file is used", nord.contains("#222222"));
		}
	}

	/**
	 * Every image a compiled sheet refers to exists for that scheme: in its nature's directory or
	 * in the theme directory. This is what catches an image that was deleted while a rule still
	 * uses it.
	 */
	@Test
	public void everyImageExists() throws Exception {
		File theme = ThemeVariantCompiler.findThemeDir();
		Pattern url = Pattern.compile("url\\(\\s*[\"']?([^\"')]+)[\"']?\\s*\\)");
		for(SchemeVariant scheme : List.of(SchemeVariant.WINTER, SchemeVariant.MIDNIGHT)) {
			Matcher m = url.matcher(m_compiler.compile(scheme.getVariantName()));
			Set<String> missing = new TreeSet<>();
			while(m.find()) {
				String ref = m.group(1).trim();
				if(ref.isEmpty() || ref.startsWith("data:") || ref.contains("://") || ref.startsWith("#"))
					continue;
				if(!new File(theme, ref).isFile() && !new File(theme, scheme.getNature().getName() + "/" + ref).isFile())
					missing.add(ref);
			}
			Assert.assertEquals("Images the " + scheme + " sheet refers to that do not exist", Set.of(), missing);
		}
	}

	/**
	 * The natures have the same images: one that only one of them has is either left over, or
	 * missing in the other. And the theme directory has no copy of an image both natures have.
	 */
	@Test
	public void naturesHaveTheSameImages() throws Exception {
		File theme = ThemeVariantCompiler.findThemeDir();
		Set<String> light = images(new File(theme, "light"));
		Set<String> dark = images(new File(theme, "dark"));
		Set<String> onlyLight = new TreeSet<>(light);
		onlyLight.removeAll(dark);
		Set<String> onlyDark = new TreeSet<>(dark);
		onlyDark.removeAll(light);
		Assert.assertEquals("Images in light/ but not in dark/", Set.of(), onlyLight);
		Assert.assertEquals("Images in dark/ but not in light/", Set.of(), onlyDark);
		Set<String> shadowed = new TreeSet<>(images(theme));
		shadowed.retainAll(light);
		Assert.assertEquals("Images in the theme directory that both natures replace", Set.of(), shadowed);
	}

	static private Set<String> images(File dir) {
		Set<String> res = new TreeSet<>();
		File[] files = dir.listFiles();
		Assert.assertNotNull(files);
		for(File f : files) {
			String n = f.getName();
			if(n.endsWith(".png") || n.endsWith(".gif"))
				res.add(n);
		}
		return res;
	}

	static private Set<String> tokensRead(File palette) throws Exception {
		Matcher m = Pattern.compile("\\bs\\.\\$([a-z0-9-]+)").matcher(Files.readString(palette.toPath(), StandardCharsets.UTF_8));
		Set<String> res = new TreeSet<>();
		while(m.find())
			res.add(m.group(1));
		return res;
	}

	static private void write(File f, String content) throws Exception {
		Files.writeString(f.toPath(), content, StandardCharsets.UTF_8);
	}

	static private Set<String> declared(File file) throws Exception {
		Set<String> names = new TreeSet<>();
		Matcher m = DECLARATION.matcher(Files.readString(file.toPath(), StandardCharsets.UTF_8));
		while(m.find())
			names.add(m.group(1));
		return names;
	}
}
