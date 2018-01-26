var WebUI;
if(! WebUI)
	WebUI = {};

WebUI.K_RETURN = 13;
WebUI.K_UP = 38;
WebUI.K_DOWN = 40;
WebUI.K_LEFT = 37;
WebUI.K_RIGHT = 39;
WebUI.K_ESC = 27;
WebUI.K_HOME = 36;
WebUI.K_END = 35;
WebUI.K_PGUP = 33;
WebUI.K_PGDN = 34;
WebUI.K_TAB = 9;

/**
 * JavascriptHandler for the new, generic SearchInput2 control.
 */
WebUI.SearchPopup = function(id, inputid) {
	this._id = id;
	this._inputid = inputid;
	this._popup = null;

	var self = this;
	$('#' + inputid).keypress(function(event) {
		self.keypressHandler(event);
	})
	.keydown(function(event) {
		self.keyUpHandler(event);
	})
	.focus(function() {
		self.handleFocus();
	})
	.blur(function(event) {
		self.handleBlur();
	});
};

/**
 * Handle for timer delayed actions, used for raising onLookupTyping event.
 */
WebUI.SearchPopup._timerID = null;

$.extend(WebUI.SearchPopup.prototype, {
	_selectedIndex: -1,

	/**
	 * Handle enter key pressed on keyPress for component with onLookupTyping listener. This needs to be executed on keyPress (was part of keyUp handling), otherwise other global return key listener (returnKeyPress handler) would fire.
	 */
	keypressHandler: function(event) {
		// console.log("SearchPopup.keypressHandler: "+event.which);
		var key = event.which;
		var selectedTrNode = null;
		switch(key) {
			default:
				return;

			case WebUI.K_RETURN:
				//locate keyword input node
				var node = document.getElementById(this._inputid);
				if(node && node.tagName.toLowerCase() === 'input') {
					var selection = $(node.parentNode).find("tr.ui-ssop-selected");
					if(selection && selection.length === 1) {
						selectedTrNode = selection.get(0);
					}
				}
				event.preventDefault();
				break;
		}
		var node = document.getElementById(this._inputid);
		if(!node || node.tagName.toLowerCase() !== 'input')
			return;

		this.cancelEvent(event);

		if(selectedTrNode) {
			$(this._popup).trigger('click'); //does server round-trip and delivers SelectOnePanel changed value
			return;
		}

		//-- Send a "returnPressed" event.
		this.lookupTypingDone();
		return false;
	},

	/*
	 * Executed as onkeyup event on input field that has implemented listener for onLookupTyping event.
	 * In case of return key call lookupTypingDone ajax that is transformed into onLookupTyping(done=true).
	 * In case of other key, lookupTyping function is called with delay of 500ms. Previuosly scheduled lookupTyping function is canceled.
	 * This cause that fast typing would not trigger ajax for each key stroke, only when user stops typing for 500ms ajax would be called by lookupTyping function.
	 */
	keyUpHandler: function(event) {
		// console.log("SearchPopup.keyup: "+event.which);
		switch(event.which) {
			//-- The following just edit, but should not cause a keytyped event.
			case WebUI.K_END:
			case WebUI.K_HOME:
			case WebUI.K_LEFT:
			case WebUI.K_RIGHT:
			case WebUI.K_RETURN:
			case WebUI.K_UP:
			case WebUI.K_DOWN:
			case WebUI.K_TAB:
				return;

			case 229:		// added by some browsers when characters are repeatedly pressed, indicating that the Input Monitor is busy
				return;

			case WebUI.K_ESC:
				//esc clears the input and selection
				var node = document.getElementById(this._inputid);
				if(node && node.tagName.toLowerCase() === 'input') {
					node.value = "";
					if(this._popup) {
						var jsPopupRef = WebUI.findInputControl(this._popup.id);
						if(jsPopupRef) {
							jsPopupRef.selectNode(-1);
						}
					}
				}
			//fall trough
			default:
				var self = this;
				this.cancelLookupTypingTimer();
				WebUI.SearchPopup._timerID = window.setTimeout(function() {
					self.lookupTyping();
				}, 500);

				return true;
		}
	},

	cancelLookupTypingTimer: function() {
		if(WebUI.SearchPopup._timerID) {
			//cancel already scheduled timer event
			window.clearTimeout(WebUI.SearchPopup._timerID);
			WebUI.SearchPopup._timerID = null;
		}
	},

	/**
	 * If the input is re-entered: show the last popup shown or message, if present.
	 */
	handleFocus: function() {
		$('#' + this._id + " .ui-ssop").fadeIn(200);
		var parentNode = document.getElementById(this._id).parentNode;

		// parentNode.style.zIndex = 10;
		// $('#' + parentNode.id + " .ui-srip-message").css("z-index", "999");
		$('#' + parentNode.id + " .ui-srip-message").fadeIn(200);
		// console.log("focus");
	},

	/**
	 * When the input is left remove any popup visible.
	 */
	handleBlur: function() {
		// console.log("blur");
		//-- 1. If we have a popup panel-> fade it out,
		var selectOnePanel = $('#' + this._id + " .ui-ssop");
		selectOnePanel.fadeOut(200);
		// var comp = selectOnePanel.data('component');
		// if(comp)
		// 	comp.handleBlur();

		//-- 2. If we have a message-> fade it out,

		var parentNode = document.getElementById(this._id).parentNode;
		$('#' + parentNode.id + " .ui-srip-message").fadeOut(200);

		//following is needed to fix situations when lookups are under each other -> inputs get over previous popups
		var node = document.getElementById(this._inputid);
		if(!node || node.tagName.toLowerCase() !== 'input')
			return;		//fix z-index to one saved in input node
		if($.browser.msie) {
			//IE kills event stack (click is canceled) when z index is set during onblur event handler... So, we need to postpone it a bit...
			window.setTimeout(function() {
				try {
					node.parentNode.style.zIndex = node.style.zIndex;
				} catch(e) {
					//just ignore
				}
			}, 200);
		} else {
			//Other browsers don't suffer of this problem, and we can set z index instantly
			node.parentNode.style.zIndex = node.style.zIndex;
		}
	},

	showLookupTypingPopupIfStillFocusedAndFixZIndex: function() {
		var node = document.getElementById(this._inputid);
		if(!node || node.tagName.toLowerCase() !== 'input')
			return;
		var wasInFocus = node === document.activeElement;
		var qDivPopup = $(node.parentNode).children("div.ui-ssop");
		var divPopup = null;
		if(qDivPopup.length > 0) {
			divPopup = qDivPopup.get(0);
			//z-index correction must be set manually from javascript (because some bug in IE7 -> if set from domui renders incorrectly until page is refreshed?)
			divPopup.style.zIndex = node.style.zIndex + 1;
			node.parentNode.style.zIndex = divPopup.style.zIndex;
		} else {
			//fix z-index to one saved in input node
			node.parentNode.style.zIndex = node.style.zIndex;
		}

		if(divPopup) {
			this._popup = divPopup;
			if(wasInFocus) {
				//show popup in case that input field still has focus
				$(divPopup).show();
			}
		}
	},

	/*
	 * In case of longer waiting for lookupTyping ajax response show waiting animated marker.
	 * Function is called with delay of 500ms from ajax.beforeSend method for lookupTyping event.
	 */
	displayWaiting: function() {
		$('#' + this._id).addClass("is-loading");

		// var waitdiv = this._waitDiv;
		// if(waitdiv)
		// 	return;
		//
		// waitdiv = $('<div class="ui-srip-waiting"><div></div></div>');
		// $('#' + this._id).append(waitdiv);
		// this._waitDiv = waitdiv;
	},

	/*
	 * Hiding waiting animated marker that was shown in case of longer waiting for lookupTyping ajax response.
	 * Function is called from ajax.completed method for lookupTyping event.
	 */
	hideWaiting: function() {
		$('#' + this._id).removeClass("is-loading");
		// var waitdiv = this._waitDiv;
		// if(!waitdiv)
		// 	return;
		// delete this._waitDiv;
		//
		// $(waitdiv).remove();
	},

	lookupTyping: function() {
		var lookupField = document.getElementById(this._id);
		//check for existence, since it is delayed action component can be removed when action is executed.
		if(lookupField) {
			var self = this;

			var showWaitingTimerId = null;

			var axaj = WebUI.prepareAjaxCall(this._id, "lookupTyping");

			axaj.beforeSend = function() {
				var parentDiv = lookupField.parentNode;
				if(parentDiv) {
					showWaitingTimerId = window.setTimeout(function() {
						self.displayWaiting();
					}, 500);
				}
			};
			axaj.global = false;

			axaj.complete = function() {
				// Handle the local complete event
				if(showWaitingTimerId) {
					window.clearTimeout(showWaitingTimerId);
					showWaitingTimerId = null;
				}
				self.hideWaiting();

				//handle received lookupTyping component content
				self.showLookupTypingPopupIfStillFocusedAndFixZIndex();
				WebUI.doCustomUpdates();
			};
			$.ajax(axaj);
		}
	},

	lookupTypingDone: function() {
		// Collect all input, then create input.
		WebUI.scall(this._id, "lookupTypingDone");
	},

	cancelEvent: function(evt) {
		this.cancelLookupTypingTimer();
		evt.preventDefault();
		evt.cancelBubble = true;
		if(evt.stopPropagation)
			evt.stopPropagation();
	}
});

