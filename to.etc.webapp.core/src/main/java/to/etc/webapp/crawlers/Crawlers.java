package to.etc.webapp.crawlers;

import to.etc.net.NetTools;
import to.etc.util.WrappedException;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 16-10-19.
 */
final public class Crawlers {
	static final public Crawlers INSTANCE = new Crawlers();

	static public class Bot {
		private final String m_name;

		private final Set<String> m_agentStrings;

		private final Set<String> m_dnsNames;

		public Bot(String name, Set<String> agentStrings, Set<String> dnsNames) {
			m_name = name;
			m_agentStrings = agentStrings;
			m_dnsNames = dnsNames;
		}

		/**
		 * Return T if the user agent string belongs to this bot.
		 */
		public boolean isUserAgent(String header) {
			for(String agentString : m_agentStrings) {
				if(header.contains(agentString))
					return true;
			}
			return false;
		}

		public boolean isDomain(String domainName) {
			for(String dnsName : m_dnsNames) {
				if(domainName.endsWith(dnsName))
					return true;
			}
			return false;
		}

		public String getName() {
			return m_name;
		}
	}

	private List<Bot> m_botList = new CopyOnWriteArrayList<>();

	private Map<String, Boolean> m_validatedRemotes = new ConcurrentHashMap<>();


	/**
	 * Checks to see if the specified request comes from a web
	 * crawler.
	 */
	public boolean isCrawler(HttpServletRequest request) {
		String header = request.getHeader("user-agent");
		if(null == header)
			return false;
		header = header.toLowerCase();
		for(Bot bot : m_botList) {
			String hostName = "(unknown)";
			if(bot.isUserAgent(header)) {
				try {
					String javaSucksWithUselessFinalGarbage = NetTools.getRemoteHost(request);
					hostName = javaSucksWithUselessFinalGarbage;
					Boolean checked = m_validatedRemotes.computeIfAbsent(hostName, a -> checkDomain(bot, javaSucksWithUselessFinalGarbage));        // Calling those guys morons is an insult to morons.
					return checked;
				} catch(Exception x) {
					System.err.println("SEO: assumed bot " + bot.getName() + "@" + hostName + " threw " + WrappedException.unwrap(x));
					return false;
				}
			}
		}
		return false;
	}

	private Boolean checkDomain(Bot bot, String hostName) {
		String dnsName = lookupDomainName(hostName);
		return bot.isDomain(dnsName);
	}

	private String lookupDomainName(String hostName) {
		try {
			InetAddress addr = InetAddress.getByName(hostName);
			return addr.getCanonicalHostName();
		} catch(Exception x) {
			throw WrappedException.wrap(x);                // It is one thing to create checked exceptions. But it is plain idiotic to persist in having them.
		}
	}

	public void registerCrawler(String name, Set<String> agentStrings, Set<String> dnsNames) {
		m_botList.add(new Bot(name, agentStrings, dnsNames));
	}

	//-- https://www.keycdn.com/blog/web-crawlers
	//-- https://www.onely.com/blog/detect-verify-crawlers/
	static {
		INSTANCE.registerCrawler("GoogleBot", Set.of("googlebot/"), Set.of(".google.com", ".googlebot.com"));
		INSTANCE.registerCrawler("BingBot", Set.of("bingbot/"), Set.of(".google.com"));
		INSTANCE.registerCrawler("Yahoo", Set.of("slurp"), Set.of(".crawl.yahoo.net"));
		INSTANCE.registerCrawler("Baidu", Set.of("baiduspider"), Set.of(".crawl.baidu.com", ".crawl.baidu.jp"));
		INSTANCE.registerCrawler("Yandex", Set.of("yandexbot/"), Set.of(".yandex.ru", ".yandex.net", ".yandex.com"));
		INSTANCE.registerCrawler("DuckDuckGo", Set.of("duckduckbot/"), Set.of(".ivegotafang.com"));
		INSTANCE.registerCrawler("Exabot", Set.of("exabot"), Set.of(".exalead.com"));
		INSTANCE.registerCrawler("FaceBot", Set.of("facebookexternalhit"), Set.of(".facebook.com"));
		INSTANCE.registerCrawler("Alexa", Set.of("ia_archiver"), Set.of(".amazonaws.com"));

		//-- We do not include SOUGOU


	}
}
