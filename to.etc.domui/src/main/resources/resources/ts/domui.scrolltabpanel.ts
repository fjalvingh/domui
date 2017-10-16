/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.ts" />
//import WebUI from "domui.webui.util";
namespace WebUI {
	let _ignoreScrollClick = 0;

	export function scrollLeft(bLeft) : void {
		if(this._ignoreScrollClick != 0 || $(bLeft).hasClass('ui-stab-dis'))
			return;

		let scrlNavig = $(bLeft.parentNode);
		let offset = -1 * parseInt($('ul',scrlNavig).css('marginLeft'));
		let diff = $(scrlNavig).width() - 2 * $('.ui-stab-scrl-left',scrlNavig).width();
		let me = this;
		let disa = false;
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
	}

	export function scrollRight(bRight) : void {
		if(this._ignoreScrollClick != 0 || $(bRight).hasClass('ui-stab-dis'))
			return;

		let scrlNavig = $(bRight.parentNode);
		let tabsTotalWidth = $('li:last',scrlNavig).width() + 8 /* paddong = 8 */ + $('li:last',scrlNavig).offset().left - $('li:first',scrlNavig).offset().left;
		let tabsVisibleWidth = $(scrlNavig).width() - 2 * $('.ui-stab-scrl-right',scrlNavig).width();
		let maxLeftOffset = tabsTotalWidth - tabsVisibleWidth;
		let diff = tabsVisibleWidth;
		let offset = -1 * parseInt($('ul',scrlNavig).css('marginLeft'));

		let disa = false;
		if (offset >= maxLeftOffset){
			return;
		} else if (diff + offset >= maxLeftOffset){
			diff = maxLeftOffset - offset;
			disa = true;
		}
		this._ignoreScrollClick++;
		let me = this;
		$('ul', scrlNavig ).animate({marginLeft: '-=' + diff},400, 'swing', function() {
			$('.ui-stab-scrl-left', scrlNavig).removeClass('ui-stab-dis');
			if (disa){
				$(bRight).addClass('ui-stab-dis');
			}
			me._ignoreScrollClick--;
		});
	}

	export function recalculateScrollers(scrlNavigId) : void {
		let scrlNavig = document.getElementById(scrlNavigId);
		let tabsTotalWidth = $('li:last',scrlNavig).width() + 8 /* paddong = 8 */ + $('li:last',scrlNavig).offset().left - $('li:first',scrlNavig).offset().left;
		let tabsVisibleWidth = $(scrlNavig).width() - 2 * $('.ui-stab-scrl-right',scrlNavig).width();
		//WebUI.debug('debug1', 50, 150, 'width:' + ($('li:last',scrlNavig).width() + 8 + $('li:last',scrlNavig).offset().left - $('li:first',scrlNavig).offset().left));
		//WebUI.debug('debug2', 50, 200, 'offsetX:' + ($('li:first',scrlNavig).offset().left - $('ul', scrlNavig).offset().left));

		if(tabsTotalWidth > tabsVisibleWidth){
			let leftM = parseInt($('ul',scrlNavig).css('marginLeft'));
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
	}
}
