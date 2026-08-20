/// <reference types="jquery" />
/// <reference types="jqueryui" />
/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.ts" />
//import WebUI from "domui.webui.util";

namespace WebUI {
	let _customUpdatesContributors = $.Callbacks("unique");

	let _customUpdatesContributorsTimerID: number = null;

	let _browserChecked = false;

	/**
	 * registers function that gets called after doCustomUpdates sequence of calls ends, with 500 delay - doCustomUpdates can trigger new doCustomUpdates etc...
	 * @param contributorFunction
	 */
	export function registerCustomUpdatesContributor(contributorFunction: Function): void {
		_customUpdatesContributors.add(contributorFunction);
	}

	export function unregisterCustomUpdatesContributor(contributorFunction: Function): void {
		_customUpdatesContributors.remove(contributorFunction);
	}

	export function doCustomUpdates(): void {
		$('.floatThead-wrapper').each(
			function (index, node) {
				$(node).attr('stretch', $(node).find('>:first-child').attr('stretch'));
			}
		);
		$('[stretch=true]').doStretch();
		$('.ui-dt, .ui-fixovfl').fixOverflow();
		$('input[marker]').setBackgroundImageMarker();

		//-- Limit textarea size on paste events
		$("textarea[mxlength], textarea[maxbytes]")
			.unbind("input.domui")
			.unbind("propertychange.domui")
			.bind('input.domui propertychange.domui', function () {
				let maxLength = attrNumber(this, 'mxlength');				// Use mxlength because Chrome improperly implements maxlength (issue 252613)
				let maxBytes = attrNumber(this, 'maxbytes');
				let val = $(this).val() as string;
				let newlines = (val.match(/\r\n/g) || []).length;				// Count the #of 2-char newlines, as they will be replaced by 1 newline character
				if (maxBytes < 0) {
					if (maxLength < 0)
						return;
				} else if (maxLength < 0) {
					maxLength = maxBytes;
				}

				if (val.length + newlines > maxLength) {
					val = val.substring(0, maxLength - newlines);
					$(this).val(val);
				}
				if (maxBytes > 0) {
					let cutoff = WebUI.truncateUtfBytes(val, maxBytes);
					if (cutoff < val.length) {
						val = val.substring(0, cutoff);
						$(this).val(val);
					}
				}
			});

		//-- Limit textarea size on key presses
		$("textarea[mxlength], textarea[maxbytes]")
			.unbind("keypress.domui")
			.bind('keypress.domui', function (evt) {
				if (evt.which == 0 || evt.which == 8)
					return true;

				//-- Is the thing too long already?
				let maxLength = attrNumber(this, 'mxlength');				// Use mxlength because Chrome improperly implements maxlength (issue 252613)
				let maxBytes = attrNumber(this, 'maxbytes');
				let val = $(this).val() as string;
				let newlines = (val.match(/\r\n/g) || []).length;				// Count the #of 2-char newlines, as they will be replaced by 1 newline character
				if (maxBytes < 0) {
					if (maxLength < 0)
						return true;
				} else if (maxLength < 0) {
					maxLength = maxBytes;
				}
				if (val.length - newlines >= maxLength)							// Too many chars -> not allowed
					return false;
				if (maxBytes > 0) {
					let bytes = WebUI.utf8Length(val);
					if (bytes >= maxBytes)
						return false;
				}
				return true;
			});

		//custom updates may fire several times in sequence, se we fire custom contributors only after it gets steady for a while (500ms)
		if (_customUpdatesContributorsTimerID) {
			window.clearTimeout(_customUpdatesContributorsTimerID);
			_customUpdatesContributorsTimerID = null;
		}
		_customUpdatesContributorsTimerID = window.setTimeout(function () {
			try {
				_customUpdatesContributors.fire()
			} catch (ex) {
			}
		}, 500);
		//$('.ui-dt-ovflw-tbl').floatThead('reflow');
	}

