$(function(){
	$.getScript("$js/domui-date-checker.js");
});

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

//-- calculate browser major and minor versions
{
	try {
		var v = $.browser.version.split(".");
		$.browser.majorVersion = parseInt(v[0], 10);
		$.browser.minorVersion = parseInt(v[1], 10);

		//-- And like clockwork MS fucks up with IE 11: it no longer registers as msie. Fix that here.
		if(navigator.appName == 'Netscape') {
			var ua = navigator.userAgent;
			if(ua.indexOf("Trident/") != -1)
				$.browser.msie = true;
		}

		if (/Edge/.test(navigator.userAgent)) {
			$.browser.ieedge = true;
		}
	} catch(x) {}

//	alert('bmaj='+$.browser.majorVersion+", mv="+$.browser.minorVersion);
}

( function($) {
	$.fn.center = function() {
		if(this.css("position") != "fixed") {
			this.css("position", "absolute");
			this.css("top", Math.max(0, ( ($(window).height() - this.outerHeight()) / 2) + $(window).scrollTop()) + "px");
			this.css("left", Math.max(0, ( ($(window).width() - this.outerWidth()) / 2) + $(window).scrollLeft()) + "px");
		} else {
			//-- For fixed: do not include scroll.
			this.css("top", Math.max(0, ( ($(window).height() - this.outerHeight()) / 2)) + "px");
			this.css("left", Math.max(0, ( ($(window).width() - this.outerWidth()) / 2)) + "px");
		}

		return this;
	};

	$.webui = function(xml) {
		processDoc(xml);
	};

	if($().jquery === "1.2.6") {
		$.expr[':'].taconiteTag = 'a.taconiteTag';
	} else {
		$.expr[':'].taconiteTag = function(a) { return a.taconiteTag === 1; };
	}

	// add 'replace' and 'replaceContent' plugins (conditionally)
	if (typeof $.fn.replace == 'undefined')
		$.fn.replace = function(a) {
			return this.after(a).remove();
		};
	if (typeof $.fn.replaceContent == 'undefined')
		$.fn.replaceContent = function(a) {
			return this.empty().append(a);
		};
	$.fn.changeTagAttributes = changeTagAttributes;

	function log() {
		if (!window.console || !window.console.debug)
			return;
		window.console.debug.apply(window.console, arguments);
		// window.console.debug("Args: "+[].join.call(arguments,''));
	}
	function processDoc(xml) {
		var status = true, ex;
		status = go(xml);
	}
	function processDocOLD(xml) {
		var status = true, ex;
		try {
			status = go(xml);
		} catch (e) {
			if (window.console && window.console.debug)
				window.console.debug("CANNOT CONVERT XML: ", e);
			status = ex = e;
		}
		if (ex)
			throw ex;
	}


	function changeTagAttributes(a) {
		log("aarg=", a);
	}

	// convert string to xml document
	function convert(s) {
		var doc;
		//log('attempting string to document conversion');
		try {
			if (window.ActiveXObject) {
				doc = new ActiveXObject('Microsoft.XMLDOM');
				doc.async = 'false';
				doc.loadXML(s);
			} else {
				var parser = new DOMParser();
				doc = parser.parseFromString(s, 'text/xml');
			}
		} catch (e) {
			if (window.console && window.console.debug)
				window.console.debug(
						'ERROR parsing XML string for conversion: ' + e, e);
			throw e;
		}
		var ok = doc && doc.documentElement && doc.documentElement.tagName != 'parsererror';
		//log('conversion ', ok ? 'successful!' : 'FAILED');
		if (!ok) {
			if(doc && doc.documentElement)
				log(doc.documentElement.textContent);
			if (window.console && window.console.debug) {
				alert('Internal error: the server response could not be parsed!? Look in the console for more info');
			} else {
				alert('Internal error: the server response could not be parsed!? I\'ll attempt to do a full refresh now...');
				window.location.href = window.location.href;
			}
			return null;
		}
		return doc;
	}
	function go(xml) {
		if(xml === "") {
			window.location.href = window.location.href;
			return;
		}

		var trimHash = {
			wrap :1
		};

		if (typeof xml == 'string')
			xml = convert(xml);
		if (!xml || !xml.documentElement) {
			log('Invalid document');
			return false;
		}
		executeXML(xml);
	}
	function executeXML(xml) {
		var trimHash = {
			wrap :1
		};

		// -- If this is a REDIRECT document -> redirect main page
		var rname = xml.documentElement.tagName;
		if (rname == 'redirect') {
			WebUI.blockUI();
			log("Redirecting- ");
			var to = xml.documentElement.getAttribute('url');

			if (!$.browser.msie && ! $.browser.ieedge) {
				//-- jal 20130129 For large documents, redirecting "inside" an existing document causes huge problems, the
				// jquery loops in the "source" document while the new one is loading. This part "clears" the existing document
				// causing an ugly white screen while loading - but the loading now always works..
				try {
					document.write('<html></html>');
					document.close();
				} catch(xxx) {
					// jal 20130626 Suddenly Firefox no longer allows this. Deep, deep sigh.
				}
			}
			window.location.href = to;
			return true;
		} else if (rname == 'expiredOnPollasy'){
			return true; // do nothing actually, page is in process of redirecting to some other page and we need to ignore responses on obsolete pollasy calls...
		} else if (rname == 'expired') {
			var msg = WebUI._T.sysSessionExpired;
			var hr = window.location.href;
			for ( var i = xml.documentElement.childNodes.length; --i >= 0;) {
				var cn = xml.documentElement.childNodes[i];
				if (cn.tagName == 'msg') {
					if(cn.textContent)
					msg = cn.textContent;
					else if(cn.text)
						msg = cn.text;
				} else if (cn.tagName == 'href') {
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

//		try {
			var t = new Date().getTime();
			// process the document
			process(xml.documentElement.childNodes);
			var lastTime = (new Date().getTime()) - t;
			//log('Response handled in ' + lastTime + 'ms');
	//	} catch (e) {
	//		if (window.console && window.console.debug)
	//			window.console.debug('ERROR in xml handler:' + e, e);
	//		throw e;
	//	}
		return true;

		// -- process the commands
		function process(commands) {
			var doPostProcess = 0;
			for ( var i = 0; i < commands.length; i++) {
				if (commands[i].nodeType != 1)
					continue; // commands are elements
				var cmdNode = commands[i], cmd = cmdNode.tagName;
				if(cmd == "parsererror") { // Chrome
					alert("The server response could not be parsed: "+cmdNode.innerText);
					window.location.href = window.location.href;
					return;
				}

				if(cmd == 'head' || cmd == 'body') {
					//-- HTML response. Server state is gone due to restart or lost session.
					if(!WebUI._hideExpiredMessage){
						alert(WebUI._T.sysSessionExpired2);
					}
 					window.location.href = window.location.href;
					return;
				}

				if (cmd == 'eval') {
					try {
						var js = (cmdNode.firstChild ? cmdNode.textContent : null); //textContent due to AJAX 4096 limit per single node content.
						//log('invoking "eval" command: ', js);
						if (js)
							$.globalEval(js);
					} catch(ex) {
						alert('eval failed: '+ex+", js="+js);
						throw ex;
					}
					continue;
				}
				var q = cmdNode.getAttribute('select');
				if(! q) {
					//-- Node sans select-> we are in trouble -> this is probably a server error/response. Report session error, then reload. (Marc, 20111017)
					alert('The server seems to have lost this page.. Reloading the page with fresh data');
					window.location.href = window.location.href;
					return;
				}
				var jq = $(q);
				if (!jq[0]) {
					log('No matching targets for selector: ', q);
					continue;
				}

				if (cmd == 'changeTagAttributes') {
					try {
						// -- Copy attributes on this tag to the target tags
						var dest = jq[0]; // Should be 1 element

						var names = [dest.attributes.length];
						for ( var ai = 0; ai < dest.attributes.length; ai++) {
							names[ai] = $.trim(dest.attributes[ai].name);
						}

						var src = commands[i];
						for ( var ai = 0, attr = ''; ai < src.attributes.length; ai++) {
							var a = src.attributes[ai], n = $.trim(a.name), v = $.trim(a.value);
							if (n == 'select' || n.substring(0, 2) == 'on')
								continue;
							if (n.substring(0, 6) == 'domjs_') {
								try {
									var s = "dest." + n.substring(6) + " = " + v;
									eval(s);
									continue;
								} catch(ex) {
									alert('domjs_ eval failed: '+ex+", value="+s);
									throw ex;
								}
							}
							if (v == '---') { // drop attribute request?
								dest.removeAttribute(n);
								continue;
							}
							if (n == 'style') { // IE workaround
								dest.style.cssText = v;
								dest.setAttribute(n, v);
								//We need this dirty fix for IE7 to force height recalculation of divs that has just become visible (IE7 sometimes fails to calculate height that stays 0!).
								if($.browser.msie && $.browser.version.substring(0, 1) == "7"){
									if ((dest.tagName.toLowerCase() == 'div' && $(dest).height() == 0) && ((v.indexOf('visibility') != -1 && v.indexOf('hidden') == -1) || (v.indexOf('display') != -1 && v.indexOf('none') == -1))){
										WebUI.refreshElement(dest.id);
									}
								}
							} else {
								//-- jal 20100720 handle disabled, readonly, checked differently: these are either present or not present; their value is always the same.
//								alert('changeAttr: id='+dest.id+' change '+n+" to "+v);

								if(dest.tagName.toLowerCase() == 'select' && n == 'class' && $.browser.mozilla) {
									dest.className = v;
									var old = dest.selectedIndex;
									dest.selectedIndex = 1;			// jal 20100720 Fixes problem where setting BG color on select removes the dropdown button image
									dest.selectedIndex = old;
								} else if(v == "" && ("checked" == n || "selected" == n || "disabled" == n || "readonly" == n)) {
									$(dest).removeAttr(n);
									removeValueFromArray(names, n);
								} else {
									$.attr(dest, n, v);
									removeValueFromArray(names, n);
								}
							}
						}
						for ( var ai = 0; ai < names.length; ai++) {
							var a = names[ai];
							if(a == 'checked' || a == 'disabled' || a == 'title') {
								$(dest).removeAttr(a);
							}
						}
						continue;
					} catch(ex) {
						alert('changeTagAttr failed: '+ex);
						throw ex;
					}
				}

				var cdataWrap = cmdNode.getAttribute('cdataWrap') || 'div';

				var a = [];
				if (cmdNode.childNodes.length > 0) {
					doPostProcess = 1;
					for ( var j = 0, els = []; j < cmdNode.childNodes.length; j++)
						els[j] = createNode(cmdNode.childNodes[j]);
					a.push(trimHash[cmd] ? cleanse(els) : els);
				}

				var n = cmdNode.getAttribute('name');
				var v = cmdNode.getAttribute('value');
				if (n !== null)
					a.push(n);
				if (v !== null)
					a.push(v);

				for ( var j = 1; true; j++) {
					v = cmdNode.getAttribute('arg' + j);
					if (v === null)
						break;
					a.push(v);
				}

				if (true) {
					var arg = els ? '...' : a.join(',');
					//log("invoke command: $('", q, "').", cmd, '(' + arg + ')');
				}
				jq[cmd].apply(jq, a);
			}
			// apply dynamic fixes
			if (doPostProcess)
				postProcess();

			function postProcess() {
				if (!$.browser.opera && !$.browser.msie)
					return;
				$('select:taconiteTag').each( function() {
					$('option:taconiteTag', this).each( function() {
						this.setAttribute('selected', 'selected');
						this.taconiteTag = null;
					});
					this.taconiteTag = null;
				});
			}
			function removeValueFromArray(names, n) {
				var index = names.indexOf(n);
				if(index > -1) {
					names.splice(index, 1);
				}
			}

			function cleanse(els) {
				for ( var i = 0, a = []; i < els.length; i++)
					if (els[i].nodeType == 1)
						a.push(els[i]);
				return a;
			}
			function createNode(node) {
				var type = node.nodeType;
				if (type == 1)
					return createElement(node);
				if (type == 3)
					return fixTextNode(node.nodeValue);
				if (type == 4)
					return handleCDATA(node.nodeValue);
				return null;
			}
			function handleCDATA(s) {
				var el = document.createElement(cdataWrap);
				el.innerHTML = s;
				return el;
			}
			function fixTextNode(s) {
				return document.createTextNode(s);
			}
			function createElement(node) {
				var e, tag = node.tagName.toLowerCase();
				// some elements in IE need to be created with attrs inline
				if ($.browser.msie && !WebUI.isNormalIE9plus()) {
					var type = node.getAttribute('type');
					if (tag == 'table'
							|| type == 'radio'
							|| type == 'checkbox'
							|| tag == 'button'
							|| (tag == 'select' && node
									.getAttribute('multiple'))) {
						try {
							var xxa = copyAttrs(null, node, true);
							e = document.createElement('<' + tag + ' '
								+  xxa + '>');
						} catch(xx) {
							alert('err= '+xx+', '+tag+", "+xxa);
						}
					}
				}
				if (!e) {
					e = document.createElement(tag);
					copyAttrs(e, node);
				}

				// IE fix; colspan must be explicitly set
				if ($.browser.msie && tag == 'td') {
					var colspan = node.getAttribute('colspan');
					if (colspan)
						e.colSpan = parseInt(colspan);
				}

				// IE fix; script tag not allowed to have children
				if ($.browser.msie && !e.canHaveChildren) {
					if (node.childNodes.length > 0)
						e.text = node.text;
				} else {
					for ( var i = 0, max = node.childNodes.length; i < max; i++) {
						var child = createNode(node.childNodes[i]);
						if (child)
							e.appendChild(child);
					}
				}
				if ($.browser.msie || $.browser.opera) {
					if (tag == 'select'
							|| (tag == 'option' && node
									.getAttribute('selected')))
						e.taconiteTag = 1;
				}
				return e;
			}
			function copyAttrs(dest, src, inline) {
				for ( var i = 0, attr = ''; i < src.attributes.length; i++) {
					var a = src.attributes[i], n = $.trim(a.name), v = $.trim(a.value);

//					if(n.substring(0, 2) == 'on' && ! this._xxxw) {
//						this._xxxw = true;
//						alert('dest='+dest+", src="+src+", inline="+inline+", ffox="+$.browser.mozilla);
//					}

					if (inline) {
						//-- 20091110 jal When inlining we are in trouble if domjs_ is used... The domjs_ mechanism is replaced with setDelayedAttributes in java.
						if(n.substring(0, 6) == 'domjs_') {
							alert('Unsupported domjs_ attribute in INLINE mode: '+n);
						} else {
							//-- jal 20100720 handle disabled, readonly, checked differently: these are either present or not present; their value is always the same.
							if("checked" == n || "selected" == n || "disabled" == n || "readonly" == n) {
//								alert('inline checking '+n+" value="+v);
								//-- only add item when value != ""
								if(v != "")
									attr += (n + '="' + v + '" ');
							} else
								attr += (n + '="' + v + '" ');
						}
					} else if (n.substring(0, 6) == 'domjs_') {
						var s = "dest." + n.substring(6) + " = " + v;
						//alert('domjs eval: '+s);
						try {
							eval(s);
						} catch(ex) {
							alert('domjs_ eval failed: '+ex+", js="+s);
							throw ex;
						}

					} else if (v != "" && dest && ($.browser.msie || $.browser.webkit || ($.browser.mozilla && $.browser.majorVersion >= 9 )) && n.substring(0, 2) == 'on') {
						try {
							if(v.indexOf("javascript:") == 0)
								v = $.trim(v.substring(11));
							var fntext = v.indexOf("return") >= 0 ? v : "return "+v;		// for now accept everything that at least does a return.

							if($.browser.msie && $.browser.majorVersion < 9)
								se = new Function(fntext);
							else
								se = new Function("event", fntext);
							dest[n] = se;

						} catch(x) {
							alert('DomUI: Cannot set EVENT '+n+" as "+v+' on '+dest+": "+x);
						}
					} else if (n == 'style') { // IE workaround
						dest.style.cssText = v;
						dest.setAttribute(n, v);
					} else {
						//-- jal 20100720 handle disabled, readonly, checked differently: these are either present or not present; their value is always the same.
						if(v == "" && ("checked" == n || "selected" == n || "disabled" == n || "readonly" == n)) {
							$(dest).removeAttr(n);
						} else {
							$.attr(dest, n, v);
						}
					}
				}
				return attr;
			}
		}
	}
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
			value= String(value);
			var c = [
				encodeURIComponent(name), '=', encodeURIComponent(value),
		        options.expires ? '; expires=' + options.expires.toUTCString() : '',
		        options.path    ? '; path=' + options.path : '',
		        options.domain  ? '; domain=' + options.domain : '',
		        options.secure  ? '; secure' : ''
	        ].join('');
			return (document.cookie = c);
		}

		var cookar= document.cookie.split("; ");
		for(var i = cookar.length; --i >= 0;) {
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

/**
 * jQuery scroll overflow fixerydoo for IE7's "let's create huge problems by putting a scrollbar inside the scrolling area" blunder. It
 * locates all scrolled-in area's and adds 20px of padding at the bottom.
 */
(function ($) {
	$.fn.fixOverflow = function () {
		if(! $.browser.msie || $.browser.version.substring(0, 1) != "7")
			return this;

		return this.each(function () {
			if (this.scrollWidth > this.offsetWidth) {
				$(this).css({ 'padding-bottom' : '20px' });
				if (this.scrollHeight <= this.offsetHeight ){
					//hide vertical scroller only if it is not needed after padding is increased.
					$(this).css({ 'overflow-y' : 'hidden' });
				}
			}

			//-- jal 20110727 Do the same for height?
			if(this.scrollHeight > this.offsetHeight) {
				$(this).css({ 'margin-right' : '17px' });
				if(this.scrollWidth <= this.offsetWidth) {
					$(this).css({ 'overflow-x' : 'hidden' });
				}
			}

		});
	};
})(jQuery);

(function($) {
	if($.browser.msie && $.browser.majorVersion < 10) {
		$.dbg = function(a,b,c,d,e) {
			if(window.console == undefined)
				return;
			switch(arguments.length) {
			default:
				window.console.log(a);
				return;
			case 2:
				window.console.log(a,b);
				return;
			case 3:
				window.console.log(a,b,c);
				return;
			case 4:
				window.console.log(a,b,c,d);
				return;
			case 5:
				window.console.log(a,b,c,d,e);
				return;
			}
		};
	} else if(window.console != undefined) {
		if(window.console.debug != undefined) {
			$.dbg = function() {
				window.console.debug.apply(window.console, arguments);
			};
		} else if(window.console.log != undefined) {
			$.dbg = function() {
				window.console.log.apply(window.console, arguments);
			};
		}
	} else {
		$.dbg = function() {};
	}
})(jQuery);

(function ($) {
	$.fn.doStretch = function () {
		return this.each(function () {
			WebUI.stretchHeightOnNode(this);
		});
	};
})(jQuery);

(function ($) {
	$.fn.setBackgroundImageMarker = function () {
		return this.each(function () {
			if($(this).markerTransformed){
				return;
			}
			var imageUrl = 'url(' + $(this).attr('marker') + ')';
			// Wrap this in a try/catch block since IE9 throws "Unspecified error" if document.activeElement
			// is undefined when we are in an IFrame. TODO: better solution?
			try {
				if((!(this == document.activeElement)) && $(this).val().length == 0){
					$(this).css('background-image', imageUrl);
				}
			} catch(e) {}
			$(this).css('background-repeat', 'no-repeat');
			$(this).bind('focus',function(e){
				$(this).css('background-image', 'none');
			});
			$(this).bind('blur',function(e){
				if($(this).val().length == 0){
					$(this).css('background-image', imageUrl);
				} else {
					$(this).css('background-image', 'none');
				}
			});
			$(this).markerTransformed = true;
		});
	};
})(jQuery);

/** WebUI helper namespace */
var WebUI;
if(WebUI === undefined)
    WebUI = {};

$.extend(WebUI, {
	/**
	 * can be set to true from server code with appendJavaScript so that the expired messages will not show and
	 * block effortless refresh on class reload. Configurable in .developer.properties domui.hide-expired-alert.
	 */
	_hideExpiredMessage: false,

	/**
	 * Will be set by startPolling to define the poll interval.
	 */
	_pollInterval: 2500,

	/**
	 * When this is > 0, this keeps any page "alive" by sending an async
	 */
	_keepAliveInterval: 0,

	_ignoreErrors: false,

	setHideExpired: function() {
		WebUI._hideExpiredMessage = true;
	},

	definePageName : function(pn) {
	    $(document.body).attr("pageName", pn);
	},

	log: function() {
		$.dbg.apply(this, arguments);
	},

	/**
	 * Create a curried function containing a 'this' and a fixed set of elements.
	 */
	curry: function(scope, fn) {
	    var scope = scope || window;
	    var args = [];
	    for (var i=2, len = arguments.length; i < len; ++i) {
	        args.push(arguments[i]);
		}
		return function() {
		    fn.apply(scope, args);
	    };
	},

	/**
	 * Embeds the "this" and any *partial* parameters to the function.
	 */
	pickle: function(scope, fn) {
	    var scope = scope || window;
	    var args = [];
	    for (var i=2, len = arguments.length; i < len; ++i) {
	        args.push(arguments[i]);
		}
		return function() {
	    	var nargs = [];
	    	for(var i = 0, len = args.length; i < len; i++) // Append all args added to pickle
	    		nargs.push(args[i]);
	    	for(var i = 0, len = arguments.length; i < len; i++) // Append all params of the actual function after it
	    		nargs.push(arguments[i]);
		    fn.apply(scope, nargs);
	    };
	},

	getInputFields : function(fields) {
		// Collect all input, then create input.
		var q1 = $("input").get();
		for ( var i = q1.length; --i >= 0;) {
			var t = q1[i];
			if (t.type == 'file')
				continue;
			if (t.type == 'hidden' && !t.getAttribute('s')) // All hidden input nodes are created directly in browser java-script and because that are filtered out from server requests.
				continue;

			var val = undefined;
			if (t.type == 'checkbox' || t.type == 'radio') {
				val = t.checked ? "y" : "n";
			} else {
				val = t.value;
			}

			fields[t.id] = val;
		}

		var q1 = $("select").get();
		for ( var i = q1.length; --i >= 0;) {
			var sel = q1[i];
			var val = undefined;
			if (sel.selectedIndex != -1) {
				val = sel.options[sel.selectedIndex].value;
			}

			if(val != undefined)
				fields[sel.id] = val;
		}
		var q1 = $("textarea").get();
		for ( var i = q1.length; --i >= 0;) {
			var sel = q1[i];
			if (sel.className == 'ui-ckeditor') {
				//-- Locate the variable for this editor.
				var editor = CKEDITOR.instances[sel.id];
				if(null == editor)
					throw "Cannot locate editor with id="+sel.id;
				val = editor.getData();
			} else {
//				if($.browser.msie) { // The MS idiots remove newlines from value....
//					val = sel.innerText;
//					//alert("inner value="+sel.innerText);
//				} else
				val = sel.value;
			}
			fields[sel.id] = val;
		}

		//-- Handle all registered controls
		var list = WebUI._inputFieldList;
		for(var i = list.length; --i >= 0;) {
			var item = list[i];
			if(! document.getElementById(item.id)) {
				//-- Node gone - remove input
				list.splice(i, 1);
			} else {
				var data = item.control.getInputField();
				fields[item.id] = data;
			}
		}

		return fields;
	},

	_inputFieldList: [],

	/**
	 * This registers a non-html control as a source of input for {@link getInputFields}. The control
	 * must have a method "getInputFields(fields: Map)" which defines the inputs to send for the control.
	 */
	registerInputControl: function(id, control) {
		var list = WebUI._inputFieldList;
		for(var i = list.length; --i >= 0;) {
			var item = list[i];
			if(item.id == id) {
				item.control = control;
				return;
			}
		}
		list.push({id: id, control:control});
	},

	findInputControl: function(id) {
		//-- return registered component by id, if not found returns null
		var list = WebUI._inputFieldList;
		for(var i = list.length; --i >= 0;) {
			var item = list[i];
			if(item.id == id && document.getElementById(item.id)) {
				return item.control;
			}
		}
		return null;
	},

	getPostURL : function() {
		var p = window.location.href;
		var ix = p.indexOf('?');
		if (ix != -1)
			p = p.substring(0, ix); // Discard query string.
		return p;
	},
	getObituaryURL: function() {
		var u = WebUI.getPostURL();
		var ix = u.lastIndexOf('.');
		if(ix < 0)
			throw "INVALID PAGE URL";
		return u.substring(0, ix)+".obit";
	},

	clicked : function(h, id, evt) {
		//-- Trigger the before-clicked event on body
		$(document.body).trigger("beforeclick", $("#"+id), evt);

		// Collect all input, then create input.
		var fields = {};
		this.getInputFields(fields);
		fields.webuia = "clicked";
		fields.webuic = id;
		fields["$pt"] = DomUIpageTag;
		fields["$cid"] = DomUICID;
		WebUI.cancelPolling();

		//-- Do not call upward handlers too.
		if(! evt)
			evt = window.event;

		// jal 20131107 Cancelling the event means that you cannot click items inside a clickable item
		if(evt) {
			evt.cancelBubble = true;
			if(evt.stopPropagation)
				evt.stopPropagation();
		}
		var e = $.event.fix(evt);		// Convert to jQuery event
		//e.preventDefault(); // jal 20110216 DO NOT PREVENTDEFAULT- it will disable checkbox enable/disable

		//-- add click-related parameters
		fields._pageX = e.pageX;
		fields._pageY = e.pageY;
		fields._controlKey = e.ctrlKey == true;
		fields._shiftKey = e.shiftKey == true;
		fields._altKey = e.altKey == true;

		$.ajax( {
			url :DomUI.getPostURL(),
			dataType :"*",
			data :fields,
			cache :false,
			type: "POST",
			error :WebUI.handleError,
			success :WebUI.handleResponse
		});
		return false;						// jal 20131107 Was false, but inhibits clicking on radiobutton inside a table in Chrome.
	},

	prepareAjaxCall: function(id, action, fields) {
		if (!fields)
			fields = {};
		// Collect all input, then create input.
		this.getInputFields(fields);
		fields.webuia = action;
		fields.webuic = id;
		fields["$pt"] = DomUIpageTag;
		fields["$cid"] = DomUICID;

		return {
			url :DomUI.getPostURL(),
			dataType :"*",
			data :fields,
			cache :false,
			type: "POST",
			success :WebUI.handleResponse,
			error :WebUI.handleError
		};
	},

	scall : function(id, action, fields) {
		var call = WebUI.prepareAjaxCall(id, action, fields);
		WebUI.cancelPolling();
		$.ajax(call);
	},

	jsoncall: function(id, fields) {
		if (!fields)
			fields = {};
		fields.webuia = "$pagejson";
		fields.webuic = id;
		fields["$pt"] = DomUIpageTag;
		fields["$cid"] = DomUICID;

		var response = "";
		$.ajax( {
			url :DomUI.getPostURL(),
			dataType :"text/xml",
			data :fields,
			cache :false,
			async: false,
			type: "POST",
			success : function(data, state) {
				response = data;
			},
			error :WebUI.handleError
		});
//		console.debug("jsoncall-", response);
//		try {
			return eval("("+response+")");
//		} catch(x) {
//			console.debug("json data error", x);
//		}
	},

	/**
	 * Send a server request to a component, which will be handled by that component's componentHandleWebAction
	 * method. The json data is sent as a string parameter with the name "json"; the response is handled as a normal
	 * DomUI page request: the page is updated and any delta is returned.
	 * @returns void
	 */
	sendJsonAction: function(id, action, json) {
		var fields = {};
		fields.json = JSON.stringify(json);
		WebUI.scall(id, action, fields);
	},

	/**
	 * Call a JSON handler on a component. This is "out of bound": the current browser state of
	 * the page is /not/ sent, and the response must be a JSON document which will be the return
	 * value of this function.
	 *
	 * @param id
	 * @param fields
	 * @returns
	 */
	callJsonFunction: function(id, action, fields) {
		if (!fields)
			fields = {};
		fields.webuia = "#"+action;
		fields.webuic = id;
		fields["$pt"] = DomUIpageTag;
		fields["$cid"] = DomUICID;

		var response = "";
		$.ajax( {
			url :DomUI.getPostURL(),
			dataType :"text/xml",
			data :fields,
			cache :false,
			async: false,
			type: "POST",
			success : function(data, state) {
				response = data;
			},
			error :WebUI.handleError
		});
//		console.debug("jsoncall-", response);
//		try {
			return eval("("+response+")");
//		} catch(x) {
//			console.debug("json data error", x);
//		}
	},

	/**
	 * Send a server request to a component, which will be handled by that component's componentHandleWebAction
	 * method. The json data is sent as a string parameter with the name "json"; the response is handled as a normal
	 * DomUI page request: the page is updated and any delta is returned.
	 * @returns void
	 */
	sendJsonAction: function(id, action, json) {
		var fields = {};
		fields.json = JSON.stringify(json);
		WebUI.scall(id, action, fields);
	},

	/**
	 * Call a JSON handler on a component. This is "out of bound": the current browser state of
	 * the page is /not/ sent, and the response must be a JSON document which will be the return
	 * value of this function.
	 *
	 * @param id
	 * @param fields
	 * @returns
	 */
	callJsonFunction: function(id, action, fields) {
		if (!fields)
			fields = {};
		fields.webuia = "#"+action;
		fields.webuic = id;
		fields["$pt"] = DomUIpageTag;
		fields["$cid"] = DomUICID;

		var response = "";
		$.ajax( {
			url :DomUI.getPostURL(),
			dataType :"text/xml",
			data :fields,
			cache :false,
			async: false,
			type: "POST",
			success : function(data, state) {
				response = data;
			},
			error :WebUI.handleError
		});
//		console.debug("jsoncall-", response);
//		try {
			return eval("("+response+")");
//		} catch(x) {
//			console.debug("json data error", x);
//		}
	},

	jsoncall: function(id, fields) {
		if (!fields)
			fields = {};
		fields.webuia = "$pagejson";
		fields.webuic = id;
		fields["$pt"] = DomUIpageTag;
		fields["$cid"] = DomUICID;

		var response = "";
		$.ajax( {
			url :DomUI.getPostURL(),
			dataType :"text/xml",
			data :fields,
			cache :false,
			async: false,
			type: "POST",
			success : function(data, state) {
				response = data;
			},
			error :WebUI.handleError
		});
//		console.debug("jsoncall-", response);
//		try {
			return eval("("+response+")");
//		} catch(x) {
//			console.debug("json data error", x);
//		}
	},

	clickandchange: function(h, id, evt) {
		//-- Do not call upward handlers too.
		if(! evt)
			evt = window.event;
		if(evt) {
			evt.cancelBubble = true;
			if(evt.stopPropagation)
				evt.stopPropagation();
		}
		WebUI.scall(id, 'clickandvchange');
	},

	valuechanged: function(h, id) {
		// FIXME 20100315 jal Temporary fix for bug 680: if a DateInput has a value changed listener the onblur does not execute. So handle it here too.... The fix is horrible and needs generalization.
		var item = document.getElementById(id);
		if(item && (item.tagName == "input" || item.tagName == "INPUT") && item.className == "ui-di") {
			//-- DateInput control: manually call the onblur listener.
			this.dateInputRepairValueIn(item);
		}

		// Collect all input, then create input.
		var fields = {};
		this.getInputFields(fields);
		fields.webuia = "vchange";
		fields.webuic = id;
		fields["$pt"] = DomUIpageTag;
		fields["$cid"] = DomUICID;
		WebUI.cancelPolling();

		$.ajax( {
			url :DomUI.getPostURL(),
			dataType :"*",
			data :fields,
			cache :false,
			type: "POST",
			success :WebUI.handleResponse,
			error :WebUI.handleError
		});
	},

	/**
	 * Handle enter key pressed on keyPress for component with onLookupTyping listener. This needs to be executed on keyPress (was part of keyUp handling), otherwise other global return key listener (returnKeyPress handler) would fire.
	 */
	onLookupTypingReturnKeyHandler : function(id, event) {
		var node = document.getElementById(id);
		if(!node || node.tagName.toLowerCase() != 'input')
			return;

		if(!event){
			event = window.event;
			if (!event)
				return;
		}

		var keyCode = WebUI.normalizeKey(event);
		var isReturn = (keyCode == 13000 || keyCode == 13);

		if (isReturn) {
			//cancel current scheduledOnLookupTypingTimerID
			if (WebUI.scheduledOnLookupTypingTimerID){
				//cancel already scheduled timer event
				window.clearTimeout(WebUI.scheduledOnLookupTypingTimerID);
				WebUI.scheduledOnLookupTypingTimerID = null;
			}
			//Do not call upward handlers too, we do not want to trigger on value changed by return pressed.
			event.cancelBubble = true;
			if(event.stopPropagation)
				event.stopPropagation();

			//locate keyword input node
			var selectedIndex = WebUI.getKeywordPopupSelectedRowIndex(node);
			var trNode = selectedIndex < 0 ? null : $(node.parentNode).children("div.ui-lui-keyword-popup").children("div").children("table").children("tbody").children("tr:nth-child(" + selectedIndex + ")").get(0);
			if(trNode){
				//trigger click on row
				WebUI.setKeywordPopupSelectedRowIndex(node, -1);
				$(trNode).trigger('click');
			} else {
				//trigger lookupTypingDone when return is pressed
				WebUI.lookupTypingDone(id);
			}
		}
	},

	/**
	 * Handle for timer delayed actions, used for onLookupTyping event.
	 */
	scheduledOnLookupTypingTimerID: null,

	/*
	 * Executed as onkeyup event on input field that has implemented listener for onLookupTyping event.
	 * In case of return key call lookupTypingDone ajax that is transformed into onLookupTyping(done=true).
	 * In case of other key, lookupTyping funcion is called with delay of 500ms. Previuosly scheduled lookupTyping function is canceled.
	 * This cause that fast typing would not trigger ajax for each key stroke, only when user stops typing for 500ms ajax would be called by lookupTyping function.
	 */
	scheduleOnLookupTypingEvent : function(id, event) {
		var node = document.getElementById(id);
		if(!node || node.tagName.toLowerCase() != 'input')
			return;

		if(!event){
			event = window.event;
			if (!event)
				return;
		}
		var keyCode = WebUI.normalizeKey(event);
		var isReturn = (keyCode == 13000 || keyCode == 13);
		if (isReturn) { //handled by onLookupTypingReturnKeyHandler, just cancel propagation
			event.cancelBubble = true;
			if(event.stopPropagation)
				event.stopPropagation();
			return;
		}

		var isLeftArrowKey = (keyCode == 37000 || keyCode == 37);
		var isRightArrowKey = (keyCode == 39000 || keyCode == 39);
		if (isLeftArrowKey || isRightArrowKey){
			//in case of left or right arrow keys do nothing
			return;
		}
		if (WebUI.scheduledOnLookupTypingTimerID){
			//cancel already scheduled timer event
			window.clearTimeout(WebUI.scheduledOnLookupTypingTimerID);
			WebUI.scheduledOnLookupTypingTimerID = null;
		}
		var isDownArrowKey = (keyCode == 40000 || keyCode == 40);
		var isUpArrowKey = (keyCode == 38000 || keyCode == 38);
		if (isDownArrowKey || isUpArrowKey) {
			//Do not call upward handlers too, we do not want to trigger on value changed by return pressed.
			event.cancelBubble = true;
			if(event.stopPropagation)
				event.stopPropagation();

			//locate keyword input node
			var selectedIndex = WebUI.getKeywordPopupSelectedRowIndex(node);
			if(selectedIndex < 0)
				selectedIndex = 0;
			var trNode = $(node.parentNode).children("div.ui-lui-keyword-popup").children("div").children("table").children("tbody").children("tr:nth-child(" + selectedIndex + ")").get(0);
			if(trNode){
				trNode.className = "ui-keyword-popup-row";
			}
			var trNodes = $(node.parentNode).children("div.ui-lui-keyword-popup").children("div").children("table").children("tbody").children("tr");
			if (trNodes.length > 0){
				var divPopup = $(node.parentNode).children("div.ui-lui-keyword-popup").get(0);
				if (divPopup){
					$(divPopup).fadeIn(300);
					//must be set due to IE bug in rendering
					node.parentNode.style.zIndex = divPopup.style.zIndex;
				}
				if (isDownArrowKey){
					selectedIndex++;
				}else{
					selectedIndex--;
				}
				if (selectedIndex > trNodes.length){
					selectedIndex = 0;
				}
				if (selectedIndex < 0){
					selectedIndex = trNodes.length;
				}
				trNode = $(node.parentNode).children("div.ui-lui-keyword-popup").children("div").children("table").children("tbody").children("tr:nth-child(" + selectedIndex + ")").get(0);
				if(trNode){
					trNode.className = "ui-keyword-popop-rowsel";
				}
			}else{
				selectedIndex = 0;
			}
			WebUI.setKeywordPopupSelectedRowIndex(node, selectedIndex);
		}
		else
			WebUI.scheduledOnLookupTypingTimerID = window.setTimeout("WebUI.lookupTyping('" + id + "')", 500);
	},

	getKeywordPopupSelectedRowIndex: function(keywordInputNode){
		var selectedIndexInput = $(keywordInputNode.parentNode).children("input:hidden").get(0);
		if (selectedIndexInput){
			if (selectedIndexInput.value && selectedIndexInput.value != ""){
				return parseInt(selectedIndexInput.value);
			}
		}
		return -1;
	},

	setKeywordPopupSelectedRowIndex: function(keywordInputNode, intValue){
		var selectedIndexInput = $(keywordInputNode.parentNode).children("input:hidden").get(0);
		if (!selectedIndexInput){
			selectedIndexInput = document.createElement("input");
			selectedIndexInput.setAttribute("type","hidden");
			$(keywordInputNode.parentNode).append($(selectedIndexInput));
		}
		selectedIndexInput.value = intValue;
	},

	lookupPopupClicked : function(id) {
		var node = document.getElementById(id);
		if(!node || node.tagName.toLowerCase() != 'input') {
			return;
		}

		var selectedIndex = WebUI.getKeywordPopupSelectedRowIndex(node);
		if(selectedIndex < 0)
			selectedIndex = 0;
		var trNode = $(node.parentNode).children("div.ui-lui-keyword-popup").children("div").children("table").children("tbody").children("tr:nth-child(" + selectedIndex + ")").get(0);
		if(trNode){
			WebUI.clicked(trNode, trNode.id, null);
		}
	},

	lookupRowMouseOver : function(keywordInputId, rowNodeId) {
		var keywordInput = document.getElementById(keywordInputId);
		if(!keywordInput || keywordInput.tagName.toLowerCase() != 'input') {
			return;
		}

		var rowNode = document.getElementById(rowNodeId);
		if(!rowNode || rowNode.tagName.toLowerCase() != 'tr') {
			return;
		}

		var oldIndex = WebUI.getKeywordPopupSelectedRowIndex(keywordInput);
		if(oldIndex < 0)
			oldIndex = 0;

		var trNodes = $(rowNode.parentNode).children("tr");
		var newIndex = 0;
		for(var i = 1; i <= trNodes.length; i++){
			if (rowNode == trNodes.get(i-1)) {
				newIndex = i;
				break;
			}
		}

		if (oldIndex != newIndex){
			var deselectRow = $(rowNode.parentNode).children("tr:nth-child(" + oldIndex + ")").get(0);
			if (deselectRow){
				deselectRow.className = "ui-keyword-popop-row";
			}
			rowNode.className = "ui-keyword-popop-rowsel";
			WebUI.setKeywordPopupSelectedRowIndex(keywordInput, newIndex);
		}
	},

	//Called only from onBlur of input node that is used for lookup typing.
	hideLookupTypingPopup: function(id) {
		var node = document.getElementById(id);
		if(!node || node.tagName.toLowerCase() != 'input')
			return;
		var divPopup = $(node.parentNode).children("div.ui-lui-keyword-popup").get();
		if (divPopup){
			$(divPopup).fadeOut(200);
		}
		//fix z-index to one saved in input node
		if ($.browser.msie){
            //IE kills event stack (click is canceled) when z index is set during onblur event handler... So, we need to postpone it a bit...
            window.setTimeout(function() { try { node.parentNode.style.zIndex = node.style.zIndex;} catch (e) { /*just ignore */ } }, 200);
		}else{
            //Other browsers dont suffer of this problem, and we can set z index instantly
            node.parentNode.style.zIndex = node.style.zIndex;
		}
	},

	showLookupTypingPopupIfStillFocusedAndFixZIndex: function(id) {
		var node = document.getElementById(id);
		if(!node || node.tagName.toLowerCase() != 'input')
			return;
		var wasInFocus = node == document.activeElement;
		var qDivPopup = $(node.parentNode).children("div.ui-lui-keyword-popup");
		if (qDivPopup.length > 0){
			var divPopup = qDivPopup.get(0);
			//z-index correction must be set manually from javascript (because some bug in IE7 -> if set from domui renders incorrectly until page is refreshed?)
			divPopup.style.zIndex = node.style.zIndex + 1;
			node.parentNode.style.zIndex = divPopup.style.zIndex;
		}else{
			//fix z-index to one saved in input node
			node.parentNode.style.zIndex = node.style.zIndex;
		}

		if (wasInFocus && divPopup){
			//show popup in case that input field still has focus
			$(divPopup).show();
		}

		var trNods = $(qDivPopup).children("div").children("table").children("tbody").children("tr");
		if (trNods && trNods.length > 0) {
			for(var i=0; i < trNods.length; i++) {
				var trNod = trNods.get(i);
				//we need this jquery way of attaching events, if we use trNod.setAttribute("onmouseover",...) it does not work in IE7
				$(trNod).bind("mouseover", {nodeId: id, trId: trNod.id}, function(event) {
					WebUI.lookupRowMouseOver(event.data.nodeId, event.data.trId);
				});
			}
		}
		if (divPopup){
			$(divPopup).bind("click", {nodeId: id}, function(event) {
				WebUI.lookupPopupClicked(event.data.nodeId);
			});
		}
	},

	/*
	 * In case of longer waiting for lookupTyping ajax response show waiting animated marker.
	 * Function is called with delay of 500ms from ajax.beforeSend method for lookupTyping event.
	 */
	displayWaiting: function(id) {
		var node = document.getElementById(id);
		if (node){
			for ( var i = 0; i < node.childNodes.length; i++ ){
				if (node.childNodes[i].className == 'ui-lui-waiting'){
					node.childNodes[i].style.display = 'inline';
				}
			}
		}
	},

	/*
	 * Hiding waiting animated marker that was shown in case of longer waiting for lookupTyping ajax response.
	 * Function is called from ajax.completed method for lookupTyping event.
	 */
	hideWaiting: function(id) {
		var node = document.getElementById(id);
		if (node){
			for ( var i = 0; i < node.childNodes.length; i++ ){
				if (node.childNodes[i].className == 'ui-lui-waiting'){
					node.childNodes[i].style.display = 'none';
				}
			}
		}
	},

	lookupTyping : function(id) {
		var lookupField = document.getElementById(id);
		//check for exsistence, since it is delayed action component can be removed when action is executed.
		if (lookupField){
			// Collect all input, then create input.
			var fields = {};
			this.getInputFields(fields);
			fields.webuia = "lookupTyping";
			fields.webuic = id;
			fields["$pt"] = DomUIpageTag;
			fields["$cid"] = DomUICID;
			WebUI.cancelPolling();
			var displayWaitingTimerID = null;

			$.ajax( {
				url :DomUI.getPostURL(),
				dataType :"*",
				data :fields,
				cache :false,
				type: "POST",
				global: false,
				beforeSend: function(){
					// Handle the local beforeSend event
					var parentDiv = lookupField.parentNode;
					if (parentDiv){
						displayWaitingTimerID = window.setTimeout("WebUI.displayWaiting('" + parentDiv.id + "')", 500);
					}
   				},
			   	complete: function(){
   					// Handle the local complete event
					if (displayWaitingTimerID) {
						//handle waiting marker
   						window.clearTimeout(displayWaitingTimerID);
   						displayWaitingTimerID = null;
   						var parentDiv = lookupField.parentNode;
   						if (parentDiv) {
   							WebUI.hideWaiting(parentDiv.id);
   						}
   					}
					//handle received lookupTyping component content
					WebUI.showLookupTypingPopupIfStillFocusedAndFixZIndex(id);
					WebUI.doCustomUpdates();
   				},

				success :WebUI.handleResponse,
				error :WebUI.handleError
			});
		}
	},
	lookupTypingDone : function(id) {
		// Collect all input, then create input.
		var fields = {};
		this.getInputFields(fields);
		fields.webuia = "lookupTypingDone";
		fields.webuic = id;
		fields["$pt"] = DomUIpageTag;
		fields["$cid"] = DomUICID;
		WebUI.cancelPolling();

		$.ajax( {
			url :DomUI.getPostURL(),
			dataType :"*",
			data :fields,
			cache :false,
			type: "POST",
			success :WebUI.handleResponse,
			error :WebUI.handleError
		});
	},
	handleResponse : function(data, state) {
		WebUI.clearErrorAsy();
		if (false && window.console && window.console.debug)
			console.debug("data is ", data);
		$.webui(data);
	},
	handleError : function(request, status, exc) {
		var txt = request.responseText;
		if (document.body)
			document.body.style.cursor = 'default';
		// alert('Server error: '+status+", len="+txt.length+", val="+txt);
		if (txt.length == 0) {
			//-- Firefox fix: if the page is unloading but a request is pending this may cause an status=ERROR for that page. Prevent us from then overwriting the new document....
			if(status == "error")
				return;

			txt = "De server is niet bereikbaar 1, status="+status+", "+request.statusText;
		}
		document.write(txt);
		document.close();
		window.setTimeout('document.body.style.cursor="default"', 1000);
		return true;
	},

	_asyalerted: false,
	_asyDialog: null,

	handleErrorAsy : function(request, status, exc) {
		if(WebUI._asyalerted) {
			//-- We're still in error.. Silently redo the poll.
			WebUI.startPolling(WebUI._pollInterval);
			return;
		}
//		$.dbg("Got into error state - start "+request.responseText);
		if(status === "abort")
			return;

		WebUI._asyalerted = true;
		var txt = request.responseText || "No response - status="+status;
		if(txt.length > 512)
			txt = txt.substring(0, 512)+"...";
		if(txt.length == 0)
			txt = WebUI._T.sysPollFailMsg+status;

		/*
		 * As usual there is a problem with error reporting: if the request is aborted because the browser reloads the page
		 * any pending request is cancelled and comes in here- but with the wrong error code of course. So to prevent us from
		 * showing an error message: set a timer to show that message 250 milli's later, and hope the stupid browser disables
		 * that timer.
		 */
		setTimeout(function() {
			if(WebUI._ignoreErrors)
				return;

			//-- Show an alert error on top of the screen
			document.body.style.cursor = 'default';
			var hdr = document.createElement('div');
			document.body.appendChild(hdr);
			hdr.className = 'ui-io-blk2';
			WebUI._asyHider = hdr;

			var ald = document.createElement('div');
			document.body.appendChild(ald);
			ald.className = 'ui-ioe-asy';
			WebUI._asyDialog = ald;

			var d = document.createElement('div');			// Title bar
			ald.appendChild(d);
			d.className = "ui-ioe-ttl";
			d.appendChild(document.createTextNode(WebUI._T.sysPollFailTitle));	// Server unreachable

			d = document.createElement('div');				// Message content
			ald.appendChild(d);
			d.className = "ui-ioe-msg";
			d.appendChild(document.createTextNode(txt));	// Server unreachable

			d = document.createElement('div');				// Message content
			ald.appendChild(d);
			d.className = "ui-ioe-msg2";

			var img = document.createElement('div');
			d.appendChild(img);
			img.className = "ui-ioe-img";
			d.appendChild(document.createTextNode(WebUI._T.sysPollFailCont));	// Waiting for the server to return.
			WebUI.startPolling(WebUI._pollInterval);
		}, 250);
	},

	clearErrorAsy: function() {
//		$.dbg("clear asy called");
		if(WebUI._asyDialog) {
			$(WebUI._asyDialog).remove();
		}
		if(WebUI._asyHider) {
			$(WebUI._asyHider).remove();
		}
		WebUI._asyDialog = null;
		WebUI._asyHider = null;
		WebUI._asyalerted = false;
	},

	/*
	 * IE/FF compatibility: IE only has the 'keycode' field, and it always hides
	 * all non-input like arrows, fn keys etc. FF has keycode which is used ONLY
	 * for non-input keys and charcode for input.
	 */
	normalizeKey : function(evt) {
        if($.browser.mozilla) {
            if(evt.charCode > 0)
                return evt.charCode;
            return evt.keyCode * 1000;
        }

		if (evt.charCode != undefined) {
            if(evt.charCode == evt.keyCode)
                return evt.charCode;
			if (evt.keyCode > 0)
				return evt.keyCode * 1000; // Firefox high # for cursor crap
			return evt.charCode;
		}
		return evt.keyCode; // Return IE charcode
	},

	isNumberKey : function(evt) {
        //-- onKeyPress event: use keyCode
        var keyCode = WebUI.normalizeKey(evt);
        //$.dbg("kp: norm="+keyCode+", keyCode="+evt.keyCode+", chc="+evt.charCode+", which="+evt.which, evt);
		return (keyCode >= 1000 || (keyCode >= 48 && keyCode <= 57) || keyCode == 45);
	},

	isFloatKey : function(evt) {
		var keyCode = WebUI.normalizeKey(evt);
		//alert('keycode='+evt.keyCode+", charCode="+evt.charCode+", which="+evt.which+", norm="+keyCode);
		return (keyCode >= 1000 || keyCode == 0x2c || keyCode == 0x2e || (keyCode >= 48 && keyCode <= 57) || keyCode == 45);
	},

	returnKeyPress : function(evt, node) {
		var keyCode = WebUI.normalizeKey(evt);
		if (keyCode != 13000 && keyCode != 13)
			return true;
		// Be sure 'onblur' is executed before next action (like submit)
		try {
			evt.target.onblur(evt);
		} catch (err) {
			// Ignore any error in case that target does not support onblur...
		}
		WebUI.scall(evt.currentTarget ? evt.currentTarget.id : node.id, 'returnpressed');
		return false;
	},

	delayedSetAttributes: function() {
		if(arguments.length < 3 || ((arguments.length & 1) != 1)) {
			alert('internal: odd call to delayedSetAttributes: '+arguments.length);
			return;
		}
		var n = document.getElementById(arguments[0]);
		if(n == undefined)
			return;
//		alert('Node is '+arguments[0]);
		//-- Now set pair values
		for(var i = 1; i < arguments.length; i += 2) {
			try {
				n[arguments[i]] = arguments[i+1];
			} catch(x) {
				alert('Failed to set javascript property '+arguments[i]+' to '+arguments[i+1]+": "+x);
			}
		}
	},

	focus : function(id) {
		var n = document.getElementById(id);
		if(n) {
			if($.browser.msie) {
				setTimeout(function() { try {
					$('body').focus();
					n.focus();
				} catch (e) { /*just ignore */ } }, 100); //Due to IE bug, we need to set focus on timeout :( See http://www.mkyong.com/javascript/focus-is-not-working-in-ie-solution/
			} else {
				try {
					n.focus();
				} catch(e) {
					//-- ignore
				}
			}
		}
	},

	/***** DateInput control code ****/
	dateInputCheckInput: function(evt) {
		if(! evt) {
			evt = window.event;
			if(! evt) {
				return;
			}
		}
		var c = evt.target || evt.srcElement;
		WebUI.dateInputRepairValueIn(c);
	},

	dateInputRepairValueIn : function(c) {
		if(!c)
			return;
		var val = c.value;

		if(!val || val.length == 0) // Nothing to see here, please move on.
			return;
		Calendar.__init();

		// -- 20130425 jal if this is a date+time thing the value will hold space-separated time, so make sure to split it;
		var pos = val.indexOf(' ');
		var timeval = null;
		if(pos != -1) {
			// -- Split into trimmed time part and val = only date
			timeval = $.trim(val.substring(pos + 1));
			val = $.trim(val.substring(0, pos));
		}

		// -- Try to decode then reformat the date input
		var fmt = Calendar._TT["DEF_DATE_FORMAT"];
		try {
			var separatorsCount = WebUI.countSeparators(val);
			if(separatorsCount < 2) {
				val = WebUI.insertDateSeparators(val, fmt, separatorsCount);
				var res = Date.parseDate(val, fmt);
				c.value = res.print(fmt);
			} else {
				try{
					var resultOfConversion = WebUI.parsingOfFormat(val, fmt);
					var res = Date.parseDate(resultOfConversion, fmt);
					c.value = res.print(fmt);
				}catch(x){
					Date.parseDate(val, fmt);
				}
			}
		} catch(x) {
			alert(Calendar._TT["INVALID"]);
		}
	},

	/**
	 * Function that checks is format valid after check that input has separators.
	 */

	parsingOfFormat: function(inputValue, format){
		// splits to array of alphanumeric "words" from an input (separators are non-alphanumeric characters)
		var inputValueSplitted = inputValue.match(/(\w+)/g);
		var formatWithoutPercentCharSplitted = format.replace(/%/g, "").match(/(\w+)/g);
		var result = "";
		for(var i = 0; i < formatWithoutPercentCharSplitted.length; i++){
			switch(formatWithoutPercentCharSplitted[i]){
			case "d":
				result = WebUI.formingResultForDayOrMonth(inputValueSplitted[i], result);
				break;
			case "m":
				result = WebUI.formingResultForDayOrMonth(inputValueSplitted[i], result);
				break;
			case "Y":
				result = WebUI.formingResultForYear(inputValueSplitted[i], result);
				break;
			}
		}
		result = WebUI.insertDateSeparators(result, format);
		return result;
	},

	formingResultForDayOrMonth: function(inputValue, result){
		if(!WebUI.hasFieldInvalidFormat(inputValue)){
			return result = WebUI.setDayOrMonthFormat(inputValue, result);
		}
		else{
			throw "Invalid date";
		}
	},

	formingResultForYear: function(inputValue, result){
		var VALID_LENGTH_YEAR = 2;
		if(inputValue.length == VALID_LENGTH_YEAR){
			return result = WebUI.setYearFormat(inputValue, result);
		}
		else{
			throw "Invalid date";
		}
	},

	/**
	 * Function that checks is format valid of fields day and month.
	 */
	hasFieldInvalidFormat: function(inputValue){
		var MAX_LENGTH = 2;
		var FORBIDDEN_CHARACTER = "0";

		return (inputValue.length === MAX_LENGTH && (inputValue.charAt(0) === FORBIDDEN_CHARACTER)) || (inputValue.length > MAX_LENGTH);
	},

	/**
	 * Function that converts day and month parts of input string that represents date.
	 */
	setDayOrMonthFormat: function(inputValue, result){
		var NEEDED_CHARACTER_DAY_MONTH = "0";

		if(inputValue.length == 1){
			result += NEEDED_CHARACTER_DAY_MONTH + inputValue;
		}else {
			result += inputValue;
		}
		return result;
	},

	/**
	 * Function that converts year part of input string that represents date.
	 */
	setYearFormat: function(inputValue, result){
		var NEEDED_CHARACTER_YEAR = "20";

		return result += NEEDED_CHARACTER_YEAR + inputValue;
	},

	/**
	 *
	 */
	showCalendar : function(id, withtime) {
		var inp = document.getElementById(id);
		var params = {
			inputField :inp,
			eventName :'click',
			ifFormat :Calendar._TT[withtime ? "DEF_DATETIME_FORMAT"
					: "DEF_DATE_FORMAT"],
			daFormat :Calendar._TT["TT_DATE_FORMAT"],
			singleClick :true,
			align :'Br',
			range : [ 1900, 2999 ],
			weekNumbers :true,
			showsTime :withtime,
			timeFormat :"24",
			electric :false, // jal 20110125 Fixes bug 885- do not update the field when moving to prevent firing the change handler.
			step :2,
			position :null,
			cache :false
		};

		// -- Try to show the selected date from the input field.
		var dateFmt = params.inputField ? params.ifFormat : params.daFormat;
		params.date = Date.parseDate(inp.value, dateFmt);

		var cal = new Calendar(1, params.date, WebUI.onDateSelect, function(
				cal) {
			cal.hide();
			cal.destroy();
		});
		cal.showsOtherMonths = false;
		cal.showsTime = withtime;
		cal.time24 = true;
		cal.params = params;
		// cal.params = params;
		cal.weekNumbers = true;
		cal.setRange(params.range[0], params.range[1]);
		// cal.setDateStatusHandler(params.dateStatusFunc);
		// cal.getDateText = params.dateText;
		// if (params.ifFormat) {
		// cal.setDateFormat(params.ifFormat);
		// }
		// if (params.inputField && typeof params.inputField.value == "string")
		// {
		// cal.parseDate(params.inputField.value);
		// }
		cal.create();
		cal.refresh();
		if (!params.position)
			cal.showAtElement(params.inputField, params.align);
		else
			cal.showAt(params.position[0], params.position[1]);
	},

	onDateSelect : function(cal) {
		var p = cal.params;
		var update = (cal.dateClicked || p.electric);
		if (update && p.inputField) {
			p.inputField.value = cal.date.print(p.ifFormat);
			if (typeof p.inputField.onchange == "function" && cal.dateClicked)
				p.inputField.onchange();
		}
		if (update && p.displayArea)
			p.displayArea.innerHTML = cal.date.print(p.daFormat);
		if (update && typeof p.onUpdate == "function")
			p.onUpdate(cal);
		if (update && p.flat) {
			if (typeof p.flatCallback == "function")
				p.flatCallback(cal);
		}
		if (update && p.singleClick && cal.dateClicked)
			cal.callCloseHandler();
	},

	wtMouseDown : function(e) {
		alert(e);
	},

	toClip : function(value) {
		if (window.clipboardData) {
			// the IE-way
			window.clipboardData.setData("Text", value);
		} else if (window.netscape) {
			if (value.createTextRange) {
				var range = value.createTextRange();
				if (range && BodyLoaded == 1)
					range.execCommand('Copy');
			} else {
				var flashcopier = 'flashcopier';
				if (!document.getElementById(flashcopier)) {
					var divholder = document.createElement('div');
					divholder.id = flashcopier;
					document.body.appendChild(divholder);
				}
				document.getElementById(flashcopier).innerHTML = '';
				var divinfo = '<embed src="$js/_clipboard.swf" FlashVars="clipboard=' + encodeURIComponent(value) + '" width="0" height="0" type="application/x-shockwave-flash"></embed>';
				document.getElementById(flashcopier).innerHTML = divinfo;
			}
		}
	},
	oddCharAndClickCallback : function(nodeId, clickId) {
		WebUI.oddChar(document.getElementById(nodeId));
		document.getElementById(clickId).click();
	},
	oddChar : function(obj) {
		WebUI.toClip(obj.innerHTML);
	},
	handleCalendarChanges : function() {
		// -- find all Calendars
		var cals = $("div.ui-wa").get();
		for ( var i = cals.length; --i >= 0;)
			WebUI.handleCalendar(cals[i]);
	},

	/** ******************* Scheduler component ************************ */
	/**
	 * This handles item placement for a single calendar. It "discovers" the
	 * calendar divs using fixed css class names, and uses "extra" attributes
	 * generated on the root divs for the parts to get the calendar data. This
	 * handles overlapping appointments by dividing a full day lane into
	 * multiple "ways". A day lane starts with one "way". For every appointment
	 * added to a day we check if this appointment overlaps another appointment
	 * on this way[0]. If it does we create a next way (or use it if it already
	 * exists) and check if the appointment clashes there. We do this until we
	 * find a way that can accept the appointment without overlaps. We do this
	 * for all appointments. At the end we have a list of ways for every day. We
	 * now start rendering the appointments by going through them per way, then
	 * per appointment. For every appointment on a way we check if there is an
	 * appointment on a LATER way that overlaps. If not the width of the
	 * appointment includes all LATER ways that do not overlap.
	 *
	 * For this code to work no item may "overlap" a day. The server code takes
	 * care of splitting long appointments into multiple "items" here.
	 */
	handleCalendar : function(caldiv) {
		// -- TEST calendar object clicky.
		var cal = new WebUI.Agenda(caldiv);
		// $(caldiv).mousedown(function(e) {
		// cal.timeDown(e);
		// });
		cal.loadLayout();
		cal.reposition();
	},

	getAbsolutePosition : function(obj) {
		var top = 0, left = 0;
		while (obj) {
			top += obj.offsetTop;
			left += obj.offsetLeft;
			obj = obj.offsetParent;
		}
		return {
			x :left,
			y :top
		};
	},

	/**
	 * None of the "standard" JS libraries like Rico or Prototype have code that
	 * actually <i>works</i> to get the actual <i>page or absolute</i>
	 * position of elements when scrolling is used. All of them unconditionally
	 * add scroll offsets to the relative positions but scrolling *will* cause
	 * items to become *invisible* because they are scrolled out of view. The
	 * calls here obtain a location for elements taking scrolling into account,
	 * and they will return null if the item is not visible at all.
	 */
	getAbsScrolledPosition : function(el) {
		// -- Calculate the element's current offseted locations
		var bx = el.offsetLeft || 0;
		var by = el.offsetTop || 0;
		var ex = bx + el.offsetWidth;
		var ey = by + el.offsetHeight;

		var el = el.parentNode;
		while (el != null) {
			if (el.clientHeight != null) {
				// -- Check the current location within the parent's bounds.
				if (by < el.scrollTop)
					by = el.scrollTop;
				if (bx < el.scrollLeft)
					bx = el.scrollLeft;
				if (bx >= ex || by >= ey) // Not visible
					return null;

				// -- Check the end coordinates.
				var vey = el.scrollTop + el.clientHeight;
				var vex = el.scrollLeft + el.clientWidth;
				if (ex > vex)
					ex = vex;
				if (ey > vey)
					ey = vey;
				if (by >= ey || bx >= ex) // Past the viewport's bounds?
					return null;

				// -- This much of the rectangle fits the viewport. Now make the
				// position absolute within the viewport.
				by -= el.scrollTop;
				ey -= el.scrollTop;
				bx -= el.scrollLeft;
				ex -= el.scrollLeft;

				by += el.offsetTop;
				ey += el.offsetTop;
				bx += el.offsetLeft;
				ex += el.offsetLeft;
			}
			el = el.parentNode;
		}
		return {
			bx :bx,
			by :by,
			ex :ex,
			ey :ey
		};
	},

	/** *************** Polling code ************* */
	startPolling : function(interval) {
		if(interval < 100 || interval == undefined || interval == null) {
			alert("Bad interval: "+interval);
			return;
		}
		WebUI._pollInterval = interval;
		if (WebUI._pollActive)
			return;
		WebUI._pollActive = true;
		WebUI._pollTimer = setTimeout("WebUI.poll()", WebUI._pollInterval);
	},
	cancelPolling : function() {
		if (!WebUI._pollActive)
			return;
		clearTimeout(WebUI._pollTimer);
		WebUI._pollActive = false;
	},

	poll : function() {
		WebUI.cancelPolling();

		/*
		 * Issue a pollasy request using ajax, then handle the result.
		 */
		var fields = {};
		fields.webuia = "pollasy";
		fields["$pt"] = DomUIpageTag;
		fields["$cid"] = DomUICID;

		$.ajax( {
			url :window.location.href,
			dataType :"*", // "text/xml",
			data :fields,
			cache :false,
			global: false, // jal 20091015 prevent block/unblock on polling call.
			success :WebUI.handleResponse,
			error :WebUI.handleErrorAsy
		});
	},

	/**
	 * Send Ajax request to the server every 2 minutes. This keeps the session
	 * alive. The response can contain commands to execute which will indicate
	 * important events or changes have taken place.
	 */
	pingServer: function(timeout) {
		var url = DomUIappURL + "to.etc.domui.parts.PollInfo.part";
		var fields= {};
		fields["$pt"] = DomUIpageTag;
		fields["$cid"] = DomUICID;
		$.ajax( {
			url: url,
			dataType: "*",
			data: fields,
			cache: false,
			global: false, // jal 20091015 prevent block/unblock on polling call.
			success: function(data, state) {
				WebUI.executePollCommands(data);
			},
			error : function() {
				//-- Ignore all errors.
			}
		});
		WebUI.startPingServer(timeout);
	},

	startPingServer: function(timeout) {
		if(timeout < 60*1000)
			timeout = 60*1000;
		setTimeout("WebUI.pingServer("+timeout+")", timeout);
	},

	executePollCommands: function(data) {
		// TBD
	},

	/** Dynamically loading stylesheets and javascript files (Header Contributer delta's) **/
	/**
	 * Load the specified stylesheet by creating a script tag and inserting it @ head.
	 */
	loadStylesheet: function(path) {
		var head = document.getElementsByTagName("head")[0];
		if(! head)
			throw "Headless document!?";
		var link = document.createElement('link');
		link.type = 'text/css';
		link.rel = 'stylesheet';
		link.href = path;
		link.media = 'screen';
		head.appendChild(link);
	},

	loadJavascript: function(path) {
		var head = document.getElementsByTagName("head")[0];
		if(! head)
			throw "Headless document!?";
		var scp = document.createElement('script');
		scp.type = 'text/javascript';
		scp.src = path;
		head.appendChild(scp);
	},

	/** Prevents default action to be executed if IE11 is detected */
	preventIE11DefaultAction : function(e){
		if((navigator.userAgent.match(/Trident\/7\./))){
			e.preventDefault();
		}
	},

	/** ***************** File upload stuff. **************** */
	fileUploadChange : function(e) {
		var tgt = e.currentTarget || e.srcElement;
		var vv = tgt.value.toString();

		if (!vv) { //IE when setting the value to empty..
			return;
		}

		// -- Check extensions,
		var allowed = tgt.getAttribute('fuallowed');
		if (allowed) {
			var ok = false;
			var spl = allowed.split(',');

			vv = vv.toLowerCase();
			for(var i = 0; i < spl.length; i++) {
				var ext = spl[i].toLowerCase();
				if(ext.substring(0, 1) != ".")
					ext = "."+ext;
				if(ext == ".*") {
					ok = true;
					break;
				} else if(vv.indexOf(ext, vv.length - ext.length) !== -1) {
					ok = true;
					break;
				}
			}

			if (!ok) {
				var parts = vv.split('.');
				alert(WebUI.format(WebUI._T.uploadType, (parts.length > 1) ? parts.pop() : '', allowed));
				tgt.value = "";
				return;
			}
		}

		if(typeof tgt.files[0] !== "undefined") {
			var s = tgt.getAttribute('fumaxsize');
			var maxSize = 0;
			try {
				maxSize = Number(s);
			} catch(x) {
			}
			if(maxSize <= 0 || maxSize === NaN)
				maxSize = 100 * 1024 * 1024;						// Default max size is 100MB

			var size = tgt.files[0].size;
			if(size > maxSize) {
				alert(WebUI.format(WebUI._T.buplTooBig, maxSize));
				tgt.value = "";
				return;
			}
		}

		// -- Step 2: create or locate an iframe to handle the upload;
		var iframe = document.getElementById('webuiif');
		if (iframe) {
			iframe.parentNode.removeChild(iframe);
			iframe = undefined;
		}
		if (!iframe) {
			if(jQuery.browser.msie && ! WebUI.isNormalIE9plus()) {			// MicroMorons report ie9 for ie7 emulation of course
				// -- IE's below 8 of course have trouble. What else.
				iframe = document
						.createElement('<iframe name="webuiif" id="webuiif" src="#" style="display:none; width:0; height:0; border:none" onload="WebUI.ieUpdateUpload(event)">');
				document.body.appendChild(iframe);
			} else {
				iframe = document.createElement('iframe');
				iframe.id = 'webuiif';
				iframe.name = "webuiif";
				iframe.src = "#";
				iframe.style.display = "none";
				iframe.style.width = "0px";
				iframe.style.height = "0px";
				iframe.style.border = "none";
				iframe.onload = function() {
					WebUI.updateUpload(iframe.contentDocument);
				};
				document.body.appendChild(iframe);
			}
		}

		// -- replace the input with a busy indicator
		var form = tgt.parentNode; // Get form to submit to later on,

		var img = document.createElement('img');
		img.border = "0";
		img.src = DomUIProgressURL;
		form.parentNode.insertBefore(img, form);
		form.style.display = 'none';

		// -- Target the iframe
		form.target = "webuiif"; // Fake a new thingy,
		form.submit(); // Force submit of the thingerydoo
		WebUI.blockUI();									// since 20160226: block UI during upload
	},

	/**
	 * Format a NLS message containing {0} and {1} markers and the like into
	 * a real message.
	 * @param message
	 * @returns
	 */
	format: function(message) {
		for(var i = 1; i < arguments.length; i++) {
			message = message.replace("{"+(i-1)+"}", arguments[i]);
		}
		return message;
	},

	/**
	 * Called for ie upload garbage only, this tries to decode the utter devastating mess that
	 * ie makes from xml uploads into an iframe in ie8+. Sigh. The main problem with IE is that
	 * the idiots that built it mess up XML documents sent to it: when the iframe we use receives
	 * an xml document the idiot translates it into an html document- with formatted tags and minus
	 * signs before it to collapse them. Instead of just /rendering/ that they actually /generate/
	 * that as the content document of the iframe- so getting that means you get no XML at all.
	 * This abomination was more or less fixed in ie9 - but of course they fucked up compatibility mode
	 * badly.
	 *
	 * Everything below ie9
	 * ====================
	 * Everything below ie9 has a special property "XMLDocument" attached to the "document" property of
	 * the window. This property contains the original XML that was used as an XML tree. So for these
	 * browsers we just get that and move on.
	 *
	 * IE9 in native mode
	 * ==================
	 * IE9 in native mode does not mess up the iframe content document, so this mode takes the path of
	 * all browsers worth the name. Since the original problem was solved the XMLDocument property no
	 * longer exists.
	 *
	 * IE9 in compatibility mode (IE7)
	 * ===============================
	 * This gets funny. In this mode the browser /still/ messes up the iframe's content model like the
	 * old IE's it emulates. But of course the XMLDocument property is helpfully removed - EVEN IN THIS
	 * MODE.
	 * Remember: this mode is also entered if you are part of a frameset or iframe that is part of an
	 * mode: the topmost frameset/page determines the mode for the entire set of pages - they are that
	 * stupid.
	 *
	 * I know no other "workaround" than the horrible concoction below; please turn away if you have
	 * a weak stomach....
	 *
	 * In this case we get the "innerText" content of the iframe. This differers from innerHTML in that
	 * all html tags that IE added are removed, and only the text is retained. Since the html was generated
	 * by IE in such a way that the xml was presented to the user - this is actually most of our XML...
	 * But it contains some problems:
	 * - there is extra whitespace around it's edges, so we need to remove that..
	 * - All tags start on a new line with a '-' sign before them... Remove all that...
	 * - The result will have the &amp; entity replaced by & because they are really stupid, again. So replace
	 *   that too.
	 * The resulting thing can sometimes be parsed as XML and then processing continues. But it is far
	 * from perfect. The biggest problem is that the resulting xml has not properly reserved the original
	 * whitespace; this may lead to rendering problems.
	 *
	 * But as far as I know no other solution to this stupifying bug is possible. Please prove me wrong.
	 *
	 * @param e
	 */
	ieUpdateUpload : function(e) { // Piece of crap
		var iframe = document.getElementById('webuiif');
		var xml;
		if(iframe.contentWindow && iframe.contentWindow.document.XMLDocument) {
			xml = iframe.contentWindow.document.XMLDocument; // IMPORTANT Fucking MS Crap!!!! See http://p2p.wrox.com/topic.asp?whichpage=1&TOPIC_ID=62981&#153594
		} else if(iframe.contentDocument) {
			var crap = iframe.contentDocument.body.innerText;
			crap = crap.replace(/^\s+|\s+$/g, ''); // trim
			crap = crap.replace(/(\n|\r)-*/g, ''); // remove '\r\n-'. The dash is optional.
			crap = crap.replace(/&/g, '&amp;');		// Replace & with entity
//			alert('crap='+crap);
			if(window.DOMParser) {
				var parser = new DOMParser();
				xml = parser.parseFromString(crap);
			} else if(window.ActiveXObject) {
				xml = new ActiveXObject("Microsoft.XMLDOM");
				if(! xml.loadXML(crap)) {
					alert('ie9 in emulation mode unfixable bug: cannot parse xml');
					window.location.href = window.location.href;
					WebUI.unblockUI();
					return;
				}
			} else {
				alert('No idea how to parse xml today.');
			}
		} else
			alert('IE error: something again changed in xml source structure of the iframe, sigh');

		WebUI.updateUpload(xml, iframe);
	},
	updateUpload : function(doc, ifr) {
		try {
			jQuery.fn.executeDeltaXML(doc);
		} catch (x) {
			alert(x);
			throw x;
		} finally {
			WebUI.unblockUI();
		}
		/*
		 * 20081015 jal De iframe zou verwijderd moeten worden, maar als ik dat
		 * doe onder Firefox dan blijft de browser in "busy" mode staan, met een
		 * rode enabled "stop" knop en een "busy" mouse.. Voor nu hergebruiken
		 * we de bestaande iframe dan maar en accepteren de verloren resources.
		 */
		// iframe.parentNode.removeChild(iframe); // Suicide..
	},

	openWindow : function(url, name, par) {
		try {
			var h = window.open(url, name, par);
		} catch(x) {
			alert("Got popup exception: "+x);
		}
		if (!h)
			alert(WebUI._T.sysPopupBlocker);
		return false;
	},

	postURL : function post(path, name,  params, target) {
		var form = document.createElement("form");
		form.setAttribute("method","post");
		form.setAttribute("action", path);
		if (null != target){
			form.setAttribute("target", target);
		}

		for (var key in params) {
			if (params.hasOwnProperty(key)) {
				var hiddenField = document.createElement("input");
				hiddenField.setAttribute("type", "hidden");
				hiddenField.setAttribute("name", key);
				hiddenField.setAttribute("value", params[key]);
				form.appendChild(hiddenField);
			}
		}
		document.body.appendChild(form);
		form.submit();
	},

	unloaded : function() {
		WebUI._ignoreErrors = true;
		WebUI.sendobituary();
	},

	beforeUnload: function() {
		//-- Make sure no "ajax" errors are reported.
		WebUI._ignoreErrors = true;
	},

	/**
	 * Called at page unload time, this quickly tries to send an obituary to the
	 * server. This is currently unconditional but can later be augmented to
	 * send the obituary only when the browser window closes.
	 */
	sendobituary : function() {
		try {
		var rq;
		if (window.XMLHttpRequest) {
			rq = new XMLHttpRequest();
		} else if (window.ActiveXObject) {
			rq = new ActiveXObject("Microsoft.XMLHTTP");
		} else {
			alert("Cannot send obituary (no transport)");
			return;
		}
		rq.open("GET", DomUI.getObituaryURL() + "?$cid=" + DomUICID + "&webuia=OBITUARY&$pt=" + DomUIpageTag, false);
		rq.send(null);
		} catch(ex) {
//			alert("Sending obit failed:"+ex);
		}
	},

	/**
	 * Do not use- kept for when we find a solution to IE's close problem.
	 */
	isBrowserClosed : function(e) {
		try {
			// -- ie does not work as usual.
			if (window.event) {
				alert('wcy=' + window.event.clientY + ", wcx="
						+ window.event.clientX + ", dw="
						+ document.documentElement.clientWidth + ", screentop="
						+ self.screenTop);
				if (window.event.clientY < 0
						&& (window.event.clientX > (document.documentElement.clientWidth - 5) || window.event.clientX < 15))
					return true;
			}
		} catch (x) {
		}

		try { // Firefox part works properly.
			if (window.innerWidth == 0 && window.innerHeight == 0)
				return true;
		} catch (x) {
		}

		return false;
	},

	_selectStart : undefined,


	/** ************ Drag-and-drop support code ****************** */
	/**
	 * When mouse is downed on an item that is draggable. This moves to PREDRAG
	 * mode where the first move will create a visible representation of this
	 * node, ready for dropping.
	 */
	dragMouseDown : function(item, evt) {
		WebUI.dragReset();
		WebUI._dragType = item.getAttribute('uitype');
		if (!WebUI._dragType)
			alert("This DRAGGABLE node has no 'uitype' attribute??");
		var dragAreaId = item.getAttribute('dragarea');
		if (dragAreaId){
			WebUI._dragNode  = document.getElementById(dragAreaId);
		}else
			WebUI._dragNode = item;
		WebUI._dragMode = 1; // PREDRAG
		$(document.body).bind("mousemove", WebUI.dragMouseMove);
		$(document.body).bind("mouseup", WebUI.dragMouseUp);
		var apos = WebUI.getAbsolutePosition(item);
		WebUI._dragSourceOffset = apos;
		apos.x = evt.clientX - apos.x;
		apos.y = evt.clientY - apos.y;
		if(evt.preventDefault)
			evt.preventDefault(); // Prevent ffox image dragging
		else{
			evt.returnValue = false;
		}
		if(document.attachEvent){
			document.attachEvent( "onselectstart", WebUI.preventSelection);
		}
	},

	preventSelection : function(){
		return false;
	},

	dragMouseUp : function() {
		// -- If we're in DRAGGING mode we may accept the drop
		try {
			if (WebUI._dragMode == 2) {
				WebUI.dragClearTimer();
				var dz = WebUI.dropTargetFind(WebUI._dragLastX,
						WebUI._dragLastY);
				if (dz) {
					WebUI.dropClearZone(); // Discard any dropzone visuals
					dz._drophandler.drop(dz);
				}else{
					WebUI._dragNode.style.display='';//no drop zone, so restore the dragged item
				}
			}
		} finally {
			WebUI.dragReset();
		}
	},

	dragMouseMove : function(e) {
		if (WebUI._dragMode == 0) {
			WebUI.dragReset();
			return;
		}

		if (WebUI._dragMode == 1) {
			// -- preDRAG mode: create the node copy, then move it to the
			// offset' location.
			WebUI._dragCopy = WebUI.dragCreateCopy(WebUI._dragNode);
			//MVE make this optional.
			WebUI._dragNode.style.display='none';

			WebUI._dragMode = 2;
			document.body.appendChild(WebUI._dragCopy);
		}
		WebUI._dragCopy.style.top = (e.clientY - WebUI._dragSourceOffset.y)
				+ "px";
		WebUI._dragCopy.style.left = (e.clientX - WebUI._dragSourceOffset.x)
				+ "px";
		// console.debug("currentMode: "+WebUI._dragMode+",
		// type="+WebUI._dragType);
		WebUI._dragLastX = e.clientX;
		WebUI._dragLastY = e.clientY;
		WebUI.dragResetTimer();
	},

	dragCreateCopy : function(source) {
		var dv = document.createElement('div');

		// If we drag a TR we need to encapsulate the thingy in a table/tbody to prevent trouble.
		if(source.tagName != "TR") {
			dv.innerHTML = source.innerHTML;
		} else {
			//-- This IS a tr. Create a table/TBody then add the content model
			var t = document.createElement('table');
			dv.appendChild(t);
			var b = document.createElement('tbody');
			t.appendChild(b);
			b.innerHTML = source.innerHTML;			// Copy tr inside tbody we just constructed

			//-- Find parent table's CSS class so we can copy it's style.
			var dad = WebUI.findParentOfTagName(source, 'TABLE');
			if(dad) {
				t.className= dad.className;
			}
		}

		dv.style.position = 'absolute';
		dv.style.width = $(source).width() + "px";
		dv.style.height = $(source).height() + "px";
		//console.debug("DragNode isa "+source.tagName+", "+dv.innerHTML);
		return dv;
	},

	findParentOfTagName: function(node, type) {
		while(node != null) {
			node = node.parentNode;
			if(node.tagName == type)
				return node;
		}
		return null;
	},

	/**
	 * Resets the dropzone timer. Called when in DRAGGING mode and the mouse
	 * moves, this resets any "open" dropzone indicators and resets the timer on
	 * which drop zone effects are done. This causes the dropzone indicator
	 * delay when moving the mouse.
	 */
	dragResetTimer : function() {
		WebUI.dragClearTimer();
		WebUI._dragTimer = setTimeout("WebUI.dragTimerFired()", 250);
	},
	dragClearTimer : function() {
		if (WebUI._dragTimer) {
			clearTimeout(WebUI._dragTimer);
			delete WebUI._dragTimer;
		}
	},

	/**
	 * Fires when in DRAGGING mode and the mouse has not moved for a while. It
	 * initiates the rendering of any drop zone indicators if the mouse is above
	 * a drop zone.
	 */
	dragTimerFired : function() {
		// console.debug("timer fired");
		var dz = WebUI.dropTargetFind(WebUI._dragLastX, WebUI._dragLastY);
		if (!dz) {
			WebUI.dropClearZone();
			return;
		}

		// -- Un-notify the previous dropzone and notify the new'un
		if (dz == WebUI._currentDropZone) {
			dz._drophandler.checkRerender(dz);
			return;
		}
		WebUI.dropClearZone();
		WebUI._currentDropZone = dz;
		dz._drophandler.hover(dz);
		// console.debug("AlterClass on "+dz._dropTarget);
	},

	findDropZoneHandler : function(type) {
		if (type == "ROW")
			return WebUI._ROW_DROPZONE_HANDLER;
		return WebUI._DEFAULT_DROPZONE_HANDLER;
	},

	dropClearZone : function() {
		if (WebUI._currentDropZone) {
			WebUI._currentDropZone._drophandler.unmark(WebUI._currentDropZone);
			delete WebUI._currentDropZone;
		}
	},

	/**
	 * Clears any node being dragged.
	 */
	dragReset : function() {
		WebUI.dragClearTimer();
		if (WebUI._dragCopy) {
			$(WebUI._dragCopy).remove();
			WebUI._dragCopy = null;
		}
		if (WebUI._dragNode) {
			$(document.body).unbind("mousemove", WebUI.dragMouseMove);
			$(document.body).unbind("mouseup", WebUI.dragMouseUp);
			WebUI._dragNode = null;
		}
		WebUI.dropClearZone();
		WebUI._dragMode = 0; // NOTDRAGGED

		if(document.detachEvent){
			document.detachEvent( "onselectstart", WebUI.preventSelection);
		}

//		if(WebUI._selectStart){
//			document.onselectstart = WebUI._selectStart;
//		}
	},

	/**
	 * Gets or recalculates the list of possible drop targets and their absolute
	 * on-screen position. This list is used to determine if the mouse is "in" a
	 * drop target. The list gets cached globally in the WebUI object; if an
	 * AJAX request is done the list gets cleared.
	 */
	dropGetList : function() {
		if (WebUI._dropList)
			return WebUI._dropList;

		// -- Reconstruct the droplist. Find all objects that possess the
		// ui-drpbl class.
		var dl = $(".ui-drpbl").get();
		WebUI._dropList = [];
		for ( var i = dl.length; --i >= 0;) {
			var drop = dl[i];
			var types = drop.getAttribute('uitypes');
			if (!types)
				continue;
			var def = {};
			def._dropTarget = drop; // Store the objects' DOM node,
			def._position = WebUI.getAbsolutePosition(drop);
			def._width = drop.clientWidth;
			def._height = drop.clientHeight;
			var tar = types.split(",");
			def._types = tar;
			def._drophandler = WebUI.findDropZoneHandler(drop
					.getAttribute('uidropmode'));
			var id = drop.getAttribute('uidropbody');
			if (id) {
				def._tbody = document.getElementById(id);
				if (!def._tbody) {
					alert('Internal error: the TBODY ID=' + id + ' cannot be located (row dropTarget)');
					continue;
				}
				WebUI.dropRemoveNonsense(def._tbody);
			}

			WebUI._dropList.push(def);
		}
		return WebUI._dropList;
	},
	dropClearList : function() {
		delete WebUI._dropList;
	},
	dropTargetFind : function(x, y) {
		var dl = WebUI.dropGetList();
		for ( var i = dl.length; --i >= 0;) {
			var d = dl[i];

			// -- Contained and of the correct type?
			if (x >= d._position.x && x < d._position.x + d._width
					&& y >= d._position.y && y < d._position.y + d._height) {
				for ( var j = d._types.length; --j >= 0;) {
					if (d._types[j] == WebUI._dragType)
						return d;
				}
			}
		}
		return null;
	},

	dropRemoveNonsense : function(body) {
		for ( var i = body.childNodes.length; --i >= 0;) {
			var n = body.childNodes[i];
			if (n.nodeName == '#text')
				body.removeChild(n);
		}
	},

	/**
	 * @deprecated This is incorrect! Use disableSelect below!
	 * Disable selection on the given element.
	 */
	disableSelection : function(node) {
		node.onselectstart = function() {
			return false;
		};
		node.unselectable = "on";
		node.style.MozUserSelect = "none";
		node.style.cursor = "default";
	},

	disableSelect: function(id) {
		if($.browser.msie) {
			$('#' + id).disableSelection();
		} else {
			$('#' + id).addClass("ui-selection-disable");
		}
	},

	enableSelect: function(id) {
		if($.browser.msie) {
			$('#' + id).enableSelection();
		} else {
			$('#' + id).removeClass("ui-selection-disable");
		}
	},

	checkBrowser: function() {
		if(this._browserChecked)
			return;
		this._browserChecked = true;

		//-- We do not support IE 7 and lower anymore.
		if($.browser.msie && $.browser.majorVersion < 8) {
			//-- Did we already report that warning this session?
			if($.cookie("domuiie") == null) {
				alert(WebUI.format(WebUI._T.sysUnsupported, $.browser.majorVersion));
				$.cookie("domuiie", "true", {});
			}
		}
	},

	/** ***************** ScrollableTabPanel stuff. **************** */
	_ignoreScrollClick: 0,

	scrollLeft : function(bLeft) {
		if(this._ignoreScrollClick != 0 || $(bLeft).hasClass('ui-stab-dis'))
			return;

		var scrlNavig = $(bLeft.parentNode);
		var offset = -1 * parseInt($('ul',scrlNavig).css('marginLeft'));
		var diff = $(scrlNavig).width() - 2 * $('.ui-stab-scrl-left',scrlNavig).width();
		var me = this;
		var disa = false;
		if ( diff >= offset ){
			disa = true;
			diff = offset;
		}
		this._ignoreScrollClick++;
		$('ul',scrlNavig).animate({marginLeft: '+=' + diff}, 400, 'swing', function() {
			$('.ui-stab-scrl-right', scrlNavig).removeClass('ui-stab-dis');
			if(disa){
				$(bLeft).addClass('ui-stab-dis');
			}
			me._ignoreScrollClick--;
		});
	},

	scrollRight : function(bRight) {
		if(this._ignoreScrollClick != 0 || $(bRight).hasClass('ui-stab-dis'))
			return;

		var scrlNavig = $(bRight.parentNode);
		var tabsTotalWidth = $('li:last',scrlNavig).width() + 8 /* paddong = 8 */ + $('li:last',scrlNavig).offset().left - $('li:first',scrlNavig).offset().left;
		var tabsVisibleWidth = $(scrlNavig).width() - 2 * $('.ui-stab-scrl-right',scrlNavig).width();
		var maxLeftOffset = tabsTotalWidth - tabsVisibleWidth;
		var diff = tabsVisibleWidth;
		var offset = -1 * parseInt($('ul',scrlNavig).css('marginLeft'));

		var disa = false;
		if (offset >= maxLeftOffset){
			return;
		} else if (diff + offset >= maxLeftOffset){
			diff = maxLeftOffset - offset;
			disa = true;
		}
		this._ignoreScrollClick++;
		var me = this;
		$('ul', scrlNavig ).animate({marginLeft: '-=' + diff},400, 'swing', function() {
			$('.ui-stab-scrl-left', scrlNavig).removeClass('ui-stab-dis');
			if (disa){
				$(bRight).addClass('ui-stab-dis');
			}
			me._ignoreScrollClick--;
		});
	},

	recalculateScrollers : function(scrlNavigId){
		var scrlNavig = document.getElementById(scrlNavigId);
		var tabsTotalWidth = $('li:last',scrlNavig).width() + 8 /* paddong = 8 */ + $('li:last',scrlNavig).offset().left - $('li:first',scrlNavig).offset().left;
		var tabsVisibleWidth = $(scrlNavig).width() - 2 * $('.ui-stab-scrl-right',scrlNavig).width();
		//WebUI.debug('debug1', 50, 150, 'width:' + ($('li:last',scrlNavig).width() + 8 + $('li:last',scrlNavig).offset().left - $('li:first',scrlNavig).offset().left));
		//WebUI.debug('debug2', 50, 200, 'offsetX:' + ($('li:first',scrlNavig).offset().left - $('ul', scrlNavig).offset().left));

		if(tabsTotalWidth > tabsVisibleWidth){
			var leftM = parseInt($('ul',scrlNavig).css('marginLeft'));
			//WebUI.debug('debug2', 50, 200, 'leftM:' + leftM);
			if (tabsTotalWidth + leftM > tabsVisibleWidth){
				$('.ui-stab-scrl-right',scrlNavig).removeClass('ui-stab-dis');
			}else{
				$('.ui-stab-scrl-right',scrlNavig).addClass('ui-stab-dis');
			}
			if (leftM < 0){
				$('.ui-stab-scrl-left',scrlNavig).removeClass('ui-stab-dis');
			}else{
				$('.ui-stab-scrl-left',scrlNavig).addClass('ui-stab-dis');
			}
		}else{

			// 20121115 btadic: we just want to render scroll buttons without arrows and actions. We don't want empty spaces.
			//$('.ui-stab-scrl-left',scrlNavig).css('visibility','hidden');
			//$('.ui-stab-scrl-right',scrlNavig).css('visibility','hidden');
			$('.ui-stab-scrl-left',scrlNavig).css('display','none');
			$('.ui-stab-scrl-right',scrlNavig).css('display','none');
			$('ul', scrlNavig).animate({marginLeft: 0}, 400, 'swing');
		}
	},

	/** ***************** Stretch elemnt height. Must be done via javascript. **************** */
	stretchHeight : function(elemId) {
		var elem = document.getElementById(elemId);
		if (!elem){
			return;
		}
		WebUI.stretchHeightOnNode(elem);
	},

	stretchHeightOnNode : function(elem) {
		var elemHeight = $(elem).height();
		var totHeight = 0;
		$(elem).siblings().each(function(index, node) {
			//do not count target element and other siblings positioned absolute or relative to parent in order to calculate how much space is actually taken / available
			if (node != elem && $(node).css('position') == 'static' && ($(node).css('float') == 'none' || $(node).css('width') != '100%' /* count in floaters that occupies total width */)){
				//In IE7 hidden nodes needs to be additionaly excluded from count...
				if (!($(node).css('visibility') == 'hidden' || $(node).css('display') == 'none')){
					//totHeight += node.offsetHeight;
					totHeight += $(node).outerHeight(true);
				}
			}
		});
		var elemDeltaHeight = $(elem).outerHeight(true) - $(elem).height(); //we need to also take into account elem paddings, borders... So we take its delta between outter and inner height.
		if (WebUI.isIE8orIE8c()){
			//from some reason we need +1 only for IE8!
			elemDeltaHeight = elemDeltaHeight + 1;
		}
		$(elem).height($(elem).parent().height() - totHeight - elemDeltaHeight);
		if($.browser.msie && $.browser.version.substring(0, 1) == "7"){
			//we need to special handle another IE7 muddy hack -> extra padding-bottom that is added to table to prevent non-necesarry vertical scrollers
			if (elem.scrollWidth > elem.offsetWidth){
				$(elem).height($(elem).height() - 20);
				//show hidden vertical scroller if it is again needed after height is decreased.
				if ($(elem).css('overflow-y') == 'hidden'){
					if (elem.scrollHeight > elem.offsetHeight){
						$(elem).css({'overflow-y' : 'auto'});
					}
				}

			}
		}
	},

	/**
	 * This adds a "resize" listener to the window, and every window "resize" it will call a method
	 * to recalculate the height of a single div (flexid) based on the position of:
	 * <ul>
	 *	<li>The 'bottom' position of the element just above it (get it's top position and add it's height)</li>
	 *	<li>A bottom 'offset', currently an integer in pixels. It will become a "bottom item" id later, so we can auto-size a div "sandwiched" between two other elements.</li>
	 * </ul>
	 * The recalculate code will be called every time the window resizes.
	 *
	 * @param topid
	 * @param flexid
	 * @param bottom
	 */
	autoHeightReset: function(topid, flexid, bottom) {
		$(window).bind("resize", function() {
			WebUI.recalculateAutoHeight(topid, flexid, bottom);
		});
		WebUI.recalculateAutoHeight(topid, flexid, bottom);
	},

	recalculateAutoHeight: function(topid, flexid, bottom) {
		try {
			var tbot = $(topid).offset().top + $(topid).height();
			var height = $(window).height() - tbot - bottom;
			$(flexid).height(height+"px");
		} catch(x) {
			//-- Ignore for now
		}
	},

	setThreePanelHeight: function(top, middle, bottom) {
//		try {
			middle = "#"+middle;
			var height = $(middle).parent().height();		// Assigned height of the container

			var theight = 0;
			if(typeof top === "string") {
				theight = $("#"+top).height();					// Get the end of the "top" element
			} else if(top) {
				theight = top;
			}

			//-- Get height of bottom, if present
			var bheight = 0;
			if(typeof bottom === "string") {
				bheight = $("#"+bottom).height();
			} else if(bottom) {
				tbottom = bottom;
			}
			height -= theight - bheight;
			if(height < 0) {
				height = 0;
			}
			$(middle).height(height+"px");
			$(middle).css({"overflow-y": "auto"});
//		} catch(x) {
//			//-- Ignore for now
//		}

	},

    autoHeight: function(flexid, bottom) {
	    $(window).bind("resize", function() {
		    WebUI.autoHeightRecalc(flexid, bottom);
	    });
	    WebUI.autoHeightRecalc(flexid, bottom);
    },

    autoHeightRecalc: function(flexid, bottom) {
	    var tbot = $(flexid).offset().top;
	    var height = $(window).height() - tbot - bottom;
	    $(flexid).height(height + "px");
    },

    notifySizePositionChangedOnId: function(elemId) {
		var element = document.getElementById(elemId);
		if (!element){
			return;
		}
		// Send back size information to server
		var fields = {};
		fields.webuia = "notifyClientPositionAndSize";
		fields[element.id + "_rect"] = $(element).position().left + "," + $(element).position().top + "," + $(element).width() + "," + $(element).height();
		fields["window_size"] = window.innerWidth + "," + window.innerHeight;
		WebUI.scall(element.id, "notifyClientPositionAndSize", fields);
    },

    notifySizePositionChanged: function(event, ui) {
   	    var element = ui.helper.get(0);
   	    if (!element){
   	    	return;
   	    }
   	    WebUI.notifySizePositionChangedOnId(element.id);
    },

	/** *************** Debug thingy - it can be used internaly for debuging javascript ;) ************** */
	debug : function(debugId, posX, posY, debugInfoHtml) {
		//Be aware that debugId must not start with digit when using FF! Just lost 1 hour to learn this...
		if ("0123456789".indexOf(debugId.charAt(0)) > -1){
			alert("debugId(" + debugId+ ") starts with digit! Please use different one!");
		}
		var debugPanel = document.getElementById(debugId);
		if (null == debugPanel){
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
	},

	_busyCount: 0,

	/*
	 * Block the UI while an AJAX call is in progress.
	 */
	blockUI: function() {
//		console.debug('block, busy=', WebUI._busyCount);
		if(WebUI._busyCount++ > 0)
			return;
		var el = document.body;
		if(! el)
			return;
		el.style.cursor = "wait";

		//-- Create a backdrop div sized 100% overlaying the body, initially just fully transparant (just blocking mouseclicks).
		var d = document.createElement('div');
		el.appendChild(d);
		d.className = 'ui-io-blk';
		WebUI._busyOvl = d;
		WebUI._busyTimer = setTimeout("WebUI.busyIndicate()", 250);
	},

	isUIBlocked: function() {
		return WebUI._busyOvl != undefined && WebUI._busyOvl != null;
	},

	busyIndicate: function() {
		if(WebUI._busyTimer) {
			clearTimeout(WebUI._busyTimer);
			WebUI._busyTimer = null;
		}
		if(WebUI._busyOvl) {
			WebUI._busyOvl.className = "ui-io-blk2";
		}
	},

	unblockUI: function() {
//		console.debug('unblock, busy=', WebUI._busyCount);
		if(WebUI._busyCount <= 0 || ! WebUI._busyOvl)
			return;
		if(--WebUI._busyCount != 0)
			return;
		if(WebUI._busyTimer) {
			clearTimeout(WebUI._busyTimer);
			WebUI._busyTimer = null;
		}
		var el = document.body;
		if(!el)
			return;

		el.style.cursor = "default";
		try {
			el.removeChild(WebUI._busyOvl);
		} catch(x) {
			//-- It can fail when the entire page has been replaced.
		}
		WebUI._busyOvl= null;
	},

	/*-- Printing support --*/
	_frmIdCounter: 0,

	backgroundPrint : function(url) {
		try {
			var frmname = WebUI.priparePrintDiv(url);
			WebUI.framePrint(frmname);
		} catch (x) {
			alert("Failed: " + x);
		}
	},

	framePrint : function(frmname){
		if (jQuery.browser.msie) {
			WebUI.documentPrintIE(frmname);
		} else {
			WebUI.documentPrintNonIE(frmname);
		}
	},

	documentPrintIE : function(frmname) {
		try {
			var frm = window.frames[frmname];
			$("#"+frmname).load(function() {
				try {
					frm.focus();
					setTimeout(function() {
						if (!frm.document.execCommand('print', true, null)){
			            	alert('cannot print: '+x);
				        }
					}, 1000);
				} catch(x) {
					alert('cannot print: '+x);
				}
			});
		} catch(x) {
			alert("Failed: "+x);
		}
	},

	documentPrintNonIE : function(frmname) {
		try {
			var frm = window.frames[frmname];
			$("#"+frmname).load(function() {
				try {
					frm.focus();
					setTimeout(function() {
						frm.print();
					}, 1000);
				} catch(x) {
					alert('cannot print: '+x);
				}
			});
		} catch(x) {
			alert("Failed: "+x);
		}
	},

	priparePrintDiv : function(url){
		// Create embedded sizeless div to contain the iframe, invisibly.
		var div = document.getElementById('domuiprif');
		if (div)
			div.innerHTML = "";
		else {
			div = document.createElement('div');
			div.id = 'domuiprif';
			div.className = 'ui-printdiv';
			document.body.appendChild(div);
		}

		// -- Create an iframe loading the required thingy.
		var frmname = "dmuifrm" + (WebUI._frmIdCounter++); // Create unique
															// name to
															// circumvent
															// ffox "print
															// only once"
															// bug
		if (url.trim() !== "") {
			$(div).html('<iframe id="' + frmname + '" name="' + frmname + '" src="' + url + '">');
		} else {
			//well, this is simple element printing, so we have some size limitations
			$(div).html('<iframe id="' + frmname + '" name="' + frmname + '" width="1000px" height="1000px"/>');
		}
		return frmname;
	},

	/*-- Printing support for simple text messages. Parameter is id of input/textarea tag that contrains text to be printed out. --*/
	printtext: function (id) {
		var item = document.getElementById(id);
		var textData;
		if(item && (item.tagName == "input" || item.tagName == "INPUT" || item.tagName == "textarea" || item.tagName == "TEXTAREA")) {
			textData = item.value;
		}
		if (textData){
			try {
				// Create embedded sizeless div to contain the iframe, invisibly.
				var div = document.getElementById('domuiprif');
				if(div)
					div.innerHTML = "";
				else {
					div = document.createElement('div');
					div.id = 'domuiprif';
					div.className = 'ui-printdiv';
					document.body.appendChild(div);
				}

				//-- Create an iframe loading the required thingy.
				var frmname = "dmuifrm"+(WebUI._frmIdCounter++);		// Create unique name to circumvent ffox "print only once" bug

				$(div).html('<iframe id="'+frmname+'" name="'+frmname+'" width="1000px" height="1000px"/>'); //well, this is simple text printing, so we have some size limitations ;)
				var frm = window.frames[frmname];
				frm.document.open();
				frm.document.write('<html></head></head><body style="margin:0px;">');
				frm.document.write(textData.replace(/\n/g, '<br/>'));
				frm.document.write('</body></html>');
				frm.document.close();
				frm.focus();
				frm.print();
			} catch(x) {
				alert("Failed: "+x);
			}
		}
	},

	nearestID: function(elem) {
		while(elem) {
			if(elem.id)
				return elem.id;
			elem = elem.parentNode;
		}
		return undefined;
	},

	handleDevelopmentMode: function() {
		$(document).bind("keydown", function(e) {
			if(e.keyCode != 192)
				return;

			var t = new Date().getTime();
			if(! WebUI._debugLastKeypress || (t - WebUI._debugLastKeypress) > 250) {
				WebUI._debugLastKeypress = t;
				return;
			}

			//-- Send a DEBUG command to the server, indicating the current node below the last mouse move....
			var id = WebUI.nearestID(WebUI._debugMouseTarget);
			if(! id) {
				id = document.body.id;
			}

			WebUI.scall(id, "DEVTREE", {});
		});
		$(document.body).bind("mousemove", function(e) {
//			if(WebUI._NOMOVE)
//				return;
//			console.debug("move ", e);
			WebUI._debugMouseTarget = e.target; // e.srcElement || e.originalTarget;
		});
	},


	/***** Popup menu code *****/

	_popinCloseList: [],

	popupMenuShow: function(refid, menu) {
		WebUI.registerPopinClose(menu);
		var pos = $(refid).offset();
		var eWidth = $(refid).outerWidth();
		var mwidth = $(menu).outerWidth();
		var left = (pos.left);
		if(left + mwidth > screen.width)
			left = screen.width - mwidth - 10;
		var top = 3+pos.top;
		$(menu).css( {
			position: 'absolute',
			zIndex: 100,
			left: left+"px",
			top: top+"px"
		});

		$(menu).hide().fadeIn();
	},

	popupSubmenuShow: function(parentId, submenu) {
		$(submenu).position({my: 'left top', at: 'center top', of: parentId});
	},

	/**
	 * Register the popup. If the mouse leaves the popup window the popup needs to send a POPINCLOSE? command; this
	 * will tell DomUI server that the popin needs to go. If an item inside the popin is clicked it should mean the
	 * popin closes too; at that point we will deregister the mouse listener to prevent sending double events.
	 *
	 * @param id
	 */
	registerPopinClose: function(id) {
		WebUI._popinCloseList.push(id);
		$(id).bind("mouseleave", WebUI.popinMouseClose);
		if(WebUI._popinCloseList.length != 1)
			return;
		$(document.body).bind("keydown", WebUI.popinKeyClose);
//		$(document.body).bind("beforeclick", WebUI.popinBeforeClick);	// Called when a click is done somewhere - not needed anymore, handled from java
	},

	popinClosed: function(id) {
		for(var i = 0; i < WebUI._popinCloseList.length; i++) {
			if(id === WebUI._popinCloseList[i]) {
				//-- This one is done -> remove mouse handler.
				$(id).unbind("mousedown", WebUI.popinMouseClose);
				WebUI._popinCloseList.splice(i, 1);
				if(WebUI._popinCloseList.length == 0) {
					$(document.body).unbind("keydown", WebUI.popinKeyClose);
					$(document.body).unbind("beforeclick", WebUI.popinBeforeClick);
				}
				return;
			}
		}
	},

	popinBeforeClick: function(ee1, obj, clickevt) {
		for(var i = 0; i < WebUI._popinCloseList.length; i++) {
			var id = WebUI._popinCloseList[i];
			obj = $(obj);
			var cl = obj.closest(id);
			if(cl.size() > 0) {
				//-- This one is done -> remove mouse handler.
				$(id).unbind("mousedown", WebUI.popinMouseClose);
				WebUI._popinCloseList.splice(i, 1);
				if(WebUI._popinCloseList.length == 0) {
					$(document.body).unbind("keydown", WebUI.popinKeyClose);
					$(document.body).unbind("beforeclick", WebUI.popinBeforeClick);
				}
				return;
			}
		}
	},

	popinMouseClose: function() {
		if(WebUI.isUIBlocked())							// We will get a LEAVE if the UI blocks during menu code... Ignore it
			return;

		try {
			for(var i = 0; i < WebUI._popinCloseList.length; i++) {
				var id = WebUI._popinCloseList[i];
				var el = $(id);
				if(el) {
					el.unbind("mousedown", WebUI.popinMouseClose);
					WebUI.scall(id.substring(1), "POPINCLOSE?", {});
				}
			}
		} finally {
			WebUI._popinCloseList = [];
//			$(document.body).unbind("mousedown", WebUI.popinMouseClose);
			$(document.body).unbind("keydown", WebUI.popinKeyClose);
			$(document.body).unbind("beforeclick", WebUI.popinBeforeClick);
		}
	},
	popinKeyClose: function(evt) {
		if(! evt)
			evt = window.event;
		var kk = WebUI.normalizeKey(evt);
		if(kk == 27 || kk == 27000) {
			// Prevent ESC from cancelling the AJAX call in Firefox!!
			evt.preventDefault();
			evt.cancelBubble = true;
			if(evt.stopPropagation)
				evt.stopPropagation();
			WebUI.popinMouseClose();
		}
	},

	//We need to re-show element to force IE7 browser to recalculate correct height of element. This must be done to fix some IE7 missbehaviors.
	refreshElement: function(id) {
		var elem = document.getElementById(id);
		if (elem){
			$(elem).hide();
			$(elem).show(1); //needs to be done on timeout/animation, otherwise it still fails to recalculate...
		}
	},

	//Use this to make sure that item would be visible inside parent scrollable area. It uses scroll animation. In case when item is already in visible part, we just do single blink to gets user attention ;)
	scrollMeToTop: function(elemId, selColor, offset) {
		var elem = document.getElementById(elemId);
		if (!elem){
			return;
		}
		var parent = elem.parentNode;
		if (!parent){
			return;
		}
		if (parent.scrollHeight > parent.offsetHeight){ //if parent has scroll
			var elemPos = $(elem).position().top;
			if (elemPos > 0 && elemPos < parent.offsetHeight){
				//if elem already visible -> just do one blink
				if (selColor){
					var oldColor = $(elem).css('background-color');
					$(elem).animate({backgroundColor: selColor}, "slow", function(){$(elem).animate({backgroundColor: oldColor}, "fast");});
				}
			}else{
				//else scroll parent to show me at top
				var newPos = $(elem).position().top + parent.scrollTop;
				if($.browser.msie && parseInt($.browser.version) < 11){
					if ($(elem).height() == 0){
						newPos = newPos - 15; //On IE browsers older than 11 we need this correction :¬|
					}
				}
				if (offset){
					newPos = newPos - offset;
				}
				$(parent).animate({scrollTop: newPos}, 'slow');
			}
		}
	},

	//Use this to make sure that option in dropdown would be visible. It needs fix only in FF sinve IE would always make visible selected option.
	makeOptionVisible: function(elemId, offset) {
		if($.browser.msie){
			//IE already fix this... we need fix only for FF and other browsers
			return;
		}
		var elem = document.getElementById(elemId);
		if (!elem){
			return;
		}
		var parent = elem.parentNode;
		if (!parent){
			return;
		}
		if (parent.scrollHeight > parent.offsetHeight){ //if parent has scroll
			var elemPos = $(elem).position().top;
			//if elem is not currenlty visible
			if (elemPos <= 0 || elemPos >= parent.offsetHeight){
				//else scroll parent to show me at top
				var newPos = elemPos + parent.scrollTop;
				if (offset){
					newPos = newPos - offset;
				}
				$(parent).animate({scrollTop: newPos}, 'slow');
			}
		}
	},

	//Returns T if browser is really using IE7 rendering engine (since IE8 compatibility mode presents  browser as version 7 but renders as IE8!)
	isReallyIE7: function() {
		//Stupid IE8 in compatibility mode lies that it is IE7, and renders as IE8! At least we can detect that using document.documentMode (it is 8 in that case)
		//document.documentMode == 7 		 --- IE8 running in IE7 mode
		//document.documentMode == 8 		 --- IE8 running in IE8 mode or IE7 Compatibility mode
		//document.documentMode == undefined --- plain old IE7
		return ($.browser.msie && parseInt($.browser.version) == 7 && (!document.documentMode || document.documentMode == 7));
	},
	//Returns T if browser is IE8 or IE8 compatibility mode
	isIE8orIE8c: function() {
		//Stupid IE8 in compatibility mode lies that it is IE7, and renders as IE8! At least we can detect that using document.documentMode (it is 8 in that case)
		//document.documentMode == 7 		 --- IE8 running in IE7 mode
		//document.documentMode == 8 		 --- IE8 running in IE8 mode or IE7 Compatibility mode
		//document.documentMode == undefined --- plain old IE7
		return ($.browser.msie && (parseInt($.browser.version) == 8 || (parseInt($.browser.version) == 7 && document.documentMode == 8)));
	},
	//Returns T if browser is IE of at least version 9 and does not run in any of compatibility modes for earlier versions
	isNormalIE9plus: function() {
		return ($.browser.msie && parseInt($.browser.version) >= 9 && document.documentMode >= 9);
	},

	//Returns T if browser is IE of at least version 8 even if it runs in IE7 compatibility mode
	isIE8orNewer: function() {
		return ($.browser.msie && (parseInt($.browser.version) >= 8 || (parseInt($.browser.version) == 7 && document.documentMode >= 8)));
	},

	// CK editor support, map of key (id of editor) value (pair of [editor instance, assigned resize function])
	_ckEditorMap : {},

	/**
	 * Register ckeditor for extra handling that is set in CKeditor_OnComplete.
	 *
	 * @param id
	 * @param ckeInstance
	 */
	registerCkEditorId : function(id, ckeInstance) {
		WebUI._ckEditorMap[id] = [ckeInstance, null];
	},

	/**
	 * Unregister ckeditor and removes handlings bound to it.
	 *
	 * @param id
	 */
	unregisterCkEditorId : function(id) {
		try {
			var editorBindings = WebUI._ckEditorMap[id];
			WebUI._ckEditorMap[id] = null;
			if (editorBindings && editorBindings[1]){
				$(window).unbind('resize', editorBindings[1]);
			}
		} catch (ex) {
			WebUI.log('error in unregisterCkEditorId: ' + ex);
		}
	},

	/**
	 * Piece of support needed for CK editor to properly fix its size, using _OnComplete handler.
	 *
	 * @param id
	 */
	CKeditor_OnComplete : function(id) {
		WebUI.doCustomUpdates();
		var elem = document.getElementById(id);
		var parentDiv = elem.parentNode;
		var editor = WebUI._ckEditorMap[id][0];
		var resizeFunction = function(ev) {
			try{
				editor.resize($(parentDiv).width() - 2, $(parentDiv).height());
			}catch (ex){
				WebUI.log('error in CKeditor_OnComplete#resizeFunction: ' + ex);
			}
		};
		WebUI._ckEditorMap[id] = [editor, resizeFunction];
		$(window).bind('resize', resizeFunction);
		$(window).trigger('resize');
	},

	// connects input to usually hidden list select and provides autocomplete
	// feature inside input. Down arrow does show and focus select list.
	initAutocomplete : function(inputId, selectId) {
		var input = document.getElementById(inputId);
		var select = document.getElementById(selectId);
		$(input).keyup(function(event) {
			WebUI.autocomplete(event, inputId, selectId);
		});
		$(select).keypress(function(event) {
			//esc hides select and prevents fireing of click and blur handlers that are temporary disconnected while focus moves back to input
			var keyCode = WebUI.normalizeKey(event);
			if (keyCode == 27 || keyCode == 27000) {
				var oldVal = input.value;
				var selectOnClick = select.click;
				var selectOnBlur = select.blur;
				select.click = null;
				select.blur = null;
				select.style.display = 'none';
				input.focus();
				input.value = oldVal;
				select.click = selectOnClick;
				select.blur = selectOnBlur;
			}
		});
	},

	// does autocomplete part of logic
	autocomplete : function (event, inputId, selectId) {
		var select = document.getElementById(selectId);
		var cursorKeys = "8;46;37;38;39;40;33;34;35;36;45;";
		if (cursorKeys.indexOf(event.keyCode + ";") == -1) {
			var input = document.getElementById(inputId);
		    var found = false;
		    var foundAtIndex = -1;
			for (var i = 0; i < select.options.length; i++){
				if ((found = select.options[i].text.toUpperCase().indexOf(input.value.toUpperCase()) == 0)){
					foundAtIndex = i;
					break;
				}
			}
		   	select.selectedIndex = foundAtIndex;

		   	var oldValue = input.value;
			var newValue = found ? select.options[foundAtIndex].text : oldValue;
			if (newValue != oldValue) {
				if (typeof input.selectionStart != "undefined") {
					//normal browsers
		            input.value = newValue;
		            input.selectionStart = oldValue.length;
			        input.selectionEnd =  newValue.length;
			        input.focus();
			    }
				if (document.selection && document.selection.createRange) {
					//IE9
					input.value = newValue;
		            input.focus();
		            input.select();
		            var range = document.selection.createRange();
		            range.collapse(true);
		            range.moveStart("character", oldValue.length);
		            range.moveEnd("character", newValue.length);
		            range.select();
		        }else if (input.createTextRange) {
					//IE8-
					input.value = newValue;
					var rNew = input.createTextRange();
					rNew.moveStart('character', oldValue.length);
					rNew.select();
				}
			}
		}else if (event.keyCode == 40){
			select.style.display = 'inline';
			select.focus();
		}
	},

	//alignment methods
	//sticks top of element with nodeId to bottom of element with alignToId, with extra offsetY.
	alignTopToBottom : function (nodeId, alignToId, offsetY, doCallback){
		var alignNode = $('#' + alignToId);
		var node = $('#' + nodeId);
		var myTopPos;
		if (node.css('position') == 'fixed'){
			myTopPos = $(alignNode).offset().top - $(document).scrollTop() + offsetY + $(alignNode).outerHeight(true);
		}else{
			myTopPos = $(alignNode).position().top + offsetY + $(alignNode).outerHeight(true);
		}
		$(node).css('top', myTopPos);
		if (doCallback){
			WebUI.notifySizePositionChangedOnId(nodeId);
		}
	},

	//align top of element with nodeId to top of element with alignToId, with extra offsetY
	alignToTop : function (nodeId, alignToId, offsetY, doCallback){
		var alignNode = $('#' + alignToId);
		var node = $('#' + nodeId);
		var myTopPos;
		if (node.css('position') == 'fixed'){
			myTopPos = $(alignNode).offset().top - $(document).scrollTop() + offsetY;
		}else{
			myTopPos = $(alignNode).position().top + offsetY;
		}
		var nodeHeight = $(node).outerHeight(true);
		if (myTopPos + nodeHeight > $(window).height()){
			myTopPos = $(window).height() - nodeHeight;
		}
		$(node).css('top', myTopPos);
		if (doCallback){
			WebUI.notifySizePositionChangedOnId(nodeId);
		}
	},

	//align left edge of element with nodeId to left edge of element with alignToId, with extra offsetX
	alignToLeft : function (nodeId, alignToId, offsetX, doCallback){
		var node = $('#' + nodeId);
		var alignNode = $('#' + alignToId);
		var myLeftPos;
		if (node.css('position') == 'fixed'){
			myLeftPos = $(alignNode).offset().left - $(document).scrollLeft() + offsetX;
		}else{
			myLeftPos = $(alignNode).position().left + offsetX;
		}
		var nodeWidth = $(node).outerWidth(true);
		if (myLeftPos + nodeWidth > $(window).width()){
			myLeftPos = $(window).width() - nodeWidth;
			if (myLeftPos < 1){
				myLeftPos = 1;
			}
		}
		$(node).css('left', myLeftPos);
		if (doCallback){
			WebUI.notifySizePositionChangedOnId(nodeId);
		}
	},

	//align right edge of element with nodeId to right edge of element with alignToId, with extra offsetX
	alignToRight : function (nodeId, alignToId, offsetX, doCallback){
		var node = $('#' + nodeId);
		var alignNode = $('#' + alignToId);
		var myLeftPos;
		if (node.css('position') == 'fixed'){
			myLeftPos = $(alignNode).offset().left - $(document).scrollLeft() + offsetX - $(node).outerWidth(true) + $(alignNode).outerWidth(true) - 3;
		}else{
			myLeftPos = $(alignNode).position().left + offsetX - $(node).outerWidth(true) + $(alignNode).outerWidth(true) - 3;
		}
		if (myLeftPos < 1){
			myLeftPos = 1;
		}
		$(node).css('left', myLeftPos);
		if (doCallback){
			WebUI.notifySizePositionChangedOnId(nodeId);
		}
	},

	//align horizontaly middle of element with nodeId to middle of element with alignToId, with extra offsetX
	alignToMiddle : function (nodeId, alignToId, offsetX, doCallback){
		var node = $('#' + nodeId);
		var alignNode = $('#' + alignToId);
		var myLeftPos;
		if (node.css('position') == 'fixed'){
			myLeftPos = $(alignNode).offset().left - $(document).scrollLeft() + ($(alignNode).outerWidth(true) / 2) - ($(node).outerWidth(true) / 2);
		}else{
			myLeftPos = $(alignNode).position().left + ($(alignNode).outerWidth(true) / 2) - ($(node).outerWidth(true) / 2);
		}
		if (myLeftPos < 1){
			myLeftPos = 1;
		}
		$(node).css('left', myLeftPos);
		if (doCallback){
			WebUI.notifySizePositionChangedOnId(nodeId);
		}
	}
});

WebUI._DEFAULT_DROPZONE_HANDLER = {
	checkRerender : function(dz) {
	},
	hover : function(dz) {
		$(dz._dropTarget).addClass("ui-drp-hover");
	},
	unmark : function(dz) {
		if (dz)
			$(dz._dropTarget).removeClass("ui-drp-hover");
	},

	drop : function(dz) {
		this.unmark(dz);
		WebUI.scall(dz._dropTarget.id, "WEBUIDROP", {
			_dragid :WebUI._dragNode.id,
			_index :0
		});
		WebUI.dragReset();
	}
};

/**
 * This handles ROW mode drops. It locates the nearest row in the TBody for this
 * dropTarget and decides to put the thingy BEFORE or AFTER that row. The
 * boundary there then gets highlighted.
 */
WebUI._ROW_DROPZONE_HANDLER = {
	locateBest : function(dz) {
		var tbody = dz._tbody;
		if (!tbody)
			throw "No TBody!";

		// -- Use the current mouseish Y position to distinguish between rows.
		var mousePos = WebUI._dragLastY;
		var mouseX = WebUI._dragLastX;
		//console.debug("Starting position det: drag Y = "+mousePos);
		var gravity = 0; // Prefer upward gravity
		var lastrow = null;
		var rowindex = 0;
		var position = { top: 0, index: 0};
		for ( var i = 0; i < tbody.childNodes.length; i++) {
			var tr = tbody.childNodes[i];
			if (tr.nodeName != 'TR')
				continue;
			lastrow = tr;
			var off = $(tr).offset();
			var prevPosition = position;
			position = { top: off.top, index: i };
			if (position) {
	//			console.debug('mouse:' +mousePos+','+mouseX+' row: prevPosition.top='+prevPosition.top+", position.top="+position.top+", index="+position.index);

				// -- Is the mouse IN the Y range for this row?
				if (mousePos >= prevPosition.top && mousePos < position.top) {
					// -- Cursor is WITHIN this node. Is it near the TOP or near the
					// BOTTOM?
					gravity = 0;
					if(prevPosition.top + position.top != 0){
						var hy = (prevPosition.top + position.top) / 2;
						gravity = mousePos < hy ? 0 : 1;
					}
	//				console.debug('ACCEPTED top='+prevPosition.top+', bottom='+position.top+', hy='+hy+', rowindex='+(rowindex-1));
	//				console.debug('index='+prevPosition.index+', gravety='+gravity);

					var colIndex = this.getColIndex(tr, mouseX);
					return {
						index :rowindex-1,
						iindex : prevPosition.index,
						gravity :gravity,
						row :tr,
						colIndex : colIndex
					};
				}

				// -- Is the thing between this row and the PREVIOUS one?
	//			if (mousePos < position.top) {
	//				// -- Use this row with gravity 0 (should insert BEFORE this row).
	//				//MVE
	//				console.debug('ACCEPTED BEFORE node by='+prevPosition.top+', ey='+position.top+', rowindex='+rowindex-1);
	//				return {
	//					index :rowindex,
	//					iindex :position.index,
	//					gravity :0,
	//					row :tr
	//				};
	//			}
				//console.debug('REFUSED by='+prevPosition.top+", ey="+position.top+", rowindex="+rowindex);
			} else {
	//			console.debug("row: no location.");
			}
			rowindex++;
		}
		//console.debug("ACCEPTED last one");

		// -- If we're here we must insert at the last location
		var colIndex = this.getColIndex(lastrow, mouseX);
		return {
			index :rowindex,
			iindex :position.index,
			gravity :1,
			row :lastrow,
			colIndex : colIndex
		};
	},

	getColIndex : function(tr, mouseX) {
		//determine the collumn
		var left = 0;
		var right = 0;
		var j;
		for ( j = 0; j < tr.childNodes.length; j++) {
			var td = tr.childNodes[j];
			if (td.nodeName != 'TD')
				continue;
			left = right;
			right = $(td).offset().left;
			if(mouseX >= left && mouseX < right ){
				//because only the left position can be asked, the check is done for the previous collumn
				return j-1;
			}

		}
		//TODO MVE should return maxColumn
		return 2;

	},

	checkRerender : function(dz) {
		var b = this.locateBest(dz);
		// console.debug("checkRerender: "+b.iindex+", "+b.index+", g="+b.gravity);
		if (b.iindex == WebUI._dropRowIndex)
			return;

		this.unmark(dz);
		this.renderTween(dz, b);
	},

	renderTween : function(dz, b) {
		var body = dz._tbody;

		var colCount = 0;
		if(dz._tbody.rows.length > 0){
			var temp = dz._tbody.rows[0].cells;
		    $(temp).each(function() {
		        colCount += $(this).attr('colspan') ? parseInt($(this).attr('colspan')) : 1;
		    });
		}


		// -- To mark, we insert a ROW at the insert location and visualize that
		var tr = document.createElement('tr');
		//b.colIndex should define the correct collumn
		var colIndex = b.colIndex;
		for(var i = 0; i<colCount;i++ ){
			this.appendPlaceHolderCell(tr, colIndex == i);
		}
		if (b.iindex >= body.childNodes.length)
			body.appendChild(tr);
		else
			body.insertBefore(tr, body.childNodes[b.iindex]);
		WebUI._dropRow = tr;
		WebUI._dropRowIndex = b.iindex;
	},

	appendPlaceHolderCell : function(tr, appendPlaceholder) {
		var td = document.createElement('td');
		if(appendPlaceholder){
			td.appendChild(document.createTextNode(WebUI._T.dndInsertHere));
			td.className = 'ui-drp-ins';
		}
		tr.appendChild(td);

	},

	hover : function(dz) {
		var b = this.locateBest(dz);
	//	console.debug("hover: "+b.iindex+", "+b.index+", g="+b.gravity + ", col=" +b.colIndex);
		this.renderTween(dz, b);
	},

	unmark : function(dz) {
		if (WebUI._dropRow) {
			$(WebUI._dropRow).remove();
			delete WebUI._dropRow;
			delete WebUI._dropRowIndex;
		}
	},

	drop : function(dz) {
		this.unmark(dz);
		var b = this.locateBest(dz);
		WebUI.scall(dz._dropTarget.id, "WEBUIDROP", {
			_dragid :WebUI._dragNode.id,
			_index :(b.index+b.gravity),
			_colIndex :b.colIndex
		});
		WebUI.dragReset();
	}
};

/**
 * Make a structure a color button.
 */
WebUI.colorPickerButton = function(btnid, inid, value,onchange) {
	$(btnid).ColorPicker({
		color: '#'+value,
		onShow: function (colpkr) {
			$(colpkr).fadeIn(500);
			return false;
		},
		onHide: function (colpkr) {
			$(colpkr).fadeOut(500);
			return false;
		},
		onChange: function (hsb, hex, rgb) {
			$(btnid+' div').css('backgroundColor', '#' + hex);
			$(inid).val(hex);
			if(onchange)
				WebUI.colorPickerOnchange(btnid, hex);
		}
	});
};

WebUI.colorPickerInput = function(inid, divid, value, onchange) {
	$(inid).ColorPicker({
		color: '#'+value,
		flat: false,
		onShow: function (colpkr) {
			$(colpkr).fadeIn(500);
			return false;
		},
		onHide: function (colpkr) {
			$(colpkr).fadeOut(500);
			return false;
		},
		onBeforeShow: function() {
			$(this).ColorPickerSetColor(this.value);
		},
		onChange: function (hsb, hex, rgb) {
			$(divid).css('backgroundColor', '#' + hex);
			$(inid).val(hex);
			if(onchange)
				WebUI.colorPickerOnchange(btnid, hex);
		}
	});
};
WebUI.colorPickerDisable = function(id) {
	$(id).die();
};

WebUI.colorPickerOnchange= function(id, last) {
	if(WebUI._colorLast == last && WebUI._colorLastID == id)
		return;

	if(WebUI._colorTimer) {
		window.clearTimeout(WebUI._colorTimer);
		window._colorTimer = undefined;
	}
	WebUI._colorLastID = id;
	WebUI._colorTimer = window.setTimeout("WebUI.colorPickerChangeEvent('" + id + "')", 500);
};

WebUI.colorPickerChangeEvent = function(id) {
	window.clearTimeout(WebUI._colorTimer);
	window._colorTimer = undefined;
	WebUI.valuechanged('eh', id);
};

var DomUI = WebUI;

WebUI._customUpdatesContributors = $.Callbacks("unique");

WebUI._customUpdatesContributorsTimerID = null;

/**
 * registers function that gets called after doCustomUpdates sequence of calls ends, with 500 delay - doCustomUpdates can trigger new doCustomUpdates etc...
 * @param contributorFunction
 */
WebUI.registerCustomUpdatesContributor = function(contributorFunction) {
	WebUI._customUpdatesContributors.add(contributorFunction);
};

WebUI.unregisterCustomUpdatesContributor = function(contributorFunction) {
	WebUI._customUpdatesContributors.remove(contributorFunction);
};

WebUI.doCustomUpdates = function() {
	$('.floatThead-wrapper').each(
		function (index, node){
			$(node).attr('stretch', $(node).find('>:first-child').attr('stretch'));
		}
	);
	$('[stretch=true]').doStretch();
	$('.ui-dt, .ui-fixovfl').fixOverflow();
	$('input[marker]').setBackgroundImageMarker();

	//-- Limit textarea size on paste events
	$("textarea[mxlength], textarea[maxbytes]").unbind("input.domui").unbind("propertychange.domui").bind('input.domui propertychange.domui', function() {
		var maxLength = Number($(this).attr('mxlength'));				// Use mxlength because Chrome improperly implements maxlength (issue 252613)
		var maxBytes = Number($(this).attr('maxbytes'));
		var val = $(this).val();
        var newlines = (val.match(/\r\n/g) || []).length;				// Count the #of 2-char newlines, as they will be replaced by 1 newline character
		if(maxBytes === NaN) {
			if(maxLength === NaN)
				return;
		} else if(maxLength === NaN) {
			maxLength = maxBytes;
		}

		if(val.length + newlines > maxLength) {
			val = val.substring(0, maxLength - newlines);
			$(this).val(val);
		}
		if(maxBytes !== NaN) {
			var cutoff = WebUI.truncateUtfBytes(val, maxBytes);
			if(cutoff < val.length) {
				val = val.substring(0, cutoff);
				$(this).val(val);
			}
		}
	});

	//-- Limit textarea size on key presses
	$("textarea[mxlength], textarea[maxbytes]").unbind("keypress.domui").bind('keypress.domui', function(evt) {
		if(evt.which == 0 || evt.which == 8)
			return true;

		//-- Is the thing too long already?
		var maxLength = Number($(this).attr('mxlength'));
		var maxBytes = Number($(this).attr('maxbytes'));
		var val = $(this).val();
		var newlines = (val.match(/\r\n/g) || []).length;				// Count the #of 2-char newlines, as they will be replaced by 1 newline character
		if(maxBytes === NaN) {
			if(maxLength === NaN)
				return true;
		} else if(maxLength === NaN) {
			maxLength = maxBytes;
		}
		if(val.length - newlines >= maxLength)							// Too many chars -> not allowed
			return false;
		if(maxBytes !== NaN) {
			var bytes = WebUI.utf8Length(val);
			if(bytes >= maxBytes)
				return false;
		}
		return true;
	});

	//custom updates may fire several times in sequence, se we fire custom contributors only after it gets steady for a while (500ms)
	if (WebUI._customUpdatesContributorsTimerID) {
		window.clearTimeout(WebUI._customUpdatesContributorsTimerID);
	}
	WebUI._customUpdatesContributorsTimerID = window.setTimeout(function(){ try{ WebUI._customUpdatesContributors.fire()}catch(ex) {}}, 500);
	//$('.ui-dt-ovflw-tbl').floatThead('reflow');
};

WebUI.truncateUtfBytes = function(str, nbytes) {
	//-- Loop characters and calculate running length
	var bytes = 0;
	var length = str.length;
	for(var ix = 0; ix < length; ix++) {
		var c = str.charCodeAt(ix);
		if(c < 0x80)
			bytes++;
		else if(c < 0x800)
			bytes += 2;
		else
			bytes += 3;
		if(bytes > nbytes)
			return ix;
	}
	return length;
};

WebUI.utf8Length = function(str) {
	var bytes = 0;
	var length = str.length;
	for(var ix = 0; ix < length; ix++) {
		var c = str.charCodeAt(ix);
		if(c < 0x80)
			bytes++;
		else if(c < 0x800)
			bytes += 2;
		else
			bytes += 3;
	}
	return bytes;
};


WebUI.onDocumentReady = function() {
	WebUI.checkBrowser();
	WebUI.handleCalendarChanges();
	if(DomUIDevel)
		WebUI.handleDevelopmentMode();
	WebUI.doCustomUpdates();
};

WebUI.floatingDivResize = function(ev, ui) {
	$(ui.helper.get(0)).css('position', 'fixed');
	$('[stretch=true]').doStretch();
	$('.ui-dt, .ui-fixovfl').fixOverflow();
};

WebUI.onWindowResize = function() {
	WebUI.doCustomUpdates();
};

WebUI.flare = function(id) {
	$('#'+id).fadeIn('fast', function() {
		$('#'+id).delay(500).fadeOut(1000, function() {
			$('#'+id).remove();
		});
	});
};

WebUI.flareStay = function(id) {
	$('#'+id).fadeIn('fast', function() {
		$('body,html').bind('mousemove.' + id, function(e){
			$('body,html').unbind('mousemove.' + id);
			$('#'+id).delay(500).fadeOut(1000, function() {
				$('#'+id).remove();
			});
		});
	});
};

WebUI.flareStayCustom = function(id, delay, fadeOut) {
	$('#'+id).fadeIn('fast', function() {
		$('body,html').bind('mousemove.' + id, function(e){
			$('body,html').unbind('mousemove.' + id);
			$('#'+id).delay(delay).fadeOut(fadeOut, function() {
				$('#'+id).remove();
			});
		});
	});
};

WebUI.replaceBrokenImageSrc = function(id, alternativeImage) {
	$('img#' + id).error(function() {
		$(this).attr("src", alternativeImage);
	});
};

/** In tables that have special class selectors that might cause text-overflow we show full text on hover */
WebUI.showOverflowTextAsTitle = function(id, selector) {
	var root = $("#" + id);
	if (root) {
		root.find(selector).each(function () {
			if (this.offsetWidth < this.scrollWidth) {
				var $this = $(this);
				$this.attr("title", $this.text());
			}
		});
	}
	return true;
};

/** Bulk upload code using swfupload */
WebUI.bulkUpload = function(id, buttonId, url) {
	var ctl = $('#'+id);
	ctl.swfupload({
		upload_url: url,
		flash_url: DomUIappURL+"$js/swfupload.swf",
		file_types: '*.*',
		file_upload_limit: 1000,
		file_queue_limit: 0,
		file_size_limit: "100 MB",
		button_width: 120,
		button_height: 24,
		button_placeholder_id: buttonId,
		button_window_mode: SWFUpload.WINDOW_MODE.TRANSPARENT,
		button_cursor: SWFUpload.CURSOR.HAND
	});
	var target = $("#"+id+" .ui-bupl-queue");

	ctl.bind('fileQueued', function(event, file) {
		var uf = new WebUI.UploadFile(file, target, function() {
			$.swfupload.getInstance(ctl).cancelUpload(file.id);
		});
	});
	ctl.bind('uploadStart', function(event, file) {
		var uf = new WebUI.UploadFile(file, target);
		uf.uploadStarted();
	});
	ctl.bind('uploadProgress', function(event, file, bytesdone, bytestotal) {
		var uf = new WebUI.UploadFile(file, target);
		var pct = bytesdone * 100 / bytestotal;
		uf.setProgress(pct);
	});
	ctl.bind('uploadError', function(event, file, code, msg) {
		var uf = new WebUI.UploadFile(file, target);
		uf.uploadError(msg);
	});
	ctl.bind('uploadSuccess', function(event, file, code, msg) {
		var uf = new WebUI.UploadFile(file, target);
		uf.uploadComplete();

		//-- Send a DomUI command so the UI can handle updates.
		WebUI.scall(id, "uploadDone", {});
	});
	ctl.bind('queueComplete', function(event, numUploaded) {
		//-- Send a DomUI command for queue complete.
		WebUI.scall(id, "queueComplete", {});
	});
//	ctl.bind('uploadComplete', function(event, file) {
//		var uf = new WebUI.UploadFile(file, target);
//	});
	ctl.bind('fileDialogComplete', function(nfiles) {
		if(0 == nfiles) {
			return;
		}

		//-- Autostart upload on dialog completion.
		ctl.swfupload('startUpload');

		//-- Send a DomUI command for queue start.
		WebUI.scall(id, "queueStart", {});
	});
	ctl.bind('fileQueueError', function(event, file, errorCode, message) {
		try {
			if(errorCode === SWFUpload.QUEUE_ERROR.QUEUE_LIMIT_EXCEEDED) {
				alert(WebUI._T.buplTooMany);
//				alert("You have attempted to queue too many files.\n" + (message === 0 ? "You have reached the upload limit." : "You may select " + (message > 1 ? "up to " + message + " files." : "one file.")));
				return;
			}
			var uf = new WebUI.UploadFile(file, target);
			switch (errorCode) {
				case SWFUpload.QUEUE_ERROR.FILE_EXCEEDS_SIZE_LIMIT:
					uf.uploadError(WebUI._T.buplTooBig);
					break;
				case SWFUpload.QUEUE_ERROR.ZERO_BYTE_FILE:
					uf.uploadError(WebUI._T.buplEmptyFile);
					break;
				case SWFUpload.QUEUE_ERROR.INVALID_FILETYPE:
					uf.uploadError(WebUI._T.buplInvalidType);
					break;
				default:
					if(file !== null) {
						uf.uploadError(WebUI._T.buplUnknownError);
					}
					break;
			}
		} catch (ex) {
			alert(ex);
	    }
	});

//	//-- TEST
//	var file = {
//			id: 'jal',
//			name: 'upload.jpg'
//	};
//	var uf = new WebUI.UploadFile(file, target, function() {
//		$.swfupload.getInstance(ctl).cancelUpload(file.id);
//	});
//
//	setTimeout(function() {
//		uf.uploadStarted();
//
//	}, 2000);
//
//	setTimeout(function() {
//		uf.setProgress(10);
//
//	}, 3000);
//
//	setTimeout(function() {
//		uf.setProgress(20);
//
//	}, 4000);
//
////	setTimeout(function() {
////		uf.uploadError("Server IO error");
////
////	}, 6000);
//
//	setTimeout(function() {
//		uf.uploadComplete();
//	}, 6000);
};

/**
 * The uploadFile object handles all progress handling for a swf file.
 */
WebUI.UploadFile = function(file, target, cancelFn) {
	this._id = file.id;

	//-- connect to pre-existing UI
	this._ui = $('#'+file.id);
	if(this._ui.length == 0) {
		//-- Create the UI.
		target.append("<div id='"+this._id+"' class='ui-bupl-file'><div class='ui-bupl-inner ui-bupl-pending'><a href='#' class='ui-bupl-cancl'> </a><div class='ui-bupl-name'>"+file.name+"</div><div class='ui-bupl-stat'>"+DomUI._T.buplPending+"</div><div class='ui-bupl-perc'></div></div></div>");
		this._ui = $('#'+file.id);
		if(cancelFn) {
			var me = this;
			$(".ui-bupl-cancl", this._ui).bind("click", function() {
				$(".ui-bupl-stat", this._ui).html(WebUI._T.buplCancelled);
				me.suicide();
				cancelFn();
			});
		}
	}
};
WebUI.UploadFile.prototype.uploadStarted = function() {
	$(".ui-bupl-stat", this._ui).html(WebUI._T.buplRunning);
	$(".ui-bupl-inner", this._ui).removeClass("ui-bupl-pending").addClass("ui-bupl-running");
};
WebUI.UploadFile.prototype.setProgress = function(pct) {
	$(".ui-bupl-perc", this._ui).width(pct+"%");
};
WebUI.UploadFile.prototype.uploadError = function(message) {
	$(".ui-bupl-stat", this._ui).html(WebUI._T.buplError+": "+message);
	$(".ui-bupl-inner", this._ui).removeClass("ui-bupl-pending").removeClass("ui-bupl-running").addClass("ui-bupl-error");
	$(".ui-bupl-cancl", this._ui).remove();
	this.setProgress(0);
	this.suicide();
};
WebUI.UploadFile.prototype.uploadComplete = function() {
	$(".ui-bupl-stat", this._ui).html(WebUI._T.buplComplete);
	$(".ui-bupl-inner", this._ui).removeClass("ui-bupl-pending").removeClass("ui-bupl-running").removeClass("ui-bupl-error").addClass("ui-bupl-complete");
	this.setProgress(100);
	$(".ui-bupl-cancl", this._ui).remove();
	this.suicide();
};
WebUI.UploadFile.prototype.suicide = function() {
	this._ui.delay(8000).fadeOut(500);
};


$(document).ready(WebUI.onDocumentReady);
$(window).resize(WebUI.onWindowResize);
$(document).ajaxComplete( function() {
	WebUI.handleCalendarChanges();
	WebUI.doCustomUpdates();
});

$(document).keydown(function(e){
	addPaggerAccessKeys(e);
});

function addPaggerAccessKeys(e) {
	var KEY = {
			HOME		: 36,
			END			: 35,
			PAGE_UP		: 33,
			PAGE_DOWN	: 34
	};
	if ($('div.ui-dp-btns').size() > 0) {
		if (e.altKey){
			if(e.keyCode == KEY.HOME) {
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

//piece of support needed for FCK editor to properly fix heights in IE8+
function FCKeditor_OnComplete(editorInstance){
	if (WebUI.isIE8orNewer()){
		for (var i = 0; i < WebUI._fckEditorIDs.length; i++) {
		    var fckId = WebUI._fckEditorIDs[i];
		    var fckIFrame = document.getElementById(fckId + '___Frame');
			if (fckIFrame){
				$(fckIFrame.contentWindow.window).bind('resize', function()
					{
						FCKeditor_fixLayout(fckIFrame, fckId);
					});
				$(fckIFrame.contentWindow.window).trigger('resize');
			}
		}
	}
}

WebUI.deactivateHiddenAccessKeys = function(windowId) {
	$('button').each(function(index) {
		var iButton = $(this);
		if(isButtonChildOfElement(iButton, windowId)){
			var oldAccessKey = $(iButton).attr('accesskey');
			if(oldAccessKey != null ){
				$(iButton).attr('accesskey', $(windowId).attr('id') + '~' + oldAccessKey);
			}
		}
	});
};

WebUI.reactivateHiddenAccessKeys = function(windowId) {
	$("button[accesskey*='" + windowId + "~']" ).each(function(index){
		var accessKeyArray = $(this).attr('accesskey').split(windowId + '~');
		$(this).attr('accesskey', accessKeyArray[accessKeyArray.length - 1]);
	});
};

WebUI.initScrollableTableOld = function(id) {
	$('#'+id+" table").fixedHeaderTable({});
	var sbody = $('#'+id+" .fht-tbody");
	sbody.scroll(function() {
		var bh = $(sbody).height();
		var st = $(sbody).scrollTop();
		var tbl = $('#'+id+" .fht-table tbody");
		var th = tbl.height();
		var left = tbl.height() - bh - st;
		//$.dbg("scrolling: bodyheight="+bh+" scrolltop="+st+" tableheight="+th+" left="+left);

		if(left > 100) {
			//$.dbg("Scrolling: area left="+left);
			return;
		}

		var lastRec = sbody.find("tr[lastRow]");
		if(lastRec.length != 0) {
			//$.dbg("scrolling: lastrec found");
			return;
		}
		WebUI.scall(id, "LOADMORE", {});
	});

};

WebUI.scrollableTableReset = function(id, tblid) {
	var tbl = $('#'+tblid);
	var container = $('#'+id);
	tbl.floatThead('reflow');
	WebUI.doCustomUpdates();

	$.dbg('recreate');

	//tbl.floatThead('destroy');
	//tbl.floatThead({
	//	scrollContainer: function() {
	//		return container;
	//	}
	//});

	container.scrollTop(0);
};

WebUI.initScrollableTable = function(id, tblid) {
	var container = $('#'+id);
	var tbl = $('#'+tblid);
	WebUI.doCustomUpdates();

	tbl.floatThead({
		scrollContainer: function() {
			return container;
		},
		getSizingRow: function($table){ // this is only called when using IE, we need any row without colspan, see http://mkoryak.github.io/floatThead/examples/row-groups/
			var rows = $table.find('tbody tr:visible').get();
			for (var i = 0; i < rows.length; i++){
				var cells = $(rows[i]).find('td');
				var isInvalidRow = false;
				for (var i = 0; i < cells.get().length; i++){
					if ($(cells[i]).attr('colspan') > 1){
						isInvalidRow = true;
					}
				}
				if (!isInvalidRow){
					return cells;
				}
			}
			if (rows.length > 0) {
				return $(rows[0]).find('td'); //as fallback we just return first row cells
			}else{
				return null; //or nothing -> but this should not be possible since getSizingRow is called only on table with rows
			}
		}
	});
	container.scroll(function() {
		var bh = $(container).height();
		var st = $(container).scrollTop();
		var tbl = $('#'+id+" tbody");
		var th = tbl.height();
		var left = tbl.height() - bh - st;
		$.dbg("scrolling: bodyheight="+bh+" scrolltop="+st+" tableheight="+th+" left="+left);

		if(left > 100) {
			//$.dbg("Scrolling: area left="+left);
			return;
		}

		var lastRec = tbl.find("tr[lastRow]");
		if(lastRec.length != 0) {
			//$.dbg("scrolling: lastrec found");
			return;
		}
		WebUI.scall(id, "LOADMORE", {

		});
	});

};



isButtonChildOfElement = function(buttonId, windowId){
	return $(buttonId).parents('#' + $(windowId).attr('id')).length == 0;
};

WebUI.notifyPage = function(command) {
	var bodyId = '_1';
	var pageBody = document.getElementById(bodyId);
	//check for exsistence, since it is delayed action component can be removed when action is executed.
	if (pageBody){
		var fields = {};
		fields.webuia = "notifyPage";
		fields[bodyId + "_command"] = command;
		WebUI.scall(bodyId, "notifyPage", fields);
	}
};

WebUI.closeOnClick = function(id) {
	this._id = id;
	var clickHandler = this._clickHandler = $.proxy(this.closeMenu, this);
    $(document).click(clickHandler);
	var keyUpHandler = this._keyUpHandler = $.proxy(this.buttonHandler, this);
    $(document).keyup(keyUpHandler);
	$('#' + id).data('inst', this);
};

$.extend(WebUI.closeOnClick.prototype, {
	closeMenu: function() {
		this.unbind();
		WebUI.scall(this._id, "CLOSEMENU?", {});
	},

	unbind: function() {
		$(document).unbind("click", this._clickHandler);
		$(document).unbind("keyup", this._keyUpHandler);
	},

	markClosed: function(id) {
		var inst = $('#' + id).data('inst');
		if(inst) {
			inst.unbind();
		}
	},

	isInputTagEvent: function(event) {
		var src = event.srcElement;
		if(src) {
			var tn = src.tagName.toUpperCase();
			if(tn === 'INPUT' || tn == 'SELECT' || tn == "TEXTAREA")
				return true;
		}
		return false;
	},

	buttonHandler: function(event) {
		if (this.isInputTagEvent(event))
			return;

		if (event.which == 27) {				// escape
			this.closeMenu();
		}
	}
});

DbPerformance = {};
DbPerformance.post = function(id,sessionid) {
	$(document).ready(function() {
		setTimeout(function() {
			$.get(DomUIappURL + "nl.itris.vp.parts.DbPerf.part?requestid=" + sessionid, function(data) {
				//-- Insert the div as the last in the body
				$('#' + id).html(data);
				$(".vp-lspf").draggable({ghosting: false, zIndex: 100, handle: '.vp-lspf-ttl'});
				$(".vp-lspf-close").click(function() {
					$(".vp-lspf").hide();
				});

			});
		}, 500);

	})
};
