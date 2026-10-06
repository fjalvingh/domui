package to.etc.domui.themes;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The variants of the winter theme: both compile, each states its own colours, and an
 * application's _custominit.scss still configures them.
 */
public class TestThemeVariants {
	static private final Pattern DECLARATION = Pattern.compile("^\\$([a-z0-9-]+)\\s*:", Pattern.MULTILINE);

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

	@Test
	public void bothVariantsCompile() throws Exception {
		String light = m_compiler.compile("default");
		String dark = m_compiler.compile("dark");
		Assert.assertTrue("The light sheet is suspiciously small", light.length() > 100_000);
		Assert.assertTrue("The dark sheet is suspiciously small", dark.length() > 100_000);
		Assert.assertNotEquals("The dark variant compiles to the light sheet", light, dark);
		Assert.assertTrue("The dark variant does not say color-scheme: dark", dark.contains("color-scheme: dark"));
	}

	/**
	 * A variant's colour files replace the light ones, so a variable that only one of them
	 * declares is a compile error in the other - but only once some sheet reads it. This finds it
	 * before that.
	 */
	@Test
	public void variantsDeclareTheSameColours() throws Exception {
		File theme = ThemeVariantCompiler.findThemeDir();
		for(String name : new String[]{"_palette.scss", "_component-colors.scss"}) {
			Set<String> light = declared(new File(theme, name));
			Set<String> dark = declared(new File(theme, "dark/" + name));
			Set<String> onlyLight = new TreeSet<>(light);
			onlyLight.removeAll(dark);
			Set<String> onlyDark = new TreeSet<>(dark);
			onlyDark.removeAll(light);
			Assert.assertEquals("Declared in " + name + " but not in dark/" + name, Set.of(), onlyLight);
			Assert.assertEquals("Declared in dark/" + name + " but not in " + name, Set.of(), onlyDark);
		}
	}

	/**
	 * An application's _custominit.scss outranks the variant: a colour set there is used by both
	 * variants, and what is computed from it is computed from the application's value.
	 */
	@Test
	public void customInitWinsOverTheVariant() throws Exception {
		File app = m_tmp.newFolder("winter");
		Files.writeString(new File(app, "_custominit.scss").toPath(), "$link-color: #123456;\n$primary: #00aa00;\n", StandardCharsets.UTF_8);
		try(ThemeVariantCompiler c = new ThemeVariantCompiler(ThemeVariantCompiler.findThemeDir(), app)) {
			for(String variant : new String[]{"default", "dark"}) {
				String css = c.compile(variant);
				Assert.assertTrue(variant + ": the application's $link-color is not used", css.contains("#123456"));
				Assert.assertTrue(variant + ": the application's $primary is not used", css.contains("#00aa00"));
			}
		}
	}

	/**
	 * An application's _variant-custominit.scss applies to its own variant only, and wins over
	 * _custominit.scss. The light one, in the theme directory itself, must not reach the dark
	 * variant: DomUI's empty dark/_variant-custominit.scss comes first on the search path.
	 */
	@Test
	public void variantCustomInitIsPerVariant() throws Exception {
		File app = m_tmp.newFolder("winter");
		File appDark = new File(app, "dark");
		appDark.mkdirs();
		write(new File(app, "_custominit.scss"), "$primary: #00aa00;\n$link-color: #0000aa;\n");
		write(new File(app, "_variant-custominit.scss"), "$link-color: #111111;\n");
		write(new File(appDark, "_variant-custominit.scss"), "$link-color: #222222;\n$primary: #00bb00;\n");
		try(ThemeVariantCompiler c = new ThemeVariantCompiler(ThemeVariantCompiler.findThemeDir(), app)) {
			String light = c.compile("default");
			String dark = c.compile("dark");
			Assert.assertTrue("light: its own $link-color is not used", light.contains("#111111"));
			Assert.assertFalse("light: the dark variant's file is used", light.contains("#222222") || light.contains("#00bb00"));
			Assert.assertFalse("light: _custominit wins over _variant-custominit", light.contains("#0000aa"));
			Assert.assertTrue("light: _custominit's $primary is not used", light.contains("#00aa00"));

			Assert.assertTrue("dark: its own $link-color is not used", dark.contains("#222222"));
			Assert.assertFalse("dark: the light variant's file is used", dark.contains("#111111"));
			Assert.assertTrue("dark: its own $primary is not used", dark.contains("#00bb00"));
			Assert.assertFalse("dark: _custominit wins over _variant-custominit", dark.contains("#00aa00") || dark.contains("#0000aa"));
		}

		//-- Without an application dark file, the light one still must not reach dark.
		new File(appDark, "_variant-custominit.scss").delete();
		try(ThemeVariantCompiler c = new ThemeVariantCompiler(ThemeVariantCompiler.findThemeDir(), app)) {
			String dark = c.compile("dark");
			Assert.assertFalse("dark: the light variant's file is used", dark.contains("#111111"));
			Assert.assertTrue("dark: _custominit's $link-color is not used", dark.contains("#0000aa"));
		}
	}

	/**
	 * Every image a compiled sheet refers to exists for that variant: in its own directory or in
	 * the theme directory. This is what catches an image that was deleted while a rule still uses
	 * it, and a dark copy that lost its light original.
	 */
	@Test
	public void everyImageExists() throws Exception {
		File theme = ThemeVariantCompiler.findThemeDir();
		Pattern url = Pattern.compile("url\\(\\s*[\"']?([^\"')]+)[\"']?\\s*\\)");
		for(String variant : new String[]{"default", "dark"}) {
			Matcher m = url.matcher(m_compiler.compile(variant));
			Set<String> missing = new TreeSet<>();
			while(m.find()) {
				String ref = m.group(1).trim();
				if(ref.isEmpty() || ref.startsWith("data:") || ref.contains("://") || ref.startsWith("#"))
					continue;
				boolean found = new File(theme, ref).isFile() || (!"default".equals(variant) && new File(theme, variant + "/" + ref).isFile());
				if(!found)
					missing.add(ref);
			}
			Assert.assertEquals("Images the " + variant + " sheet refers to that do not exist", Set.of(), missing);
		}
	}

	/**
	 * Every image in a variant's directory shadows one of the theme's own: a dark copy of an image
	 * that no longer exists in the light theme is left over.
	 */
	@Test
	public void everyVariantImageHasALightOriginal() throws Exception {
		File theme = ThemeVariantCompiler.findThemeDir();
		Set<String> orphans = new TreeSet<>();
		File[] files = new File(theme, "dark").listFiles();
		Assert.assertNotNull(files);
		for(File f : files) {
			String n = f.getName();
			if((n.endsWith(".png") || n.endsWith(".gif")) && !new File(theme, n).isFile())
				orphans.add(n);
		}
		Assert.assertEquals("Images in dark/ without a light original", Set.of(), orphans);
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
