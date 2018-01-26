/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.ts" />

(function($: any) {
	$.webui = function(xml) {
		processDoc(xml);
	};

	$.fn.extend({
			replace: function(a) {
			return this.after(a).remove();
		},

		replaceContent: function(a) {
			return this.empty().append(a);
		}
	});

	$.expr[':'].taconiteTag = function(a) {
		return (a as any).taconiteTag === 1;
	};

	function log(...str) {
		if(!window.console || !window.console.debug)
			return;
		window.console.debug.apply(window.console, arguments);
		// window.console.debug("Args: "+[].join.call(arguments,''));
	}

	function processDoc(xml: any): void {
		go(xml);
	}

	// convert string to xml document
	function convert(s) {
		let doc: any = undefined;
		try {
			if((window as any).ActiveXObject) {
				doc = new ActiveXObject('Microsoft.XMLDOM');
				doc.async = 'false';
				doc.loadXML(s);
			} else {
				const parser = new DOMParser();
				doc = parser.parseFromString(s, 'text/xml');
			}
		} catch(e) {
			if(window.console && window.console.debug)
				window.console.debug('ERROR parsing XML string for conversion: ' + e, e);
			throw e;
		}
		const ok = doc && doc.documentElement && doc.documentElement.tagName != 'parsererror';
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

	function go(xml: any): void {
		if(xml === "") {
			//-- force a reload.
			window.location.href = window.location.href;
			return;
		}

		if(typeof xml == 'string')
			xml = convert(xml);
		if(!xml || !xml.documentElement) {
			log('Invalid document');
			return;
		}
		executeXML(xml);
	}

	function executeXML(xml) : void {
		// -- If this is a REDIRECT document -> redirect main page
		const rname = xml.documentElement.tagName;
		if(rname == 'redirect') {
			WebUI.blockUI();
			log("Redirecting- ");
			const to = xml.documentElement.getAttribute('url');

			if(!$.browser.msie && !$.browser.ieedge) {
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
			return;
		} else if(rname == 'expiredOnPollasy') {
			return; 						// do nothing actually, page is in process of redirecting to some other page and we need to ignore responses on obsolete pollasy calls...
		} else if(rname == 'expired') {
			let msg = WebUI._T.sysSessionExpired;
			let hr = window.location.href;
			for(let i = xml.documentElement.childNodes.length; --i >= 0;) {
				let cn = xml.documentElement.childNodes[i];
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
			window.location.href = hr; 						// Force reload
			return;
		}

		// let t = new Date().getTime();
		process(xml.documentElement.childNodes);
	}

	$.executeXML = function(xml) {
		executeXML(xml);
	};

	// -- process the commands
	function process(commands: any[]) : void {
		const param = {
			postProcess: false
		};
		for(let i = 0; i < commands.length; i++) {
			if(commands[i].nodeType != 1)
				continue; // commands are elements
			let cmdNode = commands[i];
			if(! executeNode(cmdNode, param)) {
				return;
			}
		}

		// apply dynamic fixes
		if(param.postProcess)
			postProcess();
	}

	function executeNode(cmdNode: any, param: any) : boolean {
		let cmd = cmdNode.tagName;
		if(cmd == "parsererror") { // Chrome
			alert("The server response could not be parsed: " + cmdNode.innerText);
			window.location.href = window.location.href;
			return false;
		}

		if(cmd == 'head' || cmd == 'body') {
			//-- HTML response. Server state is gone due to restart or lost session.
			if(!WebUI._hideExpiredMessage) {
				alert(WebUI._T.sysSessionExpired2);
			}
			window.location.href = window.location.href;
			return false;
		}

		if(cmd == 'eval') {
			let js;
			try {
				js = (cmdNode.firstChild ? cmdNode.textContent : null); //textContent due to AJAX 4096 limit per single node content.
				//log('invoking "eval" command: ', js);
				if(js)
					$.globalEval(js);
			} catch(ex) {
				alert('eval failed: ' + ex + ", js=" + js);
				throw ex;
			}
			return true;
		}
		let q = cmdNode.getAttribute('select');
		if(!q) {
			//-- Node sans select-> we are in trouble -> this is probably a server error/response. Report session error, then reload. (Marc, 20111017)
			alert('The server seems to have lost this page.. Reloading the page with fresh data');
			window.location.href = window.location.href;
			return false;
		}

		const jq = $(q);
		if(!jq[0]) {
			log('No matching targets for selector: ', q);
			return true;
		}

		if(cmd == 'changeTagAttributes') {
			executeChangeTagAttributes(cmdNode, q);
			return true;
		}

		var cdataWrap = cmdNode.getAttribute('cdataWrap') || 'div';

		let a = [];
		let trimHash = {
			wrap: 1
		};
		if(cmdNode.childNodes.length > 0) {
			param.postProcess = true;
			const els = [];
			for(let j = 0; j < cmdNode.childNodes.length; j++)
				els[j] = createNode(cmdNode.childNodes[j], cdataWrap);
			a.push(trimHash[cmd] ? cleanse(els) : els);
		}

		let n = cmdNode.getAttribute('name');
		let v = cmdNode.getAttribute('value');
		if(n !== null)
			a.push(n);
		if(v !== null)
			a.push(v);

		for(let j = 1; true; j++) {
			v = cmdNode.getAttribute('arg' + j);
			if(v === null)
				break;
			a.push(v);
		}

		// if(true) {
		// 	let arg = els ? '...' : a.join(',');
		// 	//log("invoke command: $('", q, "').", cmd, '(' + arg + ')');
		// }
		try {
			(jq[cmd] as any).apply(jq, a);
		} catch(e) {
			console.log("exception on node " + jq + " command " + cmd);
			throw e;
		}
		return true;
	}

	function executeChangeTagAttributes(cmdNode: any, queryString: string) : void {
		try {
			// -- Copy attributes on this tag to the target tags
			const dest = $(queryString)[0]; 							// Should be 1 element

			const names = [];
			for(let ai = 0; ai < dest.attributes.length; ai++) {
				names[ai] = $.trim(dest.attributes[ai].name);
			}

			const src = cmdNode;
			for(let ai = 0, attr = ''; ai < src.attributes.length; ai++) {
				const a = src.attributes[ai], n = $.trim(a.name), v = $.trim(a.value);
				if(n == 'select' || n.substring(0, 2) == 'on')
					continue;
				if(n.substring(0, 6) == 'domjs_') {
					let s;
					try {
						s = "dest." + n.substring(6) + " = " + v;
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
					//We need this dirty fix for IE7 to force height recalculation of divs that has just become visible (IE7 sometimes fails to calculate height that stays 0!).
					if($.browser.msie && $.browser.version.substring(0, 1) == "7") {
						if((dest.tagName.toLowerCase() == 'div' && $(dest).height() == 0) && ((v.indexOf('visibility') != -1 && v.indexOf('hidden') == -1) || (v.indexOf('display') != -1 && v.indexOf('none') == -1))) {
							WebUI.refreshElement(dest.id);
						}
					}
				} else {
					//-- jal 20100720 handle disabled, readonly, checked differently: these are either present or not present; their value is always the same.
//								alert('changeAttr: id='+dest.id+' change '+n+" to "+v);

					if(dest.tagName.toLowerCase() == 'select' && n == 'class' && $.browser.mozilla) {
						dest.className = v;
						let ele = dest as any;
						let old = ele.selectedIndex;
						ele.selectedIndex = 1;			// jal 20100720 Fixes problem where setting BG color on select removes the dropdown button image
						ele.selectedIndex = old;
					} else if(v == "" && ("checked" == n || "selected" == n || "disabled" == n || "readonly" == n)) {
						$(queryString).removeAttr(n);
						removeValueFromArray(names, n);
					} else {
						$(queryString).attr(n, v);
						removeValueFromArray(names, n);
					}
				}
			}
			for(let ai = 0; ai < names.length; ai++) {
				let a = names[ai];
				if(a == 'checked' || a == 'disabled' || a == 'title') {
					$(dest).removeAttr(a);
				}
			}
		} catch(ex) {
			alert('changeTagAttr failed: ' + ex);
			throw ex;
		}
	}

	function postProcess() {
		if(!$.browser.opera && !$.browser.msie)
			return;
		$('select:taconiteTag').each(function() {
			$('option:taconiteTag', this).each(function() {
				this.setAttribute('selected', 'selected');
				(this as any).taconiteTag = null;
			});
			(this as any).taconiteTag = null;
		});
	}

	function removeValueFromArray(names, n) {
		const index = names.indexOf(n);
		if(index > -1) {
			names.splice(index, 1);
		}
	}

	function cleanse(els) : any[] {
		const a = [];
		for(let i = 0; i < els.length; i++)
			if(els[i].nodeType == 1)
				a.push(els[i]);
		return a;
	}

	function createNode(node, cdataWrap: string) {
		const type = node.nodeType;
		if(type == 1)
			return createElement(node, cdataWrap);
		if(type == 3)
			return fixTextNode(node.nodeValue);
		if(type == 4)
			return handleCDATA(cdataWrap, node.nodeValue);
		return null;
	}

	function handleCDATA(cdataWrap: string, s) {
		var el = document.createElement(cdataWrap);
		el.innerHTML = s;
		return el;
	}

	function fixTextNode(s) {
		return document.createTextNode(s);
	}

	function createElement(node, cdataWrap: string) {
		var e, tag = node.tagName.toLowerCase();
		// some elements in IE need to be created with attrs inline
		if($.browser.msie && !WebUI.isNormalIE9plus()) {
			var type = node.getAttribute('type');
			if(tag == 'table'
				|| type == 'radio'
				|| type == 'checkbox'
				|| tag == 'button'
				|| (tag == 'select' && node
					.getAttribute('multiple'))) {
				let xxa;
				try {
					xxa = copyAttrs(null, node, true);
					e = document.createElement('<' + tag + ' '
						+ xxa + '>');
				} catch(xx) {
					alert('err= ' + xx + ', ' + tag + ", " + xxa);
				}
			}
		}
		if(!e) {
			e = document.createElement(tag);
			copyAttrs(e, node, false);
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
			for(var i = 0, max = node.childNodes.length; i < max; i++) {
				var child = createNode(node.childNodes[i], cdataWrap);
				if(child)
					e.appendChild(child);
			}
		}
		if($.browser.msie || $.browser.opera) {
			if(tag == 'select'
				|| (tag == 'option' && node
					.getAttribute('selected')))
				e.taconiteTag = 1;
		}
		return e;
	}

	function copyAttrs(dest, src, inline) {
		for(var i = 0, attr = ''; i < src.attributes.length; i++) {
			var a = src.attributes[i], n = $.trim(a.name), v = $.trim(a.value);

//					if(n.substring(0, 2) == 'on' && ! this._xxxw) {
//						this._xxxw = true;
//						alert('dest='+dest+", src="+src+", inline="+inline+", ffox="+$.browser.mozilla);
//					}

			if(inline) {
				//-- 20091110 jal When inlining we are in trouble if domjs_ is used... The domjs_ mechanism is replaced with setDelayedAttributes in java.
				if(n.substring(0, 6) == 'domjs_') {
					alert('Unsupported domjs_ attribute in INLINE mode: ' + n);
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
			} else if(n.substring(0, 6) == 'domjs_') {
				var s = "dest." + n.substring(6) + " = " + v;
				//alert('domjs eval: '+s);
				try {
					eval(s);
				} catch(ex) {
					alert('domjs_ eval failed: ' + ex + ", js=" + s);
					throw ex;
				}

			} else if(v != "" && dest && ($.browser.msie || $.browser.webkit || ($.browser.mozilla && $.browser.majorVersion >= 9)) && n.substring(0, 2) == 'on') {
				try {
					if(v.indexOf("javascript:") == 0)
						v = $.trim(v.substring(11));
					var fntext = v.indexOf("return") >= 0 ? v : "return " + v;		// for now accept everything that at least does a return.

					let se;
					if($.browser.msie && $.browser.majorVersion < 9)
						se = new Function(fntext);
					else
						se = new Function("event", fntext);
					dest[n] = se;

				} catch(x) {
					alert('DomUI: Cannot set EVENT ' + n + " as " + v + ' on ' + dest + ": " + x);
				}
			} else if(n == 'style') { // IE workaround
				dest.style.cssText = v;
				dest.setAttribute(n, v);
			} else {
				//-- jal 20100720 handle disabled, readonly, checked differently: these are either present or not present; their value is always the same.
				if(v == "" && ("checked" == n || "selected" == n || "disabled" == n || "readonly" == n)) {
					$(dest).removeAttr(n);
				} else {
					$(dest).attr(n, v);
				}
			}
		}
		return attr;
	}
})(jQuery);
