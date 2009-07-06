package to.etc.domui.components.basic;

import java.io.*;
import java.text.*;
import java.util.*;

import to.etc.domui.component.layout.*;
import to.etc.domui.dom.html.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;

public class OddCharacters extends FloatingWindow {
	final String[] fchars = {"&euro;", "é", "í", "ó", "ë", "ï", "ö", "è"};

	final String[] chars = {"&euro;", "ƒ", "„", "…", "†", "‡", "ˆ", "‰", "Š", "‹", "Œ", "&lsquo;", "&rsquo;", "&rsquo;", "&ldquo;", "&rdquo;", "•", "&ndash;", "&mdash;", "˜", "™", "š", "›", "œ", "Ÿ",
		"&iexcl;", "&cent;", "&pound;", "&pound;", "&curren;", "&yen;", "&brvbar;", "&sect;", "&uml;", "&copy;", "&ordf;", "&laquo;", "&not;", "­", "&reg;", "&macr;", "&deg;", "&plusmn;", "&sup2;",
		"&sup3;", "&acute;", "&micro;", "&para;", "&middot;", "&cedil;", "&sup1;", "&ordm;", "&raquo;", "&frac14;", "&frac12;", "&frac34;", "&iquest;", "&Agrave;", "&Aacute;", "&Acirc;", "&Atilde;",
		"&Auml;", "&Aring;", "&AElig;", "&Ccedil;", "&Egrave;", "&Eacute;", "&Ecirc;", "&Euml;", "&Igrave;", "&Iacute;", "&Icirc;", "&Iuml;", "&ETH;", "&Ntilde;", "&Ograve;", "&Oacute;", "&Ocirc;",
		"&Otilde;", "&Ouml;", "&times;", "&Oslash;", "&Ugrave;", "&Uacute;", "&Ucirc;", "&Uuml;", "&Yacute;", "&THORN;", "&szlig;", "&agrave;", "&aacute;", "&acirc;", "&atilde;", "&auml;", "&aring;",
		"&aelig;", "&ccedil;", "&egrave;", "&eacute;", "&ecirc;", "&euml;", "&igrave;", "&iacute;", "&icirc;", "&iuml;", "&eth;", "&ntilde;", "&ograve;", "&oacute;", "&ocirc;", "&otilde;", "&ouml;",
		"&divide;", "&oslash;", "&ugrave;", "&uacute;", "&ucirc;", "&uuml;", "&uuml;", "&yacute;", "&thorn;", "&yuml;"};

	@Override
	public void createContent() throws Exception {
		setWidth("400px");
		setWindowTitle("Speciale tekens en teksten");
		super.createContent();
		TabPanel tp = new TabPanel();
		add(tp);
		tp.add(createTekens(), "Tekens", "img/btnSpecialChar.png");
		tp.add(createDates(), "Datum/tijd", "img/btnClock.png");
	}

	private NodeBase createTekens() throws IOException {
		Div root = new Div();
		root.setCssClass("vp-oddchars");
		Table tbl = new Table();
		root.add(tbl);
		TBody b = new TBody();
		tbl.add(b);
		tbl.setCssClass("vp-oddchars-tbl");
		tbl.setCellSpacing("0");
		tbl.setCellPadding("0");
		b.addRow();
		TD td = b.addCell();
		td.setColspan(fchars.length);
		td.add("Vaak gebruikte speciale tekens");
		td.setCssClass("vp-oddchars-ttl");
		b.addRow();
		int i = 0;
		StringBuilder sb = new StringBuilder(32);
		for(String t : fchars) {
			StringTool.entitiesToUnicode(sb, t, true);
			td = b.addCell();
			td.setText(sb.toString());
			td.setCssClass("vp-oddchars-c");
			td.setOnClickJS("WebUI.oddChar(this)");
			sb.setLength(0);
		}

		//-- 2nd thingy.
		tbl = new Table();
		root.add(tbl);
		tbl.setCssClass("vp-oddchars-tbl2");
		tbl.setCellSpacing("0");
		tbl.setCellPadding("0");
		b.addRow();
		int width = 15;
		td = b.addCell();
		td.setColspan(width);
		td.add("Alle speciale tekens");

		int ix = 0;
		while(ix < chars.length) {
			b.addRow();
			for(i = 0; i < width; i++) {
				td = b.addCell();
				//				td.setCssClass("vp-oddchars-c");

				if(ix < chars.length) {
					td.setCssClass("vp-oddchars-c");
					sb.setLength(0);
					StringTool.entitiesToUnicode(sb, chars[ix++], true);
					td.add(sb.toString());
					td.setOnClickJS("WebUI.oddChar(this)");
				}
			}
		}
		return root;
	}

	private NodeBase createDates() {
		Div root = new Div();
		Locale l = NlsContext.getLocale();
		addDate(root, DateFormat.getDateInstance(DateFormat.FULL, l));
		addDate(root, DateFormat.getDateInstance(DateFormat.LONG, l));
		addDate(root, DateFormat.getDateInstance(DateFormat.MEDIUM, l));
		addDate(root, DateFormat.getDateInstance(DateFormat.SHORT, l));
		addDate(root, DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, l));
		addDate(root, DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL, l));
		addDate(root, DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL, l));
		addDate(root, DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM, l));
		addDate(root, DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, l));
		addDate(root, DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, l));
		addDate(root, DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT, l));
		addDate(root, DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, l));
		addDate(root, DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, l));
		//		addDate(root, new SimpleDateFormat("", l));


		return root;
	}

	private void addDate(final NodeContainer c, final DateFormat df) {
		Div s = new Div();
		c.add(s);
		s.setCssClass("vp-oddchars-dt");
		Date d = new Date();
		s.add(df.format(d));
		s.setOnClickJS("WebUI.oddChar(this)");
	}

}
