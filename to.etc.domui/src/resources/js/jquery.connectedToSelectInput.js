// ConnectedToSelectInput javascript plugin based on jQuery.
// connects input to usually hidden list select and provides autocomplete feature inside input. Down arrow does show and focus select list.
var ConnectedToSelectInput = {		
	initialize:function (inputId, selectId){
		var input = document.getElementById(inputId);
		$(input).keyup(function(event) {
			ConnectedToSelectInput.matchFieldSelect(event, inputId, selectId); 
		});
	},

	matchFieldSelect: function (event, inputId, selectId) {
		var select = document.getElementById(selectId);
		var cursorKeys = "8;46;37;38;39;40;33;34;35;36;45;";
		if (cursorKeys.indexOf(event.keyCode + ";") == -1) {
			var input = document.getElementById(inputId);
		    var found = false;
		    var foundAtIndex = -1;
			for (var i = 0; i < select.options.length; i++){
				if ((found = select.options[i].text.toUpperCase().indexOf(input.value.toUpperCase()) == 0)){
					foundAtIndex = i;
					break;
				}
			}
		   	select.selectedIndex = foundAtIndex;

		   	var oldValue = input.value;
			var newValue = found ? select.options[foundAtIndex].text : oldValue;
			if (newValue != oldValue) {
				if (typeof input.selectionStart != "undefined") {
		            input.value = newValue;
		            input.selectionStart = oldValue.length; 
			        input.selectionEnd =  newValue.length;
			        input.focus();
			    } 
				if (document.selection && document.selection.createRange) {
					input.value = newValue;
		            input.focus();
		            input.select();
		            var range = document.selection.createRange();
		            range.collapse(true);
		            range.moveStart("character", oldValue.length);
		            range.moveEnd("character", newValue.length);
		            range.select();
		        }else if (input.createTextRange) {
					input.value = newValue;
					var rNew = input.createTextRange();
					rNew.moveStart('character', oldValue.length);
					rNew.select();
				}
			}
		}else if (event.keyCode == 40){
			select.style.display = 'inline';
			select.focus();
		}
	}
};
				 