	function attrNumber(elem, name: string): number {
		let val = $(elem).attr(name);
		if (typeof val == 'undefined')
			return -1;
		return Number(val);
	}

	export function onDocumentReady(): void {
		checkBrowser();
		checkPasswordManagerInterference();
		WebUI.handleCalendarChanges();
		if ((window as any).DomUIDevel)
			handleDevelopmentMode();
		doCustomUpdates();
	}

	/**
	 * Nodes LastPass injects into the page. Their mere presence proves nothing: LastPass adds them
	 * to every page it inspects, also when it obeys us and draws nothing at all - so they are used
	 * only to recognize what is found on top of a field, never as the symptom itself. These are
	 * LastPass internals, not a documented contract: if they are renamed the detection below
	 * silently stops working, which is the acceptable failure mode here.
	 */
	const PWMGR_MARKER = "[data-lastpass-icon-root],[data-lastpass-root]";

	/** How far to the left of a field's right edge the in-field icon can sit, in pixels. */
	const PWMGR_ICON_ZONE = 60;

	/** Fields smaller than this are collapsed or hidden, and cannot be covered in a meaningful way. */
	const PWMGR_MIN_FIELD_WIDTH = 20;

	const PWMGR_MIN_FIELD_HEIGHT = 10;

	/** Set once the user dismissed the warning, so we nag at most once per browser. */
	const PWMGR_DISMISSED = "domui.pwmgrWarningDismissed";

	/**
	 * Set once the barrier was continued past, for this loaded document only: the barrier hides the
	 * password manager's own icons, and clicking those is the fastest way to switch it off. So it
	 * must be possible to get it out of the way - but only here and now. Every next page shows it
	 * again, which is exactly the nagging that is wanted until the manager is really off.
	 */
	let _pwmgrBarrierContinued = false;

	/** How often to look for a covered field once the user touched something. */
	const PWMGR_PROBE_INTERVAL = 500;

	/** Give up after this many probes, so we never keep looking for the rest of the session. */
	const PWMGR_MAX_PROBES = 40;

	/**
	 * LastPass ignores every documented opt-out attribute we render on our inputs (see
	 * HtmlTagRenderer#renderPasswordManagerHints) and still draws its icon inside the focused
	 * field, where it covers the field's content. Its settings cannot be read from a page, so
	 * instead we detect the symptom: an icon of its own sitting on top of a field that carries
	 * our opt-out attribute. Nothing at all is observable before the user touches a field -
	 * LastPass leaves no trace on load and ignores programmatic focus - so this cannot be decided
	 * any earlier than this.
	 *
	 * Deliberately silent when nothing covers a field: a missed warning is harmless, a warning
	 * shown to someone without the problem is not.
	 */
	function checkPasswordManagerInterference(): void {
		if (isRefusingPasswordManagers()) {
			if (_pwmgrBarrierContinued)
				return;
		} else if (window.localStorage.getItem(PWMGR_DISMISSED) === "true") {
			return;
		}

		let probes = 0;
		let timer: number = null;

		function stopProbing(): void {
			if (timer !== null) {
				window.clearInterval(timer);
				timer = null;
			}
			document.removeEventListener("focusin", onInteraction);
			document.removeEventListener("click", onInteraction);
		}

		function probe(): void {
			if (isFieldCoveredByPasswordManager()) {
				stopProbing();
				if (isRefusingPasswordManagers())
					showPasswordManagerBarrier();
				else
					showPasswordManagerWarning();
			} else if (++probes >= PWMGR_MAX_PROBES) {
				stopProbing();								// No LastPass, or its icons are off.
			}
		}

		/*
		 * Measured on Chrome: LastPass injects its marker around two seconds after the field is
		 * touched, and that delay is nothing we control - hence a bounded poll instead of a
		 * single check on a guessed delay. Both events are needed to start it: clicking a field
		 * that already has focus (DomUI autofocuses one on every page) fires no focusin, while
		 * tabbing into a field fires no click.
		 */
		function onInteraction(): void {
			if (timer === null)
				timer = window.setInterval(probe, PWMGR_PROBE_INTERVAL);
		}

		document.addEventListener("focusin", onInteraction);
		document.addEventListener("click", onInteraction);
	}

