package to.etc.webapp.mailer;

import java.util.*;

import javax.annotation.*;
import javax.annotation.concurrent.*;

import to.etc.util.*;
import to.etc.webapp.query.*;

/**
 * Defines a link "target" for a given generic link in text strings. Instances of this class will be registered
 * using register() calls.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 29, 2010
 */
@Immutable
final public class TextLinkInfo {
	/** Map internal link names to instances of this class needed to render those links. */
	static final private Map<String, TextLinkInfo> m_map = new HashMap<String, TextLinkInfo>();

	/** The application-relative URL that this link should defer to. The link must contain a "{id}" at the location where the key needs to be stored. */
	@Nonnull
	final private String m_rurl;

	/** The name of the link inside the text file (the "local name") */
	@Nonnull
	final private String m_linkname;

	/**
	 * If this type of link is always used to show a given data class, then this can contain that class
	 * to make adding links easier. See {@link LinkedText#link(to.etc.webapp.query.IIdentifyable)}.
	 */
	@Nullable
	final private Class< ? > m_targetClass;

	TextLinkInfo(@Nonnull String linkname, @Nonnull String rurl, @Nullable Class< ? > tgtclass) {
		if(!rurl.contains("{id}"))
			throw new IllegalArgumentException("The RURL must contain a {id} text where the parameter(s) for the link are to be placed.");
		m_rurl = rurl;
		m_targetClass = tgtclass;
		m_linkname = linkname;
	}

	/**
	 * The application-relative URL that this link should defer to. The link will contain a "{id}" at the
	 * location where the key needs to be stored.
	 */
	@Nonnull
	public String getRurl() {
		return m_rurl;
	}

	/**
	 * If this type of link is always used to show a given data class, then this can contain that class to make adding
	 * links easier. See {@link LinkedText#link(to.etc.webapp.query.IIdentifyable)}.
	 */
	@Nullable
	public Class< ? > getTargetClass() {
		return m_targetClass;
	}

	/**
	 * The name of the link inside the text file (the "local name")
	 */
	@Nonnull
	public String getLinkname() {
		return m_linkname;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Registering valid links.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Register a link that represents a given data class.
	 *
	 * @param dataclass
	 * @param clz
	 * @param paramName
	 */
	public static void register(@Nonnull Class< ? extends IIdentifyable< ? >> dataclass, @Nonnull String rurl) {
		register(dataclass, dataclass.getName(), rurl);
	}

	/**
	 * Register an explicitly named link that represents a given data type.
	 * @param dataclass
	 * @param linkname
	 * @param rurl
	 */
	public static void register(@Nonnull Class< ? extends IIdentifyable< ? >> dataclass, @Nonnull String linkname, @Nonnull String rurl) {
		TextLinkInfo li = new TextLinkInfo(linkname, rurl, dataclass);
		if(null != m_map.put(linkname, li))
			throw new IllegalStateException("Duplicate link definition: " + linkname + " class=" + dataclass);
	}

	/**
	 * Register a link by name without data class.
	 * @param linkname
	 * @param rurl
	 */
	public static void register(@Nonnull String linkname, @Nonnull String rurl) {
		TextLinkInfo li = new TextLinkInfo(linkname, rurl, null);
		if(null != m_map.put(linkname, li))
			throw new IllegalStateException("Duplicate link definition: " + linkname + " (no data class)");
	}

	/**
	 * Get the link info for the specified link name.
	 *
	 * @param linkName
	 * @return
	 */
	@Nullable
	static public synchronized TextLinkInfo getInfo(String linkName) {
		TextLinkInfo tli = m_map.get(linkName);
		return tli;
	}

	/**
	 * Locates an instance for a specific data bclass. The "best" link info that is matched
	 * (meaning that matches the "lowest" subclass) will be returned. If no info is found
	 * then this returns null. A null instance is explicitly allowed and returns null.
	 *
	 * @param instance
	 * @return
	 */
	@Nullable
	static public synchronized TextLinkInfo getInfo(@Nullable IIdentifyable< ? > instance) {
		if(null == instance)
			return null;
		Class< ? > clz = instance.getClass();
		TextLinkInfo best = null;
		for(TextLinkInfo tl : m_map.values()) {
			Class< ? > targetClass = tl.getTargetClass();
			if(targetClass == null)
				continue;
			if(targetClass.isAssignableFrom(clz)) {
				if(best == null)
					best = tl;
				else {
					Class< ? > bestTargetClass = best.getTargetClass();
					if(null == bestTargetClass)
						throw new IllegalStateException(best + ": not a class based target");
					if(bestTargetClass.isAssignableFrom(targetClass))
						best = tl;
				} // This match is more precise (matches subclass of earlier class)
			}
		}
		return best;
	}

	/**
	 * Get the full URL by replacing {id} with the specified key.
	 * @param key
	 * @return
	 */
	@Nonnull
	public String getFullUrl(@Nonnull String key) {
		String repl = StringTool.encodeURLEncoded(key);
		return m_rurl.replace("{id}", repl);
	}
}
