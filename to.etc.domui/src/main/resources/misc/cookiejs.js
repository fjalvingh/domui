function onCookieNonsense() {
	${cookieAcceptance}
}

var cookieConsentOptions = {
	title: ${title},
	${msg},
	600,
	expires: 1,
	link: '${link}',
	onAccept: function(){
		var myPreferences = $.fn.ihavecookies.cookie();
		onCookieNonsense();
		console.log('Yay! The following preferences were saved...');
		console.log(myPreferences);
	},
	true,
	acceptBtnLabel: ${acclabel},
	${infolabel},
	'Select which cookies you want to accept',
	fixedCookieTypeLabel: 'Essential',
	fixedCookieTypeDesc: 'These are essential for the website to work correctly.',
	cookieTypes: ${cookieTypes}
}

$(document).ready(function() {
	$('body').ihavecookies(cookieConsentOptions);
	onCookieNonsense();
});
