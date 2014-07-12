/**
 * JavascriptHandler for the new, generic SearchInput2 control.
 */
WebUI.SearchPopup = function(id, inputid) {
	this._id = id;
	this._inputid = inputid;
	
	var self = this;
	$('#'+inputid).keypress(function(event) {
		self.keypressHandler(event);
	})
	.keyup(function(event) {
		self.keyUpHandler(inputid, event);
	})
	.blur(function(event) {
		self.hidePopup();
	});
};

/**
 * Handle for timer delayed actions, used for onLookupTyping event.
 */
WebUI.SearchPopup._timerID = null;

$.extend(WebUI.SearchPopup.prototype, {
	_selectedIndex: -1,

	/**
	 * Handle enter key pressed on keyPress for component with onLookupTyping listener. This needs to be executed on keyPress (was part of keyUp handling), otherwise other global return key listener (returnKeyPress handler) would fire.
	 */
	keypressHandler: function(event) {
		$.dbg('returnKeyHandler');
		if(!event){
			event = window.event;
			if (!event)
				return;
		}
		var keyCode = WebUI.normalizeKey(event);
		var isReturn = (keyCode == 13000 || keyCode == 13);
		if(! isReturn)
			return;

		var node = document.getElementById(this._inputid);
		if(!node || node.tagName.toLowerCase() != 'input')
			return;

		//cancel current _timerID
		this.cancelTimer();
		
		//Do not call upward handlers too, we do not want to trigger on value changed by return pressed.
		event.cancelBubble = true;
		if(event.stopPropagation)
			event.stopPropagation();

		//locate keyword input node
		var selectedIndex = this._selectedIndex;
		var trNode = selectedIndex < 0 ? null : $(node.parentNode).children("div.ui-srip-keyword-popup").children("div").children("table").children("tbody").children("tr:nth-child(" + selectedIndex + ")").get(0);
		if(trNode){
			//trigger click on row
			this._selectedIndex = -1;
			$(trNode).trigger('click');
		} else {
			//trigger lookupTypingDone when return is pressed
			this.lookupTypingDone();
		}
	},

	/*
	 * Executed as onkeyup event on input field that has implemented listener for onLookupTyping event.
	 * In case of return key call lookupTypingDone ajax that is transformed into onLookupTyping(done=true).
	 * In case of other key, lookupTyping funcion is called with delay of 500ms. Previuosly scheduled lookupTyping function is canceled.
	 * This cause that fast typing would not trigger ajax for each key stroke, only when user stops typing for 500ms ajax would be called by lookupTyping function.
	 */
	keyUpHandler: function(event) {
		var node = document.getElementById(this._inputid);
		if(!node || node.tagName.toLowerCase() != 'input')
			return;

		if(!event){
			event = window.event;
			if (!event)
				return;
		}
		var keyCode = WebUI.normalizeKey(event);
		var isReturn = (keyCode == 13000 || keyCode == 13);
		if (isReturn) { 										//handled by onLookupTypingReturnKeyHandler, just cancel propagation
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
		this.cancelTimer();
		var isDownArrowKey = (keyCode == 40000 || keyCode == 40);
		var isUpArrowKey = (keyCode == 38000 || keyCode == 38);
		if (isDownArrowKey || isUpArrowKey) {
			//Do not call upward handlers too, we do not want to trigger on value changed by return pressed.
			event.cancelBubble = true;
			if(event.stopPropagation)
				event.stopPropagation();

			//locate keyword input node
			var selectedIndex = this._selectedIndex;
			if(selectedIndex < 0)
				selectedIndex = 0;
			var trNode = $(node.parentNode).children("div.ui-srip-keyword-popup").children("div").children("table").children("tbody").children("tr:nth-child(" + selectedIndex + ")").get(0);
			if(trNode){
				trNode.className = "ui-keyword-popup-row";
			}
			var trNodes = $(node.parentNode).children("div.ui-srip-keyword-popup").children("div").children("table").children("tbody").children("tr");
			if (trNodes.length > 0){
				var divPopup = $(node.parentNode).children("div.ui-srip-keyword-popup").get(0);
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
				trNode = $(node.parentNode).children("div.ui-srip-keyword-popup").children("div").children("table").children("tbody").children("tr:nth-child(" + selectedIndex + ")").get(0);
				if(trNode){
					trNode.className = "ui-keyword-popop-rowsel";
				}
			} else {
				selectedIndex = 0;
			}
			this._selectedIndex = selectedIndex;
		} else {
			var self = this;
			WebUI.SearchPopup._timerID = window.setTimeout(function() {
				self.lookupTyping();
			}, 500);
		}
	},

	cancelTimer: function() {
		if(WebUI.SearchPopup._timerID) {
			//cancel already scheduled timer event
			window.clearTimeout(WebUI.SearchPopup._timerID);
			WebUI.SearchPopup._timerID = null;
		}
	},

	lookupPopupClicked : function(id) {
		var node = document.getElementById(id);
		if(!node || node.tagName.toLowerCase() != 'input') {
			return;
		}

		var selectedIndex = this._selectedIndex;
		if(selectedIndex < 0)
			selectedIndex = 0;
		var trNode = $(node.parentNode).children("div.ui-srip-keyword-popup").children("div").children("table").children("tbody").children("tr:nth-child(" + selectedIndex + ")").get(0);
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

		var oldIndex = this._selectedIndex;
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
			this._selectedIndex = newIndex;
		}
	},

	//Called only from onBlur of input node that is used for lookup typing.
	hidePopup: function() {
		var node = document.getElementById(this._inputid);
		if(!node || node.tagName.toLowerCase() != 'input')
			return;
		var divPopup = $(node.parentNode).children("div.ui-srip-keyword-popup").get();
		if (divPopup){
			$(divPopup).fadeOut(200);
		}
		//fix z-index to one saved in input node
		if ($.browser.msie){
            //IE kills event stack (click is canceled) when z index is set during onblur event handler... So, we need to postpone it a bit...
            window.setTimeout(function() { 
            	try {
            		node.parentNode.style.zIndex = node.style.zIndex;
            	} catch (e) { 
            		/*just ignore */ 
            	} 
            }, 200);
		}else{
            //Other browsers dont suffer of this problem, and we can set z index instantly
            node.parentNode.style.zIndex = node.style.zIndex;
		}
	},

	showLookupTypingPopupIfStillFocusedAndFixZIndex: function() {
		var node = document.getElementById(this._inputid);
		if(!node || node.tagName.toLowerCase() != 'input')
			return;
		var wasInFocus = node == document.activeElement;
		var qDivPopup = $(node.parentNode).children("div.ui-srip-keyword-popup");
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
					WebUI.SearchPopup.lookupRowMouseOver(event.data.nodeId, event.data.trId);
				});
			}
		}
		if (divPopup){
			$(divPopup).bind("click", {nodeId: id}, function(event) {
				WebUI.SearchPopup.lookupPopupClicked(event.data.nodeId);
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
				if (node.childNodes[i].className == 'ui-srip-waiting'){
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
				if (node.childNodes[i].className == 'ui-srip-waiting'){
					node.childNodes[i].style.display = 'none';
				}
			}
		}
	},

	lookupTyping : function() {
		var lookupField = document.getElementById(this._id);
		//check for exsistence, since it is delayed action component can be removed when action is executed.
		if (lookupField){
			var self = this;

			// Collect all input, then create input.
			var fields = new Object();
			WebUI.getInputFields(fields);
			fields.webuia = "lookupTyping";
			fields.webuic = this._id;
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
						displayWaitingTimerID = window.setTimeout(function() {
							self.displayWaiting();
						}, 500);
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
   							self.hideWaiting(parentDiv.id);
   						}
   					}
					//handle received lookupTyping component content
					self.showLookupTypingPopupIfStillFocusedAndFixZIndex();
					WebUI.doCustomUpdates();
   				},

				success :WebUI.handleResponse,
				error :WebUI.handleError
			});
		}
	},

	lookupTypingDone: function() {
		// Collect all input, then create input.
		var fields = new Object();
		WebUI.getInputFields(fields);
		fields.webuia = "lookupTypingDone";
		fields.webuic = this._id;
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
});
