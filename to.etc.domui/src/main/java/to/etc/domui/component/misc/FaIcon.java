package to.etc.domui.component.misc;

import to.etc.domui.dom.html.Span;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

/**
 * FontAwesome icon.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-8-17.
 */
@DefaultNonNull
final public class FaIcon extends Span {
	@Nullable
	private String m_iconName;

	public FaIcon(@Nullable String name) {
		m_iconName = name;
	}

	@Override
	public FaIcon css(String... classNames) {
		super.css(classNames);
		return this;
	}

	@Override public void createContent() throws Exception {
		//removeFaClasses();
		addCssClass("fa");
		if(null != m_iconName)
			addCssClass(m_iconName);
	}

	private void removeFaClasses() {
		String cssClass = getCssClass();
		if(null == cssClass)
			return;

		String[] split = cssClass.split("\\s+");
		for(String s : split) {
			if(s.equals("fa") || s.startsWith("fa-")) {
				removeCssClass(s);
			}
		}
	}

	public void setIconName(String iconName) {
		if(Objects.equals(iconName, m_iconName))
			return;
		String oldName = m_iconName;
		if(null != oldName)
			removeCssClass(oldName);
		if(null != iconName)
			addCssClass(iconName);
		m_iconName = iconName;
	}

	// Names for FontAwesome 4.7.0
	static private final String[] FANAMES = {
		"fa-500px", "fa-address-book", "fa-address-book-o", "fa-address-card", "fa-address-card-o", "fa-adjust",
		"fa-adn", "fa-align-center", "fa-align-justify", "fa-align-left", "fa-align-right", "fa-amazon", "fa-ambulance",
		"fa-american-sign-language-interpreting", "fa-anchor", "fa-android", "fa-angellist", "fa-angle-double-down",
		"fa-angle-double-left", "fa-angle-double-right", "fa-angle-double-up", "fa-angle-down", "fa-angle-left",
		"fa-angle-right", "fa-angle-up", "fa-apple", "fa-archive", "fa-area-chart", "fa-arrow-circle-down",
		"fa-arrow-circle-left", "fa-arrow-circle-o-down", "fa-arrow-circle-o-left", "fa-arrow-circle-o-right",
		"fa-arrow-circle-o-up", "fa-arrow-circle-right", "fa-arrow-circle-up", "fa-arrow-down", "fa-arrow-left",
		"fa-arrow-right", "fa-arrow-up", "fa-arrows", "fa-arrows-alt", "fa-arrows-h", "fa-arrows-v",
		"fa-assistive-listening-systems", "fa-asterisk", "fa-at", "fa-audio-description", "fa-backward",
		"fa-balance-scale", "fa-ban", "fa-bandcamp", "fa-bar-chart", "fa-barcode", "fa-bars", "fa-bath",
		"fa-battery-empty", "fa-battery-full", "fa-battery-half", "fa-battery-quarter", "fa-battery-three-quarters",
		"fa-bed", "fa-beer", "fa-behance", "fa-behance-square", "fa-bell", "fa-bell-o", "fa-bell-slash",
		"fa-bell-slash-o", "fa-bicycle", "fa-binoculars", "fa-birthday-cake", "fa-bitbucket", "fa-bitbucket-square",
		"fa-black-tie", "fa-blind", "fa-bluetooth", "fa-bluetooth-b", "fa-bold", "fa-bolt", "fa-bomb", "fa-book",
		"fa-bookmark", "fa-bookmark-o", "fa-braille", "fa-briefcase", "fa-btc", "fa-bug", "fa-building",
		"fa-building-o", "fa-bullhorn", "fa-bullseye", "fa-bus", "fa-buysellads", "fa-calculator", "fa-calendar",
		"fa-calendar-check-o", "fa-calendar-minus-o", "fa-calendar-o", "fa-calendar-plus-o", "fa-calendar-times-o",
		"fa-camera", "fa-camera-retro", "fa-car", "fa-caret-down", "fa-caret-left", "fa-caret-right",
		"fa-caret-square-o-down", "fa-caret-square-o-left", "fa-caret-square-o-right", "fa-caret-square-o-up",
		"fa-caret-up", "fa-cart-arrow-down", "fa-cart-plus", "fa-cc", "fa-cc-amex", "fa-cc-diners-club",
		"fa-cc-discover", "fa-cc-jcb", "fa-cc-mastercard", "fa-cc-paypal", "fa-cc-stripe", "fa-cc-visa",
		"fa-certificate", "fa-chain-broken", "fa-check", "fa-check-circle", "fa-check-circle-o", "fa-check-square",
		"fa-check-square-o", "fa-chevron-circle-down", "fa-chevron-circle-left", "fa-chevron-circle-right",
		"fa-chevron-circle-up", "fa-chevron-down", "fa-chevron-left", "fa-chevron-right", "fa-chevron-up", "fa-child",
		"fa-chrome", "fa-circle", "fa-circle-o", "fa-circle-o-notch", "fa-circle-thin", "fa-clipboard", "fa-clock-o",
		"fa-clone", "fa-cloud", "fa-cloud-download", "fa-cloud-upload", "fa-code", "fa-code-fork", "fa-codepen",
		"fa-codiepie", "fa-coffee", "fa-cog", "fa-cogs", "fa-columns", "fa-comment", "fa-comment-o", "fa-commenting",
		"fa-commenting-o", "fa-comments", "fa-comments-o", "fa-compass", "fa-compress", "fa-connectdevelop",
		"fa-contao", "fa-copyright", "fa-creative-commons", "fa-credit-card", "fa-credit-card-alt", "fa-crop",
		"fa-crosshairs", "fa-css3", "fa-cube", "fa-cubes", "fa-cutlery", "fa-dashcube", "fa-database", "fa-deaf",
		"fa-delicious", "fa-desktop", "fa-deviantart", "fa-diamond", "fa-digg", "fa-dot-circle-o", "fa-download",
		"fa-dribbble", "fa-dropbox", "fa-drupal", "fa-edge", "fa-eercast", "fa-eject", "fa-ellipsis-h", "fa-ellipsis-v",
		"fa-empire", "fa-envelope", "fa-envelope-o", "fa-envelope-open", "fa-envelope-open-o", "fa-envelope-square",
		"fa-envira", "fa-eraser", "fa-etsy", "fa-eur", "fa-exchange", "fa-exclamation", "fa-exclamation-circle",
		"fa-exclamation-triangle", "fa-expand", "fa-expeditedssl", "fa-external-link", "fa-external-link-square",
		"fa-eye", "fa-eye-slash", "fa-eyedropper", "fa-facebook", "fa-facebook-official", "fa-facebook-square",
		"fa-fast-backward", "fa-fast-forward", "fa-fax", "fa-female", "fa-fighter-jet", "fa-file", "fa-file-archive-o",
		"fa-file-audio-o", "fa-file-code-o", "fa-file-excel-o", "fa-file-image-o", "fa-file-o", "fa-file-pdf-o",
		"fa-file-powerpoint-o", "fa-file-text", "fa-file-text-o", "fa-file-video-o", "fa-file-word-o", "fa-files-o",
		"fa-film", "fa-filter", "fa-fire", "fa-fire-extinguisher", "fa-firefox", "fa-first-order", "fa-flag",
		"fa-flag-checkered", "fa-flag-o", "fa-flask", "fa-flickr", "fa-floppy-o", "fa-folder", "fa-folder-o",
		"fa-folder-open", "fa-folder-open-o", "fa-font", "fa-font-awesome", "fa-fonticons", "fa-fort-awesome",
		"fa-forumbee", "fa-forward", "fa-foursquare", "fa-free-code-camp", "fa-frown-o", "fa-futbol-o", "fa-gamepad",
		"fa-gavel", "fa-gbp", "fa-genderless", "fa-get-pocket", "fa-gg", "fa-gg-circle", "fa-gift", "fa-git",
		"fa-git-square", "fa-github", "fa-github-alt", "fa-github-square", "fa-gitlab", "fa-glass", "fa-glide",
		"fa-glide-g", "fa-globe", "fa-google", "fa-google-plus", "fa-google-plus-official", "fa-google-plus-square",
		"fa-google-wallet", "fa-graduation-cap", "fa-gratipay", "fa-grav", "fa-h-square", "fa-hacker-news",
		"fa-hand-lizard-o", "fa-hand-o-down", "fa-hand-o-left", "fa-hand-o-right", "fa-hand-o-up", "fa-hand-paper-o",
		"fa-hand-peace-o", "fa-hand-pointer-o", "fa-hand-rock-o", "fa-hand-scissors-o", "fa-hand-spock-o",
		"fa-handshake-o", "fa-hashtag", "fa-hdd-o", "fa-header", "fa-headphones", "fa-heart", "fa-heart-o",
		"fa-heartbeat", "fa-history", "fa-home", "fa-hospital-o", "fa-hourglass", "fa-hourglass-end",
		"fa-hourglass-half", "fa-hourglass-o", "fa-hourglass-start", "fa-houzz", "fa-html5", "fa-i-cursor",
		"fa-id-badge", "fa-id-card", "fa-id-card-o", "fa-ils", "fa-imdb", "fa-inbox", "fa-indent", "fa-industry",
		"fa-info", "fa-info-circle", "fa-inr", "fa-instagram", "fa-internet-explorer", "fa-ioxhost", "fa-italic",
		"fa-joomla", "fa-jpy", "fa-jsfiddle", "fa-key", "fa-keyboard-o", "fa-krw", "fa-language", "fa-laptop",
		"fa-lastfm", "fa-lastfm-square", "fa-leaf", "fa-leanpub", "fa-lemon-o", "fa-level-down", "fa-level-up",
		"fa-life-ring", "fa-lightbulb-o", "fa-line-chart", "fa-link", "fa-linkedin", "fa-linkedin-square", "fa-linode",
		"fa-linux", "fa-list", "fa-list-alt", "fa-list-ol", "fa-list-ul", "fa-location-arrow", "fa-lock",
		"fa-long-arrow-down", "fa-long-arrow-left", "fa-long-arrow-right", "fa-long-arrow-up", "fa-low-vision",
		"fa-magic", "fa-magnet", "fa-male", "fa-map", "fa-map-marker", "fa-map-o", "fa-map-pin", "fa-map-signs",
		"fa-mars", "fa-mars-double", "fa-mars-stroke", "fa-mars-stroke-h", "fa-mars-stroke-v", "fa-maxcdn",
		"fa-meanpath", "fa-medium", "fa-medkit", "fa-meetup", "fa-meh-o", "fa-mercury", "fa-microchip", "fa-microphone",
		"fa-microphone-slash", "fa-minus", "fa-minus-circle", "fa-minus-square", "fa-minus-square-o", "fa-mixcloud",
		"fa-mobile", "fa-modx", "fa-money", "fa-moon-o", "fa-motorcycle", "fa-mouse-pointer", "fa-music", "fa-neuter",
		"fa-newspaper-o", "fa-object-group", "fa-object-ungroup", "fa-odnoklassniki", "fa-odnoklassniki-square",
		"fa-opencart", "fa-openid", "fa-opera", "fa-optin-monster", "fa-outdent", "fa-pagelines", "fa-paint-brush",
		"fa-paper-plane", "fa-paper-plane-o", "fa-paperclip", "fa-paragraph", "fa-pause", "fa-pause-circle",
		"fa-pause-circle-o", "fa-paw", "fa-paypal", "fa-pencil", "fa-pencil-square", "fa-pencil-square-o", "fa-percent",
		"fa-phone", "fa-phone-square", "fa-picture-o", "fa-pie-chart", "fa-pied-piper", "fa-pied-piper-alt",
		"fa-pied-piper-pp", "fa-pinterest", "fa-pinterest-p", "fa-pinterest-square", "fa-plane", "fa-play",
		"fa-play-circle", "fa-play-circle-o", "fa-plug", "fa-plus", "fa-plus-circle", "fa-plus-square",
		"fa-plus-square-o", "fa-podcast", "fa-power-off", "fa-print", "fa-product-hunt", "fa-puzzle-piece", "fa-qq",
		"fa-qrcode", "fa-question", "fa-question-circle", "fa-question-circle-o", "fa-quora", "fa-quote-left",
		"fa-quote-right", "fa-random", "fa-ravelry", "fa-rebel", "fa-recycle", "fa-reddit", "fa-reddit-alien",
		"fa-reddit-square", "fa-refresh", "fa-registered", "fa-renren", "fa-repeat", "fa-reply", "fa-reply-all",
		"fa-retweet", "fa-road", "fa-rocket", "fa-rss", "fa-rss-square", "fa-rub", "fa-safari", "fa-scissors",
		"fa-scribd", "fa-search", "fa-search-minus", "fa-search-plus", "fa-sellsy", "fa-server", "fa-share",
		"fa-share-alt", "fa-share-alt-square", "fa-share-square", "fa-share-square-o", "fa-shield", "fa-ship",
		"fa-shirtsinbulk", "fa-shopping-bag", "fa-shopping-basket", "fa-shopping-cart", "fa-shower", "fa-sign-in",
		"fa-sign-language", "fa-sign-out", "fa-signal", "fa-simplybuilt", "fa-sitemap", "fa-skyatlas", "fa-skype",
		"fa-slack", "fa-sliders", "fa-slideshare", "fa-smile-o", "fa-snapchat", "fa-snapchat-ghost",
		"fa-snapchat-square", "fa-snowflake-o", "fa-sort", "fa-sort-alpha-asc", "fa-sort-alpha-desc",
		"fa-sort-amount-asc", "fa-sort-amount-desc", "fa-sort-asc", "fa-sort-desc", "fa-sort-numeric-asc",
		"fa-sort-numeric-desc", "fa-soundcloud", "fa-space-shuttle", "fa-spinner", "fa-spoon", "fa-spotify",
		"fa-square", "fa-square-o", "fa-stack-exchange", "fa-stack-overflow", "fa-star", "fa-star-half",
		"fa-star-half-o", "fa-star-o", "fa-steam", "fa-steam-square", "fa-step-backward", "fa-step-forward",
		"fa-stethoscope", "fa-sticky-note", "fa-sticky-note-o", "fa-stop", "fa-stop-circle", "fa-stop-circle-o",
		"fa-street-view", "fa-strikethrough", "fa-stumbleupon", "fa-stumbleupon-circle", "fa-subscript", "fa-subway",
		"fa-suitcase", "fa-sun-o", "fa-superpowers", "fa-superscript", "fa-table", "fa-tablet", "fa-tachometer",
		"fa-tag", "fa-tags", "fa-tasks", "fa-taxi", "fa-telegram", "fa-television", "fa-tencent-weibo", "fa-terminal",
		"fa-text-height", "fa-text-width", "fa-th", "fa-th-large", "fa-th-list", "fa-themeisle", "fa-thermometer-empty",
		"fa-thermometer-full", "fa-thermometer-half", "fa-thermometer-quarter", "fa-thermometer-three-quarters",
		"fa-thumb-tack", "fa-thumbs-down", "fa-thumbs-o-down", "fa-thumbs-o-up", "fa-thumbs-up", "fa-ticket",
		"fa-times", "fa-times-circle", "fa-times-circle-o", "fa-tint", "fa-toggle-off", "fa-toggle-on", "fa-trademark",
		"fa-train", "fa-transgender", "fa-transgender-alt", "fa-trash", "fa-trash-o", "fa-tree", "fa-trello",
		"fa-tripadvisor", "fa-trophy", "fa-truck", "fa-try", "fa-tty", "fa-tumblr", "fa-tumblr-square", "fa-twitch",
		"fa-twitter", "fa-twitter-square", "fa-umbrella", "fa-underline", "fa-undo", "fa-universal-access",
		"fa-university", "fa-unlock", "fa-unlock-alt", "fa-upload", "fa-usb", "fa-usd", "fa-user", "fa-user-circle",
		"fa-user-circle-o", "fa-user-md", "fa-user-o", "fa-user-plus", "fa-user-secret", "fa-user-times", "fa-users",
		"fa-venus", "fa-venus-double", "fa-venus-mars", "fa-viacoin", "fa-viadeo", "fa-viadeo-square",
		"fa-video-camera", "fa-vimeo", "fa-vimeo-square", "fa-vine", "fa-vk", "fa-volume-control-phone",
		"fa-volume-down", "fa-volume-off", "fa-volume-up", "fa-weibo", "fa-weixin", "fa-whatsapp", "fa-wheelchair",
		"fa-wheelchair-alt", "fa-wifi", "fa-wikipedia-w", "fa-window-close", "fa-window-close-o", "fa-window-maximize",
		"fa-window-minimize", "fa-window-restore", "fa-windows", "fa-wordpress", "fa-wpbeginner", "fa-wpexplorer",
		"fa-wpforms", "fa-wrench", "fa-xing", "fa-xing-square", "fa-y-combinator", "fa-yahoo", "fa-yelp", "fa-yoast",
		"fa-youtube", "fa-youtube-play", "fa-youtube-square"
		, "fa-chain", "fa-flash"
	};

