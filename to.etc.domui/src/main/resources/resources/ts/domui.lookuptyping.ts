/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.ts" />
//import WebUI from "domui.webui.util";

namespace WebUI {
	/**
	 * can be set to true from server code with appendJavaScript so that the expired messages will not show and
	 * block effortless refresh on class reload. Configurable in .developer.properties domui.hide-expired-alert.
	 */
	export let _hideExpiredMessage = false;

	/**
	 * When this is > 0, this keeps any page "alive" by sending an async
	 */
	let _keepAliveInterval = 0;

	export function setHideExpired() : void {
		_hideExpiredMessage = true;
	}


	/**
	 * Handle enter key pressed on keyPress for component with onLookupTyping listener. This needs to be executed on keyPress (was part of keyUp handling), otherwise other global return key listener (returnKeyPress handler) would fire.
	 */
	export function onLookupTypingReturnKeyHandler(id, event) : void {
		let node = document.getElementById(id);
		if(!node || node.tagName.toLowerCase() != 'input')
			return;

		if(!event){
			event = window.event;
			if (!event)
				return;
		}

		let keyCode = WebUI.normalizeKey(event);
		let isReturn = (keyCode == 13000 || keyCode == 13);

		if (isReturn) {
			//cancel current scheduledOnLookupTypingTimerID
			if (scheduledOnLookupTypingTimerID){
				//cancel already scheduled timer event
				window.clearTimeout(scheduledOnLookupTypingTimerID);
				scheduledOnLookupTypingTimerID = null;
			}
			//Do not call upward handlers too, we do not want to trigger on value changed by return pressed.
			event.cancelBubble = true;
			if(event.stopPropagation)
				event.stopPropagation();

			//locate keyword input node
			let selectedIndex = getKeywordPopupSelectedRowIndex(node);
			let trNode = selectedIndex < 0 ? null : $(node.parentNode).children("div.ui-lui-keyword-popup").children("div").children("table").children("tbody").children("tr:nth-child(" + selectedIndex + ")").get(0);
			if(trNode){
				//trigger click on row
				setKeywordPopupSelectedRowIndex(node, -1);
				$(trNode).trigger('click');
			} else {
				//trigger lookupTypingDone when return is pressed
				lookupTypingDone(id);
			}
		}
	}

	/**
	 * Handle for timer delayed actions, used for onLookupTyping event.
	 */
	let scheduledOnLookupTypingTimerID : number = null;

	/*
	 * Executed as onkeyup event on input field that has implemented listener for onLookupTyping event.
	 * In case of return key call lookupTypingDone ajax that is transformed into onLookupTyping(done=true).
	 * In case of other key, lookupTyping funcion is called with delay of 500ms. Previuosly scheduled lookupTyping export function is canceled.
	 * This cause that fast typing would not trigger ajax for each key stroke, only when user stops typing for 500ms ajax would be called by lookupTyping function.
	 */
	export function scheduleOnLookupTypingEvent(id, event) : void {
		let node = document.getElementById(id);
		if(!node || node.tagName.toLowerCase() != 'input')
			return;

		if(!event) {
			event = window.event;
			if(!event)
				return;
		}
		let keyCode = WebUI.normalizeKey(event);
		let isReturn = (keyCode == 13000 || keyCode == 13);
		if(isReturn) { //handled by onLookupTypingReturnKeyHandler, just cancel propagation
			event.cancelBubble = true;
			if(event.stopPropagation)
				event.stopPropagation();
			return;
		}

		let isLeftArrowKey = (keyCode == 37000 || keyCode == 37);
		let isRightArrowKey = (keyCode == 39000 || keyCode == 39);
		if(isLeftArrowKey || isRightArrowKey) {
			//in case of left or right arrow keys do nothing
			return;
		}
		if(scheduledOnLookupTypingTimerID) {
			//cancel already scheduled timer event
			window.clearTimeout(scheduledOnLookupTypingTimerID);
			scheduledOnLookupTypingTimerID = null;
		}
		let isDownArrowKey = (keyCode == 40000 || keyCode == 40);
		let isUpArrowKey = (keyCode == 38000 || keyCode == 38);
		if(isDownArrowKey || isUpArrowKey) {
			//Do not call upward handlers too, we do not want to trigger on value changed by return pressed.
			event.cancelBubble = true;
			if(event.stopPropagation)
				event.stopPropagation();

			//locate keyword input node
			let selectedIndex = getKeywordPopupSelectedRowIndex(node);
			if(selectedIndex < 0)
				selectedIndex = 0;
			let trNode = $(node.parentNode).children("div.ui-lui-keyword-popup").children("div").children("table").children("tbody").children("tr:nth-child(" + selectedIndex + ")").get(0);
			if(trNode) {
				trNode.className = "ui-keyword-popup-row";
			}
			let trNodes = $(node.parentNode).children("div.ui-lui-keyword-popup").children("div").children("table").children("tbody").children("tr");
			if(trNodes.length > 0) {
				let divPopup = $(node.parentNode).children("div.ui-lui-keyword-popup").get(0);
				if(divPopup) {
					$(divPopup).fadeIn(300);
					//must be set due to IE bug in rendering
					(node.parentNode as HTMLElement).style.zIndex = divPopup.style.zIndex;
				}
				if(isDownArrowKey) {
					selectedIndex++;
				} else {
					selectedIndex--;
				}
				if(selectedIndex > trNodes.length) {
					selectedIndex = 0;
				}
				if(selectedIndex < 0) {
					selectedIndex = trNodes.length;
				}
				trNode = $(node.parentNode).children("div.ui-lui-keyword-popup").children("div").children("table").children("tbody").children("tr:nth-child(" + selectedIndex + ")").get(0);
				if(trNode) {
					trNode.className = "ui-keyword-popop-rowsel";
				}
			} else {
				selectedIndex = 0;
			}
			setKeywordPopupSelectedRowIndex(node, selectedIndex);
		} else {
			scheduledOnLookupTypingTimerID = window.setTimeout("WebUI.lookupTyping('" + id + "')", 500);
		}
	}

