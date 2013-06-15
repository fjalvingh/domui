/* domui_load.js */
function _block() {
	WebUI.blockUI();
}
function _unblock() {
	WebUI.unblockUI();
}
$(document).ajaxStart(_block).ajaxStop(_unblock);
$(window).bind('beforeunload', function() {
	WebUI.beforeUnload();
	return undefined;
});

// -- calculate browser major and minor versions
{
	try {
		var v = $.browser.version.split(".");
		$.browser.majorVersion = parseInt(v[0], 10);
		$.browser.minorVersion = parseInt(v[1], 10);
	} catch(x) {
	}

	// alert('bmaj='+$.browser.majorVersion+", mv="+$.browser.minorVersion);
}

(function($) {
	$.fn.center = function() {
		if(this.css("position") != "fixed")
			this.css("position", "absolute");
		this.css("top", Math.max(0, ( ($(window).height() - this.outerHeight()) / 2) + $(window).scrollTop()) + "px");
		this.css("left", Math.max(0, ( ($(window).width() - this.outerWidth()) / 2) + $(window).scrollLeft()) + "px");
		return this;
	};

	$.webui = function(xml) {
		processDoc(xml);
	};

	if($().jquery === "1.2.6") {
		$.expr[':'].taconiteTag = 'a.taconiteTag';
	} else {
		$.expr[':'].taconiteTag = function(a) {
			return a.taconiteTag === 1;
		};
	}

	// add 'replace' and 'replaceContent' plugins (conditionally)
	if(typeof $.fn.replace == 'undefined')
		$.fn.replace = function(a) {
			return this.after(a).remove();
		};
	if(typeof $.fn.replaceContent == 'undefined')
		$.fn.replaceContent = function(a) {
			return this.empty().append(a);
		};
	$.fn.changeTagAttributes = changeTagAttributes;

	function log() {
		if(!window.console || !window.console.debug)
			return;
		window.console.debug.apply(window.console, arguments);
		// window.console.debug("Args: "+[].join.call(arguments,''));
	}
	;

	function processDoc(xml) {
		var status = true, ex;
		status = go(xml);
	}
	function processDocOLD(xml) {
		var status = true, ex;
		try {
			status = go(xml);
		} catch(e) {
			if(window.console && window.console.debug)
				window.console.debug("CANNOT CONVERT XML: ", e);
			status = ex = e;
		}
		if(ex)
			throw ex;
	}

	function changeTagAttributes(a) {
		log("aarg=", a);
	}

	// convert string to xml document
	function convert(s) {
		var doc;
		// log('attempting string to document conversion');
		try {
			if(window.ActiveXObject) {
				doc = new ActiveXObject('Microsoft.XMLDOM');
				doc.async = 'false';
				doc.loadXML(s);
			} else {
				var parser = new DOMParser();
				doc = parser.parseFromString(s, 'text/xml');
			}
		} catch(e) {
			if(window.console && window.console.debug)
				window.console.debug('ERROR parsing XML string for conversion: ' + e, e);
			throw e;
		}
		var ok = doc && doc.documentElement && doc.documentElement.tagName != 'parsererror';
		// log('conversion ', ok ? 'successful!' : 'FAILED');
		if(!ok) {
			if(doc && doc.documentElement)
				log(doc.documentElement.textContent);
			if(window.console && window.console.debug) {
				alert('Internal error: the server response could not be parsed!? Look in the console for more info');
			} else {
				alert('Internal error: the server response could not be parsed!? I\'ll attempt to do a full refresh now...');
				window.location.href = window.location.href;
			}
			return null;
		}
		return doc;
	}
	;

	function go(xml) {
		if(xml === "") {
			window.location.href = window.location.href;
			return;
		}

		var trimHash = {
			wrap : 1
		};

		if(typeof xml == 'string')
			xml = convert(xml);
		if(!xml || !xml.documentElement) {
			log('Invalid document');
			return false;
		}
		executeXML(xml);
	}
	function executeXML(xml) {
		var trimHash = {
			wrap : 1
		};

		// -- If this is a REDIRECT document -> redirect main page
		var rname = xml.documentElement.tagName;
		if(rname == 'redirect') {
			WebUI.blockUI();
			log("Redirecting- ");
			var to = xml.documentElement.getAttribute('url');

			// -- jal 20130129 For large documents, redirecting "inside" an existing document causes huge problems, the
			// jquery loops in the "source" document while the new one is loading. This part "clears" the existing
			// document
			// causing an ugly white screen while loading - but the loading now always works..
			document.write('<html></html>');
			document.close();
			window.location.href = to;
			return true;
		} else if(rname == 'expiredOnPollasy') {
			return true; // do nothing actually, page is in process of
			// redirecting to some other page and we need to
			// ignore responses on obsolete pollasy calls...
		} else if(rname == 'expired') {
			var msg = WebUI._T.sysSessionExpired;
			var hr = window.location.href;
			for( var i = xml.documentElement.childNodes.length; --i >= 0;) {
				var cn = xml.documentElement.childNodes[i];
				if(cn.tagName == 'msg') {
					if(cn.textContent)
						msg = cn.textContent;
					else if(cn.text)
						msg = cn.text;
				} else if(cn.tagName == 'href') {
					if(cn.textContent)
						hr = cn.textContent;
					else if(cn.text)
						hr = cn.text;
				}
			}
			alert(msg);
			window.location.href = hr; // Force reload
			return;
		}

		// try {
		var t = new Date().getTime();
		// process the document
		process(xml.documentElement.childNodes);
		var lastTime = (new Date().getTime()) - t;
		// log('Response handled in ' + lastTime + 'ms');
		// } catch (e) {
		// if (window.console && window.console.debug)
		// window.console.debug('ERROR in xml handler:' + e, e);
		// throw e;
		// }
		return true;

		// -- process the commands
		function process(commands) {
			var doPostProcess = 0;
			for( var i = 0; i < commands.length; i++) {
				if(commands[i].nodeType != 1)
					continue; // commands are elements
				var cmdNode = commands[i], cmd = cmdNode.tagName;
				if(cmd == "parsererror") { // Chrome
					alert("The server response could not be parsed: " + cmdNode.innerText);
					window.location.href = window.location.href;
					return;
				}

				if(cmd == 'head' || cmd == 'body') {
					// -- HTML response. Server state is gone due to restart or
					// lost session.
					if(!WebUI._hideExpiredMessage) {
						alert(WebUI._T.sysSessionExpired2);
					}
					window.location.href = window.location.href;
					return;
				}

				if(cmd == 'eval') {
					try {
						var js = (cmdNode.firstChild ? cmdNode.firstChild.nodeValue : null);
						// log('invoking "eval" command: ', js);
						if(js)
							$.globalEval(js);
					} catch(ex) {
						alert('eval failed: ' + ex + ", js=" + js);
						throw ex;
					}
					continue;
				}
				var q = cmdNode.getAttribute('select');
				if(!q) {
					// -- Node sans select-> we are in trouble -> this is
					// probably a server error/response. Report session error,
					// then reload. (Marc, 20111017)
					alert('The server seems to have lost this page.. Reloading the page with fresh data');
					window.location.href = window.location.href;
					return;
				}
				var jq = $(q);
				if(!jq[0]) {
					log('No matching targets for selector: ', q);
					continue;
				}

				if(cmd == 'changeTagAttributes') {
					try {
						// -- Copy attributes on this tag to the target tags
						var dest = jq[0]; // Should be 1 element
						var src = commands[i];
						for( var ai = 0, attr = ''; ai < src.attributes.length; ai++) {
							var a = src.attributes[ai], n = $.trim(a.name), v = $.trim(a.value);
							if(n == 'select' || n.substring(0, 2) == 'on')
								continue;
							if(n.substring(0, 6) == 'domjs_') {
								try {
									var s = "dest." + n.substring(6) + " = " + v;
									eval(s);
									continue;
								} catch(ex) {
									alert('domjs_ eval failed: ' + ex + ", value=" + s);
									throw ex;
								}
							}
							if(v == '---') { // drop attribute request?
								dest.removeAttribute(n);
								continue;
							}
							if(n == 'style') { // IE workaround
								dest.style.cssText = v;
								dest.setAttribute(n, v);
								// We need this dirty fix for IE7 to force
								// height recalculation of divs that has just
								// become visible (IE7 sometimes fails to
								// calculate height that stays 0!).
								if($.browser.msie && $.browser.version.substring(0, 1) == "7") {
									if( (dest.tagName.toLowerCase() == 'div' && $(dest).height() == 0)
									    && ( (v.indexOf('visibility') != -1 && v.indexOf('hidden') == -1) || (v.indexOf('display') != -1 && v
									        .indexOf('none') == -1))) {
										WebUI.refreshElement(dest.id);
									}
								}
							} else {
								// -- jal 20100720 handle disabled, readonly,
								// checked differently: these are either present
								// or not present; their value is always the
								// same.
								// alert('changeAttr: id='+dest.id+' change
								// '+n+" to "+v);

								if(dest.tagName.toLowerCase() == 'select' && n == 'class' && $.browser.mozilla) {
									dest.className = v;
									var old = dest.selectedIndex;
									dest.selectedIndex = 1; // jal 20100720
									// Fixes problem
									// where setting BG
									// color on select
									// removes the
									// dropdown button
									// image
									dest.selectedIndex = old;
								} else if(v == "" && ("checked" == n || "selected" == n || "disabled" == n || "readonly" == n)) {
									$(dest).removeAttr(n);
								} else {
									$.attr(dest, n, v);
								}
							}
						}
						continue;
					} catch(ex) {
						alert('changeTagAttr failed: ' + ex);
						throw ex;
					}
				}

				var cdataWrap = cmdNode.getAttribute('cdataWrap') || 'div';

				var a = [];
				if(cmdNode.childNodes.length > 0) {
					doPostProcess = 1;
					for( var j = 0, els = []; j < cmdNode.childNodes.length; j++)
						els[j] = createNode(cmdNode.childNodes[j]);
					a.push(trimHash[cmd] ? cleanse(els) : els);
				}

				var n = cmdNode.getAttribute('name');
				var v = cmdNode.getAttribute('value');
				if(n !== null)
					a.push(n);
				if(v !== null)
					a.push(v);

				for( var j = 1; true; j++) {
					v = cmdNode.getAttribute('arg' + j);
					if(v === null)
						break;
					a.push(v);
				}

				if(true) {
					var arg = els ? '...' : a.join(',');
					// log("invoke command: $('", q, "').", cmd, '(' + arg +
					// ')');
				}
				jq[cmd].apply(jq, a);
			}
			// apply dynamic fixes
			if(doPostProcess)
				postProcess();

			function postProcess() {
				if(!$.browser.opera && !$.browser.msie)
					return;
				$('select:taconiteTag').each(function() {
					$('option:taconiteTag', this).each(function() {
						this.setAttribute('selected', 'selected');
						this.taconiteTag = null;
					});
					this.taconiteTag = null;
				});
			}
			;

			function cleanse(els) {
				for( var i = 0, a = []; i < els.length; i++)
					if(els[i].nodeType == 1)
						a.push(els[i]);
				return a;
			}
			;

			function createNode(node) {
				var type = node.nodeType;
				if(type == 1)
					return createElement(node);
				if(type == 3)
					return fixTextNode(node.nodeValue);
				if(type == 4)
					return handleCDATA(node.nodeValue);
				return null;
			}
			;

			function handleCDATA(s) {
				var el = document.createElement(cdataWrap);
				el.innerHTML = s;
				return el;
			}
			;

			function fixTextNode(s) {
				if($.browser.msie)
					s = s.replace(/\n/g, '\r').replace(/\s+/g, ' ');
				return document.createTextNode(s);
			}
			;

			function createElement(node) {
				var e, tag = node.tagName.toLowerCase();
				// some elements in IE need to be created with attrs inline
				if($.browser.msie && !WebUI.isNormalIE9plus()) {
					var type = node.getAttribute('type');
					if(tag == 'table' || type == 'radio' || type == 'checkbox' || tag == 'button'
					    || (tag == 'select' && node.getAttribute('multiple'))) {
						try {
							var xxa = copyAttrs(null, node, true);
							e = document.createElement('<' + tag + ' ' + xxa + '>');
						} catch(xx) {
							alert('err= ' + xx + ', ' + tag + ", " + xxa);
						}
					}
				}
				if(!e) {
					e = document.createElement(tag);
					copyAttrs(e, node);
				}

				// IE fix; colspan must be explicitly set
				if($.browser.msie && tag == 'td') {
					var colspan = node.getAttribute('colspan');
					if(colspan)
						e.colSpan = parseInt(colspan);
				}

				// IE fix; script tag not allowed to have children
				if($.browser.msie && !e.canHaveChildren) {
					if(node.childNodes.length > 0)
						e.text = node.text;
				} else {
					for( var i = 0, max = node.childNodes.length; i < max; i++) {
						var child = createNode(node.childNodes[i]);
						if(child)
							e.appendChild(child);
					}
				}
				if($.browser.msie || $.browser.opera) {
					if(tag == 'select' || (tag == 'option' && node.getAttribute('selected')))
						e.taconiteTag = 1;
				}
				return e;
			}
			;

			function copyAttrs(dest, src, inline) {
				for( var i = 0, attr = ''; i < src.attributes.length; i++) {
					var a = src.attributes[i], n = $.trim(a.name), v = $.trim(a.value);

					// if(n.substring(0, 2) == 'on' && ! this._xxxw) {
					// this._xxxw = true;
					// alert('dest='+dest+", src="+src+", inline="+inline+",
					// ffox="+$.browser.mozilla);
					// }

					if(inline) {
						// -- 20091110 jal When inlining we are in trouble if
						// domjs_ is used... The domjs_ mechanism is replaced
						// with setDelayedAttributes in java.
						if(n.substring(0, 6) == 'domjs_') {
							alert('Unsupported domjs_ attribute in INLINE mode: ' + n);
						} else {
							// -- jal 20100720 handle disabled, readonly,
							// checked differently: these are either present or
							// not present; their value is always the same.
							if("checked" == n || "selected" == n || "disabled" == n || "readonly" == n) {
								// alert('inline checking '+n+" value="+v);
								// -- only add item when value != ""
								if(v != "")
									attr += (n + '="' + v + '" ');
							} else
								attr += (n + '="' + v + '" ');
						}
					} else if(n.substring(0, 6) == 'domjs_') {
						var s = "dest." + n.substring(6) + " = " + v;
						// alert('domjs eval: '+s);
						try {
							eval(s);
						} catch(ex) {
							alert('domjs_ eval failed: ' + ex + ", js=" + s);
							throw ex;
						}
						continue;
					} else if(dest && ($.browser.msie || $.browser.webkit || ($.browser.mozilla && $.browser.majorVersion >= 9))
					    && n.substring(0, 2) == 'on') {
						try {
							// if(! this._xxxw)
							// alert('event '+n+' value '+v);
							// var se = 'function(){'+v+';}';
							var se;
							if(v.indexOf('return') != -1 || v.indexOf('javascript:') != -1) {
								if(!$.browser.msie && $.browser.majorVersion >= 9) {
									se = new Function("event", v);
								} else {
									se = new Function(v);
								}
							} else {
								if(!$.browser.msie && $.browser.majorVersion >= 9) {
									se = new Function("event", 'return ' + v);
								} else {
									se = new Function('return ' + v);
								}
							}
							// if(! this._xxxw)
							// alert('event '+n+' value '+se);
							dest[n] = se;
							// this._xxxw = true;

						} catch(x) {
							alert('Cannot set EVENT: ' + n + " as " + v + ' on ' + dest);
						}
					} else if(n == 'style') { // IE workaround
						dest.style.cssText = v;
						dest.setAttribute(n, v);
					} else {
						// -- jal 20100720 handle disabled, readonly, checked
						// differently: these are either present or not present;
						// their value is always the same.
						if(v == "" && ("checked" == n || "selected" == n || "disabled" == n || "readonly" == n)) {
							$(dest).removeAttr(n);
						} else {
							$.attr(dest, n, v);
						}
					}
				}
				return attr;
			}
			;
		}
		;
	}
	;

	/**
	 * Read or set a cookie.
	 */
	$.cookie = function(name, value, options) {
		if(value !== undefined) {
			if(value === null)
				options.expires = -1;
			if(typeof options.expires === 'number') {
				var dt = new Date();
				dt.setDate(dt.getDate() + options.expires);
				options.expires = dt;
			}
			value = String(value);
			var c = [encodeURIComponent(name), '=', encodeURIComponent(value), options.expires ? '; expires=' + options.expires.toUTCString() : '',
			    options.path ? '; path=' + options.path : '', options.domain ? '; domain=' + options.domain : '', options.secure ? '; secure' : '']
			    .join('');
			return (document.cookie = c);
		}

		var cookar = document.cookie.split("; ");
		for( var i = cookar.length; --i >= 0;) {
			var par = cookar[i].split('=');
			if(par.length < 2)
				continue;
			var rname = decodeURIComponent(par.shift().replace(/\+/g, ' '));
			if(rname === name) {
				return decodeURIComponent(par.join('=').replace(/\+/g, ' '));
			}
		}
		return null;
	};

	$.fn.executeDeltaXML = executeXML;
})(jQuery);