	public static final void main(String[] args) {
		Arrays.sort(FANAMES);
		for(String faname : FANAMES) {
			System.out.println("public static final String " + alterName(faname) + " = \"" + faname + "\";");
		}
	}

	 static String alterName(String faname) {
		StringBuilder sb = new StringBuilder();
		boolean uc = false;
		for(int i = 0; i < faname.length(); i++) {
			char c = faname.charAt(i);
			if(uc) {
				sb.append(Character.toUpperCase(c));
				uc = false;
			} else if(c == '-') {
				uc = true;
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static final String fa500px = "fa-500px";
	public static final String faChain = "fa-chain";
	public static final String faFlash = "fa-flash";
	public static final String faAddressBook = "fa-address-book";
	public static final String faAddressBookO = "fa-address-book-o";
	public static final String faAddressCard = "fa-address-card";
	public static final String faAddressCardO = "fa-address-card-o";
	public static final String faAdjust = "fa-adjust";
	public static final String faAdn = "fa-adn";
	public static final String faAlignCenter = "fa-align-center";
	public static final String faAlignJustify = "fa-align-justify";
	public static final String faAlignLeft = "fa-align-left";
	public static final String faAlignRight = "fa-align-right";
	public static final String faAmazon = "fa-amazon";
	public static final String faAmbulance = "fa-ambulance";
	public static final String faAmericanSignLanguageInterpreting = "fa-american-sign-language-interpreting";
	public static final String faAnchor = "fa-anchor";
	public static final String faAndroid = "fa-android";
	public static final String faAngellist = "fa-angellist";
	public static final String faAngleDoubleDown = "fa-angle-double-down";
	public static final String faAngleDoubleLeft = "fa-angle-double-left";
	public static final String faAngleDoubleRight = "fa-angle-double-right";
	public static final String faAngleDoubleUp = "fa-angle-double-up";
	public static final String faAngleDown = "fa-angle-down";
	public static final String faAngleLeft = "fa-angle-left";
	public static final String faAngleRight = "fa-angle-right";
	public static final String faAngleUp = "fa-angle-up";
	public static final String faApple = "fa-apple";
	public static final String faArchive = "fa-archive";
	public static final String faAreaChart = "fa-area-chart";
	public static final String faArrowCircleDown = "fa-arrow-circle-down";
	public static final String faArrowCircleLeft = "fa-arrow-circle-left";
	public static final String faArrowCircleODown = "fa-arrow-circle-o-down";
	public static final String faArrowCircleOLeft = "fa-arrow-circle-o-left";
	public static final String faArrowCircleORight = "fa-arrow-circle-o-right";
	public static final String faArrowCircleOUp = "fa-arrow-circle-o-up";
	public static final String faArrowCircleRight = "fa-arrow-circle-right";
	public static final String faArrowCircleUp = "fa-arrow-circle-up";
	public static final String faArrowDown = "fa-arrow-down";
	public static final String faArrowLeft = "fa-arrow-left";
	public static final String faArrowRight = "fa-arrow-right";
	public static final String faArrowUp = "fa-arrow-up";
	public static final String faArrows = "fa-arrows";
	public static final String faArrowsAlt = "fa-arrows-alt";
	public static final String faArrowsH = "fa-arrows-h";
	public static final String faArrowsV = "fa-arrows-v";
	public static final String faAssistiveListeningSystems = "fa-assistive-listening-systems";
	public static final String faAsterisk = "fa-asterisk";
	public static final String faAt = "fa-at";
	public static final String faAudioDescription = "fa-audio-description";
	public static final String faBackward = "fa-backward";
	public static final String faBalanceScale = "fa-balance-scale";
	public static final String faBan = "fa-ban";
	public static final String faBandcamp = "fa-bandcamp";
	public static final String faBarChart = "fa-bar-chart";
	public static final String faBarcode = "fa-barcode";
	public static final String faBars = "fa-bars";
	public static final String faBath = "fa-bath";
	public static final String faBatteryEmpty = "fa-battery-empty";
	public static final String faBatteryFull = "fa-battery-full";
	public static final String faBatteryHalf = "fa-battery-half";
	public static final String faBatteryQuarter = "fa-battery-quarter";
	public static final String faBatteryThreeQuarters = "fa-battery-three-quarters";
	public static final String faBed = "fa-bed";
	public static final String faBeer = "fa-beer";
	public static final String faBehance = "fa-behance";
	public static final String faBehanceSquare = "fa-behance-square";
	public static final String faBell = "fa-bell";
	public static final String faBellO = "fa-bell-o";
	public static final String faBellSlash = "fa-bell-slash";
	public static final String faBellSlashO = "fa-bell-slash-o";
	public static final String faBicycle = "fa-bicycle";
	public static final String faBinoculars = "fa-binoculars";
	public static final String faBirthdayCake = "fa-birthday-cake";
	public static final String faBitbucket = "fa-bitbucket";
	public static final String faBitbucketSquare = "fa-bitbucket-square";
	public static final String faBlackTie = "fa-black-tie";
	public static final String faBlind = "fa-blind";
	public static final String faBluetooth = "fa-bluetooth";
	public static final String faBluetoothB = "fa-bluetooth-b";
	public static final String faBold = "fa-bold";
	public static final String faBolt = "fa-bolt";
	public static final String faBomb = "fa-bomb";
	public static final String faBook = "fa-book";
	public static final String faBookmark = "fa-bookmark";
	public static final String faBookmarkO = "fa-bookmark-o";
	public static final String faBraille = "fa-braille";
	public static final String faBriefcase = "fa-briefcase";
	public static final String faBtc = "fa-btc";
	public static final String faBug = "fa-bug";
	public static final String faBuilding = "fa-building";
	public static final String faBuildingO = "fa-building-o";
	public static final String faBullhorn = "fa-bullhorn";
	public static final String faBullseye = "fa-bullseye";
	public static final String faBus = "fa-bus";
	public static final String faBuysellads = "fa-buysellads";
	public static final String faCalculator = "fa-calculator";
	public static final String faCalendar = "fa-calendar";
	public static final String faCalendarCheckO = "fa-calendar-check-o";
	public static final String faCalendarMinusO = "fa-calendar-minus-o";
	public static final String faCalendarO = "fa-calendar-o";
	public static final String faCalendarPlusO = "fa-calendar-plus-o";
	public static final String faCalendarTimesO = "fa-calendar-times-o";
	public static final String faCamera = "fa-camera";
	public static final String faCameraRetro = "fa-camera-retro";
	public static final String faCar = "fa-car";
	public static final String faCaretDown = "fa-caret-down";
	public static final String faCaretLeft = "fa-caret-left";
	public static final String faCaretRight = "fa-caret-right";
	public static final String faCaretSquareODown = "fa-caret-square-o-down";
	public static final String faCaretSquareOLeft = "fa-caret-square-o-left";
	public static final String faCaretSquareORight = "fa-caret-square-o-right";
	public static final String faCaretSquareOUp = "fa-caret-square-o-up";
	public static final String faCaretUp = "fa-caret-up";
	public static final String faCartArrowDown = "fa-cart-arrow-down";
	public static final String faCartPlus = "fa-cart-plus";
	public static final String faCc = "fa-cc";
	public static final String faCcAmex = "fa-cc-amex";
	public static final String faCcDinersClub = "fa-cc-diners-club";
	public static final String faCcDiscover = "fa-cc-discover";
	public static final String faCcJcb = "fa-cc-jcb";
	public static final String faCcMastercard = "fa-cc-mastercard";
	public static final String faCcPaypal = "fa-cc-paypal";
	public static final String faCcStripe = "fa-cc-stripe";
	public static final String faCcVisa = "fa-cc-visa";
	public static final String faCertificate = "fa-certificate";
	public static final String faChainBroken = "fa-chain-broken";
	public static final String faCheck = "fa-check";
	public static final String faCheckCircle = "fa-check-circle";
	public static final String faCheckCircleO = "fa-check-circle-o";
	public static final String faCheckSquare = "fa-check-square";
	public static final String faCheckSquareO = "fa-check-square-o";
	public static final String faChevronCircleDown = "fa-chevron-circle-down";
	public static final String faChevronCircleLeft = "fa-chevron-circle-left";
	public static final String faChevronCircleRight = "fa-chevron-circle-right";
	public static final String faChevronCircleUp = "fa-chevron-circle-up";
	public static final String faChevronDown = "fa-chevron-down";
	public static final String faChevronLeft = "fa-chevron-left";
	public static final String faChevronRight = "fa-chevron-right";
	public static final String faChevronUp = "fa-chevron-up";
	public static final String faChild = "fa-child";
	public static final String faChrome = "fa-chrome";
	public static final String faCircle = "fa-circle";
	public static final String faCircleO = "fa-circle-o";
	public static final String faCircleONotch = "fa-circle-o-notch";
	public static final String faCircleThin = "fa-circle-thin";
	public static final String faClipboard = "fa-clipboard";
	public static final String faClockO = "fa-clock-o";
	public static final String faClone = "fa-clone";
	public static final String faCloud = "fa-cloud";
	public static final String faCloudDownload = "fa-cloud-download";
	public static final String faCloudUpload = "fa-cloud-upload";
	public static final String faCode = "fa-code";
	public static final String faCodeFork = "fa-code-fork";
	public static final String faCodepen = "fa-codepen";
	public static final String faCodiepie = "fa-codiepie";
	public static final String faCoffee = "fa-coffee";
	public static final String faCog = "fa-cog";
	public static final String faCogs = "fa-cogs";
	public static final String faColumns = "fa-columns";
	public static final String faComment = "fa-comment";
	public static final String faCommentO = "fa-comment-o";
	public static final String faCommenting = "fa-commenting";
	public static final String faCommentingO = "fa-commenting-o";
	public static final String faComments = "fa-comments";
	public static final String faCommentsO = "fa-comments-o";
	public static final String faCompass = "fa-compass";
	public static final String faCompress = "fa-compress";
	public static final String faConnectdevelop = "fa-connectdevelop";
	public static final String faContao = "fa-contao";
	public static final String faCopyright = "fa-copyright";
	public static final String faCreativeCommons = "fa-creative-commons";
	public static final String faCreditCard = "fa-credit-card";
	public static final String faCreditCardAlt = "fa-credit-card-alt";
	public static final String faCrop = "fa-crop";
	public static final String faCrosshairs = "fa-crosshairs";
	public static final String faCss3 = "fa-css3";
	public static final String faCube = "fa-cube";
	public static final String faCubes = "fa-cubes";
	public static final String faCutlery = "fa-cutlery";
	public static final String faDashcube = "fa-dashcube";
	public static final String faDatabase = "fa-database";
	public static final String faDeaf = "fa-deaf";
	public static final String faDelicious = "fa-delicious";
	public static final String faDesktop = "fa-desktop";
	public static final String faDeviantart = "fa-deviantart";
	public static final String faDiamond = "fa-diamond";
	public static final String faDigg = "fa-digg";
	public static final String faDotCircleO = "fa-dot-circle-o";
	public static final String faDownload = "fa-download";
	public static final String faDribbble = "fa-dribbble";
	public static final String faDropbox = "fa-dropbox";
	public static final String faDrupal = "fa-drupal";
	public static final String faEdge = "fa-edge";
	public static final String faEercast = "fa-eercast";
	public static final String faEject = "fa-eject";
	public static final String faEllipsisH = "fa-ellipsis-h";
	public static final String faEllipsisV = "fa-ellipsis-v";
	public static final String faEmpire = "fa-empire";
	public static final String faEnvelope = "fa-envelope";
	public static final String faEnvelopeO = "fa-envelope-o";
	public static final String faEnvelopeOpen = "fa-envelope-open";
	public static final String faEnvelopeOpenO = "fa-envelope-open-o";
	public static final String faEnvelopeSquare = "fa-envelope-square";
	public static final String faEnvira = "fa-envira";
	public static final String faEraser = "fa-eraser";
	public static final String faEtsy = "fa-etsy";
	public static final String faEur = "fa-eur";
	public static final String faExchange = "fa-exchange";
	public static final String faExclamation = "fa-exclamation";
	public static final String faExclamationCircle = "fa-exclamation-circle";
	public static final String faExclamationTriangle = "fa-exclamation-triangle";
	public static final String faExpand = "fa-expand";
	public static final String faExpeditedssl = "fa-expeditedssl";
	public static final String faExternalLink = "fa-external-link";
	public static final String faExternalLinkSquare = "fa-external-link-square";
	public static final String faEye = "fa-eye";
	public static final String faEyeSlash = "fa-eye-slash";
	public static final String faEyedropper = "fa-eyedropper";
	public static final String faFacebook = "fa-facebook";
	public static final String faFacebookOfficial = "fa-facebook-official";
	public static final String faFacebookSquare = "fa-facebook-square";
	public static final String faFastBackward = "fa-fast-backward";
	public static final String faFastForward = "fa-fast-forward";
	public static final String faFax = "fa-fax";
	public static final String faFemale = "fa-female";
	public static final String faFighterJet = "fa-fighter-jet";
	public static final String faFile = "fa-file";
	public static final String faFileArchiveO = "fa-file-archive-o";
	public static final String faFileAudioO = "fa-file-audio-o";
	public static final String faFileCodeO = "fa-file-code-o";
	public static final String faFileExcelO = "fa-file-excel-o";
	public static final String faFileImageO = "fa-file-image-o";
	public static final String faFileO = "fa-file-o";
	public static final String faFilePdfO = "fa-file-pdf-o";
	public static final String faFilePowerpointO = "fa-file-powerpoint-o";
	public static final String faFileText = "fa-file-text";
	public static final String faFileTextO = "fa-file-text-o";
	public static final String faFileVideoO = "fa-file-video-o";
	public static final String faFileWordO = "fa-file-word-o";
	public static final String faFilesO = "fa-files-o";
	public static final String faFilm = "fa-film";
	public static final String faFilter = "fa-filter";
	public static final String faFire = "fa-fire";
	public static final String faFireExtinguisher = "fa-fire-extinguisher";
	public static final String faFirefox = "fa-firefox";
	public static final String faFirstOrder = "fa-first-order";
	public static final String faFlag = "fa-flag";
	public static final String faFlagCheckered = "fa-flag-checkered";
	public static final String faFlagO = "fa-flag-o";
	public static final String faFlask = "fa-flask";
	public static final String faFlickr = "fa-flickr";
	public static final String faFloppyO = "fa-floppy-o";
	public static final String faFolder = "fa-folder";
	public static final String faFolderO = "fa-folder-o";
	public static final String faFolderOpen = "fa-folder-open";
	public static final String faFolderOpenO = "fa-folder-open-o";
	public static final String faFont = "fa-font";
	public static final String faFontAwesome = "fa-font-awesome";
	public static final String faFonticons = "fa-fonticons";
	public static final String faFortAwesome = "fa-fort-awesome";
	public static final String faForumbee = "fa-forumbee";
	public static final String faForward = "fa-forward";
	public static final String faFoursquare = "fa-foursquare";
	public static final String faFreeCodeCamp = "fa-free-code-camp";
	public static final String faFrownO = "fa-frown-o";
	public static final String faFutbolO = "fa-futbol-o";
	public static final String faGamepad = "fa-gamepad";
	public static final String faGavel = "fa-gavel";
	public static final String faGbp = "fa-gbp";
	public static final String faGenderless = "fa-genderless";
	public static final String faGetPocket = "fa-get-pocket";
	public static final String faGg = "fa-gg";
	public static final String faGgCircle = "fa-gg-circle";
	public static final String faGift = "fa-gift";
	public static final String faGit = "fa-git";
	public static final String faGitSquare = "fa-git-square";
	public static final String faGithub = "fa-github";
	public static final String faGithubAlt = "fa-github-alt";
	public static final String faGithubSquare = "fa-github-square";
	public static final String faGitlab = "fa-gitlab";
	public static final String faGlass = "fa-glass";
	public static final String faGlide = "fa-glide";
	public static final String faGlideG = "fa-glide-g";
	public static final String faGlobe = "fa-globe";
	public static final String faGoogle = "fa-google";
	public static final String faGooglePlus = "fa-google-plus";
	public static final String faGooglePlusOfficial = "fa-google-plus-official";
	public static final String faGooglePlusSquare = "fa-google-plus-square";
	public static final String faGoogleWallet = "fa-google-wallet";
	public static final String faGraduationCap = "fa-graduation-cap";
	public static final String faGratipay = "fa-gratipay";
	public static final String faGrav = "fa-grav";
	public static final String faHSquare = "fa-h-square";
	public static final String faHackerNews = "fa-hacker-news";
	public static final String faHandLizardO = "fa-hand-lizard-o";
	public static final String faHandODown = "fa-hand-o-down";
	public static final String faHandOLeft = "fa-hand-o-left";
	public static final String faHandORight = "fa-hand-o-right";
	public static final String faHandOUp = "fa-hand-o-up";
	public static final String faHandPaperO = "fa-hand-paper-o";
	public static final String faHandPeaceO = "fa-hand-peace-o";
	public static final String faHandPointerO = "fa-hand-pointer-o";
	public static final String faHandRockO = "fa-hand-rock-o";
	public static final String faHandScissorsO = "fa-hand-scissors-o";
	public static final String faHandSpockO = "fa-hand-spock-o";
	public static final String faHandshakeO = "fa-handshake-o";
	public static final String faHashtag = "fa-hashtag";
	public static final String faHddO = "fa-hdd-o";
	public static final String faHeader = "fa-header";
	public static final String faHeadphones = "fa-headphones";
	public static final String faHeart = "fa-heart";
	public static final String faHeartO = "fa-heart-o";
	public static final String faHeartbeat = "fa-heartbeat";
	public static final String faHistory = "fa-history";
	public static final String faHome = "fa-home";
	public static final String faHospitalO = "fa-hospital-o";
	public static final String faHourglass = "fa-hourglass";
	public static final String faHourglassEnd = "fa-hourglass-end";
	public static final String faHourglassHalf = "fa-hourglass-half";
	public static final String faHourglassO = "fa-hourglass-o";
	public static final String faHourglassStart = "fa-hourglass-start";
	public static final String faHouzz = "fa-houzz";
	public static final String faHtml5 = "fa-html5";
	public static final String faICursor = "fa-i-cursor";
	public static final String faIdBadge = "fa-id-badge";
	public static final String faIdCard = "fa-id-card";
	public static final String faIdCardO = "fa-id-card-o";
	public static final String faIls = "fa-ils";
	public static final String faImdb = "fa-imdb";
	public static final String faInbox = "fa-inbox";
	public static final String faIndent = "fa-indent";
	public static final String faIndustry = "fa-industry";
	public static final String faInfo = "fa-info";
	public static final String faInfoCircle = "fa-info-circle";
	public static final String faInr = "fa-inr";
	public static final String faInstagram = "fa-instagram";
	public static final String faInternetExplorer = "fa-internet-explorer";
	public static final String faIoxhost = "fa-ioxhost";
	public static final String faItalic = "fa-italic";
	public static final String faJoomla = "fa-joomla";
	public static final String faJpy = "fa-jpy";
	public static final String faJsfiddle = "fa-jsfiddle";
	public static final String faKey = "fa-key";
	public static final String faKeyboardO = "fa-keyboard-o";
	public static final String faKrw = "fa-krw";
	public static final String faLanguage = "fa-language";
	public static final String faLaptop = "fa-laptop";
	public static final String faLastfm = "fa-lastfm";
	public static final String faLastfmSquare = "fa-lastfm-square";
	public static final String faLeaf = "fa-leaf";
	public static final String faLeanpub = "fa-leanpub";
	public static final String faLemonO = "fa-lemon-o";
	public static final String faLevelDown = "fa-level-down";
	public static final String faLevelUp = "fa-level-up";
	public static final String faLifeRing = "fa-life-ring";
	public static final String faLightbulbO = "fa-lightbulb-o";
	public static final String faLineChart = "fa-line-chart";
	public static final String faLink = "fa-link";
	public static final String faLinkedin = "fa-linkedin";
	public static final String faLinkedinSquare = "fa-linkedin-square";
	public static final String faLinode = "fa-linode";
	public static final String faLinux = "fa-linux";
	public static final String faList = "fa-list";
	public static final String faListAlt = "fa-list-alt";
	public static final String faListOl = "fa-list-ol";
	public static final String faListUl = "fa-list-ul";
	public static final String faLocationArrow = "fa-location-arrow";
	public static final String faLock = "fa-lock";
	public static final String faLongArrowDown = "fa-long-arrow-down";
	public static final String faLongArrowLeft = "fa-long-arrow-left";
	public static final String faLongArrowRight = "fa-long-arrow-right";
	public static final String faLongArrowUp = "fa-long-arrow-up";
	public static final String faLowVision = "fa-low-vision";
	public static final String faMagic = "fa-magic";
	public static final String faMagnet = "fa-magnet";
	public static final String faMale = "fa-male";
	public static final String faMap = "fa-map";
	public static final String faMapMarker = "fa-map-marker";
	public static final String faMapO = "fa-map-o";
	public static final String faMapPin = "fa-map-pin";
	public static final String faMapSigns = "fa-map-signs";
	public static final String faMars = "fa-mars";
	public static final String faMarsDouble = "fa-mars-double";
	public static final String faMarsStroke = "fa-mars-stroke";
	public static final String faMarsStrokeH = "fa-mars-stroke-h";
	public static final String faMarsStrokeV = "fa-mars-stroke-v";
	public static final String faMaxcdn = "fa-maxcdn";
	public static final String faMeanpath = "fa-meanpath";
	public static final String faMedium = "fa-medium";
	public static final String faMedkit = "fa-medkit";
	public static final String faMeetup = "fa-meetup";
	public static final String faMehO = "fa-meh-o";
	public static final String faMercury = "fa-mercury";
	public static final String faMicrochip = "fa-microchip";
	public static final String faMicrophone = "fa-microphone";
	public static final String faMicrophoneSlash = "fa-microphone-slash";
	public static final String faMinus = "fa-minus";
	public static final String faMinusCircle = "fa-minus-circle";
	public static final String faMinusSquare = "fa-minus-square";
	public static final String faMinusSquareO = "fa-minus-square-o";
	public static final String faMixcloud = "fa-mixcloud";
	public static final String faMobile = "fa-mobile";
	public static final String faModx = "fa-modx";
	public static final String faMoney = "fa-money";
	public static final String faMoonO = "fa-moon-o";
	public static final String faMotorcycle = "fa-motorcycle";
	public static final String faMousePointer = "fa-mouse-pointer";
	public static final String faMusic = "fa-music";
	public static final String faNeuter = "fa-neuter";
	public static final String faNewspaperO = "fa-newspaper-o";
	public static final String faObjectGroup = "fa-object-group";
	public static final String faObjectUngroup = "fa-object-ungroup";
	public static final String faOdnoklassniki = "fa-odnoklassniki";
	public static final String faOdnoklassnikiSquare = "fa-odnoklassniki-square";
	public static final String faOpencart = "fa-opencart";
	public static final String faOpenid = "fa-openid";
	public static final String faOpera = "fa-opera";
	public static final String faOptinMonster = "fa-optin-monster";
	public static final String faOutdent = "fa-outdent";
	public static final String faPagelines = "fa-pagelines";
	public static final String faPaintBrush = "fa-paint-brush";
	public static final String faPaperPlane = "fa-paper-plane";
	public static final String faPaperPlaneO = "fa-paper-plane-o";
	public static final String faPaperclip = "fa-paperclip";
	public static final String faParagraph = "fa-paragraph";
	public static final String faPause = "fa-pause";
	public static final String faPauseCircle = "fa-pause-circle";
	public static final String faPauseCircleO = "fa-pause-circle-o";
	public static final String faPaw = "fa-paw";
	public static final String faPaypal = "fa-paypal";
	public static final String faPencil = "fa-pencil";
	public static final String faPencilSquare = "fa-pencil-square";
	public static final String faPencilSquareO = "fa-pencil-square-o";
	public static final String faPercent = "fa-percent";
	public static final String faPhone = "fa-phone";
	public static final String faPhoneSquare = "fa-phone-square";
	public static final String faPictureO = "fa-picture-o";
	public static final String faPieChart = "fa-pie-chart";
	public static final String faPiedPiper = "fa-pied-piper";
	public static final String faPiedPiperAlt = "fa-pied-piper-alt";
	public static final String faPiedPiperPp = "fa-pied-piper-pp";
	public static final String faPinterest = "fa-pinterest";
	public static final String faPinterestP = "fa-pinterest-p";
	public static final String faPinterestSquare = "fa-pinterest-square";
	public static final String faPlane = "fa-plane";
	public static final String faPlay = "fa-play";
	public static final String faPlayCircle = "fa-play-circle";
	public static final String faPlayCircleO = "fa-play-circle-o";
	public static final String faPlug = "fa-plug";
	public static final String faPlus = "fa-plus";
	public static final String faPlusCircle = "fa-plus-circle";
	public static final String faPlusSquare = "fa-plus-square";
	public static final String faPlusSquareO = "fa-plus-square-o";
	public static final String faPodcast = "fa-podcast";
	public static final String faPowerOff = "fa-power-off";
	public static final String faPrint = "fa-print";
	public static final String faProductHunt = "fa-product-hunt";
	public static final String faPuzzlePiece = "fa-puzzle-piece";
	public static final String faQq = "fa-qq";
	public static final String faQrcode = "fa-qrcode";
	public static final String faQuestion = "fa-question";
	public static final String faQuestionCircle = "fa-question-circle";
	public static final String faQuestionCircleO = "fa-question-circle-o";
	public static final String faQuora = "fa-quora";
	public static final String faQuoteLeft = "fa-quote-left";
	public static final String faQuoteRight = "fa-quote-right";
	public static final String faRandom = "fa-random";
	public static final String faRavelry = "fa-ravelry";
	public static final String faRebel = "fa-rebel";
	public static final String faRecycle = "fa-recycle";
	public static final String faReddit = "fa-reddit";
	public static final String faRedditAlien = "fa-reddit-alien";
	public static final String faRedditSquare = "fa-reddit-square";
	public static final String faRefresh = "fa-refresh";
	public static final String faRegistered = "fa-registered";
	public static final String faRenren = "fa-renren";
	public static final String faRepeat = "fa-repeat";
	public static final String faReply = "fa-reply";
	public static final String faReplyAll = "fa-reply-all";
	public static final String faRetweet = "fa-retweet";
	public static final String faRoad = "fa-road";
	public static final String faRocket = "fa-rocket";
	public static final String faRss = "fa-rss";
	public static final String faRssSquare = "fa-rss-square";
	public static final String faRub = "fa-rub";
	public static final String faSafari = "fa-safari";
	public static final String faScissors = "fa-scissors";
	public static final String faScribd = "fa-scribd";
	public static final String faSearch = "fa-search";
	public static final String faSearchMinus = "fa-search-minus";
	public static final String faSearchPlus = "fa-search-plus";
	public static final String faSellsy = "fa-sellsy";
	public static final String faServer = "fa-server";
	public static final String faShare = "fa-share";
	public static final String faShareAlt = "fa-share-alt";
	public static final String faShareAltSquare = "fa-share-alt-square";
	public static final String faShareSquare = "fa-share-square";
	public static final String faShareSquareO = "fa-share-square-o";
	public static final String faShield = "fa-shield";
	public static final String faShip = "fa-ship";
	public static final String faShirtsinbulk = "fa-shirtsinbulk";
	public static final String faShoppingBag = "fa-shopping-bag";
	public static final String faShoppingBasket = "fa-shopping-basket";
	public static final String faShoppingCart = "fa-shopping-cart";
	public static final String faShower = "fa-shower";
	public static final String faSignIn = "fa-sign-in";
	public static final String faSignLanguage = "fa-sign-language";
	public static final String faSignOut = "fa-sign-out";
	public static final String faSignal = "fa-signal";
	public static final String faSimplybuilt = "fa-simplybuilt";
	public static final String faSitemap = "fa-sitemap";
	public static final String faSkyatlas = "fa-skyatlas";
	public static final String faSkype = "fa-skype";
	public static final String faSlack = "fa-slack";
	public static final String faSliders = "fa-sliders";
	public static final String faSlideshare = "fa-slideshare";
	public static final String faSmileO = "fa-smile-o";
	public static final String faSnapchat = "fa-snapchat";
	public static final String faSnapchatGhost = "fa-snapchat-ghost";
	public static final String faSnapchatSquare = "fa-snapchat-square";
	public static final String faSnowflakeO = "fa-snowflake-o";
	public static final String faSort = "fa-sort";
	public static final String faSortAlphaAsc = "fa-sort-alpha-asc";
	public static final String faSortAlphaDesc = "fa-sort-alpha-desc";
	public static final String faSortAmountAsc = "fa-sort-amount-asc";
	public static final String faSortAmountDesc = "fa-sort-amount-desc";
	public static final String faSortAsc = "fa-sort-asc";
	public static final String faSortDesc = "fa-sort-desc";
	public static final String faSortNumericAsc = "fa-sort-numeric-asc";
	public static final String faSortNumericDesc = "fa-sort-numeric-desc";
	public static final String faSoundcloud = "fa-soundcloud";
	public static final String faSpaceShuttle = "fa-space-shuttle";
	public static final String faSpinner = "fa-spinner";
	public static final String faSpoon = "fa-spoon";
	public static final String faSpotify = "fa-spotify";
	public static final String faSquare = "fa-square";
	public static final String faSquareO = "fa-square-o";
	public static final String faStackExchange = "fa-stack-exchange";
	public static final String faStackOverflow = "fa-stack-overflow";
	public static final String faStar = "fa-star";
	public static final String faStarHalf = "fa-star-half";
	public static final String faStarHalfO = "fa-star-half-o";
	public static final String faStarO = "fa-star-o";
	public static final String faSteam = "fa-steam";
	public static final String faSteamSquare = "fa-steam-square";
	public static final String faStepBackward = "fa-step-backward";
	public static final String faStepForward = "fa-step-forward";
	public static final String faStethoscope = "fa-stethoscope";
	public static final String faStickyNote = "fa-sticky-note";
	public static final String faStickyNoteO = "fa-sticky-note-o";
	public static final String faStop = "fa-stop";
	public static final String faStopCircle = "fa-stop-circle";
	public static final String faStopCircleO = "fa-stop-circle-o";
	public static final String faStreetView = "fa-street-view";
	public static final String faStrikethrough = "fa-strikethrough";
	public static final String faStumbleupon = "fa-stumbleupon";
	public static final String faStumbleuponCircle = "fa-stumbleupon-circle";
	public static final String faSubscript = "fa-subscript";
	public static final String faSubway = "fa-subway";
	public static final String faSuitcase = "fa-suitcase";
	public static final String faSunO = "fa-sun-o";
	public static final String faSuperpowers = "fa-superpowers";
	public static final String faSuperscript = "fa-superscript";
	public static final String faTable = "fa-table";
	public static final String faTablet = "fa-tablet";
	public static final String faTachometer = "fa-tachometer";
	public static final String faTag = "fa-tag";
	public static final String faTags = "fa-tags";
	public static final String faTasks = "fa-tasks";
	public static final String faTaxi = "fa-taxi";
	public static final String faTelegram = "fa-telegram";
	public static final String faTelevision = "fa-television";
	public static final String faTencentWeibo = "fa-tencent-weibo";
	public static final String faTerminal = "fa-terminal";
	public static final String faTextHeight = "fa-text-height";
	public static final String faTextWidth = "fa-text-width";
	public static final String faTh = "fa-th";
	public static final String faThLarge = "fa-th-large";
	public static final String faThList = "fa-th-list";
	public static final String faThemeisle = "fa-themeisle";
	public static final String faThermometerEmpty = "fa-thermometer-empty";
	public static final String faThermometerFull = "fa-thermometer-full";
	public static final String faThermometerHalf = "fa-thermometer-half";
	public static final String faThermometerQuarter = "fa-thermometer-quarter";
	public static final String faThermometerThreeQuarters = "fa-thermometer-three-quarters";
	public static final String faThumbTack = "fa-thumb-tack";
	public static final String faThumbsDown = "fa-thumbs-down";
	public static final String faThumbsODown = "fa-thumbs-o-down";
	public static final String faThumbsOUp = "fa-thumbs-o-up";
	public static final String faThumbsUp = "fa-thumbs-up";
	public static final String faTicket = "fa-ticket";
	public static final String faTimes = "fa-times";
	public static final String faTimesCircle = "fa-times-circle";
	public static final String faTimesCircleO = "fa-times-circle-o";
	public static final String faTint = "fa-tint";
	public static final String faToggleOff = "fa-toggle-off";
	public static final String faToggleOn = "fa-toggle-on";
	public static final String faTrademark = "fa-trademark";
	public static final String faTrain = "fa-train";
	public static final String faTransgender = "fa-transgender";
	public static final String faTransgenderAlt = "fa-transgender-alt";
	public static final String faTrash = "fa-trash";
	public static final String faTrashO = "fa-trash-o";
	public static final String faTree = "fa-tree";
	public static final String faTrello = "fa-trello";
	public static final String faTripadvisor = "fa-tripadvisor";
	public static final String faTrophy = "fa-trophy";
	public static final String faTruck = "fa-truck";
	public static final String faTry = "fa-try";
	public static final String faTty = "fa-tty";
	public static final String faTumblr = "fa-tumblr";
	public static final String faTumblrSquare = "fa-tumblr-square";
	public static final String faTwitch = "fa-twitch";
	public static final String faTwitter = "fa-twitter";
	public static final String faTwitterSquare = "fa-twitter-square";
	public static final String faUmbrella = "fa-umbrella";
	public static final String faUnderline = "fa-underline";
	public static final String faUndo = "fa-undo";
	public static final String faUniversalAccess = "fa-universal-access";
	public static final String faUniversity = "fa-university";
	public static final String faUnlock = "fa-unlock";
	public static final String faUnlockAlt = "fa-unlock-alt";
	public static final String faUpload = "fa-upload";
	public static final String faUsb = "fa-usb";
	public static final String faUsd = "fa-usd";
	public static final String faUser = "fa-user";
	public static final String faUserCircle = "fa-user-circle";
	public static final String faUserCircleO = "fa-user-circle-o";
	public static final String faUserMd = "fa-user-md";
	public static final String faUserO = "fa-user-o";
	public static final String faUserPlus = "fa-user-plus";
	public static final String faUserSecret = "fa-user-secret";
	public static final String faUserTimes = "fa-user-times";
	public static final String faUsers = "fa-users";
	public static final String faVenus = "fa-venus";
	public static final String faVenusDouble = "fa-venus-double";
	public static final String faVenusMars = "fa-venus-mars";
	public static final String faViacoin = "fa-viacoin";
	public static final String faViadeo = "fa-viadeo";
	public static final String faViadeoSquare = "fa-viadeo-square";
	public static final String faVideoCamera = "fa-video-camera";
	public static final String faVimeo = "fa-vimeo";
	public static final String faVimeoSquare = "fa-vimeo-square";
	public static final String faVine = "fa-vine";
	public static final String faVk = "fa-vk";
	public static final String faVolumeControlPhone = "fa-volume-control-phone";
	public static final String faVolumeDown = "fa-volume-down";
	public static final String faVolumeOff = "fa-volume-off";
	public static final String faVolumeUp = "fa-volume-up";
	public static final String faWeibo = "fa-weibo";
	public static final String faWeixin = "fa-weixin";
	public static final String faWhatsapp = "fa-whatsapp";
	public static final String faWheelchair = "fa-wheelchair";
	public static final String faWheelchairAlt = "fa-wheelchair-alt";
	public static final String faWifi = "fa-wifi";
	public static final String faWikipediaW = "fa-wikipedia-w";
	public static final String faWindowClose = "fa-window-close";
	public static final String faWindowCloseO = "fa-window-close-o";
	public static final String faWindowMaximize = "fa-window-maximize";
	public static final String faWindowMinimize = "fa-window-minimize";
	public static final String faWindowRestore = "fa-window-restore";
	public static final String faWindows = "fa-windows";
	public static final String faWordpress = "fa-wordpress";
	public static final String faWpbeginner = "fa-wpbeginner";
	public static final String faWpexplorer = "fa-wpexplorer";
	public static final String faWpforms = "fa-wpforms";
	public static final String faWrench = "fa-wrench";
	public static final String faXing = "fa-xing";
	public static final String faXingSquare = "fa-xing-square";
	public static final String faYCombinator = "fa-y-combinator";
	public static final String faYahoo = "fa-yahoo";
	public static final String faYelp = "fa-yelp";
	public static final String faYoast = "fa-yoast";
	public static final String faYoutube = "fa-youtube";
	public static final String faYoutubePlay = "fa-youtube-play";
	public static final String faYoutubeSquare = "fa-youtube-square";
}