	/**
	 * T when a password manager actually put something on top of a field that we told it to leave
	 * alone - i.e. a field rendered with data-lpignore, so not the login page's credential fields
	 * where a manager is welcome.
	 *
	 * Finding LastPass' marker nodes is not enough to conclude anything: measured on Chrome it
	 * injects those into every page it inspects, including pages where it obeys us and draws no
	 * icon at all. The icon itself cannot be seen in the DOM either - it lives in a closed shadow
	 * root inside a host that is forced to 0x0 - but it does take part in hit testing. So ask who
	 * owns the pixels along the right hand side of each field, where the in-field icon is drawn:
	 * anything belonging to the manager there is covering content it was told not to touch.
	 */
	function isFieldCoveredByPasswordManager(): boolean {
		let fields = document.querySelectorAll("input[data-lpignore],textarea[data-lpignore],select[data-lpignore]");
		for (let index = 0; index < fields.length; index++) {
			let rect = fields[index].getBoundingClientRect();
			if (rect.width < PWMGR_MIN_FIELD_WIDTH || rect.height < PWMGR_MIN_FIELD_HEIGHT)
				continue;

			let y = Math.round(rect.top + rect.height / 2);
			let limit = Math.min(PWMGR_ICON_ZONE, rect.width);
			for (let dx = 2; dx < limit; dx += 4) {
				let hit = document.elementFromPoint(Math.round(rect.right - dx), y);
				if (hit && hit.closest(PWMGR_MARKER))
					return true;
			}
		}
		return false;
	}

	/**
	 * T when the application refuses to be used at all while a disobeying password manager is
	 * active - see DomApplication#isRefuseDisobeyingPasswordManagers.
	 */
	function isRefusingPasswordManagers(): boolean {
		return (window as any).DomUIRefusePwmgr === true;
	}

	/**
	 * Show the barrier for applications that refuse to run while a disobeying password manager is
	 * active: it silently alters data in fields the user never touched, and for those applications
	 * that corruption is worse than an unusable page. It covers the entire screen, so it cannot be
	 * overlooked the way the banner is.
	 *
	 * It has a "continue" button, but that only hides it until the next page: the barrier covers
	 * the password manager's own in-field icons, and clicking those is the fastest route to its
	 * "turn off for this site" option - so leaving no way to uncover them would leave the user
	 * stuck. Nothing about the dismissal is remembered beyond this document.
	 *
	 * Styled inline for the same reason as the banner below.
	 */
	function showPasswordManagerBarrier(): void {
		if (_pwmgrBarrierContinued || document.getElementById("domui-pwmgr-barrier"))
			return;

		let overlay = document.createElement("div");
		overlay.id = "domui-pwmgr-barrier";
		overlay.setAttribute("role", "alertdialog");
		overlay.tabIndex = -1;
		overlay.style.cssText = "position:fixed;left:0;top:0;right:0;bottom:0;z-index:2147483647;"
			+ "display:flex;align-items:center;justify-content:center;overflow:auto;"
			+ "padding:24px;background:#b3261e;color:#fff;font-size:16px;line-height:1.5;"
			+ "outline:none";

		let panel = document.createElement("div");
		panel.style.cssText = "max-width:720px";
		overlay.appendChild(panel);

		let title = document.createElement("div");
		title.style.cssText = "margin:0 0 16px 0;font-size:26px;font-weight:700;line-height:1.2";
		title.appendChild(document.createTextNode(WebUI._T.pwmgrBarrierTitle));
		panel.appendChild(title);

		appendBarrierText(panel, WebUI._T.pwmgrBarrierText);
		appendBarrierText(panel, WebUI._T.pwmgrBarrierAction);

		/*
		 * The barrier covers the page, but the fields below it can still be reached with the
		 * keyboard - and typing in those is exactly what must not happen while it is up. So keep
		 * the focus inside it. Refocusing the overlay itself re-fires this handler, but then the
		 * overlay contains the focus so that terminates immediately.
		 */
		function keepFocus(e: FocusEvent): void {
			if (!overlay.contains(e.target as Node))
				overlay.focus();
		}

		let btn = document.createElement("button");
		btn.type = "button";
		btn.style.cssText = "margin-top:8px;cursor:pointer;padding:8px 20px;border:1px solid #fff;"
			+ "border-radius:3px;background:#fff;color:#b3261e;font-size:inherit;font-weight:700";
		btn.appendChild(document.createTextNode(WebUI._T.pwmgrBarrierContinue));
		btn.onclick = function (): void {
			_pwmgrBarrierContinued = true;					// This document only - the next page nags again.
			document.removeEventListener("focusin", keepFocus, true);
			$(overlay).remove();
		};
		panel.appendChild(btn);

		document.body.appendChild(overlay);
		overlay.focus();
		document.addEventListener("focusin", keepFocus, true);
	}

