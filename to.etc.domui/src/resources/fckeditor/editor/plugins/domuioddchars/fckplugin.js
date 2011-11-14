/*
 * vmijic 20111114 - DomUI Odd Characters plugin for FCKeditor
 *  
 * This plugin register Toolbar items for command that would be used to implement custom odd characters dialog integrated with Domui.
 */

/***
 * Create blank command, seems useless but needs to exits according to api...
 */
var DomuiOddChar_command = function()
{

};

/***
 * Add Execute prototype
 */
DomuiOddChar_command.prototype.Execute = function()
{
    // get FCKeditor name, that is actually set as ID within domui
	var fckName = FCK.Name;

    // call domui
	window.parent.WebUI.scall(fckName, "FCKODDCHAR", {
		_fckId : fckName
	});
};

/***
 * Method that is exected when selected 'odd character dialog string' needs to be added to editor (usually as rendered response from domui handler)
 */
DomuiOddChar_addString = function(fckName, input)
{
	var oEditor = FCKeditorAPI.GetInstance(fckName);
	//oEditor.FCKUndo.SaveUndoStep() ;
	oEditor.InsertText(input) ;
};

/***
 * Method that is exected when odd chars dialog is canceled (usually as rendered response from domui handler)
 */
DomuiOddChar_cancel = function(fckName)
{
	var oEditor = FCKeditorAPI.GetInstance(fckName);
	//all we do is to get focus back to editor
	oEditor.Focus();
};

/***
 * Add GetState prototype
 * Returns 0 since button is always avalable...
 */
DomuiOddChar_command.prototype.GetState = function()
{
        return 0;
};

var oDomuiOddChar = new FCKToolbarButton( 'DomUI_OddChar', FCKLang.BtnDomuiOddChar, FCKLang.BtnDomuiOddChar, FCK_TOOLBARITEM_ONLYICON ) ;
oDomuiOddChar.IconPath = FCKConfig.PluginsPath + 'domuioddchars/image.gif' ;

//Register toolbar button
FCKToolbarItems.RegisterItem( 'DomUI_OddChar', oDomuiOddChar ) ; // 'DomUI_OddChar' is the name used in the Toolbar config.

//Register the related commands.
FCKCommands.RegisterCommand( 'DomUI_OddChar' , new DomuiOddChar_command()) ;