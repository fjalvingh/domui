package to.etc.domui.utils;

import java.util.*;

import to.etc.domui.annotations.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.nls.*;

final public class AppUIUtil {

	/**
	 *
	 * @param ma
	 * @param clz
	 * @return
	 */
	static public BundleRef		findBundle(final UIMenu ma, final Class<?> clz) {
		if(ma.bundleBase() != Object.class)	{			// Bundle base class specified?
			String s = ma.bundleName();
			if(s.length() == 0)							// Do we have a name?
				s = "messages";							// If not use messages in this package
			return BundleRef.create(ma.bundleBase(), s);
		}

		//-- No BundleBase- use class as resource base and look for 'classname' as the properties base.
		if(clz != null) {
			String	s = clz.getName();
			s	= s.substring(s.lastIndexOf('.')+1);	// Get to base class name (no path)
			BundleRef	br	= BundleRef.create(clz, s);	// Get ref to this bundle;
			if(br.exists())
				return br;								// Return if it has data

			//-- Use messages bundle off this thing
			return BundleRef.create(clz, "messages");
		}
		return null;
	}

	/**
	 * Returns the bundle for the specified class, defined as classname[nls].properties.
	 * @param clz
	 * @return
	 */
	static public BundleRef	getClassBundle(final Class<?> clz) {
		String	s = clz.getName();
		s	= s.substring(s.lastIndexOf('.')+1);	// Get to base class name (no path)
		return BundleRef.create(clz, s);			// Get ref to this bundle;
	}

	static public BundleRef	getPackageBundle(final Class<?> base) {
		return BundleRef.create(base, "messages");	// Default package bundle is messages[nls].properties
	}

	/**
	 * Lookup a page Title bar text..
	 * @param clz
	 * @return
	 */
	static public String		calcPageTitle(final Class<? extends UrlPage> clz) {
		UIMenu	ma = clz.getAnnotation(UIMenu.class);		// Is annotated with UIMenu?
		Locale	loc	= NlsContext.getLocale();
		BundleRef	br	= findBundle(ma, clz);

		//-- Explicit specification of the names?
		if(ma != null && br != null) {
			//-- Has menu annotation. Is there a title key?
			if(ma.titleKey().length() != 0)
				return br.getString(loc, ma.titleKey());	// When present it MUST exist.

			//-- Is there a keyBase?
			if(ma.baseKey().length() != 0) {
				String s = br.findMessage(loc, ma.baseKey()+".title");		// Is this base thing present?
				if(s != null)								// This can be not-present...
					return s;
			}

			//-- No title. Can we use the menu label?
			if(ma.labelKey().length() > 0)
				return br.getString(loc, ma.labelKey());	// When present this must exist

			//-- Try the label from keyBase..
			if(ma.baseKey().length() != 0) {
				String s = br.findMessage(loc, ma.baseKey()+".label");
				if(s != null)								// This can be not-present...
					return s;
			}
		}

		//-- Try default page bundle and package bundle names.
		br	= getClassBundle(clz);					// Find bundle for the class
		String	s = br.findMessage(loc, "title");	// Find title key
		if(s != null)
			return s;
		s	= br.findMessage(loc, "label");
		if(s != null)
			return s;

		//-- Try package bundle.
		br	= getPackageBundle(clz);
		String	root	= clz.getName();
		root	= root.substring(root.lastIndexOf('.')+1);	// Class name without package
		s = br.findMessage(loc, root+".title");				// Find title key
		if(s != null)
			return s;
		s	= br.findMessage(loc, root+".label");
		if(s != null)
			return s;

		//-- No annotation, or the annotation did not deliver data. Try the menu.
		return null;
	}

}