	export function getKeywordPopupSelectedRowIndex(keywordInputNode) : number {
		let selectedIndexInput = $(keywordInputNode.parentNode).children("input:hidden").get(0);
		if (selectedIndexInput instanceof HTMLInputElement) {
			if (selectedIndexInput.value && selectedIndexInput.value != ""){
				return parseInt(selectedIndexInput.value);
			}
		}
		return -1;
	}

	export function setKeywordPopupSelectedRowIndex(keywordInputNode, intValue) : void {
		let selectedIndexInput = $(keywordInputNode.parentNode).children("input:hidden").get(0) as HTMLInputElement;
		if (!selectedIndexInput){
			selectedIndexInput = document.createElement("input");
			selectedIndexInput.setAttribute("type","hidden");
			$(keywordInputNode.parentNode).append($(selectedIndexInput));
		}
		selectedIndexInput.value = intValue;
	}

	export function lookupPopupClicked(id : string) {
		let node = document.getElementById(id);
		if(!node || node.tagName.toLowerCase() != 'input') {
			return;
		}

		let selectedIndex = getKeywordPopupSelectedRowIndex(node);
		if(selectedIndex < 0)
			selectedIndex = 0;
		let trNode = $(node.parentNode).children("div.ui-lui-keyword-popup").children("div").children("table").children("tbody").children("tr:nth-child(" + selectedIndex + ")").get(0);
		if(trNode){
			WebUI.clicked(trNode, trNode.id, null);
		}
	}

	export function lookupRowMouseOver(keywordInputId, rowNodeId) : void {
		let keywordInput = document.getElementById(keywordInputId);
		if(!keywordInput || keywordInput.tagName.toLowerCase() != 'input') {
			return;
		}

		let rowNode = document.getElementById(rowNodeId);
		if(!rowNode || rowNode.tagName.toLowerCase() != 'tr') {
			return;
		}

		let oldIndex = getKeywordPopupSelectedRowIndex(keywordInput);
		if(oldIndex < 0)
			oldIndex = 0;

		let trNodes = $(rowNode.parentNode).children("tr");
		let newIndex = 0;
		for(let i = 1; i <= trNodes.length; i++){
			if (rowNode == trNodes.get(i-1)) {
				newIndex = i;
				break;
			}
		}

		if (oldIndex != newIndex){
			let deselectRow = $(rowNode.parentNode).children("tr:nth-child(" + oldIndex + ")").get(0);
			if (deselectRow){
				deselectRow.className = "ui-keyword-popop-row";
			}
			rowNode.className = "ui-keyword-popop-rowsel";
			setKeywordPopupSelectedRowIndex(keywordInput, newIndex);
		}
	}

	//Called only from onBlur of input node that is used for lookup typing.
	export function hideLookupTypingPopup(id : string) : void {
		let node = document.getElementById(id);
		if(!node || node.tagName.toLowerCase() != 'input')
			return;
		let divPopup = $(node.parentNode).children("div.ui-lui-keyword-popup").get();
		if (divPopup){
			$(divPopup).fadeOut(200);
		}
		//fix z-index to one saved in input node
		if($.browser.msie) {
			//IE kills event stack (click is canceled) when z index is set during onblur event handler... So, we need to postpone it a bit...
			window.setTimeout(function() {
				try {
					(node.parentNode as HTMLElement).style.zIndex = node.style.zIndex;
				} catch(e) { /*just ignore */
				}
			}, 200);
		} else {
			//Other browsers dont suffer of this problem, and we can set z index instantly
			(node.parentNode as HTMLElement).style.zIndex = node.style.zIndex;
		}
	}