	function appendBarrierText(panel: HTMLElement, text: string): void {
		let para = document.createElement("div");
		para.style.cssText = "margin:0 0 12px 0";
		para.appendChild(document.createTextNode(text));
		panel.appendChild(para);
	}

	/**
	 * Show the dismissible banner telling the user to switch their password manager's in-field
	 * icons off. Styled inline on purpose: this is a framework level warning that must render
	 * in every theme, including themes outside this repository that never heard of its class.
	 */
	function showPasswordManagerWarning(): void {
		if (document.getElementById("domui-pwmgr-warning"))
			return;

		let bar = document.createElement("div");
		bar.id = "domui-pwmgr-warning";
		bar.setAttribute("role", "alert");
		bar.style.cssText = "position:fixed;left:0;right:0;bottom:0;z-index:20000;display:flex;"
			+ "align-items:center;gap:12px;padding:12px 16px;background:#b3261e;color:#fff;"
			+ "border-top:1px solid #7f1a15;font-size:13px;font-weight:600;line-height:1.4;"
			+ "box-shadow:0 -2px 8px rgba(0,0,0,.3)";

		let msg = document.createElement("span");
		msg.style.cssText = "flex:1";
		msg.appendChild(document.createTextNode(WebUI._T.pwmgrIconWarning));
		bar.appendChild(msg);

		let btn = document.createElement("button");
		btn.type = "button";
		btn.style.cssText = "flex:none;cursor:pointer;padding:4px 12px;border:1px solid #fff;"
			+ "border-radius:3px;background:#fff;color:#b3261e;font-size:inherit;"
			+ "font-weight:inherit";
		btn.appendChild(document.createTextNode(WebUI._T.pwmgrIconDismiss));
		btn.onclick = function (): void {
			window.localStorage.setItem(PWMGR_DISMISSED, "true");
			$(bar).remove();
		};
		bar.appendChild(btn);

		document.body.appendChild(bar);
	}

	function checkBrowser(): void {
		if (this._browserChecked)
			return;
		this._browserChecked = true;

		// //-- We do not support IE 7 and lower anymore.
		// if($.browser.msie && $.browser.majorVersion < 8) {
		// 	//-- Did we already report that warning this session?
		// 	if($.cookie("domuiie") == null) {
		// 		alert(WebUI.format(WebUI._T.sysUnsupported, $.browser.majorVersion));
		// 		$.cookie("domuiie", "true", {});
		// 	}
		// }
	}

	let _debugLastKeypress: number;
	let _debugMouseTarget: HTMLElement;

