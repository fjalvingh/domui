function _block() {
	// $("body").attr("style.cursor", "wait");
	// var el = document.childNodes[1].childNodes[1];
	var el = document.body;
	if (el)
		el.style.cursor = "wait";
}
function _unblock() {
	// $("body").attr("style.cursor", "default");
	// var el = document.childNodes[1].childNodes[1];
	var el = document.body;
	if (el)
		el.style.cursor = "default";
}

// $().ajaxStart(_block).ajaxStop(_unblock);

( function($) {
	$.webui = function(xml) {
		processDoc(xml);
	};
	$.expr[':'].taconiteTag = 'a.taconiteTag';

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
	;

	function processDoc(xml) {
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
	;

	function changeTagAttributes(a) {
		log("aarg=", a);
	}

	// convert string to xml document
	function convert(s) {
		var doc;
		log('attempting string to document conversion');
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
		var ok = doc && doc.documentElement
				&& doc.documentElement.tagName != 'parsererror';
		log('conversion ', ok ? 'successful!' : 'FAILED');
		if (!ok) {
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
	;

	function go(xml) {
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
			log("Redirecting- ");
			var to = xml.documentElement.getAttribute('url');
			window.location.href = to;
			return true;
		} else if (rname == 'expired') {
			var msg = 'Uw sessie is verlopen. Het scherm wordt opnieuw opgevraagd met originele gegevens.';
			var hr = window.location.href;
			for ( var i = xml.documentElement.childNodes.length; --i >= 0;) {
				var cn = xml.documentElement.childNodes[i];
				if (cn.tagName == 'msg') {
					msg = cn.textContent;
				} else if (cn.tagName == 'href')
					hr = cn.textContent;
			}
			alert(msg);
			window.location.href = hr; // Force reload
			return;
		}

		try {
			var t = new Date().getTime();
			// process the document
			process(xml.documentElement.childNodes);
			var lastTime = (new Date().getTime()) - t;
			log('Response handled in ' + lastTime + 'ms');
		} catch (e) {
			if (window.console && window.console.debug)
				window.console.debug('ERROR in xml handler:' + e, e);
			throw e;
		}
		return true;

		// -- process the commands
		function process(commands) {
			var doPostProcess = 0;
			for ( var i = 0; i < commands.length; i++) {
				if (commands[i].nodeType != 1)
					continue; // commands are elements
				var cmdNode = commands[i], cmd = cmdNode.tagName;
				if (cmd == 'eval') {
					var js = (cmdNode.firstChild ? cmdNode.firstChild.nodeValue
							: null);
					log('invoking "eval" command: ', js);
					if (js)
						$.globalEval(js);
					continue;
				}
				var q = cmdNode.getAttribute('select');
				var jq = $(q);
				if (!jq[0]) {
					log('No matching targets for selector: ', q);
					continue;
				}

				if (cmd == 'changeTagAttributes') {
					// -- Copy attributes on this tag to the target tags
					var dest = jq[0]; // Should be 1 element
					var src = commands[i];
					for ( var ai = 0, attr = ''; ai < src.attributes.length; ai++) {
						var a = src.attributes[ai], n = $.trim(a.name), v = $
								.trim(a.value);
						if (n == 'select' || n.substring(0, 2) == 'on')
							continue;
						if (n.substring(0, 6) == 'domjs_') {
							var s = "dest." + n.substring(6) + " = " + v;
							eval(s);
							continue;
						}
						if (v == '---') { // drop attribute request?
							dest.removeAttribute(n);
							continue;
						}
						if (n == 'style') { // IE workaround
							dest.style.cssText = v;
							dest.setAttribute(n, v);
						} else
							$.attr(dest, n, v);
					}
					continue;
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
					log("invoke command: $('", q, "').", cmd, '(' + arg + ')');
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
			;

			function cleanse(els) {
				for ( var i = 0, a = []; i < els.length; i++)
					if (els[i].nodeType == 1)
						a.push(els[i]);
				return a;
			}
			;

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
			;

			function handleCDATA(s) {
				var el = document.createElement(cdataWrap);
				el.innerHTML = s;
				return el;
			}
			;

			function fixTextNode(s) {
				if ($.browser.msie)
					s = s.replace(/\n/g, '\r').replace(/\s+/g, ' ');
				return document.createTextNode(s);
			}
			;

			function createElement(node) {
				var e, tag = node.tagName.toLowerCase();
				// some elements in IE need to be created with attrs inline
				if ($.browser.msie) {
					var type = node.getAttribute('type');
					if (tag == 'table'
							|| type == 'radio'
							|| type == 'checkbox'
							|| tag == 'button'
							|| (tag == 'select' && node
									.getAttribute('multiple'))) {
						e = document.createElement('<' + tag + ' '
								+ copyAttrs(null, node, true) + '>');
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
			;

			function copyAttrs(dest, src, inline) {
				for ( var i = 0, attr = ''; i < src.attributes.length; i++) {
					var a = src.attributes[i], n = $.trim(a.name), v = $
							.trim(a.value);
					// alert('attr '+n+' is '+v);
					if (inline)
						attr += (n + '="' + v + '" ');
					else if (n == 'style') { // IE workaround
						dest.style.cssText = v;
						dest.setAttribute(n, v);
					} else if (n.substring(0, 6) == 'domjs_') {
						var s = "dest." + n.substring(6) + " = " + v;
						eval(s);
						continue;
					} else if ($.browser.msie && n.substring(0, 2) == 'on') {
						// alert('event '+n+' value '+v);
						// var se = 'function(){'+v+';}';
						var se;
						if (v.indexOf('return') != -1)
							se = new Function(v);
						else
							se = new Function('return ' + v);
						// alert('event '+n+' value '+se);
						dest[n] = se;
					} else
						$.attr(dest, n, v);
				}
				return attr;
			}
			;
		}
		;
	}
	;

	$.fn.executeDeltaXML = executeXML;
})(jQuery);

var WebUI = {
	getInputFields : function(fields) {
		// Collect all input, then create input.
		var q1 = $("input").get();
		for ( var i = q1.length; --i >= 0;) {
			var t = q1[i];
			if (t.type == 'file')
				continue;
			var val = undefined;
			if (t.type == 'checkbox') {
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

			fields[sel.id] = val;
		}
		var q1 = $("textarea").get();
		for ( var i = q1.length; --i >= 0;) {
			var sel = q1[i];
			if (sel.className == 'ui-fck') {
				val = "";

				// -- Get the FCKEditor that wrapped this class,
				var fck = FCKeditorAPI.GetInstance(sel.id);
				if (fck) {
					val = fck.GetXHTML();
				}
			} else
				val = sel.value;
			fields[sel.id] = val;
		}

		return fields;
	},

	getPostURL : function() {
		var p = window.location.href;
		var ix = p.indexOf('?');
		if (ix != -1)
			p = p.substring(0, ix); // Discard query string.
		return p;
	},

	clicked : function(h, id) {
		// Collect all input, then create input.
		var fields = new Object();
		this.getInputFields(fields);
		fields.webuia = "clicked";
		fields.webuic = id;
		fields["$pt"] = DomUIpageTag;
		fields["$cid"] = DomUICID;
		WebUI.cancelPolling();

		$.ajax( {
			url :DomUI.getPostURL(),
			dataType :"text/xml",
			data :fields,
			cache :false,
			success :WebUI.handleResponse,
			error :WebUI.handleError
		});
		return false;
	},

	scall : function(id, action, fields) {
		if (!fields)
			fields = new Object();
		// Collect all input, then create input.
		this.getInputFields(fields);
		fields.webuia = action;
		fields.webuic = id;
		fields["$pt"] = DomUIpageTag;
		fields["$cid"] = DomUICID;
		WebUI.cancelPolling();

		$.ajax( {
			url :DomUI.getPostURL(),
			dataType :"text/xml",
			data :fields,
			cache :false,
			success :WebUI.handleResponse,
			error :WebUI.handleError
		});
	},

	valuechanged : function(h, id) {
		// Collect all input, then create input.
		var fields = new Object();
		this.getInputFields(fields);
		fields.webuia = "vchange";
		fields.webuic = id;
		fields["$pt"] = DomUIpageTag;
		fields["$cid"] = DomUICID;
		WebUI.cancelPolling();

		$.ajax( {
			url :DomUI.getPostURL(),
			dataType :"text/xml",
			data :fields,
			cache :false,
			success :WebUI.handleResponse,
			error :WebUI.handleError
		});
	},

	handleResponse : function(data, state) {
		if (false && window.console && window.console.debug)
			console.debug("data is ", data);
		$.webui(data);
	},
	handleError : function(request, status, exc) {
		var txt = request.responseText;
		if (document.body)
			document.body.style.cursor = 'default';
		// alert('Server error: '+status+", len="+txt.length+", val="+txt);
		if (txt.length == 0)
			txt = "De server is niet bereikbaar.";
		document.write(txt);
		window.setTimeout('document.body.style.cursor="default"', 1000);
	},

	/*
	 * IE/FF compatibility: IE only has the 'keycode' field, and it always hides
	 * all non-input like arrows, fn keys etc. FF has keycode which is used ONLY
	 * for non-input keys and charcode for input.
	 */
	normalizeKey : function(evt) {
		if (evt.charCode != undefined) {
			if (evt.keyCode > 0)
				return evt.keyCode * 1000; // Firefox high # for cursor crap
			return evt.charCode;
		}
		return evt.keyCode; // Return IE charcode
	},

	isNumberKey : function(evt) {
		var keyCode = WebUI.normalizeKey(evt);
		// alert('keycode='+evt.keyCode+", charCode="+evt.charCode+",
		// which="+evt.which);
		// alert('keyCode = '+keyCode);
		return (keyCode >= 1000 || (keyCode >= 48 && keyCode <= 57));
	},

	isFloatKey : function(evt) {
		var keyCode = WebUI.normalizeKey(evt);
		return (keyCode >= 1000 || keyCode == 0x2c || keyCode == 0x2e || (keyCode >= 48 && keyCode <= 57));
	},

	returnKeyPress : function(evt) {
		var keyCode = WebUI.normalizeKey(evt);
		if (keyCode != 13000)
			return true;
		WebUI.scall(evt.currentTarget.id, 'returnpressed');
		return false;
	},

	focus : function(id) {
		var n = document.getElementById(id);
		if (n)
			n.focus();
	},

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
			electric :true,
			step :2,
			position :null,
			cache :false
		};

		// -- Try to show the selected date from the input field.
		var dateFmt = params.inputField ? params.ifFormat : params.daFormat;
		params.date = Date.parseDate(inp.value, dateFmt);

		var cal = new Calendar(null, params.date, WebUI.onDateSelect, function(
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
			if (typeof p.inputField.onchange == "function")
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
	startPolling : function() {
		if (WebUI._pollActive)
			return;
		WebUI._pollTimer = setTimeout("WebUI.poll()", 2500);
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
		fields = new Object();
		fields.webuia = "pollasy";
		fields["$pt"] = DomUIpageTag;

		$.ajax( {
			url :window.location.href,
			dataType :"text/xml",
			data :fields,
			cache :false,
			success :WebUI.handleResponse,
			error :WebUI.handleError
		});
	},

	/** ***************** File upload stuff. **************** */
	fileUploadChange : function(e) {
		var tgt = e.currentTarget || e.srcElement;
		var vv = tgt.value.toString();

		// -- Check extensions,
		var val = tgt.getAttribute('fuallowed');
		if (val) {
			var ok = false;
			var spl = val.split(',');
			var li = vv.lastIndexOf('.');
			if (li != -1) {
				var ext = vv.substring(li + 1, vv.length).toLowerCase();
				for ( var i = 0; i < spl.length; i++) {
					if (ext == spl[i]) {
						ok = true;
						break;
					}
				}
			}
			if (!ok) {
				alert("File type not allowed");
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
			if (!jQuery.browser.msie || parseInt(jQuery.browser.version) > 7) {
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
			} else {
				// -- IE's below 8 of course have trouble. What else.
				// alert('Using Microsoft\'s flagship piece of CRAP IE -
				// circumventing the umphtiest bug.');
				iframe = document
						.createElement('<iframe name="webuiif" id="webuiif" src="#" style="display:none; width:0; height:0; border:none" onload="WebUI.ieUpdateUpload(event)">');
				document.body.appendChild(iframe);
			}
		}

		// -- replace the input with a busy indicator
		var form = tgt.parentNode; // Get form to submit to later on,

		var img = document.createElement('img');
		img.border = "0";
		img.src = DomUIThemeURL + "progressbar.gif";
		form.parentNode.insertBefore(img, form);
		form.style.display = 'none';

		// -- Target the iframe
		form.target = "webuiif"; // Fake a new thingy,
		form.submit(); // Force submit of the thingerydoo

		// alert('Upload change');
	},

	ieUpdateUpload : function(e) { // Piece of crap
		var iframe = document.getElementById('webuiif');
		var xml = iframe.contentWindow.document.XMLDocument; // IMPORTANT
																// Fucking MS
																// Crap!!!! See
																// http://p2p.wrox.com/topic.asp?whichpage=1&TOPIC_ID=62981&#153594
		WebUI.updateUpload(xml, iframe);
	},
	updateUpload : function(doc, ifr) {
		try {
			jQuery.fn.executeDeltaXML(doc);
		} catch (x) {
			alert(x);
			throw x;
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
		var h = window.open(url, name, par);
		if (!h)
			alert("Er is een popup blocker actief. Deze moet voor deze website worden uitgezet.");
		return false;
	},

	unloaded : function() {
		WebUI.sendobituary();
	},

	/**
	 * Called at page unload time, this quickly tries to send an obituary to the
	 * server. This is currently unconditional but can later be augmented to
	 * send the obituary only when the browser window closes.
	 */
	sendobituary : function() {
		var rq;
		if (window.XMLHttpRequest) {
			rq = new XMLHttpRequest();
		} else if (window.ActiveXObject) {
			rq = new ActiveXObject("Microsoft.XMLHTTP");
		} else {
			alert("Cannot send obituary (no transport)");
			return;
		}
		rq.open("GET", DomUI.getPostURL() + "?$cid=" + DomUICID
				+ "&webuia=OBITUARY&$pt=" + DomUIpageTag, false);
		rq.send(null);
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
		WebUI._dragMode = 1; // PREDRAG
		WebUI._dragNode = item;
		$(document.body).bind("mousemove", WebUI.dragMouseMove);
		$(document.body).bind("mouseup", WebUI.dragMouseUp);
		var apos = WebUI.getAbsolutePosition(item);
		WebUI._dragSourceOffset = apos;
		apos.x = evt.clientX - apos.x;
		apos.y = evt.clientY - apos.y;
		evt.preventDefault(); // Prevent ffox image dragging
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
		dv.innerHTML = source.innerHTML;
		dv.style.position = 'absolute';
		dv.style.width = source.clientWidth + "px";
		dv.style.height = source.clientHeigt + "px";
		return dv;
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
		WebUI._dropList = new Array();
		for ( var i = dl.length; --i >= 0;) {
			var drop = dl[i];
			var types = drop.getAttribute('uitypes');
			if (!types)
				continue;
			var def = new Object();
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
	 * Disable selection on the given element.
	 */
	disableSelection : function(node) {
		node.onselectstart = function() {
			return false;
		};
		node.unselectable = "on";
		node.style.MozUserSelect = "none";
		node.style.cursor = "default";
	}
};

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
	var cy = WebUI._dragLastY;
	// console.debug("Starting position det: drag Y = "+cy);
	var gravity = 0; // Prefer upward gravity
	var lastrow = null;
	var rowindex = 0;
	for ( var i = 0; i < tbody.childNodes.length; i++) {
		var tr = tbody.childNodes[i];
		if (tr.nodeName != 'TR')
			continue;
		lastrow = tr;
		var position = WebUI.getAbsScrolledPosition(tr); // Take scrolling
															// into account!!
		// console.debug('row: by='+position.by+", ey="+position.ey+",
		// type="+tr.nodeName);
		if (position) {
			// -- Is the mouse IN the Y range for this row?
			if (cy >= position.by && cy < position.ey) {
				// -- Cursor is WITHIN this node. Is it near the TOP or near the
				// BOTTOM?
				var hy = (position.by + position.ey) / 2;
				gravity = cy < hy ? 0 : 1;
				// console.debug('ACCEPTED by='+position.by+",
				// ey="+position.ey+", hy="+hy+", rowindex="+rowindex);

				return {
					index :rowindex,
					iindex :i,
					gravity :gravity,
					row :tr
				};
			}

			// -- Is the thing between this row and the PREVIOUS one?
			if (cy < position.by) {
				// -- Use this row with gravity 0 (should insert BEFORE this
				// row).
				// console.debug('ACCEPTED BEFORE node by='+position.by+",
				// ey="+position.ey+", rowindex="+rowindex);
				return {
					index :rowindex,
					iindex :i,
					gravity :0,
					row :tr
				};
			}
			// console.debug('REFUSED by='+position.by+", ey="+position.ey+",
			// rowindex="+rowindex);
		}
		rowindex++;
	}
	// console.debug("ACCEPTED last one");

	// -- If we're here we must insert at the last location
	return {
		index :rowindex,
		iindex :tbody.childNodes.length,
		gravity :1,
		row :lastrow
	};
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

	// -- To mark, we insert a ROW at the insert location and visualize that
	var tr = document.createElement('tr');
	var td = document.createElement('td');
	tr.appendChild(td);
	td.appendChild(document.createTextNode('Insert here'));
	td.className = 'ui-drp-ins';
	if (b.iindex >= body.childNodes.length)
		body.appendChild(tr);
	else
		body.insertBefore(tr, body.childNodes[b.iindex]);
	WebUI._dropRow = tr;
	WebUI._dropRowIndex = b.iindex;
},

hover : function(dz) {
	var b = this.locateBest(dz);
	// console.debug("hover: "+b.iindex+", "+b.index+", g="+b.gravity);
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
		_index :b.index
	});
	WebUI.dragReset();
}
};

var DomUI = WebUI;

$(document).ready(WebUI.handleCalendarChanges);
$(document).ajaxComplete( function() {
	WebUI.handleCalendarChanges();
});