	export function showLookupTypingPopupIfStillFocusedAndFixZIndex(id: string) {
		let node = document.getElementById(id);
		if(!node || node.tagName.toLowerCase() != 'input')
			return;
		let wasInFocus = node == document.activeElement;
		let qDivPopup = $(node.parentNode).children("div.ui-lui-keyword-popup");
		let divPopup;
		if (qDivPopup.length > 0){
			divPopup = qDivPopup.get(0);
			//z-index correction must be set manually from javascript (because some bug in IE7 -> if set from domui renders incorrectly until page is refreshed?)
			divPopup.style.zIndex = node.style.zIndex + 1;
			(node.parentNode as HTMLElement).style.zIndex = divPopup.style.zIndex;
		}else{
			//fix z-index to one saved in input node
			(node.parentNode as HTMLElement).style.zIndex = node.style.zIndex;
		}

		if (wasInFocus && divPopup){
			//show popup in case that input field still has focus
			$(divPopup).show();
		}

		let trNods = $(qDivPopup).children("div").children("table").children("tbody").children("tr");
		if (trNods && trNods.length > 0) {
			for(let i=0; i < trNods.length; i++) {
				let trNod = trNods.get(i);
				//we need this jquery way of attaching events, if we use trNod.setAttribute("onmouseover",...) it does not work in IE7
				$(trNod).bind("mouseover", {nodeId: id, trId: trNod.id}, function(event) {
					lookupRowMouseOver(event.data.nodeId, event.data.trId);
				});
			}
		}
		if (divPopup){
			$(divPopup).bind("click", {nodeId: id}, function(event) {
				lookupPopupClicked(event.data.nodeId);
			});
		}
	}

	/*
	 * In case of longer waiting for lookupTyping ajax response show waiting animated marker.
	 * export function is called with delay of 500ms from ajax.beforeSend method for lookupTyping event.
	 */
	export function displayWaiting(id : string) : void {
		let node = document.getElementById(id);
		if (node){
			for ( let i = 0; i < node.childNodes.length; i++ ){
				let child = node.childNodes[i] as HTMLElement;
				if (child.className == 'ui-lui-waiting'){
					child.style.display = 'inline';
				}
			}
		}
	}

	/*
	 * Hiding waiting animated marker that was shown in case of longer waiting for lookupTyping ajax response.
	 * export function is called from ajax.completed method for lookupTyping event.
	 */
	export function hideWaiting(id : string) : void {
		let node = document.getElementById(id);
		if (node){
			for ( let i = 0; i < node.childNodes.length; i++ ){
				let child = node.childNodes[i] as HTMLElement;
				if (child.className == 'ui-lui-waiting'){
					child.style.display = 'none';
				}
			}
		}
	}

	export function lookupTyping(id : string) : void {
		let lookupField = document.getElementById(id);
		//check for existence, since it is delayed action component can be removed when action is executed.
		if (lookupField){
			// FIXME reuse domui-ajax call handler

			// Collect all input, then create input.
			let fields = {};
			WebUI.getInputFields(fields);
			fields["webuia"] = "lookupTyping";
			fields["webuic"] = id;
			fields["$pt"] = (window as any).DomUIpageTag;
			fields["$cid"] = (window as any).DomUICID;
			WebUI.cancelPolling();
			let displayWaitingTimerID = null;

			$.ajax( {
				url : WebUI.getPostURL(),
				dataType :"*",
				data :fields,
				cache :false,
				type: "POST",
				global: false,
				beforeSend: function(){
					// Handle the local beforeSend event
					// let parentDiv = lookupField.parentNode as HTMLElement;
					let parentDiv = lookupField.parentElement;
					if (parentDiv) {
						displayWaitingTimerID = window.setTimeout("WebUI.displayWaiting('" + parentDiv.id + "')", 500);
					}
				},
				complete: function(){
					// Handle the local complete event
					if (displayWaitingTimerID) {
						//handle waiting marker
						window.clearTimeout(displayWaitingTimerID);
						displayWaitingTimerID = null;
						// let parentDiv = lookupField.parentNode;
						let parentDiv = lookupField.parentElement;
						if (parentDiv) {
							hideWaiting(parentDiv.id);
						}
					}
					//handle received lookupTyping component content
					showLookupTypingPopupIfStillFocusedAndFixZIndex(id);
					WebUI.doCustomUpdates();
				},

				success :WebUI.handleResponse,
				error :WebUI.handleError
			});
		}
	}

	export function lookupTypingDone(id : string) {
		// Collect all input, then create input.
		let fields = {};
		WebUI.getInputFields(fields);
		fields["webuia"] = "lookupTypingDone";
		fields["webuic"] = id;
		fields["$pt"] = (window as any).DomUIpageTag;
		fields["$cid"] = (window as any).DomUICID;
		WebUI.cancelPolling();

		// FIXME reuse domui-ajax call handler
		$.ajax( {
			url : WebUI.getPostURL(),
			dataType :"*",
			data :fields,
			cache :false,
			type: "POST",
			success :WebUI.handleResponse,
			error :WebUI.handleError
		});
	}
}