/*** SelectOnePanel ***/
WebUI.SelectOnePanel = function(id, inputid) {
	this._id = id;
	this._inputid = inputid;
	this._selectedIndex = -1;

	// var self = this;
	// $('#'+id).on('click', function() {
	// 	self._selected = true;
	// 	console.log("click event on selectOnePanel");
	// });

	var node = inputid ? $('#' + inputid) : $(document.body);

	var self = this;
	node.keyup(function(event) {
		self.keyUpHandler(event);
	});
	this.attachHovers();
	WebUI.registerInputControl(id, this);
};

$.extend(WebUI.SelectOnePanel.prototype, {
	/**
	 * Attach a hover function to each selection node. The hover function will highlight and
	 * select the node hovered over. We cannot use :hover to handle that because that would
	 * confuse the keyboard and mouse style of selecting.
	 */
	attachHovers: function() {
		$('#' + this._id + " tr.ui-ssop-row").bind("mouseover", $.proxy(this.mouseOverHandler, this));
	},

	getInputField: function() {
		return this._selectedIndex;
	},

	mouseOverHandler: function(event) {
		var node = $(event.target).closest(".ui-ssop-row");
		if(node.length === 0)
			return;
		var index = $(node).index();							// Find # in list
		this.selectNode(index);									// Select the new node.
	},

	selectNode: function(index) {
		var trs = $('#' + this._id + " tr.ui-ssop-row");			// Find all rows
		if(index < -1 || index > trs.length)
			return;

		var selectedIndex = this._selectedIndex;
		if(selectedIndex >= 0 && selectedIndex < trs.length) {
			$(trs[selectedIndex]).removeClass("ui-ssop-selected");
		}
		if(index >= 0) {
			$(trs[index]).addClass("ui-ssop-selected");
		}
		this._selectedIndex = index;
		//console.debug("SelectOnePanel.selectNode: "+index);
	},

	keyUpHandler: function(event) {
		//console.debug("SelectOnePanel.keyUpHandler: "+event.which);
		var direction;
		switch(event.which) {
			default:
				return;

			case WebUI.K_DOWN:
				direction = 1;
				break;

			case WebUI.K_UP:
				direction = -1;
				break;
		}

		var selectedIndex = this._selectedIndex;
		selectedIndex += direction;
		var trs = $('#' + this._id + " tr.ui-ssop-row");		// Find all rows

		//when rotating, we also use -1 for no selection -> its usage is when enter is pressed -> it opens popup dialog with preselection
		//it also allows users to continue typing and narrowing results
		if(selectedIndex < -1) {
			selectedIndex = trs.length - 1;
		} else if(selectedIndex >= trs.length) {
			selectedIndex = -1;
		}
		this.selectNode(selectedIndex);

		this.cancelEvent(event);
	},

	cancelEvent: function(evt) {
		evt.preventDefault();
		evt.cancelBubble = true;
		if(evt.stopPropagation)
			evt.stopPropagation();
	}

});
