/*
 * vmijic 20131206 - DomUI Image plugin for CKeditor
 *  
 * This plugin register Toolbar items for command that would be used to implement custom image browsing integrated with Domui.
 * based on tutorial found at http://docs.cksource.com/CKEditor_3.x/Tutorials/Timestamp_Plugin#
 */

CKEDITOR.plugins.add( 'domuioddchar',
{
	lang: 'nl,en',	
	init: function( editor )
	{
		editor.ui.addButton( 'DomUI_OddChar',
				{
			        label: editor.lang.domuioddchar.toolbar,
					command: 'insertDomUI_OddChar',
					icon: this.path + 'image.gif'
				} );
		
		editor.addCommand( 'insertDomUI_OddChar',
				{
					exec : function( editor )
					{   
						var actualId = editor.name;
					    // call domui
						WebUI.scall(actualId, "CKODDCHAR", {
							_ckId : actualId
						});
					}
				});
	}
} );

/** ckeditor domuioddchar plugin for DomUI helper namespace */
var CkeditorDomUIOddChar;
if(CkeditorDomUIOddChar === undefined)
	CkeditorDomUIOddChar = {};

$.extend(CkeditorDomUIOddChar, {
	/***
	 * Method that is exected when selected 'odd character dialog string' needs to be added to editor (usually as rendered response from domui handler)
	 */
	addString : function(ckId, input){
		var oEditor = CKEDITOR.instances[ckId];
		oEditor.insertText(input);
	},

	/***
	 * Method that is exected when 'odd character dialog string' is canceled (usually as rendered response from domui handler)
	 */
	cancel : function(ckId){
		var oEditor = CKEDITOR.instances[ckId];
		oEditor.focus();
	}
});