	export function handleDevelopmentMode(): void {
		// $(document).bind("keypress", function(e) {
		// 	console.log("keypress", e);
		// });

		$(document).bind("keydown", function (e) {
			let action = '';
			// console.log("key: shiftKey=" + e.shiftKey + " ctrl=" + e.ctrlKey + " alt=" + e.altKey + " key=" + e.key);

			if((e.key == '~' || e.key == '`') && e.shiftKey && ! e.altKey && ! e.ctrlKey) {		// Tilde
				action = 'DEVTREE';
			} else if((e.key == '~' || e.key == '`') && e.ctrlKey && e.shiftKey && ! e.altKey) {
				action = 'TESTGEN';
			} else {
				_debugLastKeypress = 0;
				return;
			}

			var t = new Date().getTime();
			if (!_debugLastKeypress || (t - _debugLastKeypress) > 250) {
				_debugLastKeypress = t;
				return;
			}
			var id = WebUI.nearestID(_debugMouseTarget);
			if (!id) {
				id = document.body.id;
			}
			WebUI.scall(id, action, {});
		});
		$(document.body).bind("mousemove", function (e) {
//			if(WebUI._NOMOVE)
//				return;
//			console.debug("move ", e);
			_debugMouseTarget = e.target as HTMLElement; // e.srcElement || e.originalTarget;
		});
	}

	/** *************** Debug thingy - it can be used internaly for debuging javascript ;) ************** */
	export function debug(debugId: string, posX: number, posY: number, debugInfoHtml: any) {
		//Be aware that debugId must not start with digit when using FF! Just lost 1 hour to learn this...
		if ("0123456789".indexOf(debugId.charAt(0)) > -1) {
			alert("debugId(" + debugId + ") starts with digit! Please use different one!");
		}
		let debugPanel = document.getElementById(debugId);
		if (null == debugPanel) {
			debugPanel = document.createElement(debugId);
			$(debugPanel).attr('id', debugId);
			$(debugPanel).css('position', 'absolute');
			$(debugPanel).css('marginLeft', 0);
			$(debugPanel).css('marginTop', 0);
			$(debugPanel).css('background-color', 'yellow');
			$(debugPanel).css('border', '1px');
			$(debugPanel).css('z-index', 2000);
			$(debugPanel).appendTo('body');
		}
		$(debugPanel).css('left', posX);
		$(debugPanel).css('top', posY);
		$(debugPanel).html(debugInfoHtml);
	}

	export function addPagerAccessKeys(e): void {
		let KEY = {
			HOME: 36,
			END: 35,
			PAGE_UP: 33,
			PAGE_DOWN: 34
		};
		if ($('div.ui-dp-btns').size() > 0) {
			if (e.altKey) {
				if (e.keyCode == KEY.HOME) {
					$("div.ui-dp-btns > a:nth-child(1)").click();
				} else if (e.keyCode == KEY.PAGE_UP) {
					$("div.ui-dp-btns > a:nth-child(2)").click();
				} else if (e.keyCode == KEY.PAGE_DOWN) {
					$("div.ui-dp-btns > a:nth-child(3)").click();
				} else if (e.keyCode == KEY.END) {
					$("div.ui-dp-btns > a:nth-child(4)").click();
				}
			}
		}
	}

	export function addDropDownPickerKeys(e): void {
		let KEY = {
			ESC: 27
		};
		let visibleDropDownPicker = $('select.ddp-cmb:visible').first();
		if(visibleDropDownPicker.size() === 1) {
			if(e.keyCode === KEY.ESC) {
				visibleDropDownPicker.blur();
			}
		}
	}

	let _checkLeavePage = false;
	let _skipLeavePageCheck = false;

	const beforeUnloadListener = (event) => {
		if (_checkLeavePage && !_skipLeavePageCheck) {
			event.preventDefault();
			return event.returnValue = "Are you sure you want to exit?";
		} else {
			delete event['returnValue'];
		}
	};
	window.addEventListener('beforeunload', beforeUnloadListener);

	export function setCheckLeavePage(v): void {
		_checkLeavePage = v;
	}

	export function setSkipLeavePageCheck(v): void {
		_skipLeavePageCheck = v;
	}
}

