// ** I18N

var Calendar;
if(! Calendar)
	Calendar = {};

// Calendar NL language
// Author: fixed by legolas558
// Encoding: any
// Distributed under the same terms as the calendar itself.

// For translators: please use UTF-8 if possible.  We strongly believe that
// Unicode is the answer to a real internationalized world.  Also please
// include your contact information in the header, as can be seen above.

// full day names
Calendar._DN = ["Zondag",
 "Maandag",
 "Dinsdag",
 "Woensdag",
 "Donderdag",
 "Vrijdag",
 "Zaterdag",
 "Zondag"];

// Please note that the following array of short day names (and the same goes
// for short month names, _SMN) isn't absolutely necessary.  We give it here
// for exemplification on how one can customize the short day names, but if
// they are simply the first N letters of the full name you can simply say:
//
//   Calendar._SDN_len = N; // short day name length
//   Calendar._SMN_len = N; // short month name length
//
// If N = 3 then this is not needed either since we assume a value of 3 if not
// present, to be compatible with translation files that were written before
// this feature.

Calendar._SDN_len = 2;

// First day of the week. "0" means display Sunday first, "1" means display
// Monday first, etc.
Calendar._FD = 1;

// full month names
Calendar._MN = ["Januari",
 "Februari",
 "Maart",
 "April",
 "Mei",
 "Juni",
 "Juli",
 "Augustus",
 "September",
 "Oktober",
 "November",
 "December"];

Calendar._SMN_len = 3;

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "Info";

Calendar._TT["ABOUT"] =
"Datum selectie:\n" +
"- Gebruik de \xab \xbb knoppen om een jaar te selecteren\n" +
"- Gebruik de " + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + " knoppen om een maand te selecteren\n" +
"- Houd de muis ingedrukt op de genoemde knoppen voor een snellere selectie.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"Tijd selectie:\n" +
"- Klik op een willekeurig onderdeel van het tijd gedeelte om het te verhogen\n" +
"- of Shift-klik om het te verlagen\n" +
"- of klik en sleep voor een snellere selectie.";

Calendar._TT["PREV_YEAR"] = "Vorig jaar (ingedrukt voor menu)";
Calendar._TT["PREV_MONTH"] = "Vorige maand (ingedrukt voor menu)";
Calendar._TT["GO_TODAY"] = "Ga naar Vandaag";
Calendar._TT["NEXT_MONTH"] = "Volgende maand (ingedrukt voor menu)";
Calendar._TT["NEXT_YEAR"] = "Volgend jaar (ingedrukt voor menu)";
Calendar._TT["SEL_DATE"] = "Selecteer datum";
Calendar._TT["DRAG_TO_MOVE"] = "Klik en sleep om te verplaatsen";
Calendar._TT["PART_TODAY"] = " (vandaag)";
Calendar._TT["INVALID"] = "Ongeldige datum";
Calendar._TT ["INVALID_DATE"] = "Ongeldige datum";
Calendar._TT ["INVALID_MONTH"] = "Ongeldige maand";
Calendar._TT ["INVALID_YEAR"] = "Ongeldige jaar";
Calendar._TT ["INVALID_HOUR"] = "Ongeldige uur";
Calendar._TT ["INVALID_MINUTE"] = "Ongeldige minuut";
Calendar._TT ["INVALID_SECOND"] = "Ongeldige tweede";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "Toon %s eerst";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";
Calendar._TT["MIN_YEAR"] = "1800";
Calendar._TT["MAX_YEAR"] = "2999";

Calendar._TT["CLOSE"] = "Sluiten";
Calendar._TT["TODAY"] = "(vandaag)";
Calendar._TT["TIME_PART"] = "(Shift-)Klik of sleep om de waarde te veranderen";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%d-%m-%Y";
Calendar._TT["DEF_TIME_FORMAT"] = "%H:%M";
Calendar._TT["TT_DATE_FORMAT"] = "%a %e %b";
Calendar._TT["DEF_DATETIME_FORMAT"] = "%d-%m-%Y %H:%M";

Calendar._TT["WK"] = "wk";
Calendar._TT["TIME"] = "Tijd:";

Calendar._TT["DATE_SEPARATOR"] = "-/.";
Calendar._TT["TIME_SEPARATOR"] = ":-";
Calendar._TT["DATE_TIME_SEPARATOR"] = " ";
