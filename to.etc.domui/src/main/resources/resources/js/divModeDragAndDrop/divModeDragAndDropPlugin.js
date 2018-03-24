/* 
 * DDD = DIV mode Drag and Drop namespace. 
 * This utility javascript is used to support drag and drop that works in following layout:
 * We have single main panel [DIV.drag-body], this main panel is bound to domui drop handler and receives drop events.  
 * Main panel has one or more container panels [DIV.drag-cont]. Each container panel is meant to be used as vertical flow layout contaioner.
 * Each container panel can contain drag panels [DIV.drag-div], as direct children. Drag panel is attached to domui drag handler and also receives drop events.  
 * Each drag panel contains drag area [DIV.drag-area], area that actually responds to drag events in browser. Using drag area, user can drag complete drag panel.
 * During drag moving, special drop marker (defaults to [DIV.drop-marker], but can be customized) is visible in place where drop would insert drag panel.
 * Drop marker style and content can be customized in initDragAndDrop function (markerCss, markerContent).
 * Hovering drag panel over top part of other drag panel would show drop marker above other drag panel.
 * Hovering drag panel over bottom part of other drag panel would show drop marker under other drag panel.
 * Hovering drag panel over top section of empty drag container would show drop marker at top of drag container.
 * 
 * Droping drga panel would call WebUI.scall with following parameters:
 		main panel id, - id of main panel that is attached to domui drop handler; 
 		"WEBUIDROP", - domui name for drga and drop event;
 		{
			_dragid : id of drag panel that is dropped;
			_dropContainerId : id of drag container that recieves drop of dragged panel;
			_siblingId : id of other drag element that would become next sibling of dropped panel;
			_mode : 'DIV' - identification of drag and drop plugin that is in use;
		}
 */

