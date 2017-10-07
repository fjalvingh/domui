/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
namespace WebUIStatic {
	function getInputFields(fields) {
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
	}

	var _inputFieldList: [];

	/**
	 * This registers a non-html control as a source of input for {@link getInputFields}. The control
	 * must have a method "getInputFields(fields: Map)" which defines the inputs to send for the control.
	 */
	function registerInputControl(id, control) {
		var list = WebUI._inputFieldList;
		for(var i = list.length; --i >= 0;) {
			var item = list[i];
			if(item.id == id) {
				item.control = control;
				return;
			}
		}
		list.push({id: id, control:control});
	}

	function findInputControl(id) {
		//-- return registered component by id, if not found returns null
		var list = WebUI._inputFieldList;
		for(var i = list.length; --i >= 0;) {
			var item = list[i];
			if(item.id == id && document.getElementById(item.id)) {
				return item.control;
			}
		}
		return null;
	}

	function clicked(h, id, evt) {
		//-- Trigger the before-clicked event on body
		$(document.body).trigger("beforeclick", $("#"+id), evt);

		// Collect all input, then create input.
		var fields : any = {};
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
	}

	function prepareAjaxCall(id, action, fields) {
		if (!fields)
			fields = new Object();
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
	}

	function scall(id, action, fields) {
		var call = WebUI.prepareAjaxCall(id, action, fields);
		WebUI.cancelPolling();
		$.ajax(call);
	}

	function jsoncall(id, fields) {
		if (!fields)
			fields = new Object();
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
	}

	/**
	 * Send a server request to a component, which will be handled by that component's componentHandleWebAction
	 * method. The json data is sent as a string parameter with the name "json"; the response is handled as a normal
	 * DomUI page request: the page is updated and any delta is returned.
	 * @returns void
	 */
	function sendJsonAction(id, action, json) {
		var fields = new Object();
		fields.json = JSON.stringify(json);
		WebUI.scall(id, action, fields);
	}

	/**
	 * Call a JSON handler on a component. This is "out of bound": the current browser state of
	 * the page is /not/ sent, and the response must be a JSON document which will be the return
	 * value of this function.
	 *
	 * @param id
	 * @param fields
	 * @returns
	 */
	function callJsonFunction(id, action, fields) {
		if (!fields)
			fields = new Object();
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
	}

	/**
	 * Send a server request to a component, which will be handled by that component's componentHandleWebAction
	 * method. The json data is sent as a string parameter with the name "json"; the response is handled as a normal
	 * DomUI page request: the page is updated and any delta is returned.
	 * @returns void
	 */
	function sendJsonAction(id, action, json) {
		var fields = new Object();
		fields.json = JSON.stringify(json);
		WebUI.scall(id, action, fields);
	}

	/**
	 * Call a JSON handler on a component. This is "out of bound": the current browser state of
	 * the page is /not/ sent, and the response must be a JSON document which will be the return
	 * value of this function.
	 *
	 * @param id
	 * @param fields
	 * @returns
	 */
	function callJsonFunction(id, action, fields) {
		if (!fields)
			fields = new Object();
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
	}

	function jsoncall(id, fields) {
		if (!fields)
			fields = new Object();
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
	}

	function clickandchange(h, id, evt) {
		//-- Do not call upward handlers too.
		if(! evt)
			evt = window.event;
		if(evt) {
			evt.cancelBubble = true;
			if(evt.stopPropagation)
				evt.stopPropagation();
		}
		WebUI.scall(id, 'clickandvchange');
	}

	function valuechanged(h, id) {
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
			dataType :"*",
			data :fields,
			cache :false,
			type: "POST",
			success :WebUI.handleResponse,
			error :WebUI.handleError
		});
	}


}
