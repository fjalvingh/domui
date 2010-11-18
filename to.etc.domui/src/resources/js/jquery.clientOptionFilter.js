// ClientOptionFilter javascript plugin based on jQuery.
var ClientOptionFilter = {		
	initialize:function (selectId){
		var select = $('#' + selectId);
		var div = document.createElement('div');
		$(div).attr('id', selectId + '_div');
		$(div).insertBefore(select);
		$(div).append(select);
		var input = document.createElement('input');
		$(input).insertBefore(select);
		$(input).attr('id', selectId + '_input');
		$(input).css('width', (select.width() - 4) + 'px');
		$(input).css('display', 'block');
		var clone = $(select).clone();
		clone.appendTo(div);
		clone.attr('id', selectId + '_clone');
		clone.css('display', 'none');
		$(input).focus();
		$(input).keyup(function(event) {
			ClientOptionFilter.doFilter(selectId); 
		});
	},

	doFilter:function (controlId){
		var input = $('#' + controlId + '_input');
		var reg = $(input).val().replace(/(\^|\$|\.|\*|\+|\?|\=|\!|\:|\||\\|\/|\(|\)|\[|\]|\{|\})/g, "\\$1");
		var clone = $('#' + controlId + '_clone');
		ClientOptionFilter.getOptions(controlId, clone, reg);
	},

    getOptions:function(controlId, clone, reg){
		var reg = new RegExp(reg, 'i');
        var options = [];
		var clonedOptions = $(clone).children('option');
        for (var i = 0; i < clonedOptions.length; i++){
			if (reg.test(clonedOptions.get(i).innerHTML)){
				options[options.length] = {
					'value': $(clonedOptions.get(i)).attr('value'), 
					'name': clonedOptions.get(i).innerHTML
				};
            }
        }
        ClientOptionFilter.afterGetOptions(controlId, options, 'value', 'name');
    },

    afterGetOptions:function(controlId, options, optionValueIndex, optionNameIndex){
		var htmlOptions = [];
        for (var i = 0; i < options.length; i++){
			htmlOptions[htmlOptions.length] = '<option id="' + options[i][optionValueIndex] + '" value="' + options[i][optionValueIndex] + '"' + (options.lenght == 1 ? ' selected' : '') + '>' + options[i][optionNameIndex] + '</option>';
        }
		var input = $('#' + controlId + '_input');
        if (htmlOptions.length > 0){
			htmlOptions = htmlOptions.join('');
			input.css('color', '');
            ClientOptionFilter.setOptions(controlId, htmlOptions);
        }else{
			input.css('color', 'red');
        }
    },
	
    setOptions:function(controlId, htmlOptions){
		var select = $('#' + controlId + '_div select:first');
		$(select).html(htmlOptions);
		if ($(select).children('option').length == 1){
			$(select).children('option').attr('selected', 'true');
			$(select).change();
        }
	},

	realTrigger:function(ele, evt){
		if (document.createEvent){
			if ($.inArray(evt, ['click', 'dblclick', 'mousedown', 'mouseup', 'mouseover', 'mousemove', 'mouseout']) != '-1'){
				var evObj = document.createEvent('MouseEvents');
            }else{
				var evObj = document.createEvent('HTMLEvents');
            }
            evObj.initEvent(evt, true, false);
            ele.dispatchEvent(evObj);
        }else if (document.createEventObject){
			ele.fireEvent('on'+evt);
        }
    }
};
				 