var DDD = {
		
	dragObject: null,
	oldZindex: null,
	oldOpacity: null,
	dragObjectZindex: null,
	dragObjectXoffset: null,
	lastTarget: null,
	lastHoverNearTop: null,
	insertHere: null,
	markerCss: null,
	markerContent: null,
	doLog: false, //set this to T to enable debug log
	wasMoved: null,

	getPosition : function (e){
		var left = 0;
		var top  = 0;
	
		while (e.offsetParent){
			left += e.offsetLeft;
			top  += e.offsetTop;
			e     = e.offsetParent;
		}
	
		left += e.offsetLeft;
		top  += e.offsetTop;
	
		return {x:left, y:top};
	},

	getMouseOffset : function (target, ev){
		ev = ev || window.event;
	
		var docPos    = DDD.getPosition(target);
		var mousePos  = DDD.mouseCoords(ev);
		return {x:mousePos.x - docPos.x, y:mousePos.y - docPos.y};
	},

	mouseCoords :function (ev){
		if(ev.pageX || ev.pageY){
			return {x:ev.pageX, y:ev.pageY};
		}
		return {
			x:ev.clientX + document.body.scrollLeft - document.body.clientLeft,
			y:ev.clientY + document.body.scrollTop  - document.body.clientTop
		};
	},

	mouseMove :function (ev){
		ev           = ev || window.event;
		var mousePos = DDD.mouseCoords(ev);
		DDD.clearSelection(); //prevent browser to behave as selecting content on screen
		if(DDD.dragObject){
			if (document.body){
				document.body.style.cursor = 'move';
			}
			if (!DDD.wasMoved){
				$(DDD.dragObject).css('opacity', '0.9');
				$(DDD.dragObject).css('z-index', 1000);
				//In order to preserve dragging object width (when position is absolute), we need to set actual width. When we drop we would reset width to 'auto'.  
				var currentWidth = $(DDD.dragObject).css('width');
				$(DDD.dragObject).css('width', currentWidth);
				$(DDD.dragObject).children('.drag-area:first').css('opacity', '0.3');
				DDD.dragObject.style.position = 'absolute';
				$(DDD.dragObject).css('display', 'inline');
			}
			DDD.dragObject.style.top      = (mousePos.y) + "px";
			DDD.dragObject.style.left     = (mousePos.x - DDD.dragObjectXoffset) + "px";
			DDD.wasMoved = true;
	
			var target   = ev.target || ev.srcElement;

			if (DDD.insertHere && target === DDD.insertHere){
				return false;
			}
			if (target === DDD.dragObject){
				return false;
			}
			//This makes drag and drop more user friendly.
			//In case when we hover document (out of any content), lets calculate if any container div is on same vertical line with mouse and enable drop hit
			//In IE7 body height is stretched over whole existing content, while on IE8 and FF body stays short.
			//DDD.log(target, target.tagName, true);
			if (target.tagName === 'HTML' || target === document.body || $(target).hasClass('drag-body')){
				var dragContainerOnPosX = undefined;
				$('.drag-body').children('.drag-cont').each(
					function (){
						var containerPos    = DDD.getPosition(this);
						if (mousePos.x >= containerPos.x && mousePos.x <= containerPos.x + this.clientWidth){
							dragContainerOnPosX = this;
							return false;
						}
						return true;
					}
				);
				if (dragContainerOnPosX){
					target = dragContainerOnPosX;
				}
			}
			if (!target.parentNode){
				return false;
			}
			if (!($(target.parentNode).hasClass('drag-cont')) && !($(target).hasClass('drag-cont'))){
				var parent = $(target).parents('.drag-div').filter(':first');
				if (parent && parent.size() > 0){
					target = parent.get(0);
					if (target === DDD.dragObject){
						return false;
					}
				}else{
					return false;
				}
			}
			
			var newTarget = DDD.lastTarget;
			var newHoverNearTop = DDD.lastHoverNearTop;
		
			if (target){
				
				if ($(target).hasClass('drag-div')){
					var targetMouseOffset = DDD.getMouseOffset(target, ev);
					newHoverNearTop = targetMouseOffset.y < $(target).height() / 2;
				}
				
				if (DDD.lastTarget){
					if (DDD.lastTarget !== target){
						DDD.log(DDD.lastTarget, ' stop hovering');
						newTarget = target;
						DDD.log(newTarget, ' start hovering');
					}
				}else{
					newTarget = target;
					DDD.log(newTarget, ' start hovering');
				}
			}else{
				if (DDD.lastTarget){
					DDD.log(DDD.lastTarget, ' stop hovering');
					newTarget = null;
				}
			}
		
			if (newTarget && (newTarget !== DDD.lastTarget || newHoverNearTop !== DDD.lastHoverNearTop)){
				var dragObjectParent = null;
				var dragObjectSibling = null;			
				if ($(newTarget).hasClass('drag-cont')){
					dragObjectParent = newTarget;
					dragObjectSibling = null;
				}else{
					dragObjectParent    = newTarget.parentNode;
					if (newHoverNearTop){
						dragObjectSibling   = newTarget;
					}else{
						dragObjectSibling   = newTarget.nextSibling;
					}
				}
				DDD.initMarker();
				if(dragObjectSibling){
					dragObjectParent.insertBefore(DDD.insertHere, dragObjectSibling);
					dragObjectSibling = null;
				} else {
					dragObjectParent.appendChild(DDD.insertHere);
				}
				
			}
			DDD.lastTarget = newTarget;
			DDD.lastHoverNearTop = newHoverNearTop;
			return true;
		}
	},

	mouseUp : function (){
		if(DDD.dragObject){
			if (document.body){
				document.body.style.cursor = 'default';
			}
			if (DDD.oldOpacity){
				$(DDD.dragObject).css('opacity', DDD.oldOpacity);
			}else{
				$(DDD.dragObject).css('opacity', 1);
			}
			if (DDD.oldZindex){
				$(DDD.dragObject).css('z-index', DDD.oldZindex);
			}
			if (DDD.wasMoved){
				//Reset width of dragged object, because we are back in DIV container soon. 
				$(DDD.dragObject).css('width', 'auto');
			}
			$(DDD.dragObject).css('display', 'block');
			
			if (DDD.insertHere && DDD.insertHere.parentNode){
				var dragBody = $(DDD.insertHere).closest('.drag-body');
				if ($(dragBody) && $(dragBody).size() > 0){
					var dragBodyId = dragBody.get(0).id;
					WebUI.scall(dragBodyId, "WEBUIDROP", {
						_dragid : DDD.dragObject.id,
						_dropContainerId : DDD.insertHere.parentNode.id,
						_siblingId : DDD.insertHere.nextSibling ? DDD.insertHere.nextSibling.id : undefined, 
						_mode : 'DIV'
					});
				}
				//DDD.insertHere.parentNode.insertBefore(DDD.dragObject, DDD.insertHere);
				DDD.insertHere.parentNode.removeChild(DDD.insertHere);
			}
			$(DDD.dragObject).children('.drag-area:first').css('opacity', '1');
			DDD.dragObject.style.position = 'static';
			DDD.dragObject.style.top      = '0px';
			DDD.dragObject.style.left     = '0px';		
		}
		DDD.dragObject = null;
		DDD.dragObjectXoffset = null;
		DDD.wasMoved = false;
		$(document).unbind('mousemove', DDD.mouseMove);
		$(document).unbind('mouseup', DDD.mouseUp);			
	},

	makeDraggableById : function (elemId){
		var elem = $('#' + elemId);	
		DDD.makeDraggable(elem);
	},

	makeDraggable : function (elem){
		if(!elem) {
			return false;
		}
		$(elem).children('.drag-area').each(function (){
			$(this).bind('mousedown', function (ev){
				DDD.dragObject  = this.parentNode;
				DDD.wasMoved = false;
				DDD.oldZindex = $(this).parent().css('z-index');
				DDD.oldOpacity = $(this).parent().css('opacity');
				var mouseOffset = DDD.getMouseOffset(DDD.dragObject, ev);
				DDD.dragObjectXoffset = mouseOffset.x; 
				DDD.lastTarget = null;
				DDD.lastHoverNearTop = null;
				$(document).bind("mousemove", DDD.mouseMove);
				$(document).bind("mouseup", DDD.mouseUp);			
				return false;
			});
		});
	},
	
	initMarker : function (){
		if (!DDD.insertHere){
			DDD.insertHere = document.createElement('div');
			if (DDD.markerCss){
				DDD.insertHere.className = DDD.markerCss;
			}else{
				DDD.insertHere.className = 'drop-marker';
			}
			if (DDD.markerContent){
				DDD.insertHere.innerHTML = DDD.markerContent;
			}else{
				DDD.insertHere.innerHTML = '----- DROP HERE -----';
			}
		}
	},

	initDragAndDrop: function (markerCss, markerContent){
		DDD.markerCss = markerCss;
		DDD.markerContent = markerContent;
		DDD.initMarker();
		$('.drag-div').each(function() {
			DDD.makeDraggable(this);
		});		
	},
	
	log: function (object1, message, special){
		if (DDD.doLog || special){
			var historyDiv = document.getElementById('Log');
			if (!historyDiv){
				historyDiv = document.createElement('div');
				historyDiv.id = 'Log';
				$(historyDiv).appendTo('body');
				$(historyDiv).bind('mousedown', function (ev){
					historyDiv.innerHTML = '';
				});
			}
			var idpart = '';
			if (object1){
				idpart = object1.id + ': ';
			}
			historyDiv.appendChild(document.createTextNode(idpart + message));
			historyDiv.appendChild(document.createElement('BR'));
			historyDiv.scrollTop += 50;
		}
	},
	
	//Util method that clears selection in browser -> dragging is sometimes recognised as selecting content on screen and is has to be prevented 
	clearSelection: function() {
		if (window.getSelection) {
		   if (window.getSelection().empty) {  // Chrome
		     window.getSelection().empty();
		   } else if (window.getSelection().removeAllRanges) {  // Firefox
		     window.getSelection().removeAllRanges();
		   }
		} else if (document.selection) {  // IE?
		  document.selection.empty();
		}
	}
};
