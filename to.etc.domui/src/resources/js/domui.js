function _block() {
	WebUI.blockUI();
}
function _unblock() {
	WebUI.unblockUI();
}
$(document).ajaxStart(_block).ajaxStop(_unblock);

( function($) {
	$.webui = function(xml) {
		processDoc(xml);
	};

	if($().jquery === "1.4.1") {
		$.expr[':'].taconiteTag = function(a) { return a.taconiteTag === 1; };
	} else {
		$.expr[':'].taconiteTag = 'a.taconiteTag';
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
	;

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
			WebUI.blockUI();
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
				if (cmd == 'eval') {
					try {
						var js = (cmdNode.firstChild ? cmdNode.firstChild.nodeValue : null);
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
				var jq = $(q);
				if (!jq[0]) {
					log('No matching targets for selector: ', q);
					continue;
				}

				if (cmd == 'changeTagAttributes') {
					try {
						// -- Copy attributes on this tag to the target tags
						var dest = jq[0]; // Should be 1 element
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
								} else {
									$.attr(dest, n, v);
								}
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
					var a = src.attributes[i], n = $.trim(a.name), v = $.trim(a.value);

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
						continue;
					} else if (dest && ($.browser.msie || $.browser.webkit) && n.substring(0, 2) == 'on') {
						try {
							// alert('event '+n+' value '+v);
							// var se = 'function(){'+v+';}';
							var se;
							if (v.indexOf('return') != -1)
								se = new Function(v);
							else
								se = new Function('return ' + v);
							// alert('event '+n+' value '+se);
							dest[n] = se;
						} catch(x) {
							alert('Cannot set EVENT: '+n+" as "+v+' on '+dest);
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
			;
		}
		;
	}
	;

	$.fn.executeDeltaXML = executeXML;
})(jQuery);

var WebUI = {
	/**
	 * Create a curried function containing a 'this' and a fixed set of elements.
	 */
	curry: function(scope, fn) {
	    var scope = scope || window;
	    var args = [];
	    for (var i=2, len = arguments.length; i < len; ++i) {
	        args.push(arguments[i]);
	    };
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
	    };
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
			if (t.type == 'file' || t.type == 'hidden') // All hidden input nodes are created directly in browser java-script and because that are filtered out from server requests.				
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

			if(val != undefined)
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
			} else {
//				if($.browser.msie) { // The MS idiots remove newlines from value....
//					val = sel.innerText;
//					//alert("inner value="+sel.innerText);
//				} else
				val = sel.value;
			}
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
	getObituaryURL: function() {
		var u = WebUI.getPostURL();
		var ix = u.lastIndexOf('.');
		if(ix < 0)
			throw "INVALID PAGE URL";
		return u.substring(0, ix)+".obit";
	},

	clicked : function(h, id, evt) {
		// Collect all input, then create input.
		var fields = new Object();
		this.getInputFields(fields);
		fields.webuia = "clicked";
		fields.webuic = id;
		fields["$pt"] = DomUIpageTag;
		fields["$cid"] = DomUICID;
		WebUI.cancelPolling();
		//-- Do not call upward handlers too.
		if(! evt)
			evt = window.event;
		if(evt) {
			evt.cancelBubble = true;
			if(evt.stopPropagation)
				evt.stopPropagation();
		}

		$.ajax( {
			url :DomUI.getPostURL(),
			dataType :"text/xml",
			data :fields,
			cache :false,
			type: "POST",
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
			type: "POST",
			success :WebUI.handleResponse,
			error :WebUI.handleError
		});
	},

	valuechanged : function(h, id) {
		// FIXME 20100315 jal Temporary fix for bug 680: if a DateInput has a value changed listener the onblur does not execute. So handle it here too.... The fix is horrible and needs generalization.
		var item = document.getElementById(id);
		if(item && (item.tagName == "input" || item.tagName == "INPUT") && item.className == "ui-di") {
			//-- DateInput control: manually call the onblur listener.
			this.dateInputRepairValueIn(item);
		}

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
			type: "POST",
			success :WebUI.handleResponse,
			error :WebUI.handleError
		});
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
		var isReturn = (keyCode == 13000 || keyCode == 13);
		var isDownArrowKey = (keyCode == 40000 || keyCode == 40);
		var isUpArrowKey = (keyCode == 38000 || keyCode == 38);
		if (isReturn || isDownArrowKey || isUpArrowKey) {
			//Do not call upward handlers too, we do not want to trigger on value changed by return pressed.
			event.cancelBubble = true;
			if(event.stopPropagation)
				event.stopPropagation();
		}
		if (isReturn){
			//handle return key 
			//locate keyword input node 
			var selectedIndex = WebUI.getKeywordPopupSelectedRowIndex(node);
			var trNode = $(node.parentNode).children("div.ui-lui-keyword-popup").children("div").children("table").children("tbody").children("tr:nth-child(" + selectedIndex + ")").get(0);
			if(trNode){
				WebUI.clicked(trNode, trNode.id, null);
			} else {
				//trigger lookupTypingDone when return is pressed
				WebUI.lookupTypingDone(id);
			}
		}
		else if(isDownArrowKey || isUpArrowKey){
			//locate keyword input node
			var selectedIndex = WebUI.getKeywordPopupSelectedRowIndex(node);
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
			};
		}
		return 0;
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
		node.parentNode.style.zIndex = node.style.zIndex;
	},

	showLookupTypingPopupIfStillFocusedAndFixZIndex: function(id) {
		var node = document.getElementById(id);
		if(!node || node.tagName.toLowerCase() != 'input')    
			return;
		var wasInFocus = node == document.activeElement; 
		var qDivPopup = $(node.parentNode).children("div.ui-lui-keyword-popup");
		if (qDivPopup.length > 0){
			var divPopup = qDivPopup.get(0); 
			//must be set manually from javascript because bug in domui, parent attribute updated from child node is not rendered in response
			node.parentNode.style.zIndex = divPopup.style.zIndex;
		}else{
			//fix z-index to one saved in input node
			node.parentNode.style.zIndex = node.style.zIndex;
		}
		if (wasInFocus){
			//show popup in case that input field still has focus
			$(divPopup).show();
		}
		
		var trNods = $(qDivPopup).children("div").children("table").children("tbody").children("tr");
		if (trNods && trNods.length > 0) {
			for(var i=0; i < trNods.length; i++) {
				var trNod = trNods.get(i);
				trNod.setAttribute("onmouseover","WebUI.lookupRowMouseOver('" + id + "', '" + trNod.id + "');");
			}
		}

		divPopup.setAttribute("onclick","WebUI.lookupPopupClicked('" + id + "');");
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
			var fields = new Object();
			this.getInputFields(fields);
			fields.webuia = "lookupTyping";
			fields.webuic = id;
			fields["$pt"] = DomUIpageTag;
			fields["$cid"] = DomUICID;
			WebUI.cancelPolling();
			var displayWaitingTimerID = null;

			$.ajax( {
				url :DomUI.getPostURL(),
				dataType :"text/xml",
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
   				},

				success :WebUI.handleResponse,
				error :WebUI.handleError
			});
		}
	},	
	lookupTypingDone : function(id) {
		// Collect all input, then create input.
		var fields = new Object();
		this.getInputFields(fields);
		fields.webuia = "lookupTypingDone";
		fields.webuic = id;
		fields["$pt"] = DomUIpageTag;
		fields["$cid"] = DomUICID;
		WebUI.cancelPolling();

		$.ajax( {
			url :DomUI.getPostURL(),
			dataType :"text/xml",
			data :fields,
			cache :false,
			type: "POST",
			success :WebUI.handleResponse,
			error :WebUI.handleError
		});
	},	
	handleResponse : function(data, state) {
		WebUI._asyalerted = false;
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
	_asyalerted: false,
	handleErrorAsy : function(request, status, exc) {
		if(WebUI._asyalerted)
			return;
		WebUI._asyalerted = true;

		var txt = request.responseText;
		if (document.body)
			document.body.style.cursor = 'default';
		// alert('Server error: '+status+", len="+txt.length+", val="+txt);
		if (txt.length == 0)
			txt = "De server is niet bereikbaar.";
		else if(txt.length > 200)
			txt = txt.substring(0, 200);
		alert("Automatische server update mislukt: "+txt);
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
		//alert('keycode='+evt.keyCode+", charCode="+evt.charCode+", which="+evt.which+", norm="+keyCode);
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
		try{
			if (n)
				n.focus();
		} catch (e) {
			//just ignore
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

	dateInputRepairValueIn: function(c) {
		if(! c)
			return;
		var val = c.value;

		if(! val || val.length == 0) // Nothing to see here, please move on.
			return;
		Calendar.__init();

		//-- Try to decode then reformat the date input
		var fmt = Calendar._TT["DEF_DATE_FORMAT"];
		try {
			if(! WebUI.hasSeparators(val)) {
				val = WebUI.insertDateSeparators(val, fmt);
				var res = Date.parseDate(val, fmt);
				c.value = res.print(fmt);
			} else {
				//-- Only parse the input to see if it parses.
				var res = Date.parseDate(val, fmt);
			}
		} catch(x) {
			alert(Calendar._TT["INVALID"]);
		}
	},

	/**
	 * Returns T if the string has separator chars (anything else than letters and/or digits).
	 */
	hasSeparators: function(str) {
		for(var i = str.length; --i >= 0;) {
			var c= str.charAt(i);
			if(!( ( c >= 'A' && c <= 'Z') || (c >='a' && c <= 'z') || (c >= '0' && c <= '9')))
				return true;
		}
		return false;
	},

	insertDateSeparators: function(str, fmt) {
		var b = fmt.match(/%./g); // Split format items
		var len = str.length;
		var ylen;
		if(len == 8)
			ylen = 4;
		else if(len == 6)
			ylen = 2;
		else
			throw "date invalid";

		//-- Edit the string according to the pattern,
		var res = "";
		for(var fix= 0; fix < b.length; fix++) {
			if(res.length != 0)
				res = res + '-';				// Just a random separator.
			switch(b[fix]) {
				default:
					throw "date invalid";
				case "%d":
		    	case "%e":
			    case "%m":
		    		//-- 2-digit day or month. Copy.
		    		res += str.substring(0, 2);
		    		str = str.substring(2);
		    		break;

			    case '%y': case '%Y':
			    	//-- 2- or 4 digit year,
		    		res += str.substring(0, ylen);
		    		str = str.substring(ylen);
			    	break;
			}
		}
		return res;
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
		WebUI._pollActive = true;
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
			global: false, // jal 20091015 prevent block/unblock on polling call.
			success :WebUI.handleResponse,
			error :WebUI.handleErrorAsy
		});
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
					if (ext == spl[i] || "*" == spl[i]) {
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
		var xml = iframe.contentWindow.document.XMLDocument; // IMPORTANT Fucking MS Crap!!!! See
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
		try {
			var h = window.open(url, name, par);
		} catch(x) {
			alert("Got popup exception: "+x);
		}
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
			//MVE make this optional.
			WebUI._dragNode.style.display='block';
			WebUI._dragNode.style.visibility='hidden';
			
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
		dv.style.width = source.clientWidth + "px";
		dv.style.height = source.clientHeight + "px";
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
	},

	/** ***************** ScrollableTabPanel stuff. **************** */
	_ignoreScrollClick: 0,

	scrollLeft : function(bLeft) {
		if(this._ignoreScrollClick != 0)
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
			$('.ui-stab-scrl-right', scrlNavig).css('visibility','visible');
			if(disa){
				$(bLeft).css('visibility','hidden');
			}
			me._ignoreScrollClick--;
		});
	},

	scrollRight : function(bRight) {
		if(this._ignoreScrollClick != 0)
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
			$('.ui-stab-scrl-left', scrlNavig).css('visibility','visible');
			if (disa){
				$(bRight).css('visibility','hidden');
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
				$('.ui-stab-scrl-right',scrlNavig).css('visibility','visible');
			}else{
				$('.ui-stab-scrl-right',scrlNavig).css('visibility','hidden');
			}
			if (leftM < 0){
				$('.ui-stab-scrl-left',scrlNavig).css('visibility','visible');
			}else{
				$('.ui-stab-scrl-left',scrlNavig).css('visibility','hidden');
			}
		}else{
			$('.ui-stab-scrl-left',scrlNavig).css('visibility','hidden');
			$('.ui-stab-scrl-right',scrlNavig).css('visibility','hidden');
			$('ul', scrlNavig).animate({marginLeft: 0}, 400, 'swing');
		}
	},
	
	/** ***************** Stretch elemnt height. Must be done via javascript. **************** */
	stretchHeight : function(elemId) {
		var elem = document.getElementById(elemId);
		var elemHeight = $(elem).height();
		var totHeight = 0;
		$(elem).siblings().each(function(index, node) {
			//do not count target element and other siblings positioned absolute or relative to parent in order to calculate how much space is actually taken / available
			if (node != elem && $(node).css('position') == 'static' && $(node).css('float') == 'none'){
				totHeight += node.offsetHeight;
			}
		});
		$(elem).height($(elem).parent().height() - totHeight);
	},
	
	/** *************** Debug thingy - it can be used internaly for debuging javascript ;) ************** */
	debug : function(debugId, posX, posY, debugInfoHtml) {
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

	backgroundPrint: function(url) {
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

			$(div).html('<iframe id="'+frmname+'" name="'+frmname+'" src="'+url+'">');

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
				frm.document.write('<html></head></head><body style="margin:0px;"><form><textarea style="width:99%; height:99%" wrap="virtual">');
				frm.document.write(textData);
				frm.document.write('</textarea></form></body></html>');
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
//			console.debug("double ", e);

//			WebUI._NOMOVE = true;
			//-- Send a DEBUG command to the server, indicating the current node below the last mouse move....
			var id = WebUI.nearestID(WebUI._debugMouseTarget);
//			console.debug("idis  "+id+", m="+WebUI._debugMouseTarget);
			if(! id)
				return;

//			console.debug("Escape doublepress on ID="+id);
			WebUI.scall(id, "DEVTREE", {});
		});
		$(document.body).bind("mousemove", function(e) {
//			if(WebUI._NOMOVE)
//				return;
//			console.debug("move ", e);
			WebUI._debugMouseTarget = e.srcElement || e.originalTarget;
			
		});
		
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
	//TODO MVE should return maxCollumn
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

//MVE hier wordt de insert getoond. Deze moet dus ook per cel gaan doen. 
renderTween : function(dz, b) {
	var body = dz._tbody;

	// -- To mark, we insert a ROW at the insert location and visualize that
	var tr = document.createElement('tr');
	//b.colIndex should define the correct collumn
	var colIndex = b.colIndex;
	this.appendPlaceHolderCell(tr, colIndex == 0);
	this.appendPlaceHolderCell(tr, colIndex == 1);
	this.appendPlaceHolderCell(tr, colIndex == 2);
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
		td.appendChild(document.createTextNode('Insert here'));
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
		_index :b.index,
		_colIndex :b.colIndex
	});
	WebUI.dragReset();
}
};

var DomUI = WebUI;

$(document).ready(WebUI.handleCalendarChanges);
if(DomUIDevel)
	$(document).ready(WebUI.handleDevelopmentMode);
$(document).ajaxComplete( function() {
	WebUI.handleCalendarChanges();
